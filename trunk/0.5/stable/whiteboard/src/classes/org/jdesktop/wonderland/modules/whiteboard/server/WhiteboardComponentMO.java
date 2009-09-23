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
package org.jdesktop.wonderland.modules.whiteboard.server;

import org.jdesktop.wonderland.modules.whiteboard.server.cell.WhiteboardCellMO;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 * The server side of the communication component that provides communication between the whiteboard client and server.
 * Requires ChannelComponent to also be connected to the cell prior to construction.
 *
 * @author deronj
 */
@ExperimentalAPI
public class WhiteboardComponentMO extends CellComponentMO {

    /** A managed reference to the cell channel communications component */
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    /** 
     * Create a new instance of WhiteboardComponentMO. 
     * @param cell The cell to which this component belongs.
     * @throws IllegalStateException If the cell does not already have a ChannelComponent IllegalStateException will be thrown.
     */
    public WhiteboardComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    public void setLive(boolean isLive) {
        super.setLive(isLive);

        if (isLive) {
            channelComponentRef.getForUpdate().addMessageReceiver(WhiteboardCellMessage.class,
                    new WhiteboardComponentMOMessageReceiver(cellRef.get()));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(WhiteboardCellMessage.class);
        }
    }

    /**
     * Broadcast the given message to all clients.
     * @param sourceID the originator of this message, or null if it originated
     * with the server
     * @param message The message to broadcast.
     */
    public void sendAllClients(WonderlandClientID clientID, WhiteboardCellMessage message) {
        ChannelComponentMO channelComponent = channelComponentRef.getForUpdate();
        channelComponent.sendAll(clientID, message);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardComponent";
    }

    /**
     * Receiver for for whiteboard messages.
     * Note: inner classes of managed objects must be non-static.
     * Benefits from event recorder mechanism by extending AbstractComponentMessageReceiver
     */
    private static class WhiteboardComponentMOMessageReceiver extends AbstractComponentMessageReceiver {

        public WhiteboardComponentMOMessageReceiver(CellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            WhiteboardCellMessage cmsg = (WhiteboardCellMessage) message;
            ((WhiteboardCellMO) getCell()).receivedMessage(sender, clientID, cmsg);
        }

        @Override
        protected void postRecordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            WhiteboardCellMessage cmsg = (WhiteboardCellMessage) message;
            RecorderManager.getDefaultManager().recordMetadata(cmsg,  cmsg.getXMLString());
            UserMO user = UserManager.getUserManager().getUser(clientID);
            RecorderManager.getDefaultManager().recordMetadata(cmsg,  "Created by " + user.getUsername() + "[" + user.getIdentity().getFullName() + "]");
        }
    }
}
