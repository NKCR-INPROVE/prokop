package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Jazyk extends Entity {
    public Property<String> kod;

    public Jazyk() {
        super("Jazyk","Jazyk","Jazyk_ID");
        initFields();
    }

    protected void initFields() {
        kod = stringProperty("kod");
    }

}
