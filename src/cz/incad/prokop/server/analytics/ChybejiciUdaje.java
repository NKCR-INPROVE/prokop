package cz.incad.prokop.server.analytics;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.data.BinaryData;

import cz.incad.prokop.server.Structure;
import static cz.incad.prokop.server.analytics.ExistenceOdkazu.getTmpFile;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.analytics.akka.missing.MissingMaster;
import cz.incad.prokop.server.data.Analyza;
import java.util.Date;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.RecordContainer;

public class ChybejiciUdaje implements Analytic {

    private static ActorSystem CHYBEJICI_UDAJE_ACTOR_SYSTEM = null;
    
    
    @Override
    public boolean isRunning() {
        return ((CHYBEJICI_UDAJE_ACTOR_SYSTEM != null) && (!CHYBEJICI_UDAJE_ACTOR_SYSTEM.isTerminated()));
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopAnalyze(Record modul, Record analyza, Context ctx) {
        if (isRunning()) {
            CHYBEJICI_UDAJE_ACTOR_SYSTEM.actorFor("user/master").tell(PoisonPill.getInstance(), null);
        }
    }

    
    @Override
    public String[] getWizardKeys() {
        if (this.isRunning()) return new String[] {};
        else return new String[] {"default-wizard"};
    }
    

    // private static final String PARAM = "'Aleph NKP', 'Aleph MZK', 'Aleph NKP CNB', 'Aleph NKP base CNB'";
    /*
     *  CHYBĚJÍCÍ ÚDAJE
     a)      Statistika kolik má čČNB
     b)      Vypsat záznamy z NKCR a MZK, které nemají čČNB(non-Javadoc)
     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(final org.aplikator.client.shared.data.Record params, final Record modul, final Record analyza, final Context ctx) {
        try {
            if (this.isRunning()) {
                return;
            }
            
            final File file = getTmpFile();
            CHYBEJICI_UDAJE_ACTOR_SYSTEM = ActorSystem.create("missing");
            CHYBEJICI_UDAJE_ACTOR_SYSTEM.registerOnTermination(new Runnable() {

                @Override
                public void run() {
                    BinaryData bd;
                    try {
                        RecordContainer rc = new RecordContainer();
                        Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
                        Structure.analyza.ukonceni.setValue(analyza, new Date());

                        bd = new BinaryData("ChybejiciUdaje.txt", new FileInputStream(file), file.length());
                        Structure.analyza.vysledek.setValue(analyza, bd);

                        Structure.modul.stav.setValue(modul, Analyza.Stav.UKONCENA.getValue());
                        
                        rc.addRecord(null, analyza, analyza, Operation.UPDATE);
                        rc.addRecord(null, modul, modul, Operation.UPDATE);
                        
                        rc = ctx.getAplikatorService().processRecords(rc);

                        Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.INFO, "Analyza skoncena");
                    } catch (Exception ex) {
                        Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            Props props = new Props(new UntypedActorFactory() {
                public UntypedActor create() throws Exception {
                    return new MissingMaster(file);
                }
            });

            // create the master
            ActorRef master = CHYBEJICI_UDAJE_ACTOR_SYSTEM.actorOf(props, "master");

            // start the calculation
            master.tell(new StartAnalyze(params), null);

        } catch (IOException ex) {
            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
