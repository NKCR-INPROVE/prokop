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
package cz.incad.prokop.server.utils;

import cz.incad.prokop.server.data.Modul;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.persistence.PersisterFactory;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class PersisterUtils {

    public static Connection getConnection() {
        Connection conn = PersisterFactory.getPersister().getJDBCConnection();
        return conn;
    }

    public static List<Integer> sklizneFromSource(Connection conn, Integer zdroj, boolean closeConnection) {
        List<Integer> sklizne = new JDBCQueryTemplate<Integer>(conn, closeConnection) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int aInt = rs.getInt("sklizen_id");
                returnsList.add(aInt);
                return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
            }
        }.executeQuery("select sklizen_id from sklizen  where zdroj=?", zdroj);
        return sklizne;
    }    
    public static List<Integer> sklizneFromSource(Connection conn, Integer zdroj) {
        return sklizneFromSource(conn, zdroj, false);
    }
    
    

    
    
    public static String separatedList(List<Integer> retList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < retList.size(); i++) {
            if (i > 0) builder.append(",");
            builder.append(retList.get(i));
        }
        String separted = builder.toString();
        return separted;
    }

    
}
