package cz.incad.prokop.server.functions;

import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

public class NacistData implements Executable {

    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
        return new FunctionResult("Načetl co mohl...", true);
    }

}
