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
import com.google.common.base.Objects;
import cz.incad.prokop.server.analytics.ShodaUdaju;
import cz.incad.prokop.server.analytics.akka.equality.messages.EqualityResponse;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class EqualityWorker extends UntypedActor {

    private static final String query = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev, id.hodnota  from identifikator id left outer join zaznam zaz on id.zaznam = zaz.Zaznam_ID where id.typ = 'cCNB' order by id.hodnota, zaz.hlavniNazev";

    
    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof StartAnalyze) {
        
            List<String> lines = new JDBCQueryTemplate<String>(PersisterUtils.getConnection(), true) {

                Radek prvni = null;
                Radek druhy = null;

                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    druhy = new Radek();
                    druhy.id = rs.getString("hodnota");
                    druhy.nazev = rs.getString("hlavniNazev");
                    druhy.text.append(rs.getInt("Zaznam_ID")).append("\t").append(rs.getString("url")).append("\t").append(rs.getString("hodnota")).append("\t").append(rs.getString("hlavniNazev")).append("\n");

                    if (prvni != null ){
                        if (Objects.equal(prvni.id, druhy.id) && !Objects.equal(prvni.nazev,druhy.nazev)){
                            if (!prvni.zapsan){
                                returnsList.add(prvni.text.toString());
                            }
                            returnsList.add(druhy.text.toString());
                            druhy.zapsan = true;
                        }
                    }
                    prvni = druhy;

                    return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
                }
            }.executeQuery(query);
            
            getSender().tell(new EqualityResponse(lines), getSelf());
            
        } else {
            unhandled(message);
        }
    }
    
    
    
    private static class Radek{
        public String id;
        public String nazev;
        public StringBuilder text = new StringBuilder();
        public boolean zapsan = false;
    }
}
