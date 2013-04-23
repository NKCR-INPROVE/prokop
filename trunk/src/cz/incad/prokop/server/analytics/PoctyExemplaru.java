package cz.incad.prokop.server.analytics;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;

import cz.incad.prokop.server.Structure;
import static cz.incad.prokop.server.analytics.ExistenceOdkazu.getTmpFile;
import cz.incad.prokop.server.analytics.akka.countexemplars.CountExemplarsMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.data.Analyza;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.aplikator.server.data.BinaryData;

public class PoctyExemplaru implements Analytic {

    private static ActorSystem POCTY_EXEMPLARU_ACTOR_SYSTEM = null;

    @Override
    public boolean isRunning() {
        return ((POCTY_EXEMPLARU_ACTOR_SYSTEM != null) && (!POCTY_EXEMPLARU_ACTOR_SYSTEM.isTerminated()));
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopAnalyze(Record modul, Record analyza, Context ctx) {
        if (isRunning()) {
            POCTY_EXEMPLARU_ACTOR_SYSTEM.actorFor("user/master").tell(PoisonPill.getInstance(), null);
        }
    }

    
    @Override
    public String[] getWizardKeys() {
        return new String[] {};
    }

    public static File getTmpFile() throws IOException {
        return File.createTempFile("output", "txt");
    }


    /*
     *   POČTY – exemplářů (je závislé na profilu OAI)
     a)      NKCR – které záznamy mají pouze 1 exemplář
     b)      MZK+NKCR+OLOMOUC – statistika počtu exemplářů. (2320 dokumentů 3X, 123.445 2x … )
     (non-Javadoc)
     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(final org.aplikator.client.shared.data.Record params, final Record modul, final Record analyza, final Context ctx) {

        try {
            final File file = getTmpFile();

            POCTY_EXEMPLARU_ACTOR_SYSTEM = ActorSystem.create("validaton");
            POCTY_EXEMPLARU_ACTOR_SYSTEM.registerOnTermination(new Runnable() {
                @Override
                public void run() {
                    BinaryData bd;
                    try {
                        RecordContainer rc = new RecordContainer();
                        Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
                        Structure.analyza.ukonceni.setValue(analyza, new Date());
                        Structure.modul.stav.setValue(modul, Analyza.Stav.UKONCENA.getValue());

                        
                        bd = new BinaryData("PoctyExemplaru.txt", new FileInputStream(file), file.length());
                        Structure.analyza.vysledek.setValue(analyza, bd);

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
                    return new CountExemplarsMaster(file);
                }
            });

            // create the master
            ActorRef master = POCTY_EXEMPLARU_ACTOR_SYSTEM.actorOf(props, "master");
            // start the calculation
            master.tell(new StartAnalyze(params), null);

        } catch (IOException ex) {
            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }
}
