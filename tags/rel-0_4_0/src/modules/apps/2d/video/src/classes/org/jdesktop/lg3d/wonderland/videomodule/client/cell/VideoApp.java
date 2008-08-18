/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import java.net.URL;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.media.Player;

import javax.swing.SwingUtilities;

import javax.vecmath.Point3f;

import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;
import org.jdesktop.lg3d.wonderland.appshare.SimpleDrawingSurface;

import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;

import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;

import org.jdesktop.lg3d.wonderland.videomodule.client.cell.VideoCellMenu.Button;
import org.jdesktop.lg3d.wonderland.videomodule.common.HTTPDownloader;
import org.jdesktop.lg3d.wonderland.videomodule.common.JMFSnapper;
import org.jdesktop.lg3d.wonderland.videomodule.common.MovieSource;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.Action;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.PlayerState;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.RequestStatus;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoSource;

/**
 *
 * Video player application
 *
 * @author nsimpson
 */
public class VideoApp extends AppWindowGraphics2DApp implements VideoCellMenuListener {

    private static final Logger logger =
            Logger.getLogger(VideoApp.class.getName());
    protected VideoSource videoInstance;
    protected static final float ACTIVE_FRAME_RATE = 10.0f;
    protected static final float INACTIVE_FRAME_RATE = 2.0f;
    protected static final double DEFAULT_WIDTH = 1280;
    protected static final double DEFAULT_HEIGHT = 960;
    protected DrawingSurface drawingSurface;
    protected JMFSnapper snapper;
    protected long requestThrottle = -1;  // use default
    private Timer frameTimer;
    private FrameUpdateTask frameUpdateTask;
    private float frameRate = 0f;
    private float preferredFrameRate = ACTIVE_FRAME_RATE;
    private double preferredWidth = 0;  // none, use media width
    private double preferredHeight = 0; // none, use media height
    private boolean synced = false;
    private String video;
    private HUDButton msgButton;
    private VideoSourceDialog videoDialog;
    private boolean hudEnabled = true;
    private boolean audioEnabled = false;
    private boolean audioUserMuted = false;
    private boolean inControl = false;
    protected CellMenu cellMenu;
    protected Object actionLock = new Object();
    private HTTPDownloader downloader;
    private Thread downloadThread;
    private boolean shuttingDown = false;

