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
package org.jdesktop.wonderland.modules.proximitytest.server;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.modules.proximitytest.common.ProximityCellClientState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.proximitytest.common.ProximityCellServerState;
import org.jdesktop.wonderland.modules.proximitytest.common.ProximityEnterExitMessage;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Server behavior for proximity test
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public class ProximityCellMO extends CellMO {
    @UsesCellComponentMO(ProximityComponentMO.class)
    private ManagedReference<ProximityComponentMO> proxRef;

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    private List<BoundingVolume> serverBounds;
    private List<BoundingVolume> clientBounds;

    private ManagedReference<ServerProximityListener> listenerRef;

    /** Default constructor, used when cell is created via WFS */
    public ProximityCellMO() {
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        updateListeners();
    }

    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.proximitytest.client.ProximityCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new ProximityCellClientState();
        }

        ((ProximityCellClientState) cellClientState).setClientBounds(clientBounds);
        ((ProximityCellClientState) cellClientState).setServerBounds(serverBounds);

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);

        ProximityCellServerState pcss = (ProximityCellServerState) serverState;
        clientBounds = pcss.getClientBounds();
        serverBounds = pcss.getServerBounds();

        updateListeners();
    }

    private void updateListeners() {
        if (listenerRef != null) {
            proxRef.get().removeProximityListener(listenerRef.get());
            AppContext.getDataManager().removeObject(listenerRef.get());
            listenerRef = null;
        }

        if (proxRef != null && serverBounds != null && serverBounds.size() > 0) {
            ServerProximityListener listener = new ServerProximityListener(channelRef);
            listenerRef = AppContext.getDataManager().createReference(listener);
            proxRef.get().addProximityListener(listener,
                                               serverBounds.toArray(new BoundingVolume[0]));
        }
    }

    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        if (cellServerState == null) {
            cellServerState = new ProximityCellServerState();
        }

        ((ProximityCellServerState) cellServerState).setClientBounds(clientBounds);
        ((ProximityCellServerState) cellServerState).setServerBounds(serverBounds);

        return super.getServerState(cellServerState);
    }

    private static class ServerProximityListener
            implements ProximityListenerSrv, ManagedObject, Serializable
    {
        private ManagedReference<ChannelComponentMO> channelRef;

        public ServerProximityListener(ManagedReference<ChannelComponentMO> channelRef) {
            this.channelRef = channelRef;
        }

        public void viewEnterExit(boolean entered, CellID cell,
                                  CellID viewCellID, BoundingVolume proximityVolume,
                                  int proximityIndex)
        {
            String enter = entered ? "Enterred" : "Exited";
            logger.log(Level.WARNING, "[ServerProximityListner] " + enter +
                       " " + cell + " for view " + viewCellID + " index " +
                       proximityIndex);

            channelRef.get().sendAll(null,
                    new ProximityEnterExitMessage(proximityIndex, entered));
        }
    }
}
