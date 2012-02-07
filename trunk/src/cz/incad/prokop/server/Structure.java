package cz.incad.prokop.server;

import org.aplikator.server.descriptor.Application;

import cz.incad.prokop.server.data.Autor;
import cz.incad.prokop.server.data.DigitalniVerze;
import cz.incad.prokop.server.data.Edice;
import cz.incad.prokop.server.data.Exemplar;
import cz.incad.prokop.server.data.Rozsah;
import cz.incad.prokop.server.data.Identifikator;
import cz.incad.prokop.server.data.Jazyk;
import cz.incad.prokop.server.data.Vydani;
import cz.incad.prokop.server.data.Periodicita;
import cz.incad.prokop.server.data.Nazev;
import cz.incad.prokop.server.data.Zaznam;

public class Structure extends Application {

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

    static{

    }

}
