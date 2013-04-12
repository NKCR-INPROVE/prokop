/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.links.validations;

import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.links.messages.ResponseValidation;
import cz.incad.prokop.server.analytics.akka.links.messages.URLResponse;
import java.util.logging.Logger;

/**
 *
 * @author pavels
 */
public class CommonLinkValidate extends UntypedActor{

    public static Logger LOGGER = Logger.getLogger(CommonLinkValidate.class.getName());
    
    public static final String DESCRIPTION = "URL validace";
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof URLResponse) {
            URLResponse response = (URLResponse) message;
            LOGGER.info("validate url "+response.getUrl().toString());
            boolean valid = (response.getResponseCode() == 200);
            ResponseValidation respVal = new ResponseValidation(response, DESCRIPTION, valid,response.getZaznamId());
            getSender().tell( respVal ,getSelf());
        } else {
            unhandled(message);
        }
    }
}
