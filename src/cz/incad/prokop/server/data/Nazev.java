package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Nazev extends Entity {

    public Property<String> typNazvu;
    public Property<String> nazev;

    public Nazev() {
        super("Nazev","Nazev","Nazev_ID");
        initFields();
    }

    protected void initFields() {
        typNazvu = stringProperty("typNazvu");
        nazev = stringProperty("nazev");
    }

}
