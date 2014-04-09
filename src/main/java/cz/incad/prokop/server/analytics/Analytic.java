package cz.incad.prokop.server.analytics;

import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;

public interface Analytic {

    /** spusti analyzu */
    public void analyze(org.aplikator.client.shared.data.Record params, Record modul, Record analyza, Context ctx);
    
    /** zastavi analyzu */
    public void stopAnalyze(Record modul, Record analyza, Context ctx);
    
    /** vraci true, pokud bezi */
    public boolean isRunning();
    
    public String getDescription();

    /** vraci klice pro wizardy, ktere se maji pouzit */
    public String getWizardPageKey();
}
