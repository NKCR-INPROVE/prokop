/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka;

import akka.actor.UntypedActor;
import cz.incad.prokop.server.analytics.akka.messages.URLRequest;
import cz.incad.prokop.server.analytics.akka.messages.URLResponse;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavels
 */
public class URLConnector extends UntypedActor {
    
    static final Logger LOGGER = Logger.getLogger(URLConnector.class.getName());
    private int counter =0;
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof URLRequest) {
            this.counter ++;
            URLRequest req = (URLRequest) message;
            getSender().tell(connect(req.getUrl(), req.getZaznamId()),getSelf());
            
        } else {
            unhandled(message);
        }
    }
    
    private URLResponse connect(String url, int zaznamId) {
        System.out.println("testing  url "+url+" for zaznamID "+zaznamId);
        HttpURLConnection httpUrlConn = null;
        try {
            httpUrlConn = (HttpURLConnection) (new URL(url)).openConnection();
            httpUrlConn.setReadTimeout(2500);
            httpUrlConn.setConnectTimeout(2500);
            // jak na to ?
            httpUrlConn.setInstanceFollowRedirects(true);
            int respCode = httpUrlConn.getResponseCode();
            String respMessage = httpUrlConn.getResponseMessage();
            
            return new URLResponse(respCode,respMessage, httpUrlConn.getURL(), zaznamId);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(),ex);
            return new URLResponse(-1,ex.getMessage(), httpUrlConn.getURL(), zaznamId);
        } finally {
            if (httpUrlConn != null) {
                httpUrlConn.disconnect();
            } 
        }
    }
} 