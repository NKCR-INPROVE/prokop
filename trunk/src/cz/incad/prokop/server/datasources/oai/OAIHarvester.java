/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.datasources.oai;

import static org.aplikator.server.data.RecordUtils.newRecord;
import static org.aplikator.server.data.RecordUtils.newSubrecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.aplikator.client.data.Operation;
import org.aplikator.client.data.Record;
import org.aplikator.client.data.RecordContainer;
import org.aplikator.client.rpc.impl.ProcessRecords;
import org.aplikator.server.Context;
import org.aplikator.server.util.Configurator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fastsearch.esp.content.DocumentFactory;
import com.fastsearch.esp.content.IDocument;

import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.datasources.DataSource;
import cz.incad.prokop.server.datasources.util.XMLReader;
import cz.incad.prokop.server.fast.FastIndexer;
import cz.incad.prokop.server.fast.IndexTypes;

/**
 *
 * @author alberto
 */
public class OAIHarvester implements DataSource {

    private static final Logger logger = Logger.getLogger(OAIHarvester.class.getName());
    private ProgramArguments arguments;
    private Configuration conf;
    XMLReader xmlReader;
    private String responseDate;
    private String metadataPrefix;
    private int interval;
    String completeListSize;
    int currentDocsSent = 0;
    private FastIndexer fastIndexer;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH");
    SimpleDateFormat sdfoai;
    Transformer xformer;
    Context context;
    Record sklizen;

    @Override
    public int harvest(String params, Record sklizen, Context ctx) {
        context = ctx;

        this.sklizen = sklizen;
        arguments = new ProgramArguments();
        if (!arguments.parse(params.split(" "))) {
            System.out.println("Program arguments are invalid");
        }

        conf = new Configuration(arguments.configFile);
        xmlReader = new XMLReader(conf);
        logger.info("Indexer initialized");
        sdfoai = new SimpleDateFormat(conf.getProperty("oaiDateFormat"));
        sdf = new SimpleDateFormat(conf.getProperty("filePathFormat"));
        if (arguments.metadataPrefix.equals("")) {
            metadataPrefix = conf.getProperty("metadataPrefix");
        } else {
            metadataPrefix = arguments.metadataPrefix;
        }

        interval = Interval.parseString(conf.getProperty("interval"));
        try {
            xformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(OAIHarvester.class.getName()).log(Level.SEVERE, null, ex);
        }
        fastIndexer = new FastIndexer(conf.getProperty("fastHost"),
                conf.getProperty("fastCollection"),
                Integer.parseInt(conf.getProperty("fastBatchSize")));
        harvest();
        return currentDocsSent;

    }

    private void harvest() {
        long startTime = (new Date()).getTime();
        try {

            String from = "";
            String updateTimeFile = conf.getProperty("updateTimeFile");
            if (arguments.from != null) {
                from = arguments.from;
            } else if (arguments.fullIndex) {
                from = getInitialDate();
            } else {
                if ((new File(updateTimeFile)).exists()) {
                    BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
                    from = in.readLine();
                } else {
                    from = getInitialDate();
                }
            }


            if (!arguments.resumptionToken.equals("")) {
                getRecordWithResumptionToken(arguments.resumptionToken);
            } else if (arguments.fromDisk) {
                getRecordsFromDisk();
            } else {
                update(from);
            }

            logger.info("Harvest success");
            logger.log(Level.INFO, "Harvest success {0} records", currentDocsSent);

            long timeInMiliseconds = (new Date()).getTime() - startTime;
            logger.info(formatElapsedTime(timeInMiliseconds));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error", ex);
        } finally {
            //disconnect();
        }
    }

    private void writeResponseDate() throws FileNotFoundException, IOException {
        File dateFile = new File(Configurator.get().getConfig().getString(Configurator.HOME) +
                File.separator + conf.getProperty("updateTimeFile"));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
        out.write(responseDate);
        out.close();
    }

    private void update(String from) throws Exception {
        Calendar c_from = Calendar.getInstance();
        c_from.setTime(sdfoai.parse(from));
        Calendar c_to = Calendar.getInstance();
        c_to.setTime(sdfoai.parse(from));

        c_to.add(interval, 1);

        String to = "";
        Date date = new Date();
        //sdfoai.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (arguments.to == null) {
            to = sdfoai.format(date);
        } else {
            to = arguments.to;
        }
        Date final_date = sdfoai.parse(to);
        Date current = c_to.getTime();

