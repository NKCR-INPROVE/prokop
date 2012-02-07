package cz.incad.prokop.server.data;

import java.util.Date;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;

import cz.incad.prokop.server.Structure;

public class Sklizen extends Entity {

    public Property<Date> spusteni;
    public Property<Date> ukonceni;
    public Property<String> stav;
    public Property<Integer> pocet;
    public Property<String> uzivatel;
    public Reference<Zdroj> zdroj;

    public Sklizen() {
        super("Sklizen","Sklizen","Sklizen_ID");
        initFields();
    }

    protected void initFields() {
        spusteni = dateProperty("spusteni");
        ukonceni = dateProperty("ukonceni");
        stav = stringProperty("stav");
        pocet = integerProperty("pocet");
        uzivatel = stringProperty("uzivatel");
        zdroj = referenceProperty(Structure.zdroj, "zdroj");
    }

}
