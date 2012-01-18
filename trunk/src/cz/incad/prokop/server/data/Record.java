package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Record extends Entity {
    public Property<String> idCislo;

    public Record() {
        super("Record");
    }

    protected void initFields() {
        idCislo = stringProperty("idCislo").setEditable(false);
    }

}
