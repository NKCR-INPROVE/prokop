package cz.incad.prokop.server.data;

import cz.incad.prokop.server.Structure;
import cz.incad.prokop.server.analytics.Analytic;
import cz.incad.prokop.server.functions.SpustitAnalyzu;
import cz.incad.prokop.server.functions.ZastavitAnalyzu;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import java.io.Serializable;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.*;
import org.aplikator.server.descriptor.Wizard;
import org.aplikator.server.persistence.PersisterTriggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

public class Modul extends Entity {
    
    
    
    public Property<String> typModulu;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;
    public Collection<Analyza> analyza;
    
    public static class DefaultListItem implements ListItem<String>{
        private String name;
        private String value;

        public DefaultListItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        
        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public String getName() {
            return this.name;
        }
    } 
    
    public static List<ListItem<String>> readItems() {
        Connection conn = PersisterUtils.getConnection();
        return new JDBCQueryTemplate<ListItem<String>>(conn, true) {
            @Override
            public boolean handleRow(ResultSet rs, List<ListItem<String>> returnsList) throws SQLException {
                String name = rs.getString("Nazev");
                Integer i = rs.getInt("Zdroj_id");
                returnsList.add(new DefaultListItem(name, ""+i));
                return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
            }
        }.executeQuery("select Nazev, Zdroj_id from DEV_PROKOP.ZDROJ");
    }
    

    
    /** Vstupni analyza - funkce a wizard */
    public Function spustitAnalyzu = new Function("SpustitAnalyzu", "SpustitAnalyzu", new SpustitAnalyzu()); {

        /** Spustit analyzu */ 
        Wizard wizard = new Wizard(spustitAnalyzu,"default-wizard");
        Property<String> vstupniHodnota = wizard.stringProperty("zdroj", 10);
        
        vstupniHodnota.setListProvider(new ListProvider<String>() {

            @Override
            public List<ListItem<String>> getListValues() {
                return readItems();
            }
        });

        wizard.form(column(
                row(vstupniHodnota)
        ), true);
    }

    
    public Function zastavitAnalyzu = new Function("ZastavitAnalyzu", "ZastavitAnalyzu", new ZastavitAnalyzu()); 
    
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
        
        this.setPersistersTriggers(new PersisterTriggers() {

            @Override
            public void beforeCreate(Record record, Context ctx) {
            }

            @Override
            public void afterCreate(Record record, Context ctx) {
            }

            @Override
            public void beforeUpdate(Record record, Context ctx) {
            }

            @Override
            public void afterUpdate(Record record, Context ctx) {
            }

            @Override
            public void beforeDelete(Record record, Context ctx) {
            }

            @Override
            public void afterDelete(Record record, Context ctx) {
            }

            @Override
            public void afterLoad(Record record, Context ctx) {
                String value = (String) record.getValue("Property:Modul.trida");
                if (value != null) {
                    try {
                        Class<?> clz = Class.forName(value);
                        Analytic analytic = (Analytic) clz.newInstance();
                        String[] keys =  analytic.getWizardKeys();
                        record.putAnnotation(spustitAnalyzu.getId()+"_"+Function.ANNOTATION_SELECTED_WIZARDS_KEY, keys);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typModulu).addProperty(nazev);
        retval.form(column(
                row(typModulu,nazev, formatXML),
                row(trida, parametry),
                
                row(spustitAnalyzu,zastavitAnalyzu),

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