    public VideoApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, (int) DEFAULT_WIDTH, (int) DEFAULT_HEIGHT, true);
    }

    public VideoApp(SharedApp2DImageCell cell, int x, int y, int width, int height,
            boolean decorated) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        drawingSurface = new SimpleDrawingSurface();
        drawingSurface.setSize(width, height);
        drawingSurface.addSurfaceListener(new DrawingSurface.SurfaceListener() {

            public void redrawSurface() {
                repaint();
            }
        });

        initVideoDialog();
        initHUDMenu();
        addEventListeners();
    }

    /**
     * Sets the video source for this video app
     * @param videoInstance the video source
     */
    public void setVideoInstance(VideoSource videoInstance) {
        this.videoInstance = videoInstance;
    }

    /**
     * Set up event listeners for keyboard and mouse events
     */
    private void addEventListeners() {
        addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                logger.finest("video player: key pressed");
                dispatchKeyEvent(e);
            }

            public void keyReleased(KeyEvent e) {
                logger.finest("video player: key released");
//                dispatchKeyEvent(e);
            }

            public void keyTyped(KeyEvent e) {
                logger.finest("video player: key typed");
//                dispatchKeyEvent(e);
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                logger.finest("video player: mouse clicked");
                dispatchMouseEvent(e);
            }

            public void mousePressed(MouseEvent e) {
                logger.finest("video player: mouse pressed");
                dispatchMouseEvent(e);
            }

            public void mouseReleased(MouseEvent e) {
                logger.finest("video player: mouse released");
                dispatchMouseEvent(e);
            }

            public void mouseEntered(MouseEvent e) {
                logger.finest("video player: mouse entered");
                dispatchMouseEvent(e);
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                logger.finest("video player: mouse exited");
                dispatchMouseEvent(e);
                repaint();
            }
        });
    }

    /**
     * Initialize the video source dialog
     */
    private void initVideoDialog() {
        videoDialog = new VideoSourceDialog(null, false);
        videoDialog.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideVideoDialog();
                if (evt.getActionCommand().equals("OK")) {
                    if (isSynced()) {
                        sendCameraRequest(Action.SET_SOURCE, null);
                    } else {
                        loadVideo(videoDialog.getVideoURL(), 0.0, PlayerState.PAUSED);
                    }
                }
            }
        });
    }

    /**
     * Show the video source dialog
     */
    private void showVideoDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                videoDialog.setVisible(true);
            }
        });
    }

    /**
     * Hide the video source dialog
     */
    public void hideVideoDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                videoDialog.setVisible(false);
            }
        });
    }

    /** 
     * Initialize the Head Up Display (HUD) control panel
     */
    protected void initHUDMenu() {
        cellMenu = new VideoCellMenu();
        cellMenu.addCellMenuListener(this);
    }

    /**
     * Load a video into the player
     * @param video the URL of the video
     * @param position the initial position of the player (in seconds)
     * @param state the initial play state of the player after loading the video
     */
    public void loadVideo(final String video, final double position, final PlayerState state) {
        if (video != null) {
            new Thread(new Runnable() {

                boolean mediaReady = false;

                public void run() {
                    VideoApp.this.video = null;
                    shuttingDown = false;

                    if ((videoInstance instanceof MovieSource) && (video.startsWith("http"))) {
                        // video on web servers must be downloaded to the local file
                        // system to be playable by JMF

                        // download the video in a separate thread so we can
                        // monitor the download
                        downloader = new HTTPDownloader(video,
                                HTTPDownloader.getTempFilename(video), 100 * 1024);

                        if (downloader.downloadRequired()) {
                            // download the first 100 KB of the file
                            downloadThread = new Thread(downloader);
                            downloadThread.start();

                            synchronized (downloader) {
                                while (!downloader.downloadComplete() && !downloader.alertTriggered()) {
                                    try {
                                        downloader.wait();
                                    } catch (InterruptedException e) {
                                    }
                                }
                                logger.info("video player: downloaded: " + downloader.getDownloaded() +
                                        " of " + downloader.getDownloadSize() + " Bytes" +
                                        ", bandwidth: " + downloader.getBandwidth() + " KB/s");
                            }

                            // determine if we need to wait for the entire file to download
                            if (downloader.getRemainingTime() > 10) {
                                // If the video will take more than 10 seconds to download
                                // then completely download a copy of the video. This is
                                // necessary when connected over a slow network where video
                                // playback might overrun the buffer of downloaded video
                                //
                                // This strategy courtesy of Jo Voordeckers which he described 
                                // at JavaOne 2008 in technical session TS-7372.
                                logger.info("video player: fully downloading video, time remaining: " + downloader.getRemainingTime() + " s");

                                synchronized (downloader) {
                                    // wait for the file to be completely downloaded
                                    while (!downloader.downloadComplete()) {
                                        try {
                                            downloader.wait();
                                        } catch (InterruptedException e) {
                                        }
                                    }

                                    logger.info("video player: completed downloading video: " + video);
                                }
                            } else {
                                mediaReady = true;
                            }
                        } else {
                            mediaReady = true;
                        }
                        // file name of local copy of web video source
                        VideoApp.this.video = downloader.getLocalFile();
                    } else {
                        // a local video source
                        VideoApp.this.video = video;
                        mediaReady = true;
                    }
                    if ((VideoApp.this.video != null) && (!shuttingDown)) {
                        if (mediaReady == true) {
                            // load the new video
                            logger.info("video player: loading: " + VideoApp.this.video);
                            videoDialog.setVideoURL(video);

                            // stop any existing player threads
                            stopPlaying();

                            // start a new player thread
                            snapper = new JMFSnapper(VideoApp.this.video);

                            // stop the new video from playing
                            stopPlaying();

                            BufferedImage frame = snapper.getFrame();
                            if (frame != null) {
                                double w = frame.getWidth();
                                double h = frame.getHeight();

                                if (preferredWidth != 0) {
                                    // width preference
                                    if (w != preferredWidth) {
                                        double aspect = h / w;
                                        w = preferredWidth;
                                        h = preferredWidth * aspect;
                                    }
                                }
                                if ((w > 0) && (h > 0)) {
                                    logger.fine("video player: resizing app window to fit video: " + w + "x" + h);
                                    setSize((int) w, (int) h);
                                }
                            }

                            setInControl(inControl);

                            if (state == PlayerState.PLAYING) {
                                setPosition(position);
                                play(true);
                            } else {
                                cue(position, 0.05);
                            }
                        } else {
                            // media not ready
                            logger.info("video player: media not ready, initiating resync");
                            VideoApp.this.resync();
                        }
                    }
                }
            }).start();
        }
    }

    public void unloadVideo() {
        logger.info("video player: unloading video");
        shuttingDown = true;
        if ((downloader != null) && (!downloader.downloadComplete())) {
            logger.info("video player: unload video: aborting download due to unload");
            downloader.abortDownload();
        }
        if (isPlaying()) {
            logger.info("video player: unload video: stopping playing due to unload");
            stopPlaying();
        }
    }

    /**
     * Gets the URL of the currently loaded video
     * @return the URL of the video
     */
    public String getVideo() {
        return video;
    }

    /**
     * Sets the frame rate. The frame rate is the rate at which frames are
     * rendered. For streaming sources, such as webcams, the rate at which
     * frames are retrieved is fixed at 10 frames per second by the jipcam
     * library. So frame rate only controls the rate at which frames are
     * rendered.
     * @param rate the frame rate in frames per second
     */
    public void setFrameRate(float rate) {
        logger.info("video player: setting frame rate to: " + rate + " fps");
        showHUDMessage("fps: " + (int) rate, 3000);
    }

    /** 
     * Gets the frame rate
     * @return the frame rate in frames per second
     */
    public float getFrameRate() {
        return preferredFrameRate;
    }

    /**
     * Sets the sync status
     * @param synced true if the app is synced with the global state, false otherwise
     */
    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    /**
     * Gets whether the player is synced with the global state
     * @return
     */
    public boolean isSynced() {
        return synced;
    }

    /**
     * Sets the state of the player
     * @param state the player state (PLAYING, PAUSED, or STOPPED)
     */
    public void setState(PlayerState state) {
        logger.fine("video player: setting state to: " + state);
        switch (state) {
            case PLAYING:
                play(true);
                break;
            case PAUSED:
                play(false);
                break;
            case STOPPED:
                play(false);
                break;
        }
    }

    /**
     * Gets the player state
     * @return the state of the player (PLAYING, PAUSED, or STOPPED)
     */
    public PlayerState getState() {
        // REMIND: differentiate between stopped and paused?
        if (isPlaying()) {
            return PlayerState.PLAYING;
        } else {
            return PlayerState.PAUSED;
        }
    }

    /**
     * Sets the current playback position
     * @param time the playback position in seconds
     */
    public void setPosition(double time) {
        if (playerReady()) {
            time = ((time > snapper.getDuration()) || (time < 0)) ? 0 : time;
            logger.info("video player: setting media position to: " + time);
            snapper.setPosition(time);
        }
    }

    /**
     * Sets the playback position relative to a specified position
     * @param position the position to set relative to in seconds
     * @param offset the offser from the position in seconds
     */
    public void setPositionRelative(double position, double offset) {
        if (playerReady()) {
            position += offset;
            position = (position > snapper.getDuration()) || (position < 0) ? 0 : position;
            logger.info(((offset > 0) ? "fast forwarding" : "rewinding") + " to: " + position + "s");
            showHUDMessage(((offset > 0) ? "fast forward" : "rewind"), 3000);
            cue(position, 0.05);
        }
    }

    /**
     * Gets the position of the player
     * @return the player position in seconds
     */
    public double getPosition() {
        double time = 0;
        if (playerReady()) {
            time = snapper.getPosition();
        }

        return time;
    }

    /**
     * Sets the preferred width of the video player
     * 
     * Note that setting the preferred width to be larger than the native
     * width of the video results in software scaling of each video frame
     * which is cpu intensive. To scale the video, set the pixelScale
     * property in the Video application's WFS file.
     * @param preferredWidth the preferred width of the player in pixels
     */
    public void setPreferredWidth(double preferredWidth) {
        this.preferredWidth = preferredWidth;
        setSize((int) preferredWidth, getHeight());
    }

    /**
     * Sets the preferred height of the video player
     * 
     * Note that setting the preferred height to be larger than the native
     * height of the video results in software scaling of each video frame
     * which is cpu intensive. To scale the video, set the pixelScale
     * property in the Video application's WFS file.
     * @param preferredHeight the preferred height of the player in pixels
     */
    public void setPreferredHeight(double preferredHeight) {
        this.preferredHeight = preferredHeight;
        setSize(getWidth(), (int) preferredHeight);
    }

    /**
     * Sets a throttle which limits how frequently requests are sent by the
     * client to the server.
     * 
     * This property is not used by the base VideoApp class
     * @param requestThrottle the request throttle value
     */
    public void setRequestThrottle(long requestThrottle) {
        this.requestThrottle = requestThrottle;
    }

    /**
     * Gets the request throttle
     * @return the request throttle value
     */
    public long getRequestThrottle() {
        return requestThrottle;
    }

    /**
     * Sets the player to be in control. When the player has control, it 
     * displays its HUD control panel and enables audio.
     * @param inControl specifies whether to set the player to be in control
     */
    public void setInControl(boolean inControl) {
        this.inControl = inControl;
        // When the application gains control, increase the frame rate to the
        // frame rate defined in the application's setup data (from the WFS
        // properties) or to the rate set by the user. Also, enable audio, 
        // unless the user had previously muted.
        // 
        // When the application loses control, reduce the frame rate to
        // the inactive rate to reduce cpu load. Also mute audio, regardless
        // of what the user has done.
        //
        // Ultimately, the frame rate should vary depending on avatar proximity
        // to the video application and whether the video is in view.

        if (inControl == true) {
            setFrameRate(preferredFrameRate);
            CellMenuManager.getInstance().showMenu(this.getCell(), cellMenu, null);
            updateMenu();
        } else {
            setFrameRate(INACTIVE_FRAME_RATE);
            CellMenuManager.getInstance().hideMenu();
        }
        setAudioEnabled(inControl);
    }

    /**
     * Gets whether the player is in control
     * @return true if the player has control, false otherwise
     */
    public boolean isInControl() {
        return inControl;
    }

    /**
     * Sets whether audio is enabled
     * @param audioEnabled true to enable audio, false to disable audio
     */
    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;

        if (audioEnabled == true) {
            if (audioUserMuted == false) {
                // user hasn't manually muted video
                mute(false, true);
            }
        } else {
            mute(true, true);
        }
    }

    /**
     * Gets whether audio is enabled
     * @return true if audio is enabled, false otherwise
     */
    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    /**
     * Show a status message in the HUD
     * @param message the string to display in the message
     */
    protected void showHUDMessage(String message) {
        showHUDMessage(message, HUD.NO_TIMEOUT);
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    protected void showHUDMessage(String message, int timeout) {
        if (hudEnabled == true) {
            URL[] imgURLs = {HUD.SIMPLE_BOX_IMAGE_URL,
                VideoApp.class.getResource("resources/video-file.png")
            };

            Point[] imagePoints = {new Point(), new Point(10, 10)};

            // dismiss currently active HUD message
            if ((msgButton != null) && msgButton.isActive()) {
                hideHUDMessage(true);
            }

            // display a new HUD message
            msgButton = HUDFactory.getHUD().addHUDMultiImageButton(imgURLs,
                    imagePoints, message, new Point(50, 25),
                    Font.decode("dialog" + "-BOLD-14"),
                    -150, 100, 150, 100,
                    timeout, true);
        }
    }

    /**
     * Hide the HUD message
     * @param immediately if true, remove the message now, otherwise slide it
     * off the screen first
     */
    private void hideHUDMessage(boolean immediately) {
        if (hudEnabled == true) {
            if (msgButton != null) {
                if (!immediately) {
                    msgButton.changeLocation(new Point(-45, 100));
                }

                msgButton.setActive(false);
            }
        }
    }

    /**
     * Enables or disables HUD messages
     * @param enabled true to enable the HUD, false to disable
     */
    public void setHUDEnabled(boolean enabled) {
        if (hudEnabled == true) {
            hideHUDMessage(true);
        }
        hudEnabled = enabled;
    }

    /** 
     * Gets whether the HUD is enabled
     * @return true if the HUD is enabled, false if not
     */
    public boolean isHUDEnabled() {
        return hudEnabled;
    }

    /**
     * A convenience method to get whether the player is ready to play
     * @return true if the player is ready, false if not
     */
    private boolean playerReady() {
        return ((snapper != null) && (snapper.hasPlayer() == true));
    }

    /**
     * VideoCellMenuListener methods
     */
    /**
     * Displays the open video dialog, to open a new video
     */
    public void open() {
        showVideoDialog();
    }

    /**
     * Toggles between playing and paused
     */
    public void play() {
        if (isPlaying()) {
            // pause immediately for better responsiveness
            // other clients will catch up to this state
            play(false);
            if (isSynced()) {
                sendCameraRequest(Action.PAUSE, null);
            }
        } else {
            // play when server acknowledges request for better sync
            // with other clients
            if (isSynced()) {
                sendCameraRequest(Action.PLAY, null);
            } else {
                play(true);
            }
        }
    }

    /**
     * Sets the player state 
     * @param play true to start playing, false to stop
     */
    public void play(boolean play) {
        // perform local play action
        if (play == true) {
            showHUDMessage("play", 3000);
            startPlaying();
        } else {
            showHUDMessage("pause", 3000);
            stopPlaying();
        }
    }

    /**
     * Gets whether the player is currently playing
     * 
     * Note that the player might be transitioning between play states, so
     * the player state may not accurately reflect the last requested state.
     * 
     * @return true if the player is playing, false otherwise
     */
    public boolean isPlaying() {
        boolean playing = false;

        if (playerReady()) {
            // ask the player for the play state, this has the most
            // accurate status of what the user is seeing
            logger.finest("video player: is playing == " + playing + " (" + snapper.getPlayerState() + ")");
            playing = snapper.getPlayerState() == Player.Started;
        }

        return playing;
    }

    /**
     * Pauses playback
     */
    public void pause() {
        if (playerReady()) {
            play();
        }
    }

    /**
     * Resets the player position to the start of the video and stops playing
     */
    public void stop() {
        if (playerReady()) {
            // stop immediately, then tell everyone else
            logger.info("video player: stop");
            showHUDMessage("stop", 3000);
            snapper.stopMovie();
            cue(0.01, 0.01);
            if (isSynced()) {
                sendCameraRequest(Action.STOP, null);
            }
        }
    }

    /**
     * Rewinds the video by 5 seconds
     */
    public void rewind() {
        if (playerReady()) {
            logger.info("video player: rewind");
            if (isSynced()) {
                // sync other clients
                sendCameraRequest(Action.REWIND, null);
            } else {
                setPositionRelative(getPosition(), -5);
            }
        }
    }

    /**
     * Fast forwards the video by 5 seconds
     */
    public void fastForward() {
        if (playerReady()) {
            logger.info("video player: fast forward");
            if (isSynced()) {
                // sync other clients
                sendCameraRequest(Action.FAST_FORWARD, null);
            } else {
                setPositionRelative(getPosition(), 5);
            }
        }
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
        if (isSynced()) {
            synced = false;
            sync(true);
        }
    }

    /** 
     * Sets the sync state of the player
     * @param syncing true to sync, false to unsync
     */
    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("video player: unsynced");
            showHUDMessage("unsynced", 3000);
            updateMenu();
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("video player: requesting sync with shared state");
            showHUDMessage("syncing...", 5000);
            sendCameraRequest(Action.GET_STATE, null);
        }
    }

    /**
     * Toggle the sync state of the player
     */
    public void sync() {
        sync(!isSynced());
    }

    /**
     * Toggle the sync state of the player
     */
    public void unsync() {
        sync(!isSynced());
    }

    /**
     * Starts playing the video
     * 
     * Note: this method must be called from a separate thread otherwise
     * it will impact Wonderland client performance
     */
    private void startPlayingImpl() {
        if (!playerReady()) {
            return;
        }

        // stop the current player
        stopPlayingImpl();

        logger.info("video player: starting playback");
        snapper.startMovie();

        frameRate = preferredFrameRate;
        frameTimer = new Timer();
        frameUpdateTask = new FrameUpdateTask();
        frameTimer.scheduleAtFixedRate(frameUpdateTask, 0, (long) (1000 * 1f / frameRate));
        while (!isPlaying()) {
            try {
                Thread.sleep(200);
                logger.info("video player: waiting for player to start: " + snapper.getPlayerState());
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Starts playing the video
     */
    public void startPlaying() {
        if (preferredFrameRate > 0) {
            new Thread(new Runnable() {

                public void run() {
                    startPlayingImpl();
                    updateMenu();
                }
            }).start();
        }
    }

    /**
     * Stops playing the video
     * 
     * Note: this method must be called from a separate thread otherwise
     * it will impact Wonderland client performance
     */
    private void stopPlayingImpl() {
        if (!playerReady()) {
            return;
        }

        logger.info("video player: stopping playback");
        if (snapper != null) {
            snapper.stopMovie();
        }
        frameRate = 0;
        if (frameTimer != null) {
            frameTimer.cancel();
            frameTimer = null;
        }
        while (isPlaying()) {
            try {
                Thread.sleep(200);
                logger.info("video player: waiting for player to stop: " + snapper.getPlayerState());
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Stops playing the video
     */
    public void stopPlaying() {
        new Thread(new Runnable() {

            public void run() {
                stopPlayingImpl();
                updateMenu();
            }
        }).start();
    }

    /**
     * Cues up the video to a specified position
     * 
     * Setting the player position does not cause the current frame displayed
     * in the player to be updated. Cue solves this problem by playing the video
     * for a short lead in period.
     * 
     * @param start the cue position of the video in seconds
     * @param cueLeadIn how far back from the cue position to start playing
     */
    public void cue(final double start, final double cueLeadIn) {
        if (!playerReady()) {
            return;
        }

        new Thread(new Runnable() {

            public void run() {
                double position = ((start - cueLeadIn) < 0) ? cueLeadIn : start;

                if (isPlaying()) {
                    logger.info("video player: cue setting position directly to: " + position);
                    stopPlayingImpl();
                    setPosition(position);
                    startPlayingImpl();
                } else {
                    setPosition(position - cueLeadIn);
                    snapper.setStopTime(position);
                    logger.info("video player: cue starting from " + (position - cueLeadIn) + " to " + position);
                    // player will stop on its own at the start time
                    startPlayingImpl();
                }
            }
        }).start();
    }

    /** 
     * Mutes the audio
     * @param muting true to mute, false to unmute
     * @param quietly whether to announce the mute state change in the HUD
     */
    public void mute(boolean muting, boolean quietly) {
        if (!playerReady()) {
            return;
        }

        boolean hudOn = isHUDEnabled();

        if ((quietly == true) && (hudOn == true)) {
            setHUDEnabled(false);
        }
        mute(muting);
        setHUDEnabled(hudOn);
    }

    /**
     * Mutes the audio
     * @param muting true to mute, false to unmute
     */
    public void mute(boolean muting) {
        if (!playerReady()) {
            return;
        }

        logger.info("video player: " + ((muting == true) ? "muting" : "unmuting"));
        snapper.mute(muting);
        showHUDMessage(isMuted() ? "muted" : "unmuted", 3000);
    }

    /**
     * Gets whether the audio is muted
     * @return true if muted, false if not
     */
    public boolean isMuted() {
        return (playerReady()) ? snapper.isMuted() : true;
    }

    /**
     * Handle keyboard events in the video player
     * @param e the key event
     */
    protected void dispatchKeyEvent(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_F:
                // change the frame rate
                if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                    logger.fine("video player: increasing frame rate");
                    preferredFrameRate++;
                    setFrameRate(preferredFrameRate);
                    if (isPlaying() == true) {
                        // already playing, re-start
                        startPlaying();
                    }
                } else {
                    logger.fine("video player: decreasing frame rate");
                    preferredFrameRate = (preferredFrameRate > 0) ? preferredFrameRate - 1 : 0;
                    setFrameRate(preferredFrameRate);
                    if (isPlaying() == true) {
                        // already playing, re-start
                        startPlaying();
                    }
                }
                break;
            case KeyEvent.VK_M:
                // mute/unmute
                audioUserMuted = (isMuted() == false) ? true : false;
                mute(!isMuted());
                break;
            case KeyEvent.VK_O:
                // open a new video
                if (e.isControlDown() == true) {
                    showVideoDialog();
                }
                break;
            case KeyEvent.VK_P:
                // play/pause
                play();
                break;
            case KeyEvent.VK_S:
                // re-sync with shared state
                sync();
                break;
        }
    }

    /**
     * Handle mouse events in the video player
     * @param e the mouse event
     */
    protected void dispatchMouseEvent(MouseEvent e) {
        switch (e.getID()) {
            case MouseEvent.MOUSE_CLICKED:
                break;
        }
    }

    /**
     * ActionScheduler manages the retrying of application requests which
     * were previously denied due to request contention in the server GLO.
     */
    protected class ActionScheduler extends Thread {

        private Action action;
        private Point3f point;

        public ActionScheduler(Action action, Point3f point) {
            this.action = action;
            this.point = point;
        }

        @Override
        public void run() {
            // wait for a retry window
            synchronized (actionLock) {
                try {
                    logger.fine("video player: waiting for retry window");
                    actionLock.wait();
                } catch (Exception e) {
                    logger.fine("video player: exception waiting for retry: " + e);
                }
            }
            // retry this request
            logger.fine("video player: retrying: " + action + ", " + point);
            sendCameraRequest(action, point);
        }
    }

    /**
     * Sends a video request to the server GLO which broadcasts the action
     * to other clients to maintain synchronization
     * @param action the requested action
     * @param position the "position" of the video - this is not used by the
     * VideoApp base class
     */
    protected void sendCameraRequest(Action action, Point3f position) {
        VideoCellMessage msg = null;

        switch (action) {
            case SET_SOURCE:
                msg = new VideoCellMessage(this.getCell().getCellID(),
                        ((VideoCell) cell).getUID(),
                        videoDialog.getVideoURL(),
                        action,
                        0);
                msg.setState(PlayerState.PLAYING);
                break;
            case REWIND:
            case FAST_FORWARD:
            case PLAY:
            case PAUSE:
                msg = new VideoCellMessage(this.getCell().getCellID(),
                        ((VideoCell) cell).getUID(),
                        getVideo(),
                        action,
                        getPosition());
                break;
            case STOP:
                msg = new VideoCellMessage(this.getCell().getCellID(),
                        ((VideoCell) cell).getUID(),
                        getVideo(),
                        action,
                        0.01);
                break;
            case GET_STATE:
                msg = new VideoCellMessage(this.getCell().getCellID(),
                        ((VideoCell) cell).getUID(),
                        VideoCellMessage.Action.GET_STATE);
                break;
        }

        if (msg != null) {
            // send request to server
            logger.fine("video player: sending camera request: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    /**
     * Retries a denied request
     * @param action the action to retry
     * @param position the "position" of the video - this is not used by the
     * VideoApp base class
     */
    protected void retryCameraRequest(Action action, Point3f position) {
        new ActionScheduler(action, position).start();
    }

    /**
     * Handle a request from the server GLO
     * @param msg the request
     */
    protected void handleResponse(VideoCellMessage msg) {
        String controlling = msg.getUID();
        String myUID = ((VideoCell) cell).getUID();
        boolean forMe = (myUID.equals(controlling));
        VideoCellMessage vcm = null;

        if (isSynced()) {
            logger.fine("video player: " + myUID + " received message: " + msg);
            if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED) {
                // a client may send a request while another app has control.
                // the server denies the conflicting request and the app must
                // the re-issue the request when the app currently in control
                // relinquishes control                
                try {
                    logger.info("video player: scheduling retry of request: " + msg);
                    retryCameraRequest(msg.getAction(), new Point3f());
                } catch (Exception e) {
                    logger.warning("video player: failed to create retry request for: " + msg);
                }
            } else {
                switch (msg.getAction()) {
                    case SET_SOURCE:
                        loadVideo(msg.getSource(), 0.0, PlayerState.PAUSED);
                        break;
                    case REWIND:
                        setPositionRelative(msg.getPosition(), -5);
                        break;
                    case FAST_FORWARD:
                        setPositionRelative(msg.getPosition(), 5);
                        break;
                    case PLAY:
                    case PAUSE:
                    case STOP:
                        // change the play state of the video
                        if (msg.getAction() == Action.PLAY) {
                            // starting to play
                            setPosition(msg.getPosition());
                            play(true);
                        } else {
                            // pausing or stopping
                            if (!forMe) {
                                // initiating cell will already have paused/stopped
                                if (this.playerReady()) {
                                    snapper.stopMovie();
                                }
                                cue(msg.getPosition(), 0.01);
                            }
                        }
                        break;
                    case SET_STATE:
                        if (forMe) {
                            logger.fine("video player: syncing with state: " + msg);
                            loadVideo(msg.getSource(), msg.getPosition(), msg.getState());

                            setSynced(true);
                            logger.info("video player: video synced");
                            showHUDMessage("synced", 3000);
                        }
                        break;
                    case REQUEST_COMPLETE:
                        synchronized (actionLock) {
                            try {
                                logger.fine("video player: waking retry threads");
                                actionLock.notify();
                            } catch (Exception e) {
                                logger.warning("video player: exception notifying retry threads: " + e);
                            }
                        }
                        break;
                    default:
                        logger.warning("video player: unhandled message type: " + msg.getAction());
                        break;
                }
            }
            if ((forMe == true) && (msg.getAction() != Action.REQUEST_COMPLETE)) {
                // notify everyone that the request has completed
                vcm = new VideoCellMessage(msg);
                vcm.setAction(Action.REQUEST_COMPLETE);
            }
        }
        if (vcm != null) {
            logger.fine("video player: sending message: " + vcm);
            ChannelController.getController().sendMessage(vcm);
        }
    }

    /**
     * Updates the button state of the HUD control panel to match the
     * current state of the video player
     */
    protected void updateMenu() {
        if (((VideoCellMenu) cellMenu).isActive()) {
            if (isSynced()) {
                ((VideoCellMenu) cellMenu).enableButton(Button.SYNC);
                ((VideoCellMenu) cellMenu).disableButton(Button.UNSYNC);
            } else {
                ((VideoCellMenu) cellMenu).enableButton(Button.UNSYNC);
                ((VideoCellMenu) cellMenu).disableButton(Button.SYNC);
            }

            if (isPlaying()) {
                ((VideoCellMenu) cellMenu).enableButton(Button.PAUSE);
                ((VideoCellMenu) cellMenu).disableButton(Button.PLAY);
            } else {
                ((VideoCellMenu) cellMenu).enableButton(Button.PLAY);
                ((VideoCellMenu) cellMenu).disableButton(Button.PAUSE);
            }
        }
    }

    /**
     * Set the size of the application
     * @param width the width of the application
     * @param height the height of the application
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        drawingSurface.setSize(width, height);
    }

    /**
     * Notified when the application has control
     * @param me the mouse event that caused the application to get control
     */
    @Override
    public void takeControl(MouseEvent me) {
        logger.info("video player: has control");
        super.takeControl(me);
        setInControl(true);
    }

    /**
     * Notified when the application loses control
     * @param me the mouse event that caused the application to lsose control
     */
    @Override
    public void releaseControl(MouseEvent me) {
        logger.info("video player: lost control");
        super.releaseControl(me);
        setInControl(false);
    }

    /**
     * A periodic task which displays new video frames in the video player
     * window
     */
    private class FrameUpdateTask extends TimerTask {

        public void run() {
            doFrameUpdate();
        }
    }

    /**
     * The video player frame update task
     */
    protected void doFrameUpdate() {
        if (playerReady()) {
            if (snapper.getPlayerState() == Player.Started) {
                VideoApp.this.repaint();
            } else {
                if (frameTimer != null) {
                    logger.info("video player: stopping frame update task because video isn't playing: " + snapper.getPlayerState());
                    stopPlaying();
                }
            }
        }
    }

    /**
     * Paints the player contents
     */
    @Override
    protected void paint(Graphics2D g) {
        if (playerReady()) {
            if (drawingSurface != null) {
                BufferedImage frame = snapper.getFrame();

                if (frame != null) {
                    g.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
                    frame = null;
                }
            }
        }
    }
}
