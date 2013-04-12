/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.links.validations;

import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.links.messages.ResponseValidation;
import cz.incad.prokop.server.analytics.akka.links.messages.URLResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;


/**
 * 
 * @author pavels
 */
public class K3HandleValidate extends UntypedActor{

    public static final Logger LOGGER = Logger.getLogger(K3HandleValidate.class.getName());
            
    
    public static final String DESCRIPTION="K3 validace";
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof URLResponse) {
            URLResponse response = (URLResponse) message;
            URL sourceURL = response.getSourceURL();
            boolean valid = true;
            if (sourceURL.getPath().contains("/handle/")) {
                LOGGER.info("K3 validation "+response.getSourceURL().toString());
                String path = response.getUrl().getPath();
                valid =  (!path.endsWith("ERR_NotFound.do"));
            }
            ResponseValidation respVal = new ResponseValidation(response, DESCRIPTION, valid,response.getZaznamId());
            getSender().tell( respVal ,getSelf());
        } else {
            unhandled(message);
        }
    }
}
