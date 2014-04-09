package cz.incad.prokop.server.analytics.akka.links;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cz.incad.prokop.server.analytics.akka.links.messages.URLRequest;
import cz.incad.prokop.server.analytics.akka.links.messages.URLResponse;
import cz.incad.prokop.server.utils.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavels
 */
public class URLConnectWorker extends UntypedActor {
    
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private int counter =0;
    
    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println("Receive message");
        if (message instanceof URLRequest) {
            log.info("URLConnect STARTING {}",message);
            this.counter ++;
            URLRequest req = (URLRequest) message;
            log.info("\t Request URL :"+req.getUrl());
            getSender().tell(connect(req.getUrl(), req.getZaznamId()),getSelf());
            
        } else {
            unhandled(message);
        }
    }
    
    private URLResponse connect(String urlString, int zaznamId) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpURLConnection httpUrlConn = null;
        URL url = null;
        try {
            url = new URL(urlString);
            httpUrlConn = (HttpURLConnection) (url).openConnection();
            httpUrlConn.setReadTimeout(2500);
            httpUrlConn.setConnectTimeout(2500);
            // jak na to ?
            httpUrlConn.setInstanceFollowRedirects(true);
            int respCode = httpUrlConn.getResponseCode();
            String respMessage = httpUrlConn.getResponseMessage();
            InputStream is = httpUrlConn.getInputStream();
            IOUtils.copyStreams(is, bos);
            
            return new URLResponse(respCode,respMessage, url, httpUrlConn.getURL(), zaznamId, bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex.getMessage());
            return new URLResponse(-1,ex.getMessage(),url, httpUrlConn.getURL(), zaznamId, bos.toByteArray());
        } finally {
            if (httpUrlConn != null) {
                httpUrlConn.disconnect();
            } 
        }
    }
} 