/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.messages;

import java.net.URL;

/**
 * Odpoved na url
 */
public class URLResponse {

    private int responseCode;
    private String message;
    private URL url;
    private int zaznamId;
    
    public URLResponse(int responseCode, String message, URL url, int zaznamId) {
        this.responseCode = responseCode;
        this.message = message;
        this.url = url;
        this.zaznamId = zaznamId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }

    public URL getUrl() {
        return url;
    }

    public int getZaznamId() {
        return zaznamId;
    }

    @Override
    public String toString() {
        return "URLResponse{" + "responseCode=" + responseCode + ", message=" + message + ", url=" + url + ", zaznamId=" + zaznamId + '}';
    }
    
    
}
