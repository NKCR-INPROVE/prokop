/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.messages;

/**
 * Pozadavek na ziskani url
 */
public class URLRequest {

    private String url;
    private int zaznamId;
    
    public URLRequest(String url, int zaznamId) {
        this.url = url;
        this.zaznamId = zaznamId;
    }

    public String getUrl() {
        return url;
    }

    public int getZaznamId() {
        return zaznamId;
    }
}
