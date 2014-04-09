package cz.incad.prokop.server.analytics;

import akka.actor.*;
import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.akka.equality.EqualityMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.data.Analyza;
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

import static cz.incad.prokop.server.analytics.PoctyExemplaru.getTmpFile;

public class ShodaUdaju implements Analytic {

    static Logger log = Logger.getLogger(ShodaUdaju.class.getName());

    private static ActorSystem SHODA_UDAJU_ACTOR_SYSTEM = null;


    @Override
    public boolean isRunning() {
        return ((SHODA_UDAJU_ACTOR_SYSTEM != null) && (!SHODA_UDAJU_ACTOR_SYSTEM.isTerminated()));
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public String getWizardPageKey() {
        return null;
    }
    
    
    
    @Override
    public void stopAnalyze(Record modul, Record analyza, Context ctx) {
        if (isRunning()) {
            SHODA_UDAJU_ACTOR_SYSTEM.actorFor("user/master").tell(PoisonPill.getInstance(), null);
        }
    }

    
    

    /*
     *  SHODA ÚDAJŮ
a)      Vypsat záznamy se shodným čČNB a rozdílným Názvem
(non-Javadoc)
     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(final org.aplikator.client.shared.data.Record params, final Record modul, final Record analyza, final Context ctx) {
        try {
            final File file = getTmpFile();

            SHODA_UDAJU_ACTOR_SYSTEM = ActorSystem.create("equality");
            SHODA_UDAJU_ACTOR_SYSTEM.registerOnTermination(new Runnable() {
                @Override
                public void run() {
                    BinaryData bd;
                    try {
                        RecordContainer rc = new RecordContainer();
                        Structure.analyza.ukonceni.setValue(analyza, new Date());

                        bd = new BinaryData("ShodaUdaju.txt", new FileInputStream(file), file.length());
                        Structure.analyza.vysledek.setValue(analyza, bd);
                        Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
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
                public UntypedActor create() throws Exception {
                    return new EqualityMaster(file);
                }
            });

            // create the master
            ActorRef master = SHODA_UDAJU_ACTOR_SYSTEM.actorOf(props, "master");
            // start the calculation
            master.tell(new StartAnalyze(params), null);

        } catch (IOException ex) {
            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    private static class Radek{
        public String id;
        public String nazev;
        public StringBuilder text = new StringBuilder();
        public boolean zapsan = false;
        @Override
        public String toString() {
            return "Radek{" + "id=" + id + ", nazev=" + nazev + ", text=" + text + ", zapsan=" + zapsan + '}';
        }
    }

}
