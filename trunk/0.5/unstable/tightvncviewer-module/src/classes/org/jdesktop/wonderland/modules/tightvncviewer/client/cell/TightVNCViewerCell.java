/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.tightvncviewer.client.cell;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.tightvncviewer.client.TightVNCViewerApp;
import org.jdesktop.wonderland.modules.tightvncviewer.client.TightVNCViewerWindow;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerConstants;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerState;
import org.jdesktop.wonderland.modules.tightvncviewer.common.cell.TightVNCViewerCellClientState;

/**
 * VNC viewer client cell
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class TightVNCViewerCell extends App2DCell implements SharedMapListenerCli {

    private static final Logger logger = Logger.getLogger(TightVNCViewerCell.class.getName());
    // The (singleton) window created by the VNC viewer app
    private TightVNCViewerWindow vncViewerWindow;
    // the VNC viewer application
    private TightVNCViewerApp vncViewerApp;
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
    private TightVNCViewerCellClientState clientState;
    private String vncServer;
    private int vncPort = -1;
    private String vncUsername;
    private String vncPassword;
    private String vncState;

    /**
     * Create an instance of TightVNCViewerCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public TightVNCViewerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the VNC viewer with parameters from the server.
     *
     * @param clientState the client state to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (TightVNCViewerCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
            case ACTIVE:
                // the cell is now visible
                if (increasing) {
                    if (this.getApp() == null) {
                        vncViewerApp = new TightVNCViewerApp(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/tightvncviewer/client/resources/Bundle").getString("VNC"), clientState.getPixelScale());
                        setApp(vncViewerApp);
                    }
                    // tell the app to be displayed in this cell.
                    vncViewerApp.addDisplayer(this);

                    // set initial position above ground
                    float placementHeight = clientState.getPreferredHeight();
                    placementHeight *= clientState.getPixelScale().y;
                    setInitialPlacementSize(new Vector2f(0f, placementHeight));

                    // this app has only one window, so it is always top-level
                    try {
                        vncViewerWindow = new TightVNCViewerWindow(this, vncViewerApp,
                                clientState.getPreferredWidth(), clientState.getPreferredHeight(),
                                true, clientState.getPixelScale());
                        vncViewerApp.setWindow(vncViewerWindow);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    // load the VNC viewer's status map
                    vncViewerWindow.setSSC(ssc);
                    statusMap = ssc.get(TightVNCViewerConstants.STATUS_MAP);
                    statusMap.addSharedMapListener(this);

                    // get the current VNC server and port number
                    vncServer = statusMap.get(TightVNCViewerConstants.VNC_SERVER,
                            SharedString.class).getValue();
                    vncPort = statusMap.get(TightVNCViewerConstants.VNC_PORT,
                            SharedInteger.class).getValue();
                    vncUsername = statusMap.get(TightVNCViewerConstants.VNC_USERNAME,
                            SharedString.class).getValue();
                    vncPassword = statusMap.get(TightVNCViewerConstants.VNC_PASSWORD,
                            SharedString.class).getValue();
                    vncState = statusMap.get(TightVNCViewerConstants.VNC_VIEWER_STATE,
                            SharedString.class).getValue();
                    if (vncState.equals(TightVNCViewerState.CONNECTED.name())) {
                        openConnection(vncServer, vncPort, vncUsername, vncPassword);
                    }

                    // get the current state of the viewer
                    SharedString state = statusMap.get(TightVNCViewerConstants.VNC_VIEWER_STATE,
                            SharedString.class);
                    this.handleConnectionStateChanged(null, null, state);

                    // both the app and the user want this window to be visible
                    vncViewerWindow.setVisibleApp(true);
                    vncViewerWindow.setVisibleUser(this, true);
                }
                break;
            case DISK:
                if (!increasing) {
                    // The cell is no longer visible
                    if (vncViewerWindow != null) {
                        vncViewerWindow.closeConnection();
                        vncViewerWindow.setVisibleApp(false);

                        App2D.invokeLater(new Runnable() {

                            public void run() {
                                vncViewerWindow.cleanup();
                                vncViewerWindow = null;
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    public void propertyChanged(SharedMapEventCli event) {
        SharedMapCli map = event.getMap();
        if (map.getName().equals(TightVNCViewerConstants.STATUS_MAP)) {
            // there's only one map, a map containing the state of the viewer,
            // its key determines what changed:
            //
            // VNC_SERVER: the address of the VNC server
            // VNC_PORT: the port number of the VNC server
            // VNC_VIEWER_STATE: whether the server is connected or not
            //
            // newData specifies the new value of the key
            // note that there's only one property change processed at a time

            handleStatusChange(event.getPropertyName(), event.getOldValue(),
                    event.getNewValue());
        } else {
            logger.warning("unrecognized shared map: " + map.getName());
        }
    }

    private void handleStatusChange(String key, SharedData oldData, SharedData newData) {
        if (key.equals(TightVNCViewerConstants.VNC_SERVER)) {
            // specifying a new VNC server
            vncServer = ((SharedString) newData).getValue();
            logger.fine("cell received VNC server: " + vncServer);
        } else if (key.equals(TightVNCViewerConstants.VNC_PORT)) {
            // specifying a new VNC port
            vncPort = ((SharedInteger) newData).getValue();
            logger.fine("cell received VNC port: " + vncPort);
        } else if (key.equals(TightVNCViewerConstants.VNC_USERNAME)) {
            // specifying the VNC username
            vncUsername = ((SharedString) newData).getValue();
            logger.fine("cell received VNC username: " + vncUsername);
        } else if (key.equals(TightVNCViewerConstants.VNC_PASSWORD)) {
            // specifying a the VNC password
            logger.fine("cell received VNC password");
            vncPassword = ((SharedString) newData).getValue();
            logger.fine("cell received VNC port: " + vncPassword);
        } else if (key.equals(TightVNCViewerConstants.VNC_VIEWER_STATE)) {
            // state changed (connected/disconnected)
            logger.fine("cell received state change: " + ((SharedString) newData).getValue());
            handleConnectionStateChanged(key, oldData, newData);
        } else {
            logger.warning("unhandled status change event: " + key);
        }
    }

    private void openConnection(String server, int port, String username, String password) {
        if (vncViewerWindow != null) {
            if ((server != null)  && (server.length() > 0) && (port != -1)) {
                logger.info("connecting to VNC server: " + vncServer + ":" + vncPort);
                vncViewerWindow.openConnection(server, port, username, password);
            }
        }
    }

    private void closeConnection() {
        if (vncViewerWindow != null) {
            logger.info("disconnecting from VNC server");
            vncViewerWindow.closeConnection();
            vncServer = null;
            vncPort = -1;
        }
    }

    private void handleConnectionStateChanged(String key, SharedData oldData, SharedData newData) {
        if ((newData != null) && vncViewerWindow.isSynced()) {
            String state = ((SharedString) newData).getValue();
            logger.fine("handle state change: " + state);
            if (state.equals(TightVNCViewerState.CONNECTED.name())) {
                // connected
                if ((vncServer != null) && (vncServer.length() > 0) && (vncPort != -1)) {
                    // only connect if server and port number have been specified
                    openConnection(vncServer, vncPort, vncUsername, vncPassword);
                }
            } else if (state.equals(TightVNCViewerState.DISCONNECTED.name())) {
                // disconnected
                closeConnection();
            }
        }
    }
}