        while (current.before(final_date)) {
            update(sdfoai.format(c_from.getTime()), sdfoai.format(current));
            c_to.add(interval, 1);
            c_from.add(interval, 1);
            current = c_to.getTime();
        }
        update(sdfoai.format(c_from.getTime()), sdfoai.format(final_date));
        if (!arguments.onlyHarvest) {
            fastIndexer.sendPendingRecords();
        }

    }

    private void update(String from, String until) throws Exception {
        logger.log(Level.INFO, "Harvesting from: {0} until: {1}", new Object[]{from, until});
        responseDate = from;
        writeResponseDate();
        getRecords(from, until);
    }

    private void processRecord(Node node, String identifier) throws Exception {
        if (node != null) {

            String error = xmlReader.getNodeValue(node, "/error/@code");
            if (error.equals("")) {
                IndexTypes it = IndexTypes.INSERTED;

                if (xmlReader.getNodeValue(node, "./header/@status").equals("deleted")) {
                    if (arguments.fullIndex) {
                        logger.log(Level.FINE, "Spik deleted record when fullindex");
                        return;
                    }
                    it = IndexTypes.DELETED;
                }
                if (it != IndexTypes.DELETED) {
                    RecordContainer rc = new RecordContainer();


                    Record fr = newRecord(Structure.zaznam);
                    Structure.zaznam.sklizen.setValue(fr, sklizen.getPrimaryKey().getId());
                    String urlZdroje = conf.getProperty("baseUrl") + "?verb=GetRecord&identifier=" + identifier + "&metadataPrefix=" + metadataPrefix;
                    Structure.zaznam.urlZdroje.setValue(fr, urlZdroje);
                    String hlavninazev = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='245']/subfield[@code='a']/text()");
                    Structure.zaznam.hlavniNazev.setValue(fr, hlavninazev);
                    String leader = xmlReader.getNodeValue(node, "./metadata/record/leader/text()");
                    if(leader!=null && leader.length()>9){
                        Structure.zaznam.typDokumentu.setValue(fr, leader.substring(6, 8));
                    }
                    String xmlStr = nodeToString(node);
                    Structure.zaznam.sourceXML.setValue(fr, xmlStr);
                    rc.addRecord(null, fr, fr, Operation.CREATE);


                    //Identifikatory
                    Record ISSN = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
                    Structure.identifikator.hodnota.setValue(ISSN,
                            xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='022']/subfield[@code='a']/text()"));
                    Structure.identifikator.typ.setValue(ISSN, "ISSN");
                    rc.addRecord(null, ISSN, ISSN, Operation.CREATE);

                    Record ISBN = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
                    Structure.identifikator.hodnota.setValue(ISBN,
                            xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='020']/subfield[@code='a']/text()"));
                    Structure.identifikator.typ.setValue(ISBN, "ISBN");
                    rc.addRecord(null, ISBN, ISBN, Operation.CREATE);

                    Record cnb = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
                    String cnbStr = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='015']/subfield[@code='a']/text()");
                    Structure.identifikator.hodnota.setValue(cnb, cnbStr);
                    Structure.identifikator.typ.setValue(cnb, "cCNB");
                    rc.addRecord(null, cnb, cnb, Operation.CREATE);

                    //Autori
                    NodeList autori = xmlReader.getListOfNodes("./metadata/record/datafield[@tag='100']");
                    for (int i = 0; i < autori.getLength(); i++) {
                        Record autor = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.autor);
                        Structure.autor.nazev.setValue(autor,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='100'][position()=" + (i + 1) + "]/subfield[@code='a']/text()"));
                        rc.addRecord(null, autor, autor, Operation.CREATE);
                    }

                    //Jazyky
                    String[] jazyky = xmlReader.getListOfValues("./metadata/record/datafield[@tag='041']/subfield[@code='a']/text()");
                    for (String jazyk : jazyky) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.jazyk);
                        Structure.jazyk.kod.setValue(j, jazyk);
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }

                    //Nazvy
                    String[] nazvy = xmlReader.getListOfValues("./metadata/record/datafield[@tag='245']/subfield[@code='a']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Hlavní název");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    nazvy = xmlReader.getListOfValues("./metadata/record/datafield[@tag='245']/subfield[@code='b']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Podnázev");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    nazvy = xmlReader.getListOfValues("./metadata/record/datafield[@tag='245']/subfield[@code='p']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Název části");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    nazvy = xmlReader.getListOfValues("./metadata/record/datafield[@tag='246']/subfield[@code='a']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Alternativní název");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }

                    Structure.sklizen.pocet.setValue(sklizen, currentDocsSent++);
                    rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);

                    rc = context.getAplikatorService().execute(new ProcessRecords(rc)).getRecordContainer();

                    Record z = rc.getRecords().get(0).getEdited();
                    IDocument doc = DocumentFactory.newDocument(urlZdroje);
                    doc.addElement(DocumentFactory.newString("title", hlavninazev));
                    doc.addElement(DocumentFactory.newInteger("dbid", z.getPrimaryKey().getId()));
                    doc.addElement(DocumentFactory.newString("url", urlZdroje));
                    doc.addElement(DocumentFactory.newString("leader_format", leader));
                    doc.addElement(DocumentFactory.newString("zdroj", conf.getProperty("zdroj")));
                    doc.addElement(DocumentFactory.newString("base", conf.getProperty("base")));
                    doc.addElement(DocumentFactory.newString("harvester", conf.getProperty("harvester")));
                    doc.addElement(DocumentFactory.newString("originformat", conf.getProperty("originformat")));
                    doc.addElement(DocumentFactory.newString("data", xmlStr));
                    fastIndexer.add(doc, it);

                } else {
                    //zpracovat deletes
                }
            } else {
                logger.log(Level.SEVERE, "Can't proccess xml{0}", error);
            }
        }
    }

    private void getRecordWithResumptionToken(String resumptionToken) throws Exception {
        while (resumptionToken != null && !resumptionToken.equals("")) {
            resumptionToken = getRecords("?verb=" + conf.getProperty("verb") + "&resumptionToken=" + resumptionToken);
        }
    }

    private String getInitialDate() throws Exception {
        String urlString = conf.getProperty("baseUrl") + "?verb=Identify";

        URL url = new URL(urlString.replace("\n", ""));

        logger.log(Level.FINE, "url: {0}", url.toString());
        xmlReader.readUrl(url.toString());
        return xmlReader.getNodeValue("//Identify/earliestDatestamp/text()");
    }

    private void getRecords(String from, String until) throws Exception {
        String query = String.format("?verb=%s&from=%s&until=%s&metadataPrefix=%s&set=%s",
                conf.getProperty("verb"),
                from,
                until,
                metadataPrefix,
                conf.getProperty("set"));
        String resumptionToken = getRecords(query);
        while (resumptionToken != null && !resumptionToken.equals("")) {
            resumptionToken = getRecords("?verb=" + conf.getProperty("verb") + "&resumptionToken=" + resumptionToken);
        }
    }

    private String getRecords(String query) throws Exception {
        String urlString = conf.getProperty("baseUrl") + query;

        URL url = new URL(urlString.replace("\n", ""));

        logger.log(Level.INFO, "url: {0}", url.toString());
        xmlReader.readUrl(url.toString());
        String error = xmlReader.getNodeValue("//error/@code");
        if (error.equals("")) {
            completeListSize = xmlReader.getNodeValue("//resumptionToken/@completeListSize");
            String date;
            String identifier;
            //saveToIndexDir(date, xml, xmlNumber);

            NodeList nodes = xmlReader.getListOfNodes("//record");
            for (int i = 0; i < nodes.getLength(); i++) {
                date = xmlReader.getNodeValue("//record[position()=" + (i + 1) + "]/header/datestamp/text()");
                identifier = xmlReader.getNodeValue("//record[position()=" + (i + 1) + "]/header/identifier/text()");
                if (!arguments.onlyHarvest) {
                    processRecord(nodes.item(i), identifier);
                }
                //processRecordAsXml(nodes.item(i), identifier);
                writeNodeToFile(nodes.item(i), date, identifier);
                logger.log(Level.FINE, "number: {0} of {1}", new Object[]{(currentDocsSent++), completeListSize});
            }
            logger.log(Level.INFO, "number: {0} of {1}", new Object[]{(currentDocsSent++), completeListSize});
            return xmlReader.getNodeValue("//resumptionToken/text()");
        } else {
            logger.log(Level.INFO, "{0} for url {1}", new Object[]{error, urlString});
        }
        return null;
    }


    private void getRecordsFromDisk() throws Exception {
        logger.fine("Processing dowloaded files");
        File dir = new File(conf.getProperty("indexDirectory"));
        getRecordsFromDir(dir);
        if (!arguments.onlyHarvest) {
            fastIndexer.sendPendingRecords();
        }
    }

    private void getRecordsFromDir(File dir) throws Exception {
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (currentDocsSent >= arguments.maxDocuments && arguments.maxDocuments > 0) {
                break;
            }
            if (children[i].isDirectory()) {
                getRecordsFromDir(children[i]);
            } else {
                xmlReader.loadXmlFromFile(children[i]);

                String identifier;
                identifier = xmlReader.getNodeValue("/record/header/identifier/text()");
                processRecord(xmlReader.getNodeElement(), identifier);
                //processRecordAsXml(xmlReader.getNodeElement(), identifier);
                logger.log(Level.FINE, "number: {0}", currentDocsSent);
            }
        }
    }

    private void writeNodeToFile(Node node, String date, String identifier) throws Exception {
        String dirName = conf.getProperty("indexDirectory") + File.separatorChar + sdf.format(sdfoai.parse(date));

        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                logger.log(Level.WARNING, "Can''t create: {0}", dirName);
            }
        }
        String xmlFileName = dirName + File.separatorChar + identifier.substring(conf.getProperty("identifierPrefix").length()) + ".xml";

        Source source = new DOMSource(node);
        File file = new File(xmlFileName);
        Result result = new StreamResult(file);
        xformer.transform(source, result);
    }

    private String nodeToString(Node node) throws Exception {

        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);
        xformer.transform(source, new StreamResult(sw));
        return sw.toString();
    }

    private String formatElapsedTime(long timeInMiliseconds) {
        long hours, minutes, seconds;
        long timeInSeconds = timeInMiliseconds / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }
}
