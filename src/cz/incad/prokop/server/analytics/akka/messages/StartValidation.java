/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.messages;

/**
 *
 * @author pavels
 */
public class StartValidation {
    
    private String dataSelector;
    
    public StartValidation(String dataSelector) {
        this.dataSelector = dataSelector;
    }

    public String getDataSelector() {
        return dataSelector;
    }


}
