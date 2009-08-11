/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;

/**
 * A message when a new object has been added to a result
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderResetResultMessage extends CellMessage {    
    private TimelineQueryID queryID;

    public ProviderResetResultMessage(CellID cellID, TimelineQueryID queryID) {
        super (cellID);
    
        this.queryID = queryID;
    }

    public TimelineQueryID getID() {
        return queryID;
    }
}
