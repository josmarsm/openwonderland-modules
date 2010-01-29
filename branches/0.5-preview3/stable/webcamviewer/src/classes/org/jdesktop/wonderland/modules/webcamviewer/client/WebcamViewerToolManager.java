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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDDialog;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerConstants;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerState;

/**
 * Class to manage the selected tool.
 *
 * @author nsimpson
 */
public class WebcamViewerToolManager implements WebcamViewerToolListener {

    private static final Logger logger = Logger.getLogger(WebcamViewerToolManager.class.getName());
    private WebcamViewerWindow webcamViewerWindow;
    private SharedMapCli statusMap;
    private HUDDialog connectCameraComponent;

    WebcamViewerToolManager(WebcamViewerWindow webcamViewerWindow) {
        this.webcamViewerWindow = webcamViewerWindow;
    }

    public void setSSC(SharedStateComponent ssc) {
        statusMap = ssc.get(WebcamViewerConstants.STATUS_MAP);
    }

    // WebcamViewerToolListener methods
    /**
     * Toggle the display of the webcam viewer from in-world to on-HUD
     */
    public void toggleHUD() {
        if (webcamViewerWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            webcamViewerWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            webcamViewerWindow.setDisplayMode(DisplayMode.HUD);
        }
        webcamViewerWindow.showControls(true);
    }

    /**
     * {@inheritDoc}
     */
    public void connectCamera() {
        if (connectCameraComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            connectCameraComponent = mainHUD.createDialog(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("CONNECT_TO_WEBCAM:"));
            connectCameraComponent.setPreferredLocation(Layout.CENTER);
            connectCameraComponent.setValue(webcamViewerWindow.getCameraURI());
            mainHUD.addComponent(connectCameraComponent);
            connectCameraComponent.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("ok")) {
                        String url = connectCameraComponent.getValue();
                        if ((url != null) && (url.length() > 0)) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    connectCameraComponent.setVisible(false);
                                    connectCamera(connectCameraComponent.getValue());
                                }
                            });
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                connectCameraComponent.setVisible(false);
                            }
                        });
                    }
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                connectCameraComponent.setVisible(true);
            }
        });
    }

    /**
     * Connect to a specific webcam
     * @param cameraURI the URI of the webcam
     */
    public void connectCamera(String cameraURI) {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.CAMERA_URI, SharedString.valueOf(cameraURI));
        } else {
            webcamViewerWindow.connectCamera(cameraURI);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void play() {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.PLAYER_STATE, SharedString.valueOf(WebcamViewerState.PLAYING.name()));
        } else {
            webcamViewerWindow.play();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pause() {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.PLAYER_STATE, SharedString.valueOf(WebcamViewerState.PAUSED.name()));
        } else {
            webcamViewerWindow.pause();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.PLAYER_STATE, SharedString.valueOf(WebcamViewerState.STOPPED.name()));
        } else {
            webcamViewerWindow.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sync() {
        if (webcamViewerWindow.isSynced()) {
            // synced -> unsynced
            webcamViewerWindow.sync(false);
        } else {
            // unsynced -> synced
            webcamViewerWindow.sync(true);
            String cameraURI = ((SharedString) statusMap.get(WebcamViewerConstants.CAMERA_URI)).getValue();
            logger.fine("sync: webcam is: " + cameraURI);

            String state = ((SharedString) statusMap.get(WebcamViewerConstants.PLAYER_STATE)).getValue();
            logger.fine("sync: state is: " + state);

            if (state.equals(WebcamViewerState.PAUSED.name())) {
                webcamViewerWindow.pause();
            } else if (state.equals(WebcamViewerState.STOPPED.name())) {
                webcamViewerWindow.stop();
            } else if (state.equals(WebcamViewerState.PLAYING.name())) {
                webcamViewerWindow.play();
            }
        }
    }

    public boolean isOnHUD() {
        return (webcamViewerWindow.getDisplayMode().equals(DisplayMode.HUD));
    }
}
