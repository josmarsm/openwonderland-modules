/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.videoplayer.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDDialog;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerConstants;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerActions;
import org.jdesktop.wonderland.video.client.VideoPlayer.VideoPlayerState;

/**
 * Class to manage the selected tool.
 *
 * @author nsimpson
 */
public class VideoPlayerToolManager implements VideoPlayerToolActionListener {

    private static final Logger LOGGER =
            Logger.getLogger(VideoPlayerToolManager.class.getName());
    private static ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle");

    private VideoPlayerWindow videoPlayerWindow;
    private SharedMapCli statusMap;
    private HUDDialog openMediaComponent;
    private Executor actionExecutor = Executors.newSingleThreadExecutor();

    private VolumeControlPanel volumeControlPanel;
    private HUDComponent volumeComponent;

    VideoPlayerToolManager(VideoPlayerWindow videoPlayerWindow) {
        this.videoPlayerWindow = videoPlayerWindow;
        createOpenMediaDialog();
        createVolumeComponent();
    }

    public void setSSC(SharedStateComponent ssc) {
        if (ssc == null) {
            Thread.dumpStack();
        } else {
            statusMap = ssc.get(VideoPlayerConstants.STATUS_MAP);
        }
    }

    /*
     ** VideoPlayerToolActionListener methods
     */
    /**
     * Toggle the display of the video player from in-world to on-HUD
     */
    public void toggleHUDAction() {
        if (videoPlayerWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            videoPlayerWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            videoPlayerWindow.setDisplayMode(DisplayMode.HUD);
        }
        videoPlayerWindow.showControls(true);
    }

    /**
     * Creates the dialog used to specify the URL of a video
     */
    private void createOpenMediaDialog() {
        if (openMediaComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            openMediaComponent = mainHUD.createDialog(BUNDLE.getString("OPEN_VIDEO:"));
            openMediaComponent.setPreferredLocation(Layout.CENTER);
            mainHUD.addComponent(openMediaComponent);
            openMediaComponent.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("ok")) {
                        String url = openMediaComponent.getValue();
                        if ((url != null) && (url.length() > 0)) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    openMediaComponent.setVisible(false);
                                    openMedia(openMediaComponent.getValue());
                                }
                            });
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                openMediaComponent.setVisible(false);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Creates the dialog used to specify the URL of a video
     */
    private void createVolumeComponent() {
        if (volumeComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            volumeControlPanel = new VolumeControlPanel();
            volumeComponent = mainHUD.createComponent(volumeControlPanel);
            volumeComponent.setPreferredLocation(Layout.CENTER);
            mainHUD.addComponent(volumeComponent);
            volumeControlPanel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("done")) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                volumeComponent.setVisible(false);
                            }
                        });
                    } else if (e.getPropertyName().equals("volume")) {
                        videoPlayerWindow.setVolume((Float) e.getNewValue());
                    }
                }
            });
        }
    }

    public void setMedia(final String mediaURI) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                openMediaComponent.setValue(mediaURI);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void openMediaAction() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                openMediaComponent.setVisible(true);
            }
        });
    }

    /**
     * Open video media
     * @param mediaURI the URI of the video media
     */
    public void openMedia(String mediaURI) {
        if (videoPlayerWindow.isSynced()) {
            statusMap.put(VideoPlayerConstants.MEDIA_URI, SharedString.valueOf(mediaURI));
        } else {
            videoPlayerWindow.openMedia(mediaURI);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void playAction() {
        if (videoPlayerWindow.isPlayable()) {
            actionExecutor.execute(new Runnable() {

                public void run() {
                    if (videoPlayerWindow.isSynced()) {
                        double position = videoPlayerWindow.getPosition();
                        if (videoPlayerWindow.getState() == VideoPlayerState.STOPPED) {
                            // loop from the start if we were at the end of
                            // the video
                            position = 0d;
                        }

                        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(position)));
                        statusMap.put(VideoPlayerConstants.PLAYER_STATE, SharedString.valueOf(VideoPlayerActions.PLAY.name()));
                    } else {
                        videoPlayerWindow.play();
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pauseAction() {
        if (videoPlayerWindow.isPlayable()) {
            actionExecutor.execute(new Runnable() {

                public void run() {
                    if (videoPlayerWindow.isSynced()) {
                        // pause right now for better interactive response and
                        // so we send the correct pause position to other clients
                        videoPlayerWindow.pause();
                        double current = videoPlayerWindow.getPosition();
                        LOGGER.fine("pausing at position: " + current);
                        statusMap.put(VideoPlayerConstants.PLAYER_STATE, SharedString.valueOf(VideoPlayerActions.PAUSE.name()));
                        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(current)));
                    } else {
                        videoPlayerWindow.pause();
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopAction() {
        if (videoPlayerWindow.isPlayable()) {
            actionExecutor.execute(new Runnable() {

                public void run() {
                    if (videoPlayerWindow.isSynced()) {
                        statusMap.put(VideoPlayerConstants.PLAYER_STATE, SharedString.valueOf(VideoPlayerActions.STOP.name()));
                        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(0.0d)));
                    } else {
                        videoPlayerWindow.stop();
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void rewindAction() {
        if (videoPlayerWindow.isPlayable()) {
            actionExecutor.execute(new Runnable() {

                public void run() {
                    if (videoPlayerWindow.isSynced()) {
                        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(videoPlayerWindow.getPosition() - 10d)));
                    } else {
                        videoPlayerWindow.rewind(10.0d);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void forwardAction() {
        if (videoPlayerWindow.isPlayable()) {
            actionExecutor.execute(new Runnable() {

                public void run() {
                    if (videoPlayerWindow.isSynced()) {
                        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(videoPlayerWindow.getPosition() + 10d)));
                    } else {
                        videoPlayerWindow.forward(10.0d);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setPositionAction(final double position) {
        if (videoPlayerWindow.isPlayable()) {
            actionExecutor.execute(new Runnable() {
                public void run() {
                    if (videoPlayerWindow.isSynced()) {
                        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(Double.toString(position)));
                    } else {
                        videoPlayerWindow.setPosition(position);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void syncAction() {
        actionExecutor.execute(new Runnable() {

            public void run() {
                if (videoPlayerWindow.isSynced()) {
                    // synced -> unsynced
                    videoPlayerWindow.sync(false);
                } else {
                    // unsynced -> synced
                    videoPlayerWindow.sync(true);
                }
            }
        });
    }

    /**
     * {@inheritDOc}
     */
    public void volumeAction() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                volumeControlPanel.setVolume(videoPlayerWindow.getVolume());
                volumeComponent.setVisible(true);
            }
        });
    }

    public boolean isOnHUD() {
        return (videoPlayerWindow.getDisplayMode().equals(DisplayMode.HUD));
    }
}
