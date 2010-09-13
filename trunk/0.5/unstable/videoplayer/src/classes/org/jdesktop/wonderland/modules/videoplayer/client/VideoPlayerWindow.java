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

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.BorderLayout;
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
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerConstants;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerActions;

/**
 * A Video Player window.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class VideoPlayerWindow extends WindowSwing implements VideoPlayer {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(VideoPlayerWindow.class.getName());
    /** The cell in which this window is displayed. */
    private VideoPlayerCell cell;
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
    protected static final float ACTIVE_FRAME_RATE = 10.0f;
    protected static final float INACTIVE_FRAME_RATE = 2.0f;
    private VideoPlayerPanel videoPlayerPanel;
    private VideoPlayerToolManager toolManager;
    private VideoPlayerControlPanel controls;
    private VideoMeter timeline;
    private HUDMessage messageComponent;
    private HUDComponent controlComponent;
    private boolean synced = true;
    private DisplayMode displayMode;
    private VideoPlayerImpl mediaPlayer = null;

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
        super(app, Type.PRIMARY, width, height, topLevel, pixelScale);
        this.cell = cell;
        setTitle(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("VIDEO_PLAYER"));

        videoPlayerPanel = new VideoPlayerPanel(this);
        setComponent(videoPlayerPanel);
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
        statusMap = ssc.get(VideoPlayerConstants.STATUS_MAP);
        toolManager.setSSC(ssc);
    }

    /**
     * Sets the play position offset from a specific time
     * @param position the play position in seconds
     * @param offset the offset from the play position in seconds
     */
    public void setPositionRelative(double position, double offset) {
        if (playerReady()) {
            // calculate the position and normalize it to media bounds:
            // 0.0 - media duration
            double absolute = getPositionRelative(position, offset);
            logger.info(((offset > 0) ? "fast forwarding" : "rewinding") + " to: " + absolute + "s");
            setPosition(absolute);
        }
    }

    /**
     * Gets a valid time within the media constrained to the media duration
     * 
     * @return a valid time in seconds within the media
     */
    public double getPositionRelative(double position, double offset) {
        double relative = 0.0d;

        if (playerReady()) {
            relative = position + offset;
            relative = relative > mediaPlayer.getDuration() ? mediaPlayer.getDuration()
                    : (relative < 0.0d ? 0.0d : relative);
        }

        return relative;
    }

    /**
     * Mutes the audio
     * @param muting true to mute, false to unmute
     */
    public void mute(boolean muting, boolean quietly) {
        if (playerReady()) {
            logger.info(((muting == true) ? "muting" : "unmuting"));
            if (muting) {
                mediaPlayer.mute();
            } else {
                mediaPlayer.unmute();
            }
            if (!quietly) {
                showHUDMessage(isMuted() ? "Muted" : "Unmuted", 3000);
            }
        }
    }

    /**
     * A convenience method to get whether the player is ready to play
     * @return true if the player is ready, false if not
     */
    private boolean playerReady() {
        return (mediaPlayer != null) && mediaPlayer.isPlayable();
    }

    /**
     * VideoPlayer methods
     */
    /**
     * Open video media
     * @param uri the URI of the video media to open
     */
    public void openMedia(final String uri) {
        logger.info("opening video: " + uri);

        if (!VideoPlayerCell.isVideoAvailable() || 
            uri == null || (uri.length() == 0))
        {
            // nothing to open
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = new VideoPlayerImpl();
            mediaPlayer.addTimeListener(timeline);

            mediaPlayer.addStateListener(new VideoStateListener() {

                public void mediaStateChanged(VideoPlayerState oldState, VideoPlayerState newState) {
                    logger.fine("player state changed: " + oldState + " to " + newState);
                    if (newState != oldState) {
                        switch (newState) {
                            case NO_MEDIA:
                                break;
                            case MEDIA_READY:
                                timeline.setDuration(mediaPlayer.getDuration());
                                videoPlayerPanel.showSource(mediaPlayer.getMedia());
                                videoPlayerPanel.setConnected(true);
                                videoPlayerPanel.resizeToFit(mediaPlayer.getFrameSize());
                                showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("LOADED"), 3000);
                                if (isSynced()) {
                                    double position = Double.valueOf(((SharedString) statusMap.get(VideoPlayerConstants.MEDIA_POSITION)).getValue());

                                    String state = ((SharedString) statusMap.get(VideoPlayerConstants.PLAYER_STATE)).getValue();
                                    if (state.equals(VideoPlayerActions.PLAY.name())) {
                                        long lastChange = Long.valueOf(((SharedString) statusMap.get(VideoPlayerConstants.STATE_CHANGE_TIME)).getValue());
                                        long current = System.currentTimeMillis();
                                        logger.info("catch up estimate: " + ((double) (current - lastChange) / 1000d));
                                        setPosition(position + (double) (current - lastChange) / 1000d);
                                        play();
                                    } else if (state.equals(VideoPlayerActions.PAUSE.name())) {
                                        setPosition(position);
                                        pause();
                                    } else if (state.equals(VideoPlayerActions.STOP.name())) {
                                        setPosition(position);
                                        stop();
                                    }
                                }
                                break;
                            case STOPPED:
                                controls.setMode(VideoPlayerActions.STOP);
                                showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("STOP"), 2000);
                                break;
                            case PAUSED:
                                controls.setMode(VideoPlayerActions.PAUSE);
                                showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("PAUSE"), 2000);
                                break;
                            case PLAYING:
                                controls.setMode(VideoPlayerActions.PLAY);
                                showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("PLAY"), 2000);
                                break;
                            default:
                                break;
                        }
                    }
                }
            });
            mediaPlayer.addFrameListener(videoPlayerPanel);
        }

        if (!uri.equals(mediaPlayer.getMedia())) {
            // update open media dialog
            toolManager.setMedia(uri);

            // load new video media
            try {
                showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("LOADING_VIDEO"), 0);
                mediaPlayer.openMedia(uri);
            } catch (Exception e) {
                logger.warning("failed to open video: " + e.toString());
                showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("UNABLE_TO_OPEN_VIDEO"), 5000);
                videoPlayerPanel.showSource(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("UNABLE_TO_OPEN_VIDEO"));
                videoPlayerPanel.setConnected(false);
            }
            videoPlayerPanel.repaint();
        }
    }

    /**
     * Close video media
     */
    public void closeMedia() {
        logger.info("closing video: " + getMedia());

        if (playerReady()) {
            mediaPlayer.closeMedia();
        }
    }

    /**
     * Gets the URI of the currently loaded media
     * @return the URI of the media
     */
    public String getMedia() {
        return playerReady() ? mediaPlayer.getMedia() : null;
    }

    /**
     * Determine if video player is ready to play media
     * @return true if the video player is ready to play, false otherwise
     */
    public boolean isPlayable() {
        return playerReady() ? mediaPlayer.isPlayable() : false;
    }

    /**
     * Play video
     */
    public void play() {
        if (playerReady()) {
            logger.info("play");
            mediaPlayer.play();
        }
    }

    /**
     * Gets whether the media is currently playing
     * @return true if the media is playing, false otherwise
     */
    public boolean isPlaying() {
        boolean playing = true;

        if (playerReady()) {
            playing = mediaPlayer.isPlaying();
        }

        return playing;
    }

    /**
     * Pause video
     */
    public void pause() {
        if (playerReady() && isPlaying()) {
            logger.info("pause");
            mediaPlayer.pause();
        }
    }

    /**
     * Stop playing video
     */
    public void stop() {
        if (playerReady()) {
            logger.info("stop");
            mediaPlayer.stop();
        }
    }

    /**
     * Rewind the video
     * @param offset the time in seconds to skip back
     */
    public void rewind(double offset) {
        if (playerReady()) {
            logger.info("rewind");
            mediaPlayer.rewind(offset);
            showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("REWIND"), 2000);
        }
    }

    /**
     * Skip forward the video
     * @param offset the time in seconds to skip forward
     */
    public void forward(double offset) {
        if (playerReady()) {
            logger.info("forward");
            mediaPlayer.forward(offset);
            showHUDMessage(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("FORWARD"), 2000);
        }
    }

    /**
     * Set the position within the media
     * @param mediaPosition the position in seconds
     */
    public void setPosition(double mediaPosition) {
        if (playerReady()) {
            double absolute = getPositionRelative(mediaPosition, 0.0d);
            logger.fine("setting media position to: " + absolute);
            mediaPlayer.setPosition(absolute);
        }
    }

    /**
     * Get the current position
     * @return the current position in seconds
     */
    public double getPosition() {
        return playerReady() ? mediaPlayer.getPosition() : 0.0d;
    }

    /**
     * Gets the duration of the media
     * @return the duration of the media in second
     */
    public double getDuration() {
        return playerReady() ? mediaPlayer.getDuration() : 0.0d;
    }

    /**
     * Mutes audio
     */
    public void mute() {
        if (playerReady()) {
            mute(true, true);
        }
    }

    /**
     * Unmutes audio
     */
    public void unmute() {
        if (playerReady()) {
            mute(false, true);
        }
    }

    /**
     * Gets the state of the audio
     * @return true if the audio is muted, false otherwise
     */
    public boolean isMuted() {
        return (playerReady()) ? mediaPlayer.isMuted() : false;
    }

    /**
     * Gets the state of the player
     * @return the player state
     */
    public VideoPlayerState getState() {
        return (playerReady()) ? mediaPlayer.getState() : VideoPlayerState.NO_MEDIA;
    }

    /**
     * Set the sync state
     * @param syncing true if re-syncing, false if unsyncing
     */
    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            // synced -> unsynced
            synced = false;
            logger.info("unsynced");
        } else if ((syncing == true) && (synced == false)) {
            // unsynced -> synced
            String mediaURI = ((SharedString) statusMap.get(VideoPlayerConstants.MEDIA_URI)).getValue();
            logger.fine("sync: video is: " + mediaURI);
            openMedia(mediaURI);
            synced = true;
            logger.info("synced");
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

    /**
     * Force a resync with shared state
     * Resynchronization is only performed if the app is currently synced.
     */
    public void resync() {
        if (isSynced()) {
            // pretend we're unsynced
            synced = false;
            // sync all over again...
            sync(true);
        }
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
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        if (controlComponent == null) {
            // create control panel
            controls = new VideoPlayerControlPanel(VideoPlayerWindow.this);

            // add a timeline to the control panel
            timeline = new VideoMeter();
            // listen for user actions on the timeline
            timeline.addTimeListener(new TimeListener() {

                public void timeChanged(double newTime) {
                    //mediaPlayer.setPosition(newTime);
                }
            });
            controls.getPanel().add(timeline, BorderLayout.CENTER);

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

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }

    protected void updateControls() {
        controls.setSynced(isSynced());

        controls.setOnHUD(!toolManager.isOnHUD());
    }
}
