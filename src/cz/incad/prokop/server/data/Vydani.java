package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Vydani extends Entity {
    public Property<String> oznaceni;
    public Property<String> nakladatel;
    public Property<String> misto;
    public Property<String> datum;

    public Vydani() {
        super("Vydani","Vydani","Vydani_ID");
        initFields();
    }

    protected void initFields() {
        oznaceni = stringProperty("oznaceni");
        nakladatel = stringProperty("nakladatel");
        misto = stringProperty("misto");
        datum = stringProperty("datum");
    }

}
