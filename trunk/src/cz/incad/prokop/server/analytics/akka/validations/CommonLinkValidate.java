/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.validations;

import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.messages.ResponseValidation;
import cz.incad.prokop.server.analytics.akka.messages.URLResponse;

/**
 *
 * @author pavels
 */
public class CommonLinkValidate extends UntypedActor{

    public static final String DESCRIPTION = "URL validace";
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof URLResponse) {
            URLResponse response = (URLResponse) message;
            boolean valid = (response.getResponseCode() == 200);
            ResponseValidation respVal = new ResponseValidation(response, DESCRIPTION, valid,response.getZaznamId());
            getSender().tell( respVal ,getSelf());
        } else {
            unhandled(message);
        }
    }
}
