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
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityZaznamRecords;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityLabelsDifferent;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import cz.incad.prokop.server.utils.TestConnectionUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class EqualitySameLabelWorker extends UntypedActor {
    
    
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private static String query = "select zaz.ZAZNAM_ID,zaz.HLAVNINAZEV, zaz.URL,zaz.SKLIZEN from zaznam zaz where zaz.ZAZNAM_ID=?";

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
        if (message instanceof EqualityZaznamRecords) {
            final Set<String> labels = new HashSet<String>();
            final List<List<String>> rows = new ArrayList<List<String>>();
            EqualityZaznamRecords eq = (EqualityZaznamRecords) message;
            List<Integer> ids = eq.getCnbRecords();
            for (Integer id : ids) {
                List<String> row = new JDBCQueryTemplate<String>(getConnection(),true) {

                    @Override
                    public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                        String nazev = rs.getString("HLAVNINAZEV");
                        String url = rs.getString("URL");
                        int zaznamId = rs.getInt("ZAZNAM_ID");
                        returnsList.add(""+zaznamId);
                        returnsList.add(nazev);
                        returnsList.add(url);
 
                        labels.add(nazev);
                        return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
                    }
                }.executeQuery(query, id);
                rows.add(row);
            }
            // nebyly vsechny stejne (velikost setu je vetsi nez dve)
            if (labels.size() > 2) {
                log.info("\tfound equal labels ", labels);
                getSender().tell(new EqualityLabelsDifferent(rows),getSelf());
            } else {
                log.info("\tno equal labels found");
                getSender().tell(new EqualityLabelsDifferent(new ArrayList<List<String>>()),getSelf());
            }
        } else {
            unhandled(message);
        }
    }
    
}
