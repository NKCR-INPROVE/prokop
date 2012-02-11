package cz.incad.prokop.server.data;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.View;

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

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typModulu).addProperty(nazev);
        retval.form(column(
                row(typModulu,nazev, formatXML),
                row(trida, parametry),
                RepeatedForm.repeated(analyza)
            ));
        return retval;
    }

    private View reverseView;
    View getReverseView(){
        if(reverseView == null){
            reverseView = new View(this, "reverseView");
            reverseView.addProperty(typModulu).addProperty(nazev);
            reverseView.form(column(
                    row(typModulu,nazev, formatXML)
                ));

        }
        return reverseView;
    }

}
