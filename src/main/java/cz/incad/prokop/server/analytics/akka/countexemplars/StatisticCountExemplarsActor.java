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
import cz.incad.prokop.server.analytics.akka.countexemplars.messages.OnlyOneExemplarResult;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import cz.incad.prokop.server.utils.TestConnectionUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class StatisticCountExemplarsActor extends UntypedActor{

    private static final String query2 = 
            "select count(c) as pocet, c as exemplaru from  " +
            "    (select count(id.zaznam) as c, id.hodnota  " +
            "    from identifikator id " +
            "    where id.typ = 'cCNB' and (id.hodnota is not null or id.hodnota <> '') " +
            "    group by hodnota) subquery " +
            "group by c order by c";

    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartAnalyze) {
            System.out.println("Statistic count example");
            List<String> list = new JDBCQueryTemplate<String>(PersisterUtils.getConnection(), true) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    StringBuilder builder = new StringBuilder();
                    builder.append(Integer.toString(rs.getInt("pocet")))
                        .append("\t").append(Integer.toString(rs.getInt("exemplaru")));
                    returnsList.add(builder.toString());
                    return super.handleRow(rs, returnsList); 
                }
            }.executeQuery(query2);
            getSender().tell(new CountExemplarsResults(list), getSelf());
        } else {
            unhandled(message);
        }
    }
    
}
