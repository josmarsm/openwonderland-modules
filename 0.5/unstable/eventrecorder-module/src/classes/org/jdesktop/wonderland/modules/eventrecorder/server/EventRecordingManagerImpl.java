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


package org.jdesktop.wonderland.modules.eventrecorder.server;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Implementation of EventRecordingManager.  This just forwards everything to
 * the service.
 * @author Bernard Horan
 */
public class EventRecordingManagerImpl implements EventRecordingManager {
    private EventRecordingService service;

    public EventRecordingManagerImpl(EventRecordingService service) {
        this.service = service;
    }

    public void recordPosition(String tapeName, PositionComponentServerState positionState, PositionRecordedListener listener) {
        service.recordPosition(tapeName, positionState, listener);
    }

    public void createChangesFile(String tapeName, ChangesFileCreationListener listener) {
        service.createChangesFile(tapeName, listener);
    }

    public void recordMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessageRecordingListener listener) {
        service.recordMessage(tapeName, clientID, message, listener);
    }

    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener) {
        service.closeChangesFile(tapeName, listener);
    }

    public void recordLoadedCell(String tapeName, CellID cellID, CellID parentID, LoadedCellRecordingListener listener) {
        service.recordLoadedCell(tapeName, cellID, parentID, listener);
    }

    public void recordUnloadedCell(String tapeName, CellID cellID, UnloadedCellsRecordingListener listener) {
        service.recordUnloadedCell(tapeName, cellID, listener);
    }

    public void recordMetadata(String tapeName, MessageID messageID, String metadata, MetadataRecordingListener listener) {
        service.recordMetadata(tapeName, messageID, metadata, listener);
    }



}
