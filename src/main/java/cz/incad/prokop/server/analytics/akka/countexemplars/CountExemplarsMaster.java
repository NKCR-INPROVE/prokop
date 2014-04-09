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
import akka.actor.Props;
import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.countexemplars.messages.CountExemplarsResults;
import cz.incad.prokop.server.analytics.akka.countexemplars.messages.OnlyOneExemplarResult;
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
public class CountExemplarsMaster  extends UntypedActor {

    
    private ActorRef countExemplar = null;
    private ActorRef onlyOneActor = null;

    private int sendMessages = 0;
 
    private CountExemplarsResults cs = null;
    private OnlyOneExemplarResult onr = null;
    
    private File outFile;

    public CountExemplarsMaster(File outFile) {
        this.outFile = outFile;
    }
    
    
    @Override
    public void preStart() {
        super.preStart(); //To change body of generated methods, choose Tools | Templates.
        this.onlyOneActor = getContext().actorOf(new Props(OnlyOneExemplarActor.class));
        this.countExemplar = getContext().actorOf(new Props(StatisticCountExemplarsActor.class));
    }
    
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartAnalyze) {
            System.out.println("Start computing ");
            StartAnalyze stv = (StartAnalyze) message;
            
            this.onlyOneActor.tell(stv, getSelf());
            this.sendMessages ++;
            this.countExemplar.tell(stv,getSelf());
            this.sendMessages++;
       } else if (message instanceof CountExemplarsResults) {
            this.sendMessages-=1;
            CountExemplarsResults cs = (CountExemplarsResults) message;
            this.cs = cs;
            System.out.println("REceiving result :"+cs.getResults());
            this.checkStop();
       } else if (message instanceof OnlyOneExemplarResult) {
            this.sendMessages-=1;
            OnlyOneExemplarResult onr = (OnlyOneExemplarResult) message;
            this.onr = onr;
            System.out.println("REceiving result :"+onr.keySet());
            this.checkStop();
        } else {
            unhandled(message);
        }
    }

    private void checkStop() {
        if (this.sendMessages <= 0) {
            FileWriter fw = null;
            try {
                System.out.println("messages :"+this.sendMessages);
                StringBuilder vysledek = new StringBuilder();
                if (this.onr.keySet().size() > 0) {
                    vysledek.append("Záznamy které mají pouze 1 exemplář").append("\n");
                    List<String> list = this.onr.get(this.onr.keySet().iterator().next());
                    for (String line : list) {
                        vysledek.append(line).append("\n");
                    }
                }
                vysledek.append("\n Statistika počtu exemplářů podle čČNB.\n");
                List<String> results = this.cs.getResults();
                for (String line : results) {
                    vysledek.append(line).append("\n");
                }

                fw = new FileWriter(this.outFile);
                fw.write(vysledek.toString());

                getContext().system().shutdown();

            }catch(IOException ex) {
                Logger.getLogger(CountExemplarsMaster.class.getName()).log(Level.SEVERE,ex.getMessage(),ex);
            } finally {
                if (fw != null) { 
                    try {
                        fw.close(); 
                    } catch(IOException ex2)  { 
                        Logger.getLogger(CountExemplarsMaster.class.getName()).log(Level.SEVERE,ex2.getMessage(),ex2);
                    } 
                }
            }
        } 
    }
}
