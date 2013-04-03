/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cz.incad.prokop.server.analytics.akka.messages.ResponseValidation;
import cz.incad.prokop.server.analytics.akka.messages.StartValidation;
import cz.incad.prokop.server.analytics.akka.messages.URLRequest;
import cz.incad.prokop.server.analytics.akka.messages.URLResponse;
import cz.incad.prokop.server.analytics.akka.validations.CommonLinkValidate;
import cz.incad.prokop.server.analytics.akka.validations.K3Validate;
import cz.incad.prokop.server.analytics.akka.validations.K4Validate;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavels
 */
public class URLValidationMaster extends UntypedActor {
    

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    
    
    private Connection connection = null;
    private int sentURLRequests = 0;
    private int receivedValidationResult = 0;

    private File outputFile = null;
    
    private final List<String> validatorsNames = new ArrayList<String>(); {
        validatorsNames.add("common");
        validatorsNames.add("K4");
        validatorsNames.add("K3");
    }
    
    private final Map<String,ActorRef> mappers = new HashMap<String, ActorRef>();

    public URLValidationMaster(Connection con, File outputFile) {
        this.connection = con;
        this.outputFile = outputFile;
    }
    
    
    @Override
    public void preStart() {
        super.preStart(); 
        mappers.put("connector",getContext().actorOf(new Props(URLConnector.class),"connector"));
        mappers.put("common",getContext().actorOf(new Props(CommonLinkValidate.class),"common"));
        mappers.put("K4",getContext().actorOf(new Props(K4Validate.class),"K4"));
        mappers.put("K3",getContext().actorOf(new Props(K3Validate.class),"K3"));
        try {
            this.outputFile.createNewFile();
        } catch (IOException ex) {
            //Logger.getLogger(URLValidationMaster.class.getName()).log(Level.SEVERE, null, ex);
            log.error(ex.getMessage());
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void postStop() {
        super.postStop(); //To change body of generated methods, choose Tools | Templates.
        getContext().system().shutdown();
    }
    
    
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartValidation) {
            StartValidation stv = (StartValidation) message;
            String sql = stv.getDataSelector();
            System.out.println("Processing table ");
            try {
                processTable(this.connection, sql);
            } catch(SQLException ex) {
                log.error(ex.getMessage());
                getContext().system().shutdown();
            }
            System.out.println("After processing table ");
            //getContext().stop(mappers.remove("connector"));
        } else if (message instanceof URLResponse) {
            for (String vname : validatorsNames) {
                mappers.get(vname).tell(message, getSelf());
            }
        } else if (message instanceof ResponseValidation) {
            this.receivedValidationResult ++;
            System.out.println(" >> REceived :"+this.receivedValidationResult+" ("+this.sentURLRequests+")");
            ResponseValidation respVal = (ResponseValidation) message;
            System.out.println(">> Received respval :"+respVal);
            System.out.println("Storing resp val "+respVal);
            storeResult(respVal);
            if (receivedValidationResult >= (this.validatorsNames.size()*this.sentURLRequests)) {
                getContext().system().shutdown();
            }
        } else {
            unhandled(message);
        }
    }

    public void storeResult(ResponseValidation validationResponse) throws FileNotFoundException, IOException {
        if (!validationResponse.isValid()) {
            StringBuilder mess = new StringBuilder().append(validationResponse.getUrlResponse().getZaznamId()).append(";");
            mess.append(validationResponse.getActorDescription()).append(";");
            mess.append(validationResponse.getUrlResponse().getMessage()).append(";");
            mess.append(validationResponse.getUrlResponse().getResponseCode()).append(";");
            mess.append(validationResponse.getUrlResponse().getUrl());
            mess.append("\n");
            
            RandomAccessFile raf = new RandomAccessFile(this.outputFile, "rw");
            raf.seek(this.outputFile.length());
            raf.writeBytes(mess.toString());
            raf.close();
        }
    }
    
    private int processTable(Connection conn, String query) throws SQLException {
        List<Integer> result = new JDBCQueryTemplate<Integer>(conn, true) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                sentURLRequests++;
                String urlString = rs.getString("URL");
                int zaznamId = rs.getInt("Zaznam_ID");
                System.out.println("Sending request for "+urlString);
                // sending url reqest
                URLRequest req = new URLRequest(urlString, zaznamId);
                //only for test
                mappers.get("connector").tell(req, getSelf());
                if (sentURLRequests > 100) return false;
                else return true;
            }
        }.executeQuery(query);

        Integer count = result.isEmpty() ? 0 : result.get(0);
        return count;
    }

    /*
    public static Connection getRemoteConnection() throws ClassNotFoundException, SQLException {
        String driverClass = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@//oratest.incad.cz:1521/orcl";
        Class.forName(driverClass);
        return DriverManager.getConnection(url, "DEV_PROKOP", "prokop");
    }

    public static Connection getLocalConnection() throws ClassNotFoundException, SQLException {
        String driverClass = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@//localhost:1521/orcl";
        Class.forName(driverClass);
        return DriverManager.getConnection(url, "DEV_PROKOP", "prokop");
    }*/

}

