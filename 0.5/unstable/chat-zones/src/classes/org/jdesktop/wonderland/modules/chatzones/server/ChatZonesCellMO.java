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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellClientState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellServerState;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;
import org.jdesktop.wonderland.modules.grouptextchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.modules.grouptextchat.server.TextChatConnectionHandler;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

public class ChatZonesCellMO extends CellMO {

    private static final Logger logger = Logger.getLogger(ChatZonesCellMO.class.getName());

    private GroupID group;

    @UsesCellComponentMO(ProximityComponentMO.class)
    private ManagedReference<ProximityComponentMO> proxRef;

    private ChatZoneProximityListener proxListener;


    public ChatZonesCellMO () {
        super();        
    }

    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.chatzones.client.ChatZonesCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        
        this.group = ((ChatZonesCellServerState)state).getChatGroup();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ChatZonesCellServerState();
        }

        ((ChatZonesCellServerState)state).setChatGroup(group);
        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new ChatZonesCellClientState();

        }
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        logger.info("Setting ChatZonesCellMO live: " + live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {
            channel.addMessageReceiver(ChatZonesCellChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver)new ChatZonesCellMessageReceiver(this));

            // Just guessing here...
            BoundingVolume[] bounds = {new BoundingBox(new Vector3f(), 4, 4, 4)};

            proxListener =
                new ChatZoneProximityListener();
            proxRef.getForUpdate().addProximityListener(proxListener, bounds);

            logger.info("Just set proximity listener: " + proxListener);

            // do my init work here? Not sure where it's supposed to go.
            CommsManager cm = WonderlandContext.getCommsManager();
            TextChatConnectionHandler handler = (TextChatConnectionHandler) cm.getClientHandler(TextChatConnectionType.CLIENT_TYPE);

            group = handler.createChatGroup();
            logger.info("Setting up Chat Zone, got chat group: " + group);
        }
        else {
            channel.removeMessageReceiver(ChatZonesCellChangeMessage.class);

            proxRef.getForUpdate().removeProximityListener(proxListener);
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
        }
    }

    void userEnteredCell(WonderlandClientID wcid) {
        TextChatConnectionHandler tcmh = (TextChatConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(TextChatConnectionType.CLIENT_TYPE);
        tcmh.addUserToChatGroup(group, wcid);
    }

    void userLeftCell(WonderlandClientID wcid) {
        TextChatConnectionHandler tcmh = (TextChatConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(TextChatConnectionType.CLIENT_TYPE);
        tcmh.removeUserFromChatGroup(group, wcid);
    }
}