package cz.incad.prokop.server.functions;

import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.Analytic;
import cz.incad.prokop.server.data.Analyza;
import cz.incad.prokop.server.data.Modul;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Operation;

import static org.aplikator.server.data.RecordUtils.getValue;
import static org.aplikator.server.data.RecordUtils.setValue;
import static org.aplikator.server.data.RecordUtils.newSubrecord;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.WizardPage;
import org.aplikator.server.processes.ProcessManager;

public class SpustitAnalyzu implements Executable {

    public static Logger log = Logger.getLogger(SpustitAnalyzu.class.getName());

    public Function function;
    
    public static String readName(Integer id) {
        Connection conn = PersisterUtils.getConnection();
        List<String> names = new JDBCQueryTemplate<String>(conn, true) {
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                String name = rs.getString("Nazev");
                returnsList.add(name);
                return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
            }
        }.executeQuery("select Nazev from ZDROJ where Zdroj_id=?",id);
        return names.isEmpty() ?  "" : names.get(0);
    }
    

    @Override
    public FunctionResult execute(FunctionParameters functionParameters, Context context) {
        Record modul = functionParameters.getClientContext().getCurrentRecord();
        Record parameters = functionParameters.getClientParameters();
        Record analyza = null;
        try {
            String analyticName = getValue(modul, Structure.modul.trida);
            Analytic an = null;
            try {
                an = (Analytic) Class.forName(analyticName).newInstance();
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Cannot instantiate analytic", e);
                throw e;    
            }

            if (!an.isRunning()) {
                
                //processManager.prepareProcess(new ProcessThread());
                
                RecordContainer rc = new RecordContainer();
                analyza = newSubrecord(modul.getPrimaryKey(), Structure.modul.analyza);
                Structure.analyza.spusteni.setValue(analyza, new Date());
                Structure.analyza.stav.setValue(analyza, Analyza.Stav.ZAHAJENA.getValue());
                if (functionParameters != null && functionParameters.getClientParameters() != null) {
                    String value = (String) functionParameters.getClientParameters().getValue("Property:Wizard:SpustitAnalyzu_default-wizard.zdroj");
                    if (value != null) {
                        Integer id = Integer.valueOf(value);
                        Structure.analyza.parametry.setValue(analyza, readName(id));
                    }
                } 
                Principal up = context.getHttpServletRequest().getUserPrincipal();
                if (up != null) {
                    Structure.analyza.uzivatel.setValue(analyza, up.getName());
                }
                
                rc.addRecord(null, analyza, analyza, Operation.CREATE);
                rc.addRecord(null, modul, modul, Operation.UPDATE);
                
                rc = context.getAplikatorService().processRecords(rc);
                an.analyze(parameters, modul, rc.getRecords().get(0).getEdited(), context);
                
                return new FunctionResult("Analýza pro modul " + modul.getValue(Structure.modul.nazev.getId())+" bezi na pozadi. Pri dobehnuti bude zaznam upraven.", true);
            } else {
                return new FunctionResult("Nelze spustit analyzu pro modul   " + modul.getValue(Structure.modul.nazev.getId())+". Jiz  bezi.", false);
            }
        } catch (Throwable t) {
            
            log.log(Level.SEVERE, "Error analyzing: ", t);
            RecordContainer rc = new RecordContainer();
            Structure.analyza.stav.setValue(analyza, Analyza.Stav.CHYBA.getValue());
            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
            rc = context.getAplikatorService().processRecords(rc);

            return new FunctionResult("Analýza pro modul " + modul.getValue(Structure.modul.nazev.getId()) + "selhala: " + t, false);
        }
    }

    @Override
    public WizardPage getWizardPage(String currentPage, boolean forwardFlag, Record currentProcessingRecord, Record clientParameters) {
        String value = (String) currentProcessingRecord .getValue("Property:Modul.trida");
        if (value != null) {
            try {
                Class<?> clz = Class.forName(value);
                Analytic analytic = (Analytic) clz.newInstance();
                String key =  analytic.getWizardPageKey();
                return this.function.getRegistredView(key);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (InstantiationException ex) {
                Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else return null;
    }

    @Override
    public void setFunction(Function func) {
        this.function = func;
    }

    @Override
    public Function getFunction() {
        return this.function;
    }
    
}
