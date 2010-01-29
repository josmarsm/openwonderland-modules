/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;

/**
 * A message to manually add a new object
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderManualObjectMessage extends CellMessage {
    private DatedObject obj;

    public ProviderManualObjectMessage(CellID cellID, DatedObject obj) {
        super (cellID);

        this.obj = obj;
    }

    public DatedObject getObject() {
        return obj;
    }
}
