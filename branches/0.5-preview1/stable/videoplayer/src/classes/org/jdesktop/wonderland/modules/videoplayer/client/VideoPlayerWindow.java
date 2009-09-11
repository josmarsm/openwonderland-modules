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
package org.jdesktop.wonderland.modules.videoplayer.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDDialog;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerState;

/**
 * A Video Player window
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class VideoPlayerWindow extends WindowSwing {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(VideoPlayerWindow.class.getName());
    /** The cell in which this window is displayed. */
    private VideoPlayerCell cell;
    private VideoPlayerPanel videoPanel;
    private VideoPlayerToolManager toolManager;
    private VideoPlayerControlPanel controls;
    private HUDComponent controlComponent;
    private HUDDialog openDialogComponent;
    private SharedStateComponent ssc;
    private boolean synced = true;
    private DisplayMode displayMode;

    /**
     * Create a new instance of a VideoPlayerWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public VideoPlayerWindow(VideoPlayerCell cell, App2D app, int width, int height,
            boolean topLevel, Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        this.cell = cell;
        setTitle("Video Player");

        videoPanel = new VideoPlayerPanel(this);
        setComponent(videoPanel);

        setDisplayMode(DisplayMode.WORLD);
        showControls(false);
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        videoPanel.setSSC(ssc);
    }

    /**
     * Open a new media source
     */
    public void openMedia() {
        if (openDialogComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            openDialogComponent = mainHUD.createDialog("Open Video:");
            openDialogComponent.setPreferredLocation(Layout.CENTER);
            mainHUD.addComponent(openDialogComponent);
            openDialogComponent.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("ok")) {
                        String url = openDialogComponent.getValue();
                        if ((url != null) && (url.length() > 0)) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    openDialogComponent.setVisible(false);
                                    openMedia(openDialogComponent.getValue());
                                }
                            });
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                openDialogComponent.setVisible(false);
                            }
                        });
                    }
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                openDialogComponent.setVisible(true);
            }
        });
    }

    public void openMedia(String mediaURI) {
        videoPanel.openMedia(mediaURI);
    }

    public String getMediaURI() {
        return videoPanel.getMediaURI();
    }

    /**
     * Set the position (time) within the media
     * @param position the position in seconds
     */
    public void setMediaPosition(double position) {
        videoPanel.setMediaPosition(position);
    }

    /**
     * Get the currentposition (time) within the media
     * @return the media position in seconds
     */
    public double getMediaPosition() {
        return videoPanel.getMediaPosition();
    }

    /**
     * Rewind the media
     */
    public void rewind() {
        videoPanel.rewind();
    }

    /**
     * Play the media
     */
    public void play() {
        videoPanel.play();
        controls.setMode(VideoPlayerState.PLAYING);
    }

    /**
     * Gets whether the media is currently playing
     * @return true if the media is playing, false otherwise
     */
    public boolean isPlaying() {
        return videoPanel.isPlaying();
    }

    /**
     * Pause the media
     */
    public void pause() {
        videoPanel.pause();
        controls.setMode(VideoPlayerState.PAUSED);
    }

    /**
     * Stop playing the media
     */
    public void stop() {
        videoPanel.stop();
        controls.setMode(VideoPlayerState.STOPPED);
    }

    /**
     * Fast forward the media
     */
    public void fastForward() {
        videoPanel.fastForward();
    }

    /**
     * Synchronize with the shared state
     */
    public void sync() {
        videoPanel.sync();
    }

    /**
     * Unsynchronize from the shared state
     */
    public void unsync() {
        videoPanel.unsync();
    }

    /**
     * Resynchronize the state of the cell.
     *
     * A resync is necessary when the cell transitions from INACTIVE to
     * ACTIVE cell state, where the cell may have missed state synchronization
     * messages while in the INACTIVE state.
     *
     * Resynchronization is only performed if the cell is currently synced.
     * To sync an unsynced cell, call sync(true) instead.
     */
    public void resync() {
        if (synced) {
            synced = false;
            sync(true);
        }
    }

    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("video player: unsynced");
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("video player: synced");
        }
    }

    public boolean isSynced() {
        return synced;
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
     *
     * @param visible true to show the controls, hide to hide them
     */
    public void showControls(final boolean visible) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                if (controlComponent == null) {
                    // create control panel
                    controls = new VideoPlayerControlPanel(VideoPlayerWindow.this);

                    // add event listeners
                    toolManager = new VideoPlayerToolManager(VideoPlayerWindow.this);
                    controls.addCellMenuListener(toolManager);

                    // create HUD control panel
                    controlComponent = mainHUD.createComponent(controls, cell);
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
}
