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
package cz.incad.prokop.server.analytics.akka.missing;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.analytics.akka.missing.messages.CountCNBResult;
import cz.incad.prokop.server.analytics.akka.missing.messages.EmptyCNBResult;
import cz.incad.prokop.server.analytics.akka.missing.messages.NoCNBResult;
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
public class MissingMaster extends UntypedActor {

    int resultCounter = 3;

    private ActorRef noCnbWorker;
    private ActorRef emptyCNBWorker;
    private ActorRef countCNBWorker;
    
    private File outFile = null;
    
    public MissingMaster(File ofile) {
        this.outFile = ofile;
    }
    
    private NoCNBResult noCnbResult;
    private EmptyCNBResult emptyCNBResult;
    private CountCNBResult countCNBResult;
    
    @Override
    public void preStart() {
        super.preStart(); //To change body of generated methods, choose Tools | Templates.
        this.noCnbWorker = getContext().actorOf(new Props(NoCNBWorker.class));
        this.emptyCNBWorker = getContext().actorOf(new Props(EmptyCNBWorker.class));
        this.countCNBWorker = getContext().actorOf(new Props(CountCNBWorker.class));
    }
    
    
    @Override
    public void onReceive(Object mess) throws Exception {
        if (mess instanceof StartAnalyze) {
            noCnbWorker.tell(mess, getSelf());
            emptyCNBWorker.tell(mess,getSelf());
            countCNBWorker.tell(mess,getSelf());
        } else if (mess instanceof NoCNBResult ){
            NoCNBResult nocnb = (NoCNBResult) mess;
            this.noCnbResult = nocnb;
            System.out.println("NOCNB = "+nocnb.getNoCNB());
            this.resultCounter --;
            this.checkStop();
        } else if (mess instanceof EmptyCNBResult ){
            System.out.println("REceive result rom emtpyCNB");
            EmptyCNBResult empty = (EmptyCNBResult) mess;
            this.emptyCNBResult = empty;
            System.out.println("empty "+empty.getEmptyCNB());
            this.resultCounter --;
            this.checkStop();            
        } else if (mess instanceof CountCNBResult ){
            System.out.println("Receive result from countCNB");
            CountCNBResult count = (CountCNBResult) mess;
            this.countCNBResult = count;
            System.out.println("Count c "+count.getCount());
            this.resultCounter --;
            this.checkStop();
        } else {
            unhandled(mess);
        }
    }
    
    public void checkStop() {
        if (this.resultCounter <= 0 ){
            try {
                storeResults();
            }catch(IOException ex) {
                Logger.getLogger(MissingMaster.class.getName()).log(Level.SEVERE, ex.getMessage(),ex);
            }
            getContext().system().shutdown();
        }
    }

    private void storeResults() throws IOException {
        // save result 
        StringBuilder vysledek =  new StringBuilder();
        List<String> count = countCNBResult.getCount();
        vysledek.append("Počet záznamů s čČNB: ");
        for (String line : count) { vysledek.append(line); }
        vysledek.append("\n");

        vysledek.append("Záznamy bez čČNB: ");
        List<String> nocnb = noCnbResult.getNoCNB();
        for (String line : nocnb) { vysledek.append(line); }
        vysledek.append("\n");
        
        vysledek.append("Záznamy s prázdným čČNB: ").append("\n");
        List<String> emptycnb = emptyCNBResult.getEmptyCNB();
        for (String line : emptycnb) { vysledek.append(line); }
            
        
        FileWriter fw = new FileWriter(this.outFile);
        fw.write(vysledek.toString());
        fw.close();
    }
}
