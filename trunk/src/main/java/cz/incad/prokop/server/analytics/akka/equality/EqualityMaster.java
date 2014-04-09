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
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityCCNB;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityLabelsDifferent;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityZaznamRecords;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import cz.incad.prokop.server.utils.TestConnectionUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class EqualityMaster extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    
    private static String query = "select count(*) as count, max(id1.HODNOTA) as ccnb from identifikator id1 join identifikator id2 on (id1.HODNOTA = id2.HODNOTA) where id1.typ = 'cCNB' group by id1.HODNOTA having count(*) > 2";


    private ActorRef cnbFounder = null;
    private ActorRef labelsComparator = null;
    private File outFile;

    private int cnbsCounter = 0;
    private boolean allRequestsSent = false;
    
    public EqualityMaster(File outFile) {
        this.outFile = outFile;
    }

    @Override
    public void preStart() {
        super.preStart(); //To change body of generated methods, choose Tools | Templates.
        this.cnbFounder = getContext().actorOf(new Props(EqualityCNBWorker.class),"cnbfounder");
        this.labelsComparator = getContext().actorOf(new Props(EqualitySameLabelWorker.class),"labelscomparator");
    }

    @Override
    public void postStop() {
        super.postStop(); //To change body of generated methods, choose Tools | Templates.
        getContext().system().shutdown();
    }

    
    
    public static Connection getConnection() {
        if (Boolean.getBoolean("equality.debug")) {
            try {
                return TestConnectionUtils.getConnection();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(EqualityCNBWorker.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (SQLException ex) {
                Logger.getLogger(EqualityCNBWorker.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return PersisterUtils.getConnection();
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartAnalyze) {
                log.info("ContCNB STARTING {}",message);

                List<String> ccnbs = new JDBCQueryTemplate<String>(getConnection(),true) {

                    @Override
                    public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                        String ccnb = rs.getString("ccnb");
                        
                        returnsList.add(ccnb);
                        return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
                    }
                }.executeQuery(query);
                log.info("All duplicates ccnb collected");
                
                for (String ccnb : ccnbs) {
                    cnbFounder.tell(new EqualityCCNB(ccnb),getSelf());
                    cnbsCounter ++;
                }
                log.info("All requests sent");
                this.allRequestsSent = true;
                if (this.cnbsCounter == 0) {
                    getContext().system().shutdown();
                }
        } else if (message instanceof EqualityZaznamRecords) {
            log.info("Forward zaznam records");
            labelsComparator.tell(message, getSelf());
        } else if (message instanceof EqualityLabelsDifferent) {
            EqualityLabelsDifferent labelsDiff = (EqualityLabelsDifferent) message;
            log.info("Receiving result");
            this.cnbsCounter --;
            writeRulsts(labelsDiff);
            if (allRequestsSent && this.cnbsCounter == 0) {
                getContext().system().shutdown();
            }
        } else {
            unhandled(message);
        }
    }

    private String writeLine(List<String> line) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0,ll=line.size(); i < ll; i++) {
            builder.append(line.get(i));
            
        }
        return builder.toString();
    }
    
    private void writeRulsts(EqualityLabelsDifferent labelsDiff) throws FileNotFoundException, IOException {
        StringBuilder builder = new StringBuilder(">");
        List<List<String>> differentNames = labelsDiff.getDifferentNames();
        for (List<String> line : differentNames) {
            builder.append("\t").append(writeLine(line));
        }
        
        RandomAccessFile raf = new RandomAccessFile(this.outFile, "rw");
        raf.seek(this.outFile.length());
        raf.writeBytes(builder.toString());
        raf.close();
    }
}
