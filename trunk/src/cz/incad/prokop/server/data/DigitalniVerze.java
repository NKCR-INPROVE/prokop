package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

public class DigitalniVerze extends Entity {
    public Property<String> url;

    public DigitalniVerze() {
        super("DigitalniVerze","DigitalniVerze","DigitalniVerze_ID");
        initFields();
    }

    protected void initFields() {
        url = stringProperty("url");
    }

}
