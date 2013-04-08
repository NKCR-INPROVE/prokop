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
            System.out.println("Starting analyze workers ...");
            noCnbWorker.tell(mess, getSelf());
            emptyCNBWorker.tell(mess,getSelf());
            countCNBWorker.tell(mess,getSelf());
        } else if (mess instanceof NoCNBResult ){
            System.out.println("REceived result from noCNB");
            NoCNBResult nocnb = (NoCNBResult) mess;
            System.out.println("NOCNB = "+nocnb.getNoCNB());
            this.resultCounter --;
            this.checkStop();
        } else if (mess instanceof EmptyCNBResult ){
            System.out.println("REceive result rom emtpyCNB");
            EmptyCNBResult empty = (EmptyCNBResult) mess;
            System.out.println("empty "+empty.getEmptyCNB());
            this.resultCounter --;
            this.checkStop();            
        } else if (mess instanceof CountCNBResult ){
            System.out.println("Receive result from countCNB");
            CountCNBResult count = (CountCNBResult) mess;
            System.out.println("Count c "+count.getCount());
            this.resultCounter --;
            this.checkStop();
            System.out.println("");
        } else {
            unhandled(mess);
        }
    }
    
    public void checkStop() {
        if (this.resultCounter <= 0 ){
            // save result 
            getContext().system().shutdown();
        }
    }
}
