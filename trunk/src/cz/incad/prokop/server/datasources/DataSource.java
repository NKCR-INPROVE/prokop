package cz.incad.prokop.server.datasources;

import org.aplikator.client.data.Record;
import org.aplikator.server.Context;

public interface DataSource {

    public int harvest(String params, Record sklizen, Context ctx );

}
