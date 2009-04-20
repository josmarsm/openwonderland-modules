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

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Bernard Horan
 */
public class EventPlayingManagerImpl implements EventPlayingManager {
    private EventPlayingService service;

    /**
     *
     * @param service
     */
    public EventPlayingManagerImpl(EventPlayingService service) {
        this.service = service;
    }

    public void openChangesFile(String tapeName, ChangesFileOpenListener listener) {
        service.openChangesFile(tapeName, listener);
    }

    public void playMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessagePlayingListener listener) {
        service.playMessage(tapeName, clientID, message, listener);
    }

    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener) {
        service.closeChangesFile(tapeName, listener);
    }

}
