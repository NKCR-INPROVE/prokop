package cz.incad.prokop.server.functions;

import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.Analytic;
import cz.incad.prokop.server.data.Analyza;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Operation;

import static org.aplikator.server.data.RecordUtils.getValue;
import static org.aplikator.server.data.RecordUtils.setValue;
import static org.aplikator.server.data.RecordUtils.newSubrecord;

public class SpustitAnalyzu implements Executable {

    Logger log = Logger.getLogger(SpustitAnalyzu.class.getName());

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
                RecordContainer rc = new RecordContainer();
                analyza = newSubrecord(modul.getPrimaryKey(), Structure.modul.analyza);
                Structure.analyza.spusteni.setValue(analyza, new Date());
                Structure.analyza.stav.setValue(analyza, Analyza.Stav.ZAHAJENA.getValue());
                rc.addRecord(null, analyza, analyza, Operation.CREATE);

                setValue(modul, Structure.modul.parametry, "RUNNING");
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



}
