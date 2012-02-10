package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Modul extends Entity {

    public Property<String> typModulu;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;
    public Collection<Analyza> analyza;

    public Modul() {
        super("Modul","Modul","Modul_ID");
        initFields();
    }

    protected void initFields() {
        typModulu = stringProperty("typModulu");
        nazev = stringProperty("nazev");
        formatXML = stringProperty("formatXML");
        trida = stringProperty("trida");
        parametry = stringProperty("parametry");
    }

}
