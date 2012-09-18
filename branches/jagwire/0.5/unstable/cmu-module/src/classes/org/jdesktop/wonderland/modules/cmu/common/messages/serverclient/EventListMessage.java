/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;

/**
 * Message informing the server or clients of an update to the list of
 * Wonderland events to respond to.  Updates are sent simply by resending
 * the entire event list - this is less efficient, but we don't have to deal
 * with merging changes.
 * @author kevin
 */
public class EventListMessage extends CellMessage {

    private final EventResponseList list;

    public EventListMessage(EventResponseList list) {
        this.list = list;
    }

    public EventResponseList getList() {
        return list;
    }
}
