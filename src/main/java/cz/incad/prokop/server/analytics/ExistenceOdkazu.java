package cz.incad.prokop.server.analytics;

import akka.actor.*;
import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.akka.links.URLValidationMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.data.Analyza;
import cz.incad.prokop.server.data.Modul;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import org.aplikator.server.data.BinaryData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExistenceOdkazu implements Analytic {

    public static ActorSystem EXISTENCE_ODKAZU_ACTOR_SYSTEM = null;
    
    
    @Override
    public boolean isRunning() {
        return ((EXISTENCE_ODKAZU_ACTOR_SYSTEM != null) && (!EXISTENCE_ODKAZU_ACTOR_SYSTEM.isTerminated()));
     }

    @Override
    public void stopAnalyze(Record modul, Record analyza, Context ctx) {
        if (isRunning()) {
            EXISTENCE_ODKAZU_ACTOR_SYSTEM.actorFor("user/master").tell(PoisonPill.getInstance(), null);
        }
    }

    
    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getWizardPageKey() {
        return Modul.WIZARD_PAGE_KEY;
     }


    public static File getTmpFile() throws IOException {
        return File.createTempFile("output", "txt");
    }
    
    /*
     *  ODKAZY – existence platnost
     a)      Katalog NKCR – vypsat záznamy, které mají link do K4 a link není platný (error UUID). Report : ID záznamu/link/status

     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(final org.aplikator.client.shared.data.Record params, final Record modul, final Record analyza, final Context ctx) {
 
            try {
                final File file = getTmpFile();

                EXISTENCE_ODKAZU_ACTOR_SYSTEM = ActorSystem.create("validaton");
                EXISTENCE_ODKAZU_ACTOR_SYSTEM.registerOnTermination(new Runnable() {

                    @Override
                    public void run() {
                        BinaryData bd;
                        try {
                            RecordContainer rc = new RecordContainer();
                            Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
                            Structure.analyza.ukonceni.setValue(analyza, new Date());
                            
                            bd = new BinaryData("ExistenceOdkazu.csv", new FileInputStream(file), file.length());
                            Structure.analyza.vysledek.setValue(analyza, bd);
                            Structure.modul.stav.setValue(modul, Analyza.Stav.UKONCENA.getValue());

                            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
                            rc.addRecord(null, modul, modul, Operation.UPDATE);

                            rc = ctx.getAplikatorService().processRecords(rc);
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
                ActorRef master = EXISTENCE_ODKAZU_ACTOR_SYSTEM.actorOf(props, "master");
                // start the calculation
                master.tell(new StartAnalyze(params),null);
                
            } catch(IOException ex) {
                Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE,ex.getMessage(), ex);
            }
    }
}
