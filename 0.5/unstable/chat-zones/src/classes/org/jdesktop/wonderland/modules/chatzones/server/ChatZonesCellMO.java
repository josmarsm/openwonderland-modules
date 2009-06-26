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

package org.jdesktop.wonderland.modules.chatzones.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellClientState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

public class ChatZonesCellMO extends CellMO {

        @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.chatzones.client.ChatZonesCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
//        this.url = ((ChatZonesCellServerState)state).getUrl();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ChatZonesCellServerState();
        }
//        ((ChatZonesCellServerState)state).setUrl(this.url);
        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new ChatZonesCellClientState();
        }
//        ((ChatZonesCellClientState)cellClientState).setShapeType(shapeType);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live==true) {
            channel.addMessageReceiver(ChatZonesCellChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver)new ChatZonesCellMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(ChatZonesCellChangeMessage.class);
        }
    }

    private static class ChatZonesCellMessageReceiver extends AbstractComponentMessageReceiver {
        public ChatZonesCellMessageReceiver(ChatZonesCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // do something.
            ChatZonesCellMO cellMO = (ChatZonesCellMO)getCell();

            ChatZonesCellChangeMessage bsccm = (ChatZonesCellChangeMessage)message;


//            cellMO.sendCellMessage(clientID, bsccm);
        }
    }
}