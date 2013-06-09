/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.functions;

import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.Analytic;
import cz.incad.prokop.server.data.Analyza;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import static org.aplikator.server.data.RecordUtils.getValue;
import static org.aplikator.server.data.RecordUtils.newSubrecord;
import static org.aplikator.server.data.RecordUtils.setValue;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.WizardPage;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

/**
 *
 * @author pavels
 */
public class ZastavitAnalyzu implements Executable {

    static Logger log = Logger.getLogger(ZastavitAnalyzu.class.getName());
     
    @Override
    public FunctionResult execute(FunctionParameters functionParameters, Context context) {
        Record modul = functionParameters.getClientContext().getCurrentRecord();
        Record parameters = functionParameters.getClientParameters();
        //Record analyza = null;
        try {
            String analyticName = getValue(modul, Structure.modul.trida);
            Analytic an = null;
            try {
                an = (Analytic) Class.forName(analyticName).newInstance();
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Cannot instantiate analytic", e);
                throw e;    
            }
            
            if (an.isRunning()) {
                an.stopAnalyze(modul, null, context);

                RecordContainer rc = new RecordContainer();
                Structure.modul.stav.setValue(modul, "Pozadavek na ukoceni");
                rc.addRecord(null, modul, modul, Operation.UPDATE);
                rc = context.getAplikatorService().processRecords(rc);

                return new FunctionResult("Analýza pro modul " + modul.getValue(Structure.modul.nazev.getId())+" bude zastavena. Vyckejte.", true);
            } else {

                RecordContainer rc = new RecordContainer();
                Structure.modul.stav.setValue(modul, Analyza.Stav.UKONCENA.getValue());
                rc.addRecord(null, modul, modul, Operation.UPDATE);
                rc = context.getAplikatorService().processRecords(rc);

                return new FunctionResult("Nelze zastavit. Nebezi ! ", false);
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Error analyzing: ", t);
//            RecordContainer rc = new RecordContainer();
//            Structure.analyza.stav.setValue(analyza, Analyza.Stav.CHYBA.getValue());
//            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
//            rc = context.getAplikatorService().processRecords(rc);

            return new FunctionResult("Analýza pro modul " + modul.getValue(Structure.modul.nazev.getId()) + "selhala: " + t, false);
        }
    }

    @Override
    public WizardPage getWizardPage(String currentPage, boolean forwardFlag, Record currentProcessingRecord, Record clientParameters) {
        return null;
    }

    @Override
    public void setFunction(Function func) {
    }

    @Override
    public Function getFunction() {
        return null;
    }


    
}
