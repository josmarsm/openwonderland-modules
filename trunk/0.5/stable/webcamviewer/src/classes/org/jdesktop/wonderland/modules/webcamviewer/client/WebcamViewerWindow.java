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

import com.charliemouse.cambozola.shared.CamStream;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.EventQueue;
import java.awt.Image;
import java.net.URI;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDMessage;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.webcamviewer.client.cell.WebcamViewerCell;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerConstants;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerState;

/**
 * A Webcam Viewer window.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class WebcamViewerWindow extends WindowSwing implements WebcamViewerToolListener {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(WebcamViewerWindow.class.getName());
    /** The cell in which this window is displayed. */
    private WebcamViewerCell cell;
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
    private String cameraURI;
    private String username;
    private String password;
    private CamStream camStream;
    private WebcamViewerPanel webcamViewerPanel;
    private WebcamViewerToolManager toolManager;
    private WebcamViewerControls controls;
    private HUDMessage messageComponent;
    private HUDComponent controlComponent;
    private boolean synced = true;
    private DisplayMode displayMode;
    private FPSMonitor fpsMonitor;
    private Thread fpsMonitorThread;

    /**
     * Create a new instance of a WebcamViewerWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public WebcamViewerWindow(WebcamViewerCell cell, App2D app, int width, int height,
            boolean topLevel, Vector2f pixelScale)
            throws InstantiationException {
        super(app, Type.PRIMARY, width, height, topLevel, new Vector2f(0.02f, 0.02f));
        this.cell = cell;
        setTitle(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("WEBCAM_VIEWER"));
        webcamViewerPanel = new WebcamViewerPanel(this);
        setComponent(webcamViewerPanel);
        initHUD();

        setDisplayMode(DisplayMode.HUD);
        showControls(false);
    }

    private void initHUD() {
        if (messageComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            messageComponent = mainHUD.createMessage("");
            messageComponent.setPreferredLocation(Layout.NORTHEAST);
            messageComponent.setDecoratable(false);
            mainHUD.addComponent(messageComponent);
        }
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    public void showHUDMessage(final String message, final int timeout) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                messageComponent.setMessage(message);
                messageComponent.setVisible(true);
                if (timeout > 0) {
                    messageComponent.setVisible(false, timeout);
                }
            }
        });
    }

    public void showHUDMessage(String message) {
        showHUDMessage(message, 0);
    }

    public void hideHUDMessage() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                messageComponent.setVisible(false);
            }
        });
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        // load the video player's status map
        statusMap = ssc.get(WebcamViewerConstants.STATUS_MAP);
    }

    /**
     * Gets the URI of the camera
     * @return the URI of the currently connected camera
     */
    public String getCameraURI() {
        return cameraURI;
    }

    public void connectCamera() {
        connectCamera(cameraURI, username, password);
    }

    /**
     * Open connection to camera
     * @param camera the URI of the camera to open
     */
    public void connectCamera(final String cameraURI, final String username, final String password) {
        this.cameraURI = cameraURI;
        this.username = username;
        this.password = password;

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    ///logger.fine("connecting webcam: " + cameraURI + ", username: " + username + ", password: " + password);
                    try {
                        URI camURI = new URI(cameraURI);
                        camURI = new URI(camURI.getScheme(),
                                (username != null) ? username + ":" + password : null,
                                camURI.getHost(), Integer.valueOf(camURI.getPort()),
                                camURI.getPath(), camURI.getQuery(), null);

                        camStream = new CamStream(camURI.toURL(), "WebcamViewer", null, 0, 0, webcamViewerPanel, false);
                        camStream.addImageChangeListener(webcamViewerPanel);
                        camStream.start();
                        if (fpsMonitor == null) {
                            fpsMonitor = new FPSMonitor();
                            fpsMonitorThread = new Thread(fpsMonitor);
                            fpsMonitorThread.start();
                        }

                        webcamViewerPanel.showSource(cameraURI);
                        webcamViewerPanel.setConnected(true);
                        showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("PLAY"), 3000);
                        controls.setMode(WebcamViewerState.PLAYING);
                    } catch (Exception e) {
                        logger.severe("invalid webcam URI: " + cameraURI + ": " + e.getMessage());
                        camStream = null;
                    }
                } catch (Exception e) {
                    logger.warning("error connecting to webcam: " + e.toString());
                    showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("UNABLE_TO_CONNECT_TO_WEBCAM"), 5000);
                    webcamViewerPanel.showSource(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("UNABLE_TO_CONNECT_TO_WEBCAM"));
                    webcamViewerPanel.setConnected(false);
                }
            }
        });
    }

    /**
     * Toggle the display of the webcam viewer from in-world to on-HUD
     */
    public void toggleHUD() {
    }

    /**
     * Play webcam video
     */
    public void play() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.info("play");
                connectCamera(cameraURI, username, password);
            }
        });
    }

    /**
     * Gets whether the webcam video is currently playing
     * @return true if the webcam is playing, false otherwise
     */
    public boolean isPlaying() {
        return ((camStream != null) && camStream.isAlive());
    }

    /**
     * Pause webcam video
     */
    public void pause() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.info("pause");
                if (camStream != null) {
                    camStream.unhook();
                    showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("PAUSE"), 2000);
                    controls.setMode(WebcamViewerState.PAUSED);
                    if (fpsMonitor != null) {
                        fpsMonitor.stop();
                        fpsMonitor = null;
                    }
                }
            }
        });
    }

    /**
     * Stop playing webcam video
     */
    public void stop() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.fine("stop");
                if (camStream != null) {
                    camStream.unhook();
                    showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("STOP"), 2000);
                    controls.setMode(WebcamViewerState.STOPPED);
                    if (fpsMonitor != null) {
                        fpsMonitor.stop();
                        fpsMonitor = null;
                    }
                }
            }
        });
    }

    /**
     * Clear the window
     */
    public void clear() {
        webcamViewerPanel.clear();
    }

    /**
     * Synchronize with the shared state
     */
    public void sync() {
        sync(true);
    }

    /**
     * Set the sync state
     * @param syncing true if re-syncing, false if unsyncing
     */
    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("webcam viewer: unsynced");
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("webcam viewer: synced");
        }
        controls.setSynced(syncing);
    }

    /**
     * Gets whether the application is currently synced with the shared
     * state
     * @return true if the application is synced, false otherwise
     */
    public boolean isSynced() {
        return synced;
    }

    private class FPSMonitor implements Runnable {

        private boolean running = false;
        private double fps = 0.0d;

        public void run() {
            running = true;

            while (running) {
                fps = 0.0d;
                if (camStream != null) {
                    fps = camStream.getFPS();
                }
                updateFPS(fps);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }

        public void stop() {
            webcamViewerPanel.showFPS(0.0);
            running = false;
        }

        private void updateFPS(double fps) {
            webcamViewerPanel.showFPS(fps);
        }
    }

    /**
     * Gets the current frame from the webcam video stream
     * @return the current frame as an Image
     */
    public Image getFrame() {
        Image frame = null;

        if (camStream != null) {
            frame = camStream.getCurrent();
        }
        return frame;
    }

    /**
     * Sets the display mode for the control panel to in-world or on-HUD
     * @param mode the control panel display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Gets the control panel display mode
     * @return the display mode of the control panel: in-world or on HUD
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Shows or hides the HUD controls.
     * The controls are shown in-world or on-HUD depending on the selected
     * DisplayMode.
     * @param visible true to show the controls, hide to hide them
     */
    public void showControls(final boolean visible) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                if (controlComponent == null) {
                    // create control panel
                    controls = createControls(WebcamViewerWindow.this);

                    // add event listeners
                    toolManager = createToolManager(WebcamViewerWindow.this);
                    toolManager.setSSC(ssc);
                    controls.addCellMenuListener(toolManager);

                    // create HUD control panel
                    controlComponent = mainHUD.createComponent(controls.getComponent(), cell);
                    controlComponent.setPreferredLocation(Layout.SOUTH);

                    // add HUD control panel to HUD
                    mainHUD.addComponent(controlComponent);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        // change visibility of controls
                        if (getDisplayMode() == DisplayMode.HUD) {
                            if (controlComponent.isWorldVisible()) {
                                controlComponent.setWorldVisible(false);
                            }
                            controlComponent.setVisible(visible);
                        } else {
                            controlComponent.setWorldLocation(new Vector3f(0.0f, -3.2f, 0.1f));
                            if (controlComponent.isVisible()) {
                                controlComponent.setVisible(false);
                            }
                            controlComponent.setWorldVisible(visible); // show world view
                        }

                        updateControls();
                    }
                });
            }
        });
    }

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }

    protected void updateControls() {
        controls.setSynced(isSynced());

        controls.setOnHUD(!toolManager.isOnHUD());
    }

    protected WebcamViewerControls createControls(WebcamViewerWindow window) {
        return new WebcamViewerControlPanel(window);
    }

    protected WebcamViewerToolManager createToolManager(WebcamViewerWindow window) {
        return new WebcamViewerToolManager(window);
    }
}
