package cz.incad.prokop.server;

import org.aplikator.server.descriptor.Application;

import cz.incad.prokop.server.data.*;
import cz.incad.prokop.server.data.triggers.SklizenTrigger;
import cz.incad.prokop.server.data.triggers.ZaznamTrigger;

public class Structure extends Application {

    public static final Zdroj zdroj = new Zdroj();
        
    public static final Sklizen sklizen = new Sklizen();


    public static final Modul modul = new Modul();
    public static final Analyza analyza = new Analyza();


    public static final Autor autor = new Autor();
    public static final Edice edice = new Edice();
    public static final Exemplar exemplar = new Exemplar();
    public static final Rozsah rozsah = new Rozsah();
    public static final Identifikator identifikator = new Identifikator();
    public static final Jazyk jazyk = new Jazyk();
    public static final Vydani vydani = new Vydani();
    public static final Periodicita periodicita = new Periodicita();
    public static final Nazev nazev = new Nazev();
    public static final DigitalniVerze digitalniVerze = new DigitalniVerze();

    public static final Zaznam zaznam = new Zaznam();



    static {
        zdroj.sklizen = zdroj.reverseCollectionProperty("sklizen", sklizen, sklizen.zdroj);
        modul.analyza = modul.reverseCollectionProperty("analyza", analyza, analyza.modul);
        zaznam.setPersistersTriggers(new ZaznamTrigger());
    }
}
