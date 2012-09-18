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
package org.jdesktop.wonderland.modules.webcamviewer.server.cell;

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
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapEventSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerConstants;
import org.jdesktop.wonderland.modules.webcamviewer.common.cell.WebcamViewerCellClientState;
import org.jdesktop.wonderland.modules.webcamviewer.common.cell.WebcamViewerCellServerState;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerState;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell associated with a Webcam viewer.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class WebcamViewerCellMO extends App2DCellMO implements SharedMapListenerSrv {

    private static final Logger logger = Logger.getLogger(WebcamViewerCellMO.class.getName());
    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;
    private ManagedReference<SharedMapSrv> statusMapRef;
    // the preferred width
    private int preferredWidth;
    // the preferred height
    private int preferredHeight;
    // whether to decorate the window with a frame
    private boolean decorated;
    // the URI of the currently connected webcam
    private String cameraURI;
    // the username used to access the webcam
    private String username;
    // the password used to access the webcam
    private String password;
    // the current state of the player (playing, paused, stopped)
    private WebcamViewerState playerState = WebcamViewerState.STOPPED;

    public WebcamViewerCellMO() {
        super();
        addComponent(new SharedStateComponentMO(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.webcamviewer.client.cell.WebcamViewerCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            // get or create the shared maps we use
            SharedMapSrv statusMap = sscRef.get().get(WebcamViewerConstants.STATUS_MAP);
            statusMap.addSharedMapListener(this);

            // put the current status
            statusMap.put(WebcamViewerConstants.CAMERA_USERNAME, SharedString.valueOf(username));
            statusMap.put(WebcamViewerConstants.CAMERA_PASSWORD, SharedString.valueOf(password));
            statusMap.put(WebcamViewerConstants.CAMERA_URI, SharedString.valueOf(cameraURI));
            statusMap.put(WebcamViewerConstants.CAMERA_STATE, SharedString.valueOf(playerState.name()));

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
            cellClientState = new WebcamViewerCellClientState(pixelScale);
        }
        ((WebcamViewerCellClientState) cellClientState).setPreferredWidth(preferredWidth);
        ((WebcamViewerCellClientState) cellClientState).setPreferredHeight(preferredHeight);
        ((WebcamViewerCellClientState) cellClientState).setDecorated(decorated);

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new WebcamViewerCellServerState();
        }
        ((WebcamViewerCellServerState) state).setUsername(username);
        ((WebcamViewerCellServerState) state).setPassword(password);
        ((WebcamViewerCellServerState) state).setCameraURI(cameraURI);
        // TODO: set state
        ((WebcamViewerCellServerState) state).setDecorated(decorated);
        ((WebcamViewerCellServerState) state).setPreferredWidth(preferredWidth);
        ((WebcamViewerCellServerState) state).setPreferredHeight(preferredHeight);

        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);
        WebcamViewerCellServerState state = (WebcamViewerCellServerState) serverState;

        cameraURI = state.getCameraURI();
        username = state.getUsername();
        password = state.getPassword();
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
        if (map.getName().equals(WebcamViewerConstants.STATUS_MAP)) {
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

        if (key.equals(WebcamViewerConstants.CAMERA_URI)) {
            cameraURI = ((SharedString) newData).getValue();
        } else if (key.equals(WebcamViewerConstants.CAMERA_USERNAME)) {
            username = ((SharedString) newData).getValue();
        } else if (key.equals(WebcamViewerConstants.CAMERA_PASSWORD)) {
            password = ((SharedString) newData).getValue();
        } else if (key.equals(WebcamViewerConstants.CAMERA_STATE)) {
            String statusStr = ((SharedString) newData).getValue();
            playerState = WebcamViewerState.valueOf(statusStr);
            logger.finest("handle player state change: " + playerState);
        }

        return true;
    }
}
