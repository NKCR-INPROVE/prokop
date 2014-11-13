/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.prokop.server.data.triggers;

import cz.incad.prokop.server.Structure;
import org.aplikator.client.shared.data.ContainerNode;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.descriptor.PropertyDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author pavels
 */
public class ZaznamTrigger extends PersisterTriggers.Default {

    @Override
    public void onCreate(ContainerNode node, Context ctx) {
        System.out.println("Before create trigger");
        HttpServletRequest request = ctx.getHttpServletRequest();
        String remoteUser = request.getRemoteUser();
        PropertyDTO uzivatel = Structure.zaznam.uzivatel.clientClone(ctx);
        if (remoteUser != null) {
            uzivatel.setValue(node.getEdited(), remoteUser);
        } else {
            uzivatel.setValue(node.getEdited(), " ---- ");
        }
    }

    @Override
    public void onLoad(Record record, Context ctx) {
        record.setPreview("<b>"+ record.getValue(Structure.zaznam.hlavniNazev.getId())
                +"</b> ("+ record.getValue(Structure.zaznam.typDokumentu.getId())+")<br>"
                + record.getValue(Structure.zaznam.urlZdroje.getId()));
    }

}
