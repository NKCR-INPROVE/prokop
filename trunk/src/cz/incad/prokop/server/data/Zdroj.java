package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Zdroj extends Entity {

    public Property<String> typZdroje;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;
    public Property<String> cron;
    public Collection<Sklizen> sklizen;

    public Zdroj() {
        super("Zdroj","Zdroj","Zdroj_ID");
        initFields();
    }

    protected void initFields() {
        typZdroje = stringProperty("typZdroje");
        nazev = stringProperty("nazev");
    }

}
