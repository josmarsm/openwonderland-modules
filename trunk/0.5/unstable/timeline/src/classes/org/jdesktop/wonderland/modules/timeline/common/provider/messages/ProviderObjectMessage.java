/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;

/**
 * A message when a new object has been added to a result
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderObjectMessage extends CellMessage {
    public enum Action { ADD, REMOVE };
    
    private TimelineQueryID queryID;
    private Action action;
    private DatedObject obj;

    public ProviderObjectMessage(CellID cellID, TimelineQueryID queryID,
                                 Action action, DatedObject obj)
    {
        super (cellID);
    
        this.queryID = queryID;
        this.action = action;
        this.obj = obj;
    }

    public TimelineQueryID getID() {
        return queryID;
    }

    public Action getAction() {
        return action;
    }

    public DatedObject getObject() {
        return obj;
    }
}
