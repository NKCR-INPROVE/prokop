/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.analytics.akka.messages;

import org.aplikator.client.shared.data.Record;

/**
 *
 * @author pavels
 */
public class StartAnalyze {
 
    private Record params;

    public StartAnalyze(Record params) {
        this.params = params;
    }

    public Record getParams() {
        return params;
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
