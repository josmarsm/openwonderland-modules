/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.bestview.server;

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.bestview.common.BestViewChangeMessage;
import org.jdesktop.wonderland.modules.bestview.common.BestViewClientState;
import org.jdesktop.wonderland.modules.bestview.common.BestViewServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server side cell component for best view
 * 
 * @author Abhishek Upadhyay
 */
public class BestViewComponentMO extends CellComponentMO {

    private static final Logger LOGGER =
            Logger.getLogger(BestViewComponentMO.class.getName());
    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private float lookDirX;
    private float lookDirY;
    private float lookDirZ;
    private float lookDirW;
    private float zoom;
    private int trigger = 1;
    private float oldObjPosX;
    private float oldObjPosY = 999;
    private float oldObjPosZ;
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    public BestViewComponentMO(CellMO cellMO) {
        super(cellMO);
    }

    /**
     * Get the class name of the client CellComponent to instantiate.
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.bestview.client.BestViewComponent";
    }

    /**
     * Get the client state for this component
     */
    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        // if an existing state is not passed in from a subclass, create one
        // ourselves
        if (state == null) {
            state = new BestViewClientState();
        }

        // do any configuration necessary
        ((BestViewClientState) state).setOffsetX(offsetX);
        ((BestViewClientState) state).setOffsetY(offsetY);
        ((BestViewClientState) state).setOffsetZ(offsetZ);

        ((BestViewClientState) state).setLookDirX(lookDirX);
        ((BestViewClientState) state).setLookDirY(lookDirY);
        ((BestViewClientState) state).setLookDirZ(lookDirZ);
        ((BestViewClientState) state).setLookDirW(lookDirW);
        ((BestViewClientState) state).setZoom(zoom);
        ((BestViewClientState) state).setTrigger(trigger);
        ((BestViewClientState) state).setOldObjPosX(oldObjPosX);
        ((BestViewClientState) state).setOldObjPosY(oldObjPosY);
        ((BestViewClientState) state).setOldObjPosZ(oldObjPosZ);

        // pass the state we created up to the superclass to add any other
        // necessary properties
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * Get the server state for this component
     */
    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        // if an existing state is not passed in from a subclass, create one
        // ourselves
        if (state == null) {
            state = new BestViewServerState();
        }

        // do any configuration necessary
        ((BestViewServerState) state).setOffsetX(offsetX);
        ((BestViewServerState) state).setOffsetY(offsetY);
        ((BestViewServerState) state).setOffsetZ(offsetZ);

        ((BestViewServerState) state).setLookDirX(lookDirX);
        ((BestViewServerState) state).setLookDirY(lookDirY);
        ((BestViewServerState) state).setLookDirZ(lookDirZ);
        ((BestViewServerState) state).setLookDirW(lookDirW);
        ((BestViewServerState) state).setZoom(zoom);
        ((BestViewServerState) state).setTrigger(trigger);
        ((BestViewServerState) state).setOldObjPosX(oldObjPosX);
        ((BestViewServerState) state).setOldObjPosY(oldObjPosY);
        ((BestViewServerState) state).setOldObjPosZ(oldObjPosZ);

        // pass the state we created up to the superclass to add any other
        // necessary properties
        return super.getServerState(state);
    }

    /**
     * Handle when the system sets our server state, for example when restoring
     * from WFS
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        // pass the state object to the superclass for further processing
        super.setServerState(state);
        offsetX = ((BestViewServerState) state).getOffsetX();
        offsetY = ((BestViewServerState) state).getOffsetY();
        offsetZ = ((BestViewServerState) state).getOffsetZ();

        lookDirX = ((BestViewServerState) state).getLookDirX();
        lookDirY = ((BestViewServerState) state).getLookDirY();
        lookDirZ = ((BestViewServerState) state).getLookDirZ();
        lookDirW = ((BestViewServerState) state).getLookDirW();
        zoom = ((BestViewServerState) state).getZoom();
        trigger = ((BestViewServerState) state).getTrigger();
        oldObjPosX = ((BestViewServerState) state).getOldObjPosX();
        oldObjPosY = ((BestViewServerState) state).getOldObjPosY();
        oldObjPosZ = ((BestViewServerState) state).getOldObjPosZ();

    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        ChannelComponentMO channelComponent = channelRef.get();
        if (live) {
            BestViewComponentMO.ComponentMessageReceiverImpl receiver = new BestViewComponentMO.ComponentMessageReceiverImpl(cellRef);
            channelComponent.addMessageReceiver(BestViewChangeMessage.class, receiver);
        } else {
            channelComponent.removeMessageReceiver(BestViewChangeMessage.class);
        }
    }

    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver {

        private ManagedReference<CellMO> cellMO;

        private ComponentMessageReceiverImpl(ManagedReference<CellMO> cellMO) {
            super(cellMO.get());
            this.cellMO = cellMO;
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
                CellMessage message) {
            if (message instanceof BestViewChangeMessage) {
                BestViewServerState ser = (BestViewServerState) ((CellServerComponentMessage) message).getCellComponentServerState();
                BestViewComponentMO ntcMO = cellMO.get().getComponent(BestViewComponentMO.class);
                ntcMO.setServerState(ser);
            }
        }
    }
}
