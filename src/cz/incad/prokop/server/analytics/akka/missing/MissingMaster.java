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
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.analytics.akka.messages.StoppedWork;
import cz.incad.prokop.server.analytics.akka.missing.messages.CountCNBResult;
import cz.incad.prokop.server.analytics.akka.missing.messages.EmptyCNBResult;
import cz.incad.prokop.server.analytics.akka.missing.messages.NoCNBResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class MissingMaster extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    
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
        this.noCnbWorker = getContext().actorOf(new Props(NoCNBWorker.class),"nocnb");
        this.emptyCNBWorker = getContext().actorOf(new Props(EmptyCNBWorker.class),"emptycnb");
        this.countCNBWorker = getContext().actorOf(new Props(CountCNBWorker.class),"countcnb");
    }

    @Override
    public void postStop() {
        super.postStop(); 
        log.info("stopping {}",this.getSelf().path());
    }
    

    
    
    @Override
    public void onReceive(Object mess) throws Exception {
        if (mess instanceof StartAnalyze) {
            log.info("STARTING ANALYZE {}",mess);
            noCnbWorker.tell(mess, getSelf());
            emptyCNBWorker.tell(mess,getSelf());
            countCNBWorker.tell(mess,getSelf());
        } else if (mess instanceof NoCNBResult ){
            NoCNBResult nocnb = (NoCNBResult) mess;
            this.noCnbResult = nocnb;
            log.info("NoCNB result {}",nocnb.getNoCNB());
        } else if (mess instanceof EmptyCNBResult ){
            EmptyCNBResult empty = (EmptyCNBResult) mess;
            log.info("EmptyCNB result {}",empty.getEmptyCNB().size());
            if (this.emptyCNBResult == null) {
                this.emptyCNBResult = empty;
            } else {
                List<String> elist = this.emptyCNBResult.getEmptyCNB();
                List<String> all = new ArrayList<String>(elist);
                all.addAll(empty.getEmptyCNB());
                this.emptyCNBResult = new EmptyCNBResult(all);
            }
        } else if (mess instanceof CountCNBResult ){
            CountCNBResult count = (CountCNBResult) mess;
            log.info("CountCNB result {}",count.getCount().size());
            this.countCNBResult = count;
        } else if (mess instanceof StoppedWork ){
            this.resultCounter --;
            log.info("decreasing counter {} ",this.resultCounter);
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
        FileWriter fw = new FileWriter(this.outFile);
        try {
            List<String> count = countCNBResult.getCount();
            fw.write("Počet záznamů s čČNB: "); fw.write('\n');
            for (String line : count) { fw.write(line); fw.write('\n'); }
            fw.write("\n");

            fw.write("Záznamy bez čČNB: "); fw.write('\n');
            List<String> nocnb = noCnbResult.getNoCNB();
            for (String line : nocnb) { fw.write(line); fw.write('\n');}

            fw.write("Záznamy s prázdným čČNB: "); fw.write('\n');
            List<String> emptycnb = emptyCNBResult.getEmptyCNB();
            for (String line : emptycnb) { fw.write(line); fw.write('\n');}

        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
}
