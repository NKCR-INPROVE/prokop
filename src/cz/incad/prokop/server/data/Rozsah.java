package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class Rozsah extends Entity {
    public Property<String> strankovani;
    public Property<String> vybaveni;
    public Property<String> rozmer;

    public Rozsah() {
        super("Rozsah","Rozsah","Rozsah_ID");
        initFields();
    }

    protected void initFields() {
        strankovani = stringProperty("strankovani");
        vybaveni = stringProperty("vybaveni");
        rozmer = stringProperty("rozmer");
    }

}
