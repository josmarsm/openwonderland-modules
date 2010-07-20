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
package org.jdesktop.wonderland.modules.tightvncviewer.client;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerConstants;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerState;

/**
 * Class to manage the selected tool.
 *
 * @author nsimpson
 */
public class TightVNCViewerToolManager implements TightVNCViewerToolListener {

    private static final Logger logger = Logger.getLogger(TightVNCViewerToolManager.class.getName());
    private TightVNCViewerWindow vncViewerWindow;
    private SharedMapCli statusMap;
    private Executor actionExecutor = Executors.newSingleThreadExecutor();

    TightVNCViewerToolManager(TightVNCViewerWindow vncViewerWindow) {
        this.vncViewerWindow = vncViewerWindow;
    }

    public void setSSC(SharedStateComponent ssc) {
        if (ssc == null) {
            Thread.dumpStack();
        } else {
            statusMap = ssc.get(TightVNCViewerConstants.STATUS_MAP);
        }
    }

    // TightVNCViewerToolListener methods
    /**
     * Toggle the display of the VNC viewer from in-world to on-HUD
     */
    public void toggleHUD() {
        if (vncViewerWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            vncViewerWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            vncViewerWindow.setDisplayMode(DisplayMode.HUD);
        }
        vncViewerWindow.showControls(true);
    }

    /**
     * {@inheritDoc}
     */
    public void openConnection() {
        vncViewerWindow.openConnection();
    }

    /**
     * {@inheritDoc}
     */
    public void closeConnection() {
        actionExecutor.execute(new Runnable() {

            public void run() {
                if (vncViewerWindow.isSynced()) {
                    //statusMap.put(TightVNCViewerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(vncViewerWindow.getPosition())));
                    statusMap.put(TightVNCViewerConstants.VNC_VIEWER_STATE, SharedString.valueOf(TightVNCViewerState.DISCONNECTED.name()));
                } else {
                    vncViewerWindow.closeConnection();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void sendCtrlAltDel() {
        vncViewerWindow.sendCtrlAltDel();
    }
    
    /**
     * {@inheritDoc}
     */
    public void sync() {
        actionExecutor.execute(new Runnable() {

            public void run() {
                if (vncViewerWindow.isSynced()) {
                    // synced -> unsynced
                    vncViewerWindow.sync(false);
                } else {
                    // unsynced -> synced
                    vncViewerWindow.sync(true);
                    String server = ((SharedString) statusMap.get(TightVNCViewerConstants.VNC_SERVER)).getValue();
                    int port = ((SharedInteger) statusMap.get(TightVNCViewerConstants.VNC_PORT)).getValue();
                    String username = ((SharedString) statusMap.get(TightVNCViewerConstants.VNC_USERNAME)).getValue();
                    String password = ((SharedString) statusMap.get(TightVNCViewerConstants.VNC_PASSWORD)).getValue();
                    logger.fine("sync: VNC server is: " + server + ":" + port);

                    String state = ((SharedString) statusMap.get(TightVNCViewerConstants.VNC_VIEWER_STATE)).getValue();
                    logger.fine("sync: state is: " + state);

                    if (state.equals(TightVNCViewerState.CONNECTED.name())) {
                        vncViewerWindow.openConnection(server, port, username, password);
                    } else if (state.equals(TightVNCViewerState.DISCONNECTED.name())) {
                        vncViewerWindow.closeConnection();
                    }
                }
            }
        });
    }

    public boolean isOnHUD() {
        return (vncViewerWindow.getDisplayMode().equals(DisplayMode.HUD));
    }
}
