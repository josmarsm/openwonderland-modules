/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * A message when a new result has been added
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderAddResultMessage extends CellMessage {
    private TimelineQuery query;

    public ProviderAddResultMessage(CellID cellID, TimelineQuery query)
    {
        super (cellID);
    
        this.query = query;
    }

    public TimelineQuery getQuery() {
        return query;
    }
}
