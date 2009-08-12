/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
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
