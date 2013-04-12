package cz.incad.prokop.server.analytics.akka.links;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.descriptor.Property;

/**
 *
 * @author pavels
 */
public class URLCheckMain  {
    
    
    
    public void validate() throws IOException {
        
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
                return new URLValidationMaster(tmpFile);
            }
        });
        
        // create the master
        final ActorRef master = system.actorOf(props, "master");
        System.out.println("master:"+master.path().toString());
        ActorRef actorFor = system.actorFor("user/master");
        System.out.println("master 2:"+actorFor);
        System.out.println("master 2 path :"+actorFor.path().toString());
        actorFor.tell(PoisonPill.getInstance(), null);
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        URLCheckMain urlVal = new URLCheckMain();
        urlVal.validate();
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