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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
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
    private WebcamViewerConnectPanel connectCameraPanel;
    private HUDComponent connectCameraComponent;

    WebcamViewerToolManager(WebcamViewerWindow webcamViewerWindow) {
        this.webcamViewerWindow = webcamViewerWindow;
    }

    public void setSSC(SharedStateComponent ssc) {
        if (ssc != null) {
            statusMap = ssc.get(WebcamViewerConstants.STATUS_MAP);
        }
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
            connectCameraPanel = new WebcamViewerConnectPanel();
            connectCameraComponent = mainHUD.createComponent(connectCameraPanel);
            //Set the textfields of the panel according to the shared state
            SharedString cameraURI = (SharedString) statusMap.get(WebcamViewerConstants.CAMERA_URI);
            connectCameraPanel.setCameraURI(cameraURI.getValue());
            SharedString cameraPassword = (SharedString) statusMap.get(WebcamViewerConstants.CAMERA_PASSWORD);
            connectCameraPanel.setPassword(cameraPassword.getValue());
            SharedString cameraUsername = (SharedString) statusMap.get(WebcamViewerConstants.CAMERA_USERNAME);
            connectCameraPanel.setUsername(cameraUsername.getValue());
            connectCameraComponent.setPreferredLocation(Layout.CENTER);
            connectCameraComponent.setName(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("CONNECT_TO_WEBCAM"));
            connectCameraPanel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    connectCameraComponent.setVisible(false);
                    if (e.getID() == (ActionEvent.ACTION_PERFORMED) && e.getActionCommand().equals("OK")) {
                        // open connection
                        String cameraURI = connectCameraPanel.getCameraURI();
                        String username = connectCameraPanel.getUsername();
                        String password = connectCameraPanel.getPassword();

                        connectCamera(cameraURI, username, password);
                    }
                }
            });

            mainHUD.addComponent(connectCameraComponent);
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
    public void connectCamera(String cameraURI, String username, String password) {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.CAMERA_USERNAME, SharedString.valueOf(username));
            statusMap.put(WebcamViewerConstants.CAMERA_PASSWORD, SharedString.valueOf(password));
            statusMap.put(WebcamViewerConstants.CAMERA_URI, SharedString.valueOf(cameraURI));
        } else {
            webcamViewerWindow.connectCamera(cameraURI, username, password);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void play() {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.CAMERA_STATE, SharedString.valueOf(WebcamViewerState.PLAYING.name()));
        } else {
            webcamViewerWindow.play();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pause() {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.CAMERA_STATE, SharedString.valueOf(WebcamViewerState.PAUSED.name()));
        } else {
            webcamViewerWindow.pause();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        if (webcamViewerWindow.isSynced()) {
            statusMap.put(WebcamViewerConstants.CAMERA_STATE, SharedString.valueOf(WebcamViewerState.STOPPED.name()));
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

            String state = ((SharedString) statusMap.get(WebcamViewerConstants.CAMERA_STATE)).getValue();
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
