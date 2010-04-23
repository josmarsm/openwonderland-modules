/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import java.util.Collections;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;

/**
 * A message when a new object has been added to a result
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderObjectsMessage extends CellMessage {
    public enum Action { ADD, REMOVE };
    
    private TimelineQueryID queryID;
    private Action action;
    private Set<? extends DatedObject> objs;

    public ProviderObjectsMessage(CellID cellID, TimelineQueryID queryID,
                                 Action action, DatedObject obj)
    {
        this (cellID, queryID, action, Collections.singleton(obj));
    }

    public ProviderObjectsMessage(CellID cellID, TimelineQueryID queryID,
                                 Action action, Set<? extends DatedObject> objs)
    {
        super (cellID);
    
        this.queryID = queryID;
        this.action = action;
        this.objs = objs;
    }

    public TimelineQueryID getID() {
        return queryID;
    }

    public Action getAction() {
        return action;
    }

    public Set<? extends DatedObject> getObjects() {
        return objs;
    }
}
