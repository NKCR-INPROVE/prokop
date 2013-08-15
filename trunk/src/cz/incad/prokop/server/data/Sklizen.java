package cz.incad.prokop.server.data;

import cz.incad.prokop.server.Structure;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.descriptor.*;

import java.util.Date;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

public class Sklizen extends Entity {

    public static enum Stav implements ListItem  {
        ZAHAJEN("zahajen"), UKONCEN("ukoncen"), CHYBA("chyba");

        private Stav(String value){
            this.value = value;
        }
        private String value;
        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getName() {
            return value;
        }

    }

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
        stav = stringProperty("stav").setListProvider(new ListProvider.Default(Stav.values()));
        pocet = integerProperty("pocet");
        uzivatel = stringProperty("uzivatel");

        zdroj = referenceProperty(Structure.zdroj, "zdroj");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(stav).addProperty(spusteni).addProperty(ukonceni).addProperty(pocet).addProperty(uzivatel);
        retval.form(column(
                row(stav,spusteni, ukonceni),
                row(pocet, uzivatel)
            ));
        return retval;
    }



}
