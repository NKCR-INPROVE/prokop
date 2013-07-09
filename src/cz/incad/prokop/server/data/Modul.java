package cz.incad.prokop.server.data;

import cz.incad.prokop.server.functions.SpustitAnalyzu;
import cz.incad.prokop.server.functions.ZastavitAnalyzu;
import cz.incad.prokop.server.utils.JDBCQueryTemplate;
import cz.incad.prokop.server.utils.PersisterUtils;
import java.io.File;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.*;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;
import org.aplikator.server.persistence.PersisterTriggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import org.aplikator.server.processes.OSProcessConfiguration;
import static org.aplikator.server.processes.ProcessConfiguration.processConf;
import org.aplikator.server.processes.ProcessFactory;
import org.aplikator.server.processes.ProcessType;
import org.aplikator.server.processes.RunnableSerializationAware;

public class Modul extends Entity {
    
    
    public static final String WIZARD_PAGE_KEY = "zdrojPage";
    public static final String ZDROJ_KEY = "zdrojProperty";
    
    
    public Property<String> typModulu;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;

    public Property<String> stav;
    
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
        }.executeQuery("select Nazev, Zdroj_id from ZDROJ");
    }
    

    
    /** Vstupni analyza - funkce a wizard */
    public Function spustitAnalyzu = new Function("SpustitAnalyzu", "SpustitAnalyzu", new SpustitAnalyzu()); 
    {

        /** Spustit analyzu */ 
        WizardPage wizard = new WizardPage(spustitAnalyzu,WIZARD_PAGE_KEY);
        Property<String> vstupniHodnota = wizard.stringProperty(ZDROJ_KEY, 3);
        
        
        vstupniHodnota.setListProvider(new ListProvider<String>() {

            @Override
            public List<ListItem<String>> getListValues(Context ctx) {
                return readItems();
            }
        });

        wizard.form(column(
                row(vstupniHodnota)
        ), true);
    }

    
    public Function zastavitAnalyzu = new Function("ZastavitAnalyzu", "ZastavitAnalyzu", new ZastavitAnalyzu()); 
    
    public final Function testFunction = new Function("test","test", new Executable() {


        @Override
        public WizardPage getWizardPage(String currentPage, boolean forwardFlag, Record currentProcessingRecord, Record clientParameters, Context context) {
            List<String> pages = Arrays.asList("first","second","third");
            String nextPage = currentPage.trim().equals("") ? "first" : pages.get(pages.indexOf(currentPage)+1);

            boolean execFlag = false;
            boolean nextFlag = true;
            boolean prevFlag = true;
            
            if (forwardFlag) {
                if (nextPage.equals("third")) { execFlag = true; nextFlag = false; }
            } else {
                if (nextPage.equals("first")) {  prevFlag = false; }
            }

            WizardPage p = this.function.getRegistredView(nextPage);
            p.setHasExecute(execFlag);
            p.setHasNext(nextFlag);
            p.setHasPrevious(prevFlag);
            return p;
        }



        @Override
        public FunctionResult execute(FunctionParameters parameters, Context context) {
            context.getHttpServletRequest().getServletPath();
            
            OSProcessConfiguration conf = processConf().classpathlib(System.getProperty("user.dir")+File.separator+"libs");
            org.aplikator.server.processes.Process process = ProcessFactory.get(ProcessType.PROCESS).create(conf, new RunnableSerializationAware() {

                @Override
                public void run() {
                    System.out.println("... testik ... ");
                }
            });

            
            Record clientRecord = parameters.getClientParameters();
            Record procsRecord = parameters.getClientContext().getCurrentRecord();
            
            StringBuilder builder = new StringBuilder();
            builder.append("record:").append(clientRecord);
            builder.append("processingRecord:").append(procsRecord);
            return new FunctionResult(builder.toString(),true);
        }
    }); 
    {

        WizardPage p1 = new WizardPage(testFunction, "first");
        Property<String> p1input = p1.stringProperty("finput", 3);
        p1.form(row(
                column(p1input)
        ), false);
        
        WizardPage p2 = new WizardPage(testFunction, "second");
        Property<String> p2input = p2.stringProperty("sinput", 3);
        p2.form(row(
                column(p2input)
        ), false);

        WizardPage p3 = new WizardPage(testFunction, "third");
        Property<String> p3input = p3.stringProperty("tinput", 3);
        p3.form(row(
                column(p3input)
        ), false);
    }
        

    
    
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
        stav = stringProperty("stav");
        
        this.setPersistersTriggers(new PersisterTriggers.Default() {

            @Override
            public void onLoad(Record record, Context ctx) {
                /*
                String value = (String) record.getValue("Property:Modul.trida");
                if (value != null) {
                    try {
                        Class<?> clz = Class.forName(value);
                        Analytic analytic = (Analytic) clz.newInstance();
                        String[] keys =  analytic.getWizardKeys();
                        record.putAnnotation(spustitAnalyzu.getId()+"_"+Function.ANNOTATION_SELECTED_WIZARDS_KEY, keys);
                        //record.putAnnotation(value, value);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(Modul.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                */ 
            }
        });
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typModulu).addProperty(nazev);
        retval.form(column(
                row(typModulu,nazev, formatXML),
                row(trida, stav),
                
                row(spustitAnalyzu,zastavitAnalyzu),

                row(testFunction),
                RepeatedForm.repeated(analyza)
        ));
        return retval;
    }

    private View reverseView;
    public View getReverseView(){
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
