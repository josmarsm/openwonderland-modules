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

import com.jme.bounding.BoundingCapsule;
import com.jme.bounding.BoundingVolume;
import com.jme.math.LineSegment;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage.ChatZoneAction;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellClientState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellServerState;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;
import org.jdesktop.wonderland.modules.grouptextchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.modules.grouptextchat.server.TextChatConnectionHandler;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

public class ChatZonesCellMO extends CellMO {

    private static final Logger logger = Logger.getLogger(ChatZonesCellMO.class.getName());

    private GroupID group;

    private int numAvatarsInZone = 0;

    @UsesCellComponentMO(ProximityComponentMO.class)
    private ManagedReference<ProximityComponentMO> proxRef;

    @UsesCellComponentMO(MovableComponentMO.class)
    private ManagedReference<MovableComponentMO> moveRef;

    private ChatZoneProximityListener proxListener;



    public ChatZonesCellMO () {
        super();

        // Need to do this before the Cell goes live.
        this.setLocalBounds(new BoundingCapsule(new Vector3f(), new LineSegment(new Vector3f(0, 0, -10), new Vector3f(0, 0, 10)), 1));
    }

    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.chatzones.client.ChatZonesCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        
        this.group = ((ChatZonesCellServerState)state).getChatGroup();
        this.numAvatarsInZone = ((ChatZonesCellServerState)state).getNumAvatarsInZone();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ChatZonesCellServerState();
        }

        ((ChatZonesCellServerState)state).setChatGroup(group);
        ((ChatZonesCellServerState)state).setNumAvatarsInZone(numAvatarsInZone);

        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new ChatZonesCellClientState();

        }

        ((ChatZonesCellClientState)cellClientState).setNumAvatarsInZone(this.numAvatarsInZone);
        ((ChatZonesCellClientState)cellClientState).setGroup(group);
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
//            logger.info("localBounds: " + this.getLocalBounds());
            BoundingVolume[] bounds = {this.getLocalBounds().clone(null)};

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

//    public updateGroupLabel(String label) {
//        group.setLabel(label);
//        ChatZonesCellChangeMessage msg = new ChatZonesCellChangeMessage(ChatZoneAction.LABEL);
//        msg.setLabel(label);
//        sendCellMessage(clientID, msg);
//    }
//
    
    private static class ChatZonesCellMessageReceiver extends AbstractComponentMessageReceiver {
        public ChatZonesCellMessageReceiver(ChatZonesCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // do something.
            ChatZonesCellMO cellMO = (ChatZonesCellMO)getCell();

            ChatZonesCellChangeMessage bsccm = (ChatZonesCellChangeMessage)message;

            switch(bsccm.getAction()) {
                case LABEL:
//                    cellMO.updateGroupLabel(bsccm.getLabel());
                    TextChatConnectionHandler textChat = (TextChatConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(TextChatConnectionType.CLIENT_TYPE);

                    String label = bsccm.getLabel();
                    
                    // Tell the text chat system that we're changing the label. 
                    textChat.setGroupLabel(cellMO.group, label);
                    
                    cellMO.group.setLabel(label);
                    ChatZonesCellChangeMessage msg = new ChatZonesCellChangeMessage(ChatZoneAction.LABEL);
                    msg.setLabel(label);

                    cellMO.sendCellMessage(clientID, msg);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * This event is fired by the ProximityListener when an avatar enters this
     * cell.
     *
     * @param wcid The WonderlandClientID of the avatar that entered the cell.
     */
    public void userEnteredCell(WonderlandClientID wcid) {
        TextChatConnectionHandler tcmh = (TextChatConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(TextChatConnectionType.CLIENT_TYPE);
        tcmh.addUserToChatGroup(group, wcid);

        this.numAvatarsInZone++;

        logger.info("numAvatarsInZone: " + numAvatarsInZone);

//        this.updateScaleTransform();

        // Send a message to all clients that the number of avatars in this
        // cell has changed. 
        ChatZonesCellChangeMessage msg = new ChatZonesCellChangeMessage(ChatZoneAction.JOINED);
        msg.setName(wcid.getSession().getName());
        msg.setNumAvatarInZone(numAvatarsInZone);
        this.sendCellMessage(null, msg);

        this.updateProximityListenerBounds();
    }

    /**
     * This event is fired by the ProximityListener when an avatar leaves this
     * cell.
     *
     * @param wcid The WonderlandClientID of the avatar that entered the cell.
     */
    public void userLeftCell(WonderlandClientID wcid) {
        TextChatConnectionHandler tcmh = (TextChatConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(TextChatConnectionType.CLIENT_TYPE);
        tcmh.removeUserFromChatGroup(group, wcid);

        this.numAvatarsInZone--;

        logger.info("numAvatarsInZone: " + numAvatarsInZone);


//        this.updateScaleTransform();


        // Send a message to all clients that the number of avatars in this
        // cell has changed.
        ChatZonesCellChangeMessage msg = new ChatZonesCellChangeMessage(ChatZoneAction.LEFT);
        msg.setName(wcid.getSession().getName());
        msg.setNumAvatarInZone(numAvatarsInZone);
        this.sendCellMessage(null, msg);

        this.updateProximityListenerBounds();
    }

    /**
     * Call this when we have reason to think the bounds of the cell have
     * updated and we should change the proximity listener appropriately.
     */
    public void updateProximityListenerBounds() {
        BoundingVolume[] bounds = {new BoundingCapsule(new Vector3f(), new LineSegment(new Vector3f(0, 0, -10), new Vector3f(0, 0, 10)),(float) (1 + 0.3 * numAvatarsInZone))};

        logger.info("Updating proximity bounds: " + bounds);

        ProximityComponentMO proxComp = proxRef.getForUpdate();
        proxComp.setProximityListenerBounds(proxListener, bounds);
    }

//    private void updateScaleTransform() {
//        // Decide how big we should be based on the number of avatars in the cell.
//
//        // Start with linear scaling.
//        float scaleFactor = (float) (1 + 2 * numAvatarsInZone);
//        CellTransform scale = new CellTransform(new Quaternion(), this.getLocalTransform(null).getTranslation(null), scaleFactor);
//
//        MovableComponentMO mc = this.moveRef.getForUpdate();
//        mc.moveRequest(null, scale);
//        logger.info("Just send a scale change request to MoveComponent with scaleFactor: " + scaleFactor);
//    }
}