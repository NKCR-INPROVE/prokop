package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Periodicita extends Entity {
    public Property<String> typ;
    public Property<String> platnost;

    public Periodicita() {
        super("Periodicita","Periodicita","Periodicita_ID");
        initFields();
    }

    protected void initFields() {
        typ = stringProperty("typ");
        platnost = stringProperty("platnost");
    }

}
