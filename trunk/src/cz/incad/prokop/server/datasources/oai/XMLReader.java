package cz.incad.prokop.server.datasources.oai;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.*;
import java.io.*;


import java.util.ArrayList;
import java.util.logging.Level;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.xml.sax.InputSource;

import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 *
 * @author Alberto
 */
public class XMLReader {

    Logger logger = Logger.getLogger(this.getClass().getName());
    public String separator = " - ";
    private XPath xpath;
    private Document doc;
    private Configuration configuration;

    public XMLReader(Configuration _conf) {
        configuration = _conf;
    }

    public void loadXml(String xml) throws ParserConfigurationException, SAXException, IOException {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            //Document doc = builder.parse(url);

            InputSource source = new InputSource(new StringReader(xml));
            doc = builder.parse(source);


            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
    }

    public void loadXmlFromFile(File file) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            //Document doc = builder.parse(url);

            InputSource source = new InputSource(new FileInputStream(file));
            doc = builder.parse(source);

            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Can''t load xml: {0}", ex.getMessage());
        }
    }

    public void readUrl(String urlString) throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            URL url = new URL(urlString);
            InputStream stream = url.openStream();
            doc = builder.parse(stream);

            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
    }

    public NodeList getListOfNodes(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

    }

    public String[] getListOfValues(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {


        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        String[] s = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s[i] = nodes.item(i).getNodeValue();
        }
        return s;
    }

    public String getNodeValue(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);

        /*
        Object result = expr.evaluate(doc, XPathConstants.NODE);
        Node node = (Node) result; 
        if(node!=null)
        return node.getNodeValue();
        else return "";
         */

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s.append(nodes.item(i).getNodeValue()).append(separator);
        }
        int pos = s.lastIndexOf(separator);
        if (pos > 0) {
            s.delete(pos, s.length());
        }
        return s.toString();

    }

//    public String getMappingGroupValue(MappingGroup mg) {
//        String result = "";
//        try {
//            NodeList nodes = getListOfNodes(mg.basePath);
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Element node = (Element) nodes.item(i);
//
//                for (int j = 0; j < mg.mappings.size(); j++) {
//                    String path = mg.mappings.get(j);
//                    NodeList els = node.getElementsByTagName(path);
//                    if (els.getLength() > 0 && node.getElementsByTagName(path).item(0).hasChildNodes()) {
//                        result += node.getElementsByTagName(path).item(0).getFirstChild().getNodeValue();
//                        if (j < mg.mappings.size() - 1) {
//                            result += mg.rec_sep_str;
//                        }
//                    }
//                }
//                result += mg.sep_str;
//
//            }
//            return result;
//        } catch (Exception ex) {
//            logger.error(ex);
//            return "";
//        }
//    }
    String getNodeValue(Node node, String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(node, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s.append(nodes.item(i).getNodeValue() + separator);
        }
        int pos = s.lastIndexOf(separator);
        if (pos > 0) {
            s.delete(pos, s.length());
        }
        return s.toString();

    }

    public Node getNodeElement() {
        return (Node)this.doc.getDocumentElement();
    }

    ArrayList<String> getNodeValues(Node node, String xPath) throws XPathExpressionException {
        ArrayList<String> s = new ArrayList<String>();
        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(node, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            s.add(nodes.item(i).getNodeValue());
        }
        return s;
    }
}
