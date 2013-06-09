package cz.incad.prokop.server.functions;

import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.WizardPage;

public class TestFunction implements Executable {

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

    @Override
    public void setFunction(Function func) {
    }

    @Override
    public Function getFunction() {
        return null;
    }



}
