/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventrecorder.server;

import org.jdesktop.wonderland.server.eventrecorder.*;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Bernard Horan
 */
public class EventRecordingManagerImpl implements EventRecordingManager {
    private EventRecordingService service;

    /**
     *
     * @param service
     */
    public EventRecordingManagerImpl(EventRecordingService service) {
        this.service = service;
    }

    public void openChangesFile(String tapeName, ChangesFileCreationListener listener) {
        service.openChangesFile(tapeName, listener);
    }

    public void recordMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessageRecordingListener listener) {
        service.recordMessage(tapeName, clientID, message, listener);
    }

    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener) {
        service.closeChangesFile(tapeName, listener);
    }

}
