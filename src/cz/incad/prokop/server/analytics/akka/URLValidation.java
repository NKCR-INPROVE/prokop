/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import cz.incad.prokop.server.analytics.akka.messages.StartValidation;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavels
 */
public class URLValidation  {
    
    
    
    private static final String query = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev, id.hodnota  from identifikator id left outer join zaznam zaz on id.zaznam = zaz.Zaznam_ID where id.typ = 'cCNB' order by id.hodnota, zaz.hlavniNazev";
    private static final String query2 ="select * from DEV_PROKOP.DIGITALNIVERZE as dg\n" +
                                        " join DEV_PROKOP.ZAZNAM zaznam on(dg.zaznam=zaznam.zaznam_id)\n";
    private static final String query3 ="select URL, ZAZNAM as ZAZNAM_ID from DEV_PROKOP.DIGITALNIVERZE";
    
    
    public void validate(final Connection con) throws IOException {
        
        final File tmpFile = new File("output.csv");
        tmpFile.createNewFile();
        
        final ActorSystem system = ActorSystem.create("validaton");
        system.registerOnTermination(new Runnable() {

            @Override
            public void run() {
                System.out.println("OUTPUT FILE = "+tmpFile.getAbsolutePath());
            }
        });
        Props props = new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new URLValidationMaster(con, tmpFile);
            }
        });
        
        // create the master
        final ActorRef master = system.actorOf(props, "master");

        
        // start the calculation
        master.tell(new StartValidation(query3),null);
        
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        URLValidation urlVal = new URLValidation();
        urlVal.validate(getLocalConnection());
    }
    
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
    }
}