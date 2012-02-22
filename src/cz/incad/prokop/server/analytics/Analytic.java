package cz.incad.prokop.server.analytics;

import org.aplikator.client.data.Record;
import org.aplikator.server.Context;

public interface Analytic {

    public void analyze(String params, Record analyza, Context ctx );

}
