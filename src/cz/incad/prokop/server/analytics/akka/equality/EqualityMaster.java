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
import akka.actor.Props;
import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.countexemplars.CountExemplarsMaster;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityResponse;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class EqualityMaster extends UntypedActor {

    private ActorRef worker = null;
    private File outFile;

    public EqualityMaster(File outFile) {
        this.outFile = outFile;
    }

    @Override
    public void preStart() {
        super.preStart(); //To change body of generated methods, choose Tools | Templates.
        this.worker = getContext().actorOf(new Props(EqualityWorker.class));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartAnalyze) {
            worker.tell(message, getSelf());
        } else if (message instanceof EqualityResponse) {
            EqualityResponse er = (EqualityResponse) message;
            writeRulsts(er);
        } else {
            unhandled(message);
        }
    }

    private void writeRulsts(EqualityResponse eqRes) {
        FileWriter fw = null;
        try {
            List<String> messages = eqRes.getMessages();
            StringBuilder strBuilder = new StringBuilder();
            for (String line : messages) {
                strBuilder.append(line).append('\n');
            }

            fw = new FileWriter(this.outFile);
            fw.write(strBuilder.toString());

            getContext().system().shutdown();

        } catch (IOException ex) {
            Logger.getLogger(CountExemplarsMaster.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex2) {
                    Logger.getLogger(CountExemplarsMaster.class.getName()).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
            }
        }
    }
}
