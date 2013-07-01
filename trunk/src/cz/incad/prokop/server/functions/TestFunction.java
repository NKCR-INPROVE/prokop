package cz.incad.prokop.server.functions;

import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.WizardPage;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import java.util.logging.Logger;

public class TestFunction extends Executable {

    Logger log = Logger.getLogger(TestFunction.class.getName());

    @Override
    public FunctionResult execute(FunctionParameters functionParameters, Context context) {
        //Record zdroj = functionParameters.getClientContext().getCurrentRecord();
        try {
            return new FunctionResult("Test doběhl", true);
        } catch (Throwable t) {

            return new FunctionResult("Test selhal: " + t, false);
        }
    }

    @Override
    public WizardPage getWizardPage(String currentPage, boolean forwardFlag, Record currentProcessingRecord, Record clientParameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }





}
