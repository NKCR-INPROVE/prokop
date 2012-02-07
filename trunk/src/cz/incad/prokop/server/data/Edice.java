package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Edice extends Entity {
    public Property<String> nazev;
    public Property<String> cisloCasti;
    public Property<String> nazevCasti;
    public Property<String> svazek;
    public Property<String> issn;

    public Edice() {
        super("Edice","Edice","Edice_ID");
        initFields();
    }

    protected void initFields() {
        nazev = stringProperty("nazev");
        cisloCasti = stringProperty("cisloCasti");
        nazevCasti = stringProperty("nazevCasti");
        svazek = stringProperty("svazek");
        issn = stringProperty("issn");
    }

}
