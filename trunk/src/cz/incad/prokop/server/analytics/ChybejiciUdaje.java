package cz.incad.prokop.server.analytics;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.Files;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.data.BinaryData;
import org.aplikator.server.persistence.PersisterFactory;

import cz.incad.prokop.server.Structure;
import static cz.incad.prokop.server.analytics.ExistenceOdkazu.getConnection;
import static cz.incad.prokop.server.analytics.ExistenceOdkazu.getTmpFile;
import cz.incad.prokop.server.analytics.akka.links.URLValidationMaster;
import cz.incad.prokop.server.analytics.akka.messages.StartAnalyze;
import cz.incad.prokop.server.analytics.akka.missing.MissingMaster;
import cz.incad.prokop.server.data.Analyza;
import java.util.Date;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.RecordContainer;

public class ChybejiciUdaje implements Analytic {

    Logger log = Logger.getLogger(ChybejiciUdaje.class.getName());


    private static final String noCNBquery = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev  from  zaznam zaz left outer join sklizen on (sklizen.sklizen_id=zaz.sklizen) left outer join zdroj on (sklizen.zdroj = zdroj.zdroj_id) where not exists ( select id.hodnota from identifikator id where id.zaznam = zaz.Zaznam_ID and id.typ = 'cCNB') and zdroj.nazev in( %s ) order by  zaz.hlavniNazev";
    private static final String emptyCNBquery = "select zaz.Zaznam_ID,zaz.url, zaz.hlavniNazev, id.hodnota  from identifikator id left outer join zaznam zaz on id.zaznam = zaz.Zaznam_ID left outer join sklizen on (sklizen.sklizen_id=zaz.sklizen) left outer join zdroj on (sklizen.zdroj = zdroj.zdroj_id) where id.typ = 'cCNB' and (id.hodnota is null or id.hodnota = '') and zdroj.nazev in( %s ) order by  zaz.hlavniNazev";
    private static final String countCNBquery = "select count(*)  from  zaznam zaz left outer join sklizen on (sklizen.sklizen_id=zaz.sklizen) left outer join zdroj on (sklizen.zdroj = zdroj.zdroj_id) where exists ( select id.hodnota from identifikator id where id.zaznam = zaz.Zaznam_ID and id.typ = 'cCNB' and (id.hodnota is not null or not (id.hodnota = ''))) and zdroj.nazev in( %s ) order by  zaz.hlavniNazev";

   // private static final String PARAM = "'Aleph NKP', 'Aleph MZK', 'Aleph NKP CNB', 'Aleph NKP base CNB'";
    /*
     *  CHYBĚJÍCÍ ÚDAJE
a)      Statistika kolik má čČNB
b)      Vypsat záznamy z NKCR a MZK, které nemají čČNB(non-Javadoc)
     * @see cz.incad.prokop.server.analytics.Analytic#analyze(java.lang.String, org.aplikator.client.data.Record, org.aplikator.server.Context)
     */
    @Override
    public void analyze(final String params, final Record analyza, final Context context) {
        try {
                final File file = getTmpFile();
                final Connection conn = getConnection();

                ActorSystem system = ActorSystem.create("missing");
                system.registerOnTermination(new Runnable() {

                    @Override
                    public void run() {
                        BinaryData bd;
                        try {
                            RecordContainer rc = new RecordContainer();
                            Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
                            Structure.analyza.ukonceni.setValue(analyza, new Date());
                            
                            /*
                            bd = new BinaryData("ExistenceOdkazu.txt", new FileInputStream(file), file.length());
                            Structure.analyza.vysledek.setValue(analyza, bd);
                            */
                            
                            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
                            rc = context.getAplikatorService().processRecords(rc);
 
                            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.INFO, "Analyza skoncena");
                        } catch (Exception ex) {
                            Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                Props props = new Props(new UntypedActorFactory() {
                    public UntypedActor create() throws Exception{
                        return new MissingMaster(file);
                    }
                });

                // create the master
                ActorRef master = system.actorOf(props, "master");

                // start the calculation
                master.tell(new StartAnalyze(params),null);
                
            } catch(IOException ex) {
                Logger.getLogger(ExistenceOdkazu.class.getName()).log(Level.SEVERE,ex.getMessage(), ex);
            }

        
        
        /*
        File tempFile = null;
        log.info("params  = "+params);
        log.info("analyza = "+analyza);
        log.info("Context = "+context);
        
        Connection conn = PersisterFactory.getPersister().getJDBCConnection();
        Statement st = null;
        ResultSet rs = null;
        try{
            File tempDir = Files.createTempDir();
            tempFile = new File(tempDir, UUID.randomUUID().toString());
            tempFile.createNewFile();
            log.info("ChybejiciUdaje TEMPFILE:" + tempFile);
            Writer vysledek = new FileWriter(tempFile);
            vysledek.append("Analyzované zdroje: ").append(params).append("\n\n");
            log.info("Analyzovane zdroje:"+params);
            String sql = String.format(countCNBquery, params);
            st = conn.createStatement();
            log.info("Spusten dotaz na pocet zaznamu s cCNB");
            rs = st.executeQuery(sql);
            while (rs.next()){
                vysledek.append("Počet záznamů s čČNB: ").append(Integer.toString(rs.getInt(1))).append("\n").append("\n").append("Záznamy bez čČNB:").append("\n");
            }
            rs.close();
            st.close();

            sql = String.format(noCNBquery, params);
            st = conn.createStatement();
            log.info("Spusten dotaz na seznam zaznamu bez cCNB");
            rs = st.executeQuery(sql);
            int counter = 0;
            while (rs.next()){
                vysledek.append(Integer.toString(rs.getInt("Zaznam_ID"))).append("\t").append(rs.getString("url")).append("\t").append(rs.getString("hlavniNazev")).append("\n");
                counter++;
            }
            rs.close();
            st.close();
            vysledek.append ("\nPočet záznamů bez čČNB: ").append(Integer.toString(counter)).append("\n");
            log.info("Pocet zaznamu bez cCNB: "+counter);

            counter = 0;
            vysledek.append("\n\nZáznamy s prázdným čČNB: ").append("\n");
            sql = String.format(emptyCNBquery, params);
            st = conn.createStatement();
            log.info("Spusten dotaz na seznam zaznamu s prazdnym cCNB");
            rs = st.executeQuery(sql);
            while (rs.next()){
                vysledek.append(Integer.toString(rs.getInt("Zaznam_ID"))).append("\t").append(rs.getString("url")).append("\t").append(rs.getString("hlavniNazev")).append("\n");
                counter++;
                vysledek.flush();
            }
            vysledek.append ("\nPočet záznamů s prázdným čČNB: ").append(Integer.toString(counter)).append("\n");
            log.info("Pocet zaznamu s prazdnym cCNB: "+counter);
            log.info("Analyza ukoncena");
            vysledek.close();

            if (tempFile != null){
                System.out.println("TEMP FILE :"+tempFile.getAbsolutePath());
                BinaryData bd  = new BinaryData("ChybejiciUdaje.txt", new FileInputStream(tempFile), tempFile.length());
                Structure.analyza.vysledek.setValue(analyza, bd);
            }
        } catch (Exception ex){
            log.log(Level.SEVERE, "Chyba v analyze", ex);
        } finally{
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {}
            }
            if (st != null){
                try {
                    st.close();
                } catch (SQLException e) {}
            }
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {}
            }
        }
        */
    }





}
