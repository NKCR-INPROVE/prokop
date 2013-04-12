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
package cz.incad.prokop.server.analytics.akka.countexemplars;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import cz.incad.prokop.server.analytics.akka.links.URLValidationMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import org.aplikator.client.shared.data.Record;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class CountExemplarsMain {
    
    public static void main(String[] args) throws IOException {
        System.setProperty("missing.debug", "true");
        CountExemplarsMain main = new CountExemplarsMain();
        main.validate();
    }
    
    
    
    public void validate() throws IOException {
        final File tmpFile = new File("output.csv");
        tmpFile.createNewFile();

        final ActorSystem system = ActorSystem.create("countexemplars");
        system.registerOnTermination(new Runnable() {

            @Override
            public void run() {
            }
        });
        Props props = new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CountExemplarsMaster();
            }
        });
        
        // create the master
        final ActorRef master = system.actorOf(props, "master");

        
        // start the calculation

        Record record = new Record();
        record.setValue("Property:Wizard:SpustitAnalyzu_default-wizard.zdroj", "2");
        master.tell(new StartAnalyze(record),null);
    }
}
