package cz.incad.prokop.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Menu;

import cz.incad.prokop.server.functions.ReindexFast;
import cz.incad.prokop.server.functions.TestFunction;
import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.WizardPage;

@SuppressWarnings("serial")
public class ProkopLoaderServlet extends ApplicationLoaderServlet {

    private static final Logger LOG = Logger.getLogger(ProkopLoaderServlet.class.getName());

    Structure struct;


    @Override
    public void init() throws ServletException {
        try {
            LOG.info("Prokop Loader started");
            struct = (Structure) Application.get();
            Menu records = new Menu("Zaznamy");
            records.addView(Structure.zaznam.view());
            struct.addMenu(records);
            Menu admin = new Menu("Admin");

            admin.addView(Structure.zdroj.view());
            admin.addView(Structure.modul.view());
            Function globalFunction = new Function("GlobalFunction", "GlobalFunction", new ReindexFast());
            admin.addFunction(globalFunction);

            
            Function testFunction = new Function("Test function","Test function", new TestFunction());
            {
                WizardPage p1 = new WizardPage(testFunction, "first");
                Property<String> p1input = p1.stringProperty("finput", 3);
                p1.form(row(
                        column(p1input)), false);
        
                WizardPage p2 = new WizardPage(testFunction, "second");
                Property<String> p2input = p2.stringProperty("sinput", 3);
                p2.form(row(
                        column(p2input)), false);

                WizardPage p3 = new WizardPage(testFunction, "third");
                Property<String> p3input = p3.stringProperty("tinput", 3);
                p3.form(row(
                        column(p3input)), false);
            }
            admin.addFunction(testFunction);
                    
            struct.addMenu(admin);
            
            LOG.info("Prokop Loader finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Prokop Loader error:", ex);
            throw new ServletException("Prokop Loader error: ", ex);
        }
    }
}
