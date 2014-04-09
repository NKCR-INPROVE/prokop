/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.links.messages;

import java.net.URL;

/**
 * Odpoved na url
 */
public class URLResponse {

    private int responseCode;
    private String message;
    private URL url;
    private URL sourceURL;
    private int zaznamId;
    private byte[] bytes;
    
    public URLResponse(int responseCode, String message, URL sourceURL, URL url, int zaznamId, byte[] bytes) {
        this.responseCode = responseCode;
        this.message = message;
        this.url = url;
        this.zaznamId = zaznamId;
        this.bytes = bytes;
        this.sourceURL = sourceURL;
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

    public URL getSourceURL() {
        return sourceURL;
    }
    
    
    public int getZaznamId() {
        return zaznamId;
    }

    public byte[] getContent() {
        return this.bytes;
    }

    @Override
    public String toString() {
        return "URLResponse{" + "responseCode=" + responseCode + ", message=" + message + ", url=" + url + ", sourceURL=" + sourceURL + ", zaznamId=" + zaznamId + ", bytes=" + bytes + '}';
    }
}
