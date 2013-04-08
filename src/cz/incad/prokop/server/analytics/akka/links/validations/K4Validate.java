/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.links.validations;

import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.links.messages.ResponseValidation;
import cz.incad.prokop.server.analytics.akka.links.messages.URLResponse;
import static cz.incad.prokop.server.analytics.akka.links.validations.K3HandleValidate.DESCRIPTION;


/**
 *
 * @author pavels
 */
public class K4Validate extends UntypedActor {

    public static final String DESCRIPTION="K4 validace";
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof URLResponse) {
            URLResponse response = (URLResponse) message;
            boolean errorParam = false;
            String q = response.getUrl().getQuery();
            if (q != null) {
                String[] splitted = q.split("&");
                for (String par : splitted) {
                    par = par.trim();
                    if (par.startsWith("error=uuid_not_found")) {
                        errorParam = true;
                        break;
                    }
                }
            }
            //return errorParam;
            boolean valid = (!errorParam);
            ResponseValidation respVal = new ResponseValidation(response, DESCRIPTION, valid,response.getZaznamId());
            getSender().tell( respVal ,getSelf());
        } else {
            unhandled(message);
        }
    }
}
