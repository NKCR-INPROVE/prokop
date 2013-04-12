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
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class CountExemplarsMaster  extends UntypedActor {

    
    
    private ActorRef countExemplar = null;
    private ActorRef onlyOneActor = null;

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
            this.countExemplar.tell(stv,getSelf());
        } else {
            unhandled(message);
        }
    }
}
