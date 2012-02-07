package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Autor extends Entity {
    public Property<String> prijmeni;
    public Property<String> jmeno;
    public Property<String> nazev;
    public Property<String> datumNarozeni;
    public Property<String> datumUmrti;
    public Property<String> odpovednost;

    public Autor() {
        super("Autor","Autor","Autor_ID");
        initFields();
    }

    protected void initFields() {
        prijmeni = stringProperty("prijmeni");
        jmeno = stringProperty("jmeno");
        nazev = stringProperty("nazev");
        datumNarozeni = stringProperty("datumNarozeni");
        datumUmrti = stringProperty("datumUmrti");
        odpovednost = stringProperty("odpovednost");

    }

}
