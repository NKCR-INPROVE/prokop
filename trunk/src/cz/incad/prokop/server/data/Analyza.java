package cz.incad.prokop.server.data;

import java.util.Date;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;

import cz.incad.prokop.server.Structure;

public class Analyza extends Entity {

    public Property<Date> spusteni;
    public Property<Date> ukonceni;
    public Property<String> stav;
    public Property<String> vysledek;
    public Property<String> uzivatel;
    public Reference<Modul> modul;

    public Analyza() {
        super("Analyza","Analyza","Analyza_ID");
        initFields();
    }

    protected void initFields() {
        spusteni = dateProperty("spusteni");
        ukonceni = dateProperty("ukonceni");
        stav = stringProperty("stav");
        vysledek = textProperty("vysledek");
        uzivatel = stringProperty("uzivatel");
        modul = referenceProperty(Structure.modul, "modul");
    }

}
