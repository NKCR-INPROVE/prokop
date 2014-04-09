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

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityCCNB;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityZaznamRecords;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import cz.incad.prokop.server.utils.TestConnectionUtils;
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
public class EqualityCNBWorker extends UntypedActor {

    private static final String query ="select zaznam from  identifikator id where id.HODNOTA=?";
    
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    
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
        if (message instanceof EqualityCCNB) {
            log.info("EqualityCNB STARTING {}",message);

            EqualityCCNB eccnb = (EqualityCCNB) message;
            List<Integer> ids = new JDBCQueryTemplate<Integer>(getConnection(),true) {

                    @Override
                    public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                        Integer id = rs.getInt("zaznam");
                        returnsList.add(id);
                        return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
                    }
                }.executeQuery(query, eccnb.getCcnb());
            
            log.info("Collected ids for ccnb {} - {}",eccnb.getCcnb(),ids);
            getSender().tell(new EqualityZaznamRecords(ids),getSelf());
        } else {
            unhandled(message);
        }
    }
    
}
