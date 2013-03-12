package cz.incad.prokop.server.analytics;

import static org.aplikator.server.data.RecordUtils.newRecord;
import static org.aplikator.server.data.RecordUtils.newSubrecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.Files;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.client.shared.rpc.impl.ProcessRecords;
import org.aplikator.server.Context;
import org.aplikator.server.data.BinaryData;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.util.Configurator;

import com.google.common.base.Objects;

import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ExistenceOdkazu implements Analytic {

    Logger log = Logger.getLogger(ExistenceOdkazu.class.getName());


    private static final String query = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev, id.hodnota  from identifikator id left outer join zaznam zaz on id.zaznam = zaz.Zaznam_ID where id.typ = 'cCNB' order by id.hodnota, zaz.hlavniNazev";


    /*
     *  ODKAZY – existence platnost
a)      Katalog NKCR – vypsat záznamy, které mají link do K4 a link není platný (error UUID). Report : ID záznamu/link/status

     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(String params, Record analyza, Context context) {
        //ukázka, jak použít parametry
        String userHome = Configurator.get().getConfig().getString(Configurator.HOME);
        String configFileName = userHome+System.getProperty("file.separator")+params;
        log.info("Random harvester config file name: "+configFileName);
        Connection conn = PersisterFactory.getPersister().getJDBCConnection();

        File tempFile = null;
        Writer tempFileWriter = null;

        try{
            tempFile = createTempFile();
            log.info("ExistenceOdkazu TEMPFILE:" + tempFile);
            tempFileWriter = new FileWriter(tempFile);

            final Writer vysledek = tempFileWriter;

            List<Integer> result = new JDBCQueryTemplate<Integer>(conn) {
                @Override
                public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                    //String analyzaId = rs.getString("analyzaID");
                    String urlString = rs.getString("URL");
                    String zaznamId = rs.getString("Zaznam_ID");
                    String hodnota = rs.getString("hodnota");
                    String hlNazev = rs.getString("hlavniNazev");
                    
                    Result res = null;
                    try {
                        res = testURL(urlString);
                        Integer count  = 0;
                        if (!returnsList.isEmpty()) {
                            count = returnsList.get(0);
                        } 
                        count = res.isLinkBroken() ? count : count +1;
                        returnsList.set(0, count);
                    } catch (IOException ex) {
                        // cannot test url: log this event
                        try { vysledek.append("ID:"+zaznamId+"\tURL:"+urlString+"\tError:"+ex.getMessage()).append("\n"); } catch(IOException ex2) {  Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex2);}
                        Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if (res != null) {
                        try {
                            vysledek.append("ID:"+zaznamId+"\tURL:"+urlString+"\t"+res.toTabbedString()).append("\n");
                        } catch (IOException ex) {
                           Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return super.handleRow(rs, returnsList); 
                }
            }.executeQuery(query);
            
            Integer count = result.isEmpty() ? 0 : result.get(0);
            vysledek.append ("\nPočet záznamů s chybným url: ").append(""+count).append("\n");
            log.info("Analyza ukoncena");
            vysledek.flush();
            
            if (tempFile != null){
                BinaryData bd  = new BinaryData("ExistenceOdkazu.txt", new FileInputStream(tempFile), tempFile.length());
                Structure.analyza.vysledek.setValue(analyza, bd);
            }

        } catch (Exception ex){
            log.log(Level.SEVERE, "Chyba v analyze", ex);
        } finally {
            if (tempFileWriter != null) { 
                try { 
                    tempFileWriter.close();
                } catch (IOException ex) {
                    Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }           
    }

    public static Result testURL(String url) throws IOException {
        HttpURLConnection httpUrlConn = (HttpURLConnection) (new URL(url)).openConnection();
        httpUrlConn.setReadTimeout(2500);
        httpUrlConn.setConnectTimeout(2500);
        // jak na to ?
        httpUrlConn.setInstanceFollowRedirects(true);
        int respCode = httpUrlConn.getResponseCode();
        String respMessage = httpUrlConn.getResponseMessage();
        
        return new Result(respMessage, respCode, httpUrlConn.getURL());
    }

    private File createTempFile() throws IOException {
        File tempFile;
        File tempDir = Files.createTempDir();
        tempFile = new File(tempDir, UUID.randomUUID().toString());
        tempFile.createNewFile();
        return tempFile;
    }

    public static class Result {

        private int responseCode = -1;
        private String errMessage;
        private URL url;
        
        public Result(String errMessage, int respCode, URL url) {
            this.errMessage = errMessage;
            this.responseCode = respCode;
            this.url = url;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getErrMessage() {
            return errMessage;
        }
        
        public boolean isOkCode() {
            return this.responseCode == 200;
        }

        @Override
        public String toString() {
            return "Result{" + "responseCode=" + responseCode + ", errMessage=" + errMessage + '}';
        }
    
        public String toTabbedString() {
            return "ResponseCode:"+this.responseCode+"\tResultMessage:"+this.errMessage;
        }
        
        public boolean containsK4ErrorParam() {
            boolean errorParam = false;
            String q = this.url.getQuery();
            String[] splitted = q.split("&");
            for (String par : splitted) {
                par = par.trim();
                if (par.startsWith("error=uuid_not_found")) {
                    errorParam = true;
                    break;
                }
            }
            return errorParam;
        }
        
        
        public boolean isLinkBroken() {
            return ((!isOkCode())  ||  containsK4ErrorParam());
        }
    }
    
    public static void main(String[] args) throws IOException, IOException {
        Result result = testURL("http://vmkramerius:8080/search/i.jsp?pid=uuid:4a7ec660-af36-11dd-a782-000d606f5dca");
        //Result result = testURL("http://194.108.215.227:8080/search/i.jsp?pid=uuid:4a7ec660-af36-11dd-a782-000d606f5dc6");
        System.out.println("result = "+result.isLinkBroken());
    }
}
