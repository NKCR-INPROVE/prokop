/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.messages;

/**
 *
 * @author pavels
 */
public class StartAnalyze {
 
    private String params;

    public StartAnalyze(String params) {
        this.params = params;
    }

    public String getParams() {
        return params;
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
