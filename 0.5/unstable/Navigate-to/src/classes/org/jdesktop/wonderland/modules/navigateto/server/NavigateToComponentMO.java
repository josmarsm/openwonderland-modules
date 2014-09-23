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
package org.jdesktop.wonderland.modules.navigateto.server;

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.bestview.server.BestViewComponentMO;
import org.jdesktop.wonderland.modules.navigateto.common.NavigateToClientState;
import org.jdesktop.wonderland.modules.navigateto.common.NavigateToServerState;
import org.jdesktop.wonderland.modules.navigateto.common.NavigationChangeMessage;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server side cell component for best view
 *
 * @author nilang shah
 * @author Abhishek Upadhyay
 */
@DependsOnCellComponentMO({SharedStateComponentMO.class, ChannelComponentMO.class})
public class NavigateToComponentMO extends CellComponentMO {

    private static final Logger LOGGER =
            Logger.getLogger(NavigateToComponentMO.class.getName());
    private int trigger = 0;
    private float offsetX = 1f;
    private float offsetY = 2f;
    private float offsetZ = 1f;
    private float lookDirX = 999;
    private float lookDirY;
    private float lookDirZ;
    public boolean bestview = true;
    @UsesCellComponentMO(BestViewComponentMO.class)
    ManagedReference<BestViewComponentMO> bvcMO;
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    public NavigateToComponentMO(CellMO cellMO) {
        super(cellMO);
    }

    /**
     * Get the class name of the client CellComponent to instantiate.
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.navigateto.client.NavigateToComponent";
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
            state = new NavigateToClientState();
        }
        ((NavigateToClientState) state).setTrigger(trigger);
        ((NavigateToClientState) state).setOffsetX(offsetX);
        ((NavigateToClientState) state).setOffsetY(offsetY);
        ((NavigateToClientState) state).setOffsetZ(offsetZ);
        ((NavigateToClientState) state).setLookDirX(lookDirX);
        ((NavigateToClientState) state).setLookDirY(lookDirY);
        ((NavigateToClientState) state).setLookDirZ(lookDirZ);
        ((NavigateToClientState) state).setBestView(bestview);

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
            state = new NavigateToServerState();
        }
        ((NavigateToServerState) state).setTrigger(trigger);
        ((NavigateToServerState) state).setOffsetX(offsetX);
        ((NavigateToServerState) state).setOffsetY(offsetY);
        ((NavigateToServerState) state).setOffsetZ(offsetZ);
        ((NavigateToServerState) state).setLookDirX(lookDirX);
        ((NavigateToServerState) state).setLookDirY(lookDirY);
        ((NavigateToServerState) state).setLookDirZ(lookDirZ);
        ((NavigateToServerState) state).setBestView(bestview);

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
        trigger = ((NavigateToServerState) state).getTrigger();
        offsetX = ((NavigateToServerState) state).getOffsetX();
        offsetY = ((NavigateToServerState) state).getOffsetY();
        offsetZ = ((NavigateToServerState) state).getOffsetZ();
        lookDirX = ((NavigateToServerState) state).getLookDirX();
        lookDirY = ((NavigateToServerState) state).getLookDirY();
        lookDirZ = ((NavigateToServerState) state).getLookDirZ();
        bestview = ((NavigateToServerState) state).getBestView();
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        ChannelComponentMO channelComponent = channelRef.get();
        if (live) {
            NavigateToComponentMO.ComponentMessageReceiverImpl receiver = new NavigateToComponentMO.ComponentMessageReceiverImpl(cellRef);
            channelComponent.addMessageReceiver(NavigationChangeMessage.class, receiver);
        } else {
            channelComponent.removeMessageReceiver(NavigationChangeMessage.class);
        }
    }

    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver {

        ManagedReference<CellMO> cellMO;

        private ComponentMessageReceiverImpl(ManagedReference<CellMO> cellMO) {
            super(cellMO.get());
            this.cellMO = cellMO;
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
                CellMessage message) {
            if (message instanceof NavigationChangeMessage) {
                NavigateToServerState ser = (NavigateToServerState) ((CellServerComponentMessage) message).getCellComponentServerState();
                NavigateToComponentMO ntcMO = cellMO.get().getComponent(NavigateToComponentMO.class);
                ntcMO.setServerState(ser);
            }
        }
    }
}
