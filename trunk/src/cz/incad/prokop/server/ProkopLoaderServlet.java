package cz.incad.prokop.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Menu;

import cz.incad.prokop.server.functions.ReindexFast;

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
            struct.addMenu(admin);

            LOG.info("Prokop Loader finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Prokop Loader error:", ex);
            throw new ServletException("Prokop Loader error: ", ex);
        }
    }
}
