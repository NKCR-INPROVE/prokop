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

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.analytics.akka.messages.StoppedWork;
import cz.incad.prokop.server.analytics.akka.missing.messages.NoCNBResult;
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
public class NoCNBWorker extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    
    /*
    private static final String noCNBquery = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev  "
            + "from  zaznam zaz left outer join "
            + "sklizen on (sklizen.sklizen_id=zaz.sklizen) "
            + "left outer join zdroj on (sklizen.zdroj = zdroj.zdroj_id) "
            + "where not exists ( select id.hodnota from identifikator id where id.zaznam = zaz.Zaznam_ID and id.typ = 'cCNB') "
            + "and zdroj.nazev in( '%s' ) order by  zaz.hlavniNazev";
      */

    private static final String noCNBquery = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev  "+
                                             " from  zaznam zaz "+
                                            " where not exists ( select id.hodnota from identifikator id where id.zaznam = zaz.Zaznam_ID and id.typ = 'cCNB') "+
                                            " and zaz.sklizen in(%s) order by  zaz.hlavniNazev";
    
    public NoCNBWorker() {}
    
    
    @Override
    public void onReceive(Object mess) throws Exception {
        if (mess instanceof StartAnalyze) {
            StartAnalyze sta = (StartAnalyze) mess;
            log.info("NoCNB STARTING {}",mess);
            
            String value = (String) sta.getParams().getValue("Property:Wizard:SpustitAnalyzu_default-wizard.zdroj");
            List<Integer> sklizne = PersisterUtils.sklizneFromSource(getConnection(), Integer.valueOf(value), true);
            String sql = String.format(noCNBquery, PersisterUtils.separatedList(sklizne));
           
            log.info("Executing query {}",sql);
            List<String> ret = new JDBCQueryTemplate<String>(getConnection(),true) {
                int counter = 0;
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    StringBuilder row = new StringBuilder();
                    row.append(Integer.toString(rs.getInt("Zaznam_ID"))).append("\t").append(rs.getString("url")).append("\t").append(rs.getString("hlavniNazev"));
                    returnsList.add(row.toString());
                    return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
                }
            }.executeQuery(sql);
            log.info("NoCNB Sending result");
            getSender().tell( new NoCNBResult(ret) ,getSelf());
            getSender().tell(new StoppedWork(), getSelf());
        } else {
            unhandled(mess);
        }
    }

    @Override
    public void postStop() {
        super.postStop(); //To change body of generated methods, choose Tools | Templates.
        log.info("stopping {}",this.getSelf().path());
}

    
    
    public Connection getConnection() {
        if (Boolean.getBoolean("missing.debug")) {
            try {
                return TestConnectionUtils.getConnection();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(NoCNBWorker.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (SQLException ex) {
                Logger.getLogger(NoCNBWorker.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return PersisterUtils.getConnection();
        }
    }
}
