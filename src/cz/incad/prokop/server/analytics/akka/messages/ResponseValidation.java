/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.messages;

public class ResponseValidation {

    private URLResponse urlResponse;
    private String actorDescription;
    private boolean valid;
    private int zaznamId;

    public ResponseValidation(URLResponse urlResponse, String actorDescription, boolean validatedResult, int zaznamId) {
        this.urlResponse = urlResponse;
        this.actorDescription = actorDescription;
        this.valid = validatedResult;
        this.zaznamId = zaznamId;
    }

    public URLResponse getUrlResponse() {
        return urlResponse;
    }

    public String getActorDescription() {
        return actorDescription;
    }

    public boolean isValid() {
        return valid;
    }
    
    public int getZaznamId() {
        return this.zaznamId;
    }

    @Override
    public String toString() {
        return "ResponseValidation{" + "urlResponse=" + urlResponse + ", actorDescription=" + actorDescription + ", valid=" + valid + ", zaznamId=" + zaznamId + '}';
    }
    
    
}
