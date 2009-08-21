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
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.LineSegment;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
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
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
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

    private CellID parentID;

    public ChatZonesCellMO () {
        super();

        // Need to do this before the Cell goes live.
        // For some reason BoundingCapsule doesn't work anymore?
        // got this error:
        //   java.lang.RuntimeException: Bounds not supported com.jme.bounding.BoundingCapsule
        //
        // So switching to spheres which aren't quite right, but will have to do.
        
     //        this.setLocalBounds(new BoundingCapsule(new Vector3f(), new LineSegment(new Vector3f(0, 0, -10), new Vector3f(0, 0, 10)), 1));
        this.setLocalBounds(new BoundingSphere(1, Vector3f.ZERO));
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
        this.parentID = new CellID(Long.parseLong(((ChatZonesCellServerState)state).getParentID()));
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ChatZonesCellServerState();
        }

        ((ChatZonesCellServerState)state).setChatGroup(group);
        ((ChatZonesCellServerState)state).setNumAvatarsInZone(numAvatarsInZone);
        ((ChatZonesCellServerState)state).setParentID(parentID.toString());
        
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

            // REMOVE THIS WHEN YOU TURN REPARENTING BACK ON
            // Now setup the transform changed listener on my parent cell.
            CellMO parentCell = CellManagerMO.getCell(this.parentID);
            CellTransform originalTransform = parentCell.getWorldTransform(null);
            parentCell.addTransformChangeListener(new MyTransformChangedListener(originalTransform, this));
        }
        else {
            channel.removeMessageReceiver(ChatZonesCellChangeMessage.class);
            proxRef.getForUpdate().removeProximityListener(proxListener);
        }
    }


    // REMOVE THIS WHEN YOU TURN REPARENTING BACK ON
    private static class MyTransformChangedListener implements TransformChangeListenerSrv {
        private ManagedReference imageRef;
        private CellTransform originalTransform;

        public MyTransformChangedListener(CellTransform originalTransform, ChatZonesCellMO cellMO) {
            this.originalTransform = originalTransform;
            imageRef = AppContext.getDataManager().createReference(cellMO);
        }

        public void transformChanged(ManagedReference<CellMO> cellRef,
                CellTransform localTransform, CellTransform worldTransform) {

            Logger logger = Logger.getLogger(MyTransformChangedListener.class.getName());
            logger.warning("CELL " + worldTransform.getTranslation(null).toString());

            // From the last know position, take find the amount that the Cell
            // has been moved.
            Vector3f newPosition = worldTransform.getTranslation(null);
            Vector3f diff = newPosition.subtract(originalTransform.getTranslation(null));
            originalTransform = worldTransform.clone(null);

            AppContext.getTaskManager().scheduleTask(new MyTask(imageRef, diff));
        }
        }

    // REMOVE THIS WHEN YOU TURN REPARENTING BACK ON
    private static class MyTask implements Task, Serializable {

        private ManagedReference imageRef;
        private Vector3f diff;

        public MyTask(ManagedReference imageRef, Vector3f diff) {
            this.imageRef = imageRef;
            this.diff = diff;
        }

        public void run() {
             // Add that to the current position of this Cell
            CellTransform transform = ((ChatZonesCellMO)imageRef.get()).getLocalTransform(null);
            Vector3f thisPosition = transform.getTranslation(null);
            thisPosition = thisPosition.add(diff);
            transform.setTranslation(thisPosition);

            ChatZonesCellMO imageMO = (ChatZonesCellMO)imageRef.get();
            imageMO.move(transform);
        }

        public void cancel() {
            
        }
    }

    // REMOVE THIS WHEN YOU TURN REPARENTING BACK ON
    public void move(CellTransform transform) {
        moveRef.getForUpdate().moveRequest(null, transform);
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
                    // I think there's a bug here where if you log out while in the cell,
                    // then someone else changes the label, the cell will hang. But since
                    // the logging out problem is going to get fixed in the near future,
                    // I don't want to fix it right now.
                    
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