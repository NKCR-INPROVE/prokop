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

import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.countexemplars.messages.CountExemplarsResults;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.data.Modul;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import cz.incad.prokop.server.utils.TestConnectionUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Record;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class OnlyOneExemplarActor extends UntypedActor{

    public static final String KEY="Aleph NKP";
    
    private static final String query = 
            "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev, zaznam " +
            "from exemplar ex, zaznam zaz  " +
            "where ex.zaznam=zaz.zaznam_id and zaznam.sklizen in (%s) " +
            "group by ex.zaznam, zaz.zaznam_id, zaz.url, zaz.HLAVNINAZEV " +
            "having count(zaznam)=1";

    
    
    
    

    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartAnalyze) {
            System.out.println("Only one example started");

            StartAnalyze stv = (StartAnalyze) message;
            
            List<Map<Integer, String>> retList = new JDBCQueryTemplate<Map<Integer, String>>(TestConnectionUtils.getConnection(),true) {

                public boolean handleRow(ResultSet rs, List<Map<Integer, String>> returnsList) throws SQLException {
                    Map<Integer, String> m = new HashMap<Integer, String>();
                    m.put(rs.getInt("zdroj_id"), rs.getString("Nazev"));
                    returnsList.add(m);
                    return super.handleRow(rs, returnsList); 
                }
            }.executeQuery("select Nazev, Zdroj_id from DEV_PROKOP.ZDROJ");
 

            for (Map<Integer, String> map : retList) {
                Set<Integer> keys = map.keySet();
                String name = map.get(keys.iterator().next());
                if (name.equals(KEY)) {
                    List<Integer> sklizneFromSource = PersisterUtils.sklizneFromSource(TestConnectionUtils.getConnection(), Integer.valueOf(keys.iterator().next()));
                    String sql = String.format(query, PersisterUtils.separatedList(sklizneFromSource));
                    List<String> list = new JDBCQueryTemplate<String>(TestConnectionUtils.getConnection(), true) {

                        @Override
                        public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                            StringBuilder builder= new StringBuilder();
                            builder.append(Integer.toString(rs.getInt("Zaznam_ID"))).append("\t");
                            builder.append(rs.getString("url")).append("\t");
                            builder.append(rs.getString("hlavniNazev"));
                            returnsList.add(builder.toString());
                            return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
                        }
                    }.executeQuery(sql);
                    
                    getSender().tell(new CountExemplarsResults(list), getSelf());
                }
                
            }
            
        } else {
            unhandled(message);
        }
    }
    
}
