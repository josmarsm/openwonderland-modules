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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.StickyNoteSyncMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.StickyNoteCellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server side of the communication component that provides communication between the sticky note client and server.
 * Requires ChannelComponent to also be connected to the cell prior to construction.
 *
 * Taken almost directly from SVG whiteboard
 * @author deronj
 *
 * @author Ryan (mymegabyte)
 */
@ExperimentalAPI
public class StickyNoteComponentMO extends CellComponentMO {

    /** A managed reference to the cell channel communications component */
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    /** 
     * Create a new instance of WhiteboardComponentMO. 
     * @param cell The cell to which this component belongs.
     * @throws IllegalStateException If the cell does not already have a ChannelComponent IllegalStateException will be thrown.
     */
    public StickyNoteComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    public void setLive(boolean isLive) {
        super.setLive(isLive);

        if (isLive) {
            channelComponentRef.getForUpdate().addMessageReceiver(StickyNoteSyncMessage.class,
                    new StickynoteComponentMOMessageReceiver(cellRef.get()));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(StickyNoteSyncMessage.class);
        }
    }

    /**
     * Broadcast the given message to all clients.
     * @param sourceID the originator of this message, or null if it originated
     * with the server
     * @param message The message to broadcast.
     */
    public void sendAllClients(WonderlandClientID clientID, StickyNoteSyncMessage message) {
        ChannelComponentMO channelComponent = channelComponentRef.getForUpdate();
        channelComponent.sendAll(clientID, message);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.StickyNoteComponent";
    }

    /**
     * Receiver for for whiteboard messages.
     * Note: inner classes of managed objects must be non-static.
     * Benefits from event recorder mechanism by extending AbstractComponentMessageReceiver
     */
    private static class StickynoteComponentMOMessageReceiver extends AbstractComponentMessageReceiver {

        public StickynoteComponentMOMessageReceiver(CellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            StickyNoteSyncMessage cmsg = (StickyNoteSyncMessage) message;
            ((StickyNoteCellMO) getCell()).receivedMessage(sender, clientID, cmsg);
        }
    }
}
