package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Identifikator extends Entity {
    public Property<String> typ;
    public Property<String> hodnota;

    public Identifikator() {
        super("Identifikator","Identifikator","Identifikator_ID");
        initFields();
    }

    protected void initFields() {
        typ = stringProperty("typ");
        hodnota = stringProperty("hodnota");
    }

}
