package cz.incad.prokop.server.data;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;

import cz.incad.prokop.server.Structure;

public class Zaznam extends Entity {
    public Property<String> typDokumentu;
    public Collection<Identifikator> identifikator;
    public Collection<Autor> autor;
    public Collection<Jazyk> jazyk;
    public Property<String> hlavniNazev;
    public Collection<Nazev> nazev;
    public Collection<Vydani> vydani;
    public Collection<Rozsah> rozsah;
    public Collection<Periodicita> periodicita;
    public Collection<Edice> edice;
    public Collection<Exemplar> exemplar;
    public Collection<DigitalniVerze> digitalniVerze;
    public Property<String> url;
    public Property<String> zdroj;
    public Property<String> typHarvestu;
    public Property<String> formatXML;
    public Property<String> sourceXML;

    public Zaznam() {
        super("Zaznam","Zaznam","Zaznam_ID");
        initFields();
    }

    protected void initFields() {
        typDokumentu = stringProperty("typDokumentu");
        identifikator = collectionProperty(Structure.identifikator, "identifikator", "zaznam");
        autor = collectionProperty(Structure.autor, "autor", "zaznam");
        jazyk = collectionProperty(Structure.jazyk, "jazyk", "zaznam");
        hlavniNazev = stringProperty("hlavniNazev");
        nazev = collectionProperty(Structure.nazev, "nazev", "zaznam");
        vydani = collectionProperty(Structure.vydani, "vydani", "zaznam");
        rozsah = collectionProperty(Structure.rozsah, "extent", "zaznam");
        periodicita = collectionProperty(Structure.periodicita, "periodicita", "zaznam");
        edice = collectionProperty(Structure.edice, "edice", "zaznam");
        exemplar = collectionProperty(Structure.exemplar, "exemplar", "zaznam");
        digitalniVerze = collectionProperty(Structure.digitalniVerze, "digitalniVerze", "zaznam");
        url = stringProperty("url");
        zdroj = stringProperty("zdroj");
        typHarvestu = stringProperty("typHarvestu");
        formatXML = stringProperty("formatXML");
        sourceXML = textProperty("sourceXML");

    }



}
