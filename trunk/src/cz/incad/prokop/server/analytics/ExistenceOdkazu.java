package cz.incad.prokop.server.analytics;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.akka.links.URLValidationMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.data.BinaryData;
import org.aplikator.server.persistence.PersisterFactory;
import cz.incad.prokop.server.data.Analyza;
import java.util.Date;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.RecordContainer;


public class ExistenceOdkazu implements Analytic {


    private static final String query = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev, id.hodnota  from identifikator id left outer join zaznam zaz on id.zaznam = zaz.Zaznam_ID where id.typ = 'cCNB' order by id.hodnota, zaz.hlavniNazev";
    private static final String query2 ="select * from DEV_PROKOP.DIGITALNIVERZE as dg\n" +
                                        " join DEV_PROKOP.ZAZNAM zaznam on(dg.zaznam=zaznam.zaznam_id)\n";

    private static final String query3 ="select URL, ZAZNAM as ZAZNAM_ID from DEV_PROKOP.DIGITALNIVERZE";

 
    public static Connection getConnection() {
        Connection conn = PersisterFactory.getPersister().getJDBCConnection();
        return conn;
    }

    public static File getTmpFile() throws IOException {
        return File.createTempFile("output", "csv");
    }
    
    /*
     *  ODKAZY – existence platnost
     a)      Katalog NKCR – vypsat záznamy, které mají link do K4 a link není platný (error UUID). Report : ID záznamu/link/status

     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(final String params, final Record analyza, final Context context) {
 
            try {
                final File file = getTmpFile();
                final Connection conn = getConnection();

                ActorSystem system = ActorSystem.create("validaton");
                system.registerOnTermination(new Runnable() {

                    @Override
                    public void run() {
                        BinaryData bd;
                        try {
                            RecordContainer rc = new RecordContainer();
                            Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
                            Structure.analyza.ukonceni.setValue(analyza, new Date());
                            
                            bd = new BinaryData("ExistenceOdkazu.txt", new FileInputStream(file), file.length());
                            Structure.analyza.vysledek.setValue(analyza, bd);

                            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
                            rc = context.getAplikatorService().processRecords(rc);
 
                            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.INFO, "Analyza skoncena");
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                Props props = new Props(new UntypedActorFactory() {
                    public UntypedActor create() throws Exception{
                        return new URLValidationMaster(file);
                    }
                });

                // create the master
                ActorRef master = system.actorOf(props, "master");

                // start the calculation
                master.tell(new StartAnalyze(params),null);
                
            } catch(IOException ex) {
                Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE,ex.getMessage(), ex);
            }
    }
}
