/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.tightvncviewer.server.cell;

import com.jme.math.Vector2f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapEventSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerConstants;
import org.jdesktop.wonderland.modules.tightvncviewer.common.cell.TightVNCViewerCellClientState;
import org.jdesktop.wonderland.modules.tightvncviewer.common.cell.TightVNCViewerCellServerState;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerState;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell associated with a VNC viewer.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class TightVNCViewerCellMO extends App2DCellMO implements SharedMapListenerSrv {

    private static final Logger logger = Logger.getLogger(TightVNCViewerCellMO.class.getName());
    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;
    private ManagedReference<SharedMapSrv> statusMapRef;
    // the preferred width
    private int preferredWidth;
    // the preferred height
    private int preferredHeight;
    // whether to decorate the window with a frame
    private boolean decorated;
    // the currently connected VNC server
    private String vncServer;
    // the port number of the VNC server
    private int vncPort;
    // the username
    private String vncUsername;
    // the password
    private String vncPassword;
    // the current state of the viewer (connected, disconnected)
    private TightVNCViewerState viewerState = TightVNCViewerState.CONNECTED;

    public TightVNCViewerCellMO() {
        super();
        addComponent(new SharedStateComponentMO(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.tightvncviewer.client.cell.TightVNCViewerCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            // get or create the shared maps we use
            SharedMapSrv statusMap = sscRef.get().get(TightVNCViewerConstants.STATUS_MAP);
            statusMap.addSharedMapListener(this);

            // put the current status
            statusMap.put(TightVNCViewerConstants.VNC_SERVER, SharedString.valueOf(vncServer));
            statusMap.put(TightVNCViewerConstants.VNC_PORT, SharedInteger.valueOf(vncPort));
            statusMap.put(TightVNCViewerConstants.VNC_USERNAME, SharedString.valueOf(vncUsername));
            statusMap.put(TightVNCViewerConstants.VNC_PASSWORD, SharedString.valueOf(vncPassword));
            statusMap.put(TightVNCViewerConstants.VNC_VIEWER_STATE, SharedString.valueOf(viewerState.name()));

            statusMapRef = AppContext.getDataManager().createReference(statusMap);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
            WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TightVNCViewerCellClientState();
        }
        ((TightVNCViewerCellClientState) cellClientState).setPreferredWidth(preferredWidth);
        ((TightVNCViewerCellClientState) cellClientState).setPreferredHeight(preferredHeight);
        ((TightVNCViewerCellClientState) cellClientState).setDecorated(decorated);

        CellClientState state = super.getClientState(cellClientState, clientID, capabilities);
        // override default pixel scale by 50% to make VNC window easier to read
        Vector2f scale = ((TightVNCViewerCellClientState) cellClientState).getPixelScale();
        scale.x += 0.5f * scale.x;
        scale.y += 0.5f * scale.y;
        ((TightVNCViewerCellClientState) state).setPixelScale(scale);

        return state;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new TightVNCViewerCellServerState();
        }
        ((TightVNCViewerCellServerState) state).setVNCServer(vncServer);
        ((TightVNCViewerCellServerState) state).setVNCPort(vncPort);
        ((TightVNCViewerCellServerState) state).setVNCUsername(vncUsername);
        ((TightVNCViewerCellServerState) state).setVNCPassword(vncPassword);
        ((TightVNCViewerCellServerState) state).setDecorated(decorated);
        ((TightVNCViewerCellServerState) state).setPreferredWidth(preferredWidth);
        ((TightVNCViewerCellServerState) state).setPreferredHeight(preferredHeight);

        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);
        TightVNCViewerCellServerState state = (TightVNCViewerCellServerState) serverState;

        vncServer = state.getVNCServer();
        vncPort = state.getVNCPort();
        vncUsername = state.getVNCUsername();
        vncPassword = state.getVNCPassword();
        preferredWidth = state.getPreferredWidth();
        preferredHeight = state.getPreferredHeight();
        decorated = state.getDecorated();
        pixelScale = new Vector2f(state.getPixelScaleX(), state.getPixelScaleY());
    }

    /**
     * {@inheritDoc}
     */
    public boolean propertyChanged(SharedMapEventSrv event) {
        SharedMapSrv map = event.getMap();
        if (map.getName().equals(TightVNCViewerConstants.STATUS_MAP)) {
            return handleStatusChange(event.getSenderID(), event.getPropertyName(),
                    event.getOldValue(), event.getNewValue());
        } else {
            logger.warning("unrecognized shared map: " + map.getName());
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    private boolean handleStatusChange(WonderlandClientID sourceID,
            String key, SharedData oldData, SharedData newData) {
        if (key.equals(TightVNCViewerConstants.VNC_SERVER)) {
            vncServer = ((SharedString) newData).getValue();
        } else if (key.equals(TightVNCViewerConstants.VNC_PORT)) {
            vncPort = ((SharedInteger) newData).getValue();
        } else if (key.equals(TightVNCViewerConstants.VNC_USERNAME)) {
            vncUsername = ((SharedString) newData).getValue();
        } else if (key.equals(TightVNCViewerConstants.VNC_PASSWORD)) {
            vncPassword = ((SharedString) newData).getValue();
        } else if (key.equals(TightVNCViewerConstants.VNC_VIEWER_STATE)) {
            String statusStr = ((SharedString) newData).getValue();
            viewerState = TightVNCViewerState.valueOf(statusStr);
        }

        return true;
    }
}
