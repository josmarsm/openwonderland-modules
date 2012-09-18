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
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;

/**
 * A message when a result has been removed added
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderRemoveResultMessage extends CellMessage {
    private TimelineQueryID queryID;

    public ProviderRemoveResultMessage(CellID cellID, TimelineQueryID queryID)
    {
        super (cellID);
    
        this.queryID = queryID;
    }

    public TimelineQueryID getID() {
        return queryID;
    }
}
