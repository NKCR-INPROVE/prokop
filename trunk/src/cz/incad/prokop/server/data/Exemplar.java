package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Exemplar extends Entity {
    public Property<String> signatura;
    public Property<String> carovyKod;
    public Property<String> popis;
    public Property<String> svazek;
    public Property<String> rocnik;
    public Property<String> cislo;
    public Property<String> rok;

    public Exemplar() {
        super("Exemplar","Exemplar","Exemplar_ID");
        initFields();
    }

    protected void initFields() {
        signatura = stringProperty("signatura");
        carovyKod = stringProperty("carovyKod");
        popis = stringProperty("popis");
        svazek = stringProperty("svazek");
        rocnik = stringProperty("rocnik");
        cislo = stringProperty("cislo");
        rok = stringProperty("rok");
    }

}
