/*
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package cz.incad.prokop.server.analytics.akka.equality;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import cz.incad.prokop.server.analytics.akka.countexemplars.CountExemplarsMain;
import cz.incad.prokop.server.analytics.akka.countexemplars.CountExemplarsMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class Equality {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Equality ... ");
        System.setProperty("equality.debug", "true");
        Equality main = new Equality();
        ActorRef master = main.validate();
        
        Thread.sleep(20000);
        System.out.println("Killing system");
        master.tell(PoisonPill.getInstance(), null);
    }
    
    
    public ActorRef validate() throws IOException {
        final File tmpFile = new File("testout.txt");
        tmpFile.createNewFile();

        final ActorSystem system = ActorSystem.create("equality");
        system.registerOnTermination(new Runnable() {

            @Override
            public void run() {
                System.out.println("System killed");
            }
        });
        Props props = new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                try {
                    return new EqualityMaster(File.createTempFile("out", "txt"));
                } catch (IOException ex) {
                    Logger.getLogger(CountExemplarsMain.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        });
        
        // create the master
        final ActorRef master = system.actorOf(props, "master");

        
        // start the calculation

        Record record = new Record();
        record.setValue("Property:Wizard:SpustitAnalyzu_default-wizard.zdroj", "2");
        master.tell(new StartAnalyze(record),null);

        return master;
    }
}
