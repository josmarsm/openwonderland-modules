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
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.media.Manager;

import javax.media.Player;

import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;
import org.jdesktop.lg3d.wonderland.appshare.SimpleDrawingSurface;

import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;

import org.jdesktop.lg3d.wonderland.scenemanager.EventController;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;
import org.jdesktop.lg3d.wonderland.videomodule.common.JMFSnapper;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellMessage;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellMessage.PlayerState;

/**
 *
 * An application for testing use of Swing components in Wonderland
 *
 * @author nsimpson
 */
public class VideoModuleApp extends AppWindowGraphics2DApp {

    private static final Logger logger =
            Logger.getLogger(VideoModuleApp.class.getName());
    private static final float DEFAULT_FRAME_RATE = 2.0f; // frames per second
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 960;
    private DrawingSurface drawingSurface;
    private JMFSnapper snapper;
    private Timer frameTimer;
    private FrameUpdateTask frameUpdateTask;
    private float frameRate = 0f;
    private float preferredFrameRate = DEFAULT_FRAME_RATE;
    private double preferredWidth = 0;  // none, use media width
    private double preferredHeight = 0; // none, use media height
    private boolean synced = false;
    private String video;
    private HUDButton msgButton;

    public VideoModuleApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, (int) DEFAULT_WIDTH, (int) DEFAULT_HEIGHT);
    }

    public VideoModuleApp(SharedApp2DImageCell cell, int x, int y, int width, int height) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        drawingSurface = new SimpleDrawingSurface();
        drawingSurface.setSize(width, height);

        drawingSurface.addSurfaceListener(new DrawingSurface.SurfaceListener() {

            public void redrawSurface() {
                repaint();
            }
        });

        initComponents();

        addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                logger.finest("key pressed on app");
                dispatchKeyEvent(e);
            }

            public void keyReleased(KeyEvent e) {
                logger.finest("key released on app");
            //dispatchKeyEvent(e);
            }

            public void keyTyped(KeyEvent e) {
                logger.finest("key typed on app");
            //dispatchKeyEvent(e);
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                logger.finest("mouse dragged on app");
                dispatchMouseEvent(e);
            }

            public void mouseMoved(MouseEvent e) {
                logger.finest("mouse moved on app");
                dispatchMouseEvent(e);
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                logger.finest("mouseClicked on app");
                dispatchMouseEvent(e);
            }

            public void mousePressed(MouseEvent e) {
                logger.finest("mousePressed on app");
                dispatchMouseEvent(e);
            }

            public void mouseReleased(MouseEvent e) {
                logger.finest("mouseReleased on app");
                dispatchMouseEvent(e);
            }

            public void mouseEntered(MouseEvent e) {
                logger.finest("mouseEntered on app");
                dispatchMouseEvent(e);
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                logger.finest("mouseExited on app");
                dispatchMouseEvent(e);
                repaint();
            }
        });

        setShowing(true);
    }

    private void initComponents() {

    }

    public void loadVideo(String video) {
        if ((this.video == null) || (!this.video.equals(video))) {
            this.video = video;
            snapper = new JMFSnapper(video);
            Manager.setHint(Manager.CACHING, new Boolean(false));
            snapper.stopMovie();
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
                    logger.fine("resizing app window to fit video: " + w + "x" + h);
                    setSize((int) w, (int) h);
                }
            }
            setFrameRate(DEFAULT_FRAME_RATE);
        }
    }

    public String getVideo() {
        return video;
    }

    public void setFrameRate(float rate) {
        if (rate != preferredFrameRate) {
            logger.info("setting frame rate to: " + rate);
            showHUDMessage("fps: " + (int) rate, 2000);
            preferredFrameRate = rate;
        }
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
        if (synced == true) {
            requestSync();
            showHUDMessage("syncing...", 2000);
        }
    }

    public boolean isSynced() {
        return synced;
    }

    private void requestSync() {
        logger.fine("--> requesting sync");
        VideoModuleCellMessage msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                VideoModuleCellMessage.Action.GET_STATUS);
        logger.fine("--- sending message: " + msg);
        ChannelController.getController().sendMessage(msg);
    }

    public synchronized void handleSync(String video, PlayerState state, double position) {
        if (synced == true) {
            loadVideo(video);
            setPosition(position);
            setState(state);
            synced = true;
            logger.fine("=== video synced");
            showHUDMessage("in sync", 2000);
        }
    }

    public void setState(PlayerState state) {
        logger.fine("--- setting state to: " + state);
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

    public PlayerState getState() {
        // REMIND: differentiate between stopped and paused?
        if (isPlaying()) {
            return PlayerState.PLAYING;
        } else {
            return PlayerState.PAUSED;
        }
    }

    public synchronized void setPosition(double time) {
        logger.fine("--- setting position to: " + time);
        boolean wasPlaying = isPlaying();

        if (isPlaying()) {
            stop();
        }

        snapper.setPosition(time);
    }

    public double getPosition() {
        double time = 0;
        if (snapper != null) {
            time = snapper.getPosition();
        }
        return time;
    }

    public void reportStatus() {
        if (synced == true) {
            VideoModuleCellMessage msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                    VideoModuleCellMessage.Action.STATUS);
            msg.setVideoURL(getVideo());
            msg.setState(getState());
            msg.setPosition(getPosition());

            // send message
            logger.fine("--- reporting my status: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    public void setPreferredWidth(double preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public void setPreferredHeight(double preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /**
     * Show a status message in the HUD
     * @param message the string to display in the message
     */
    private void showHUDMessage(String message) {
        showHUDMessage(message, HUD.NO_TIMEOUT);
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    private void showHUDMessage(String message, int timeout) {
        URL[] imgURLs = {HUD.SIMPLE_BOX_IMAGE_URL,
            EventController.class.getResource("resources/preferences-system-windows.png")
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

    /**
     * Hide the HUD message
     * @param immediately if true, remove the message now, otherwise slide it
     * off the screen first
     */
    private void hideHUDMessage(boolean immediately) {
        if (msgButton != null) {
            if (!immediately) {
                msgButton.changeLocation(new Point(-45, 100));
            }
            msgButton.setActive(false);
        }
    }

    public void play(boolean play) {
        logger.fine("--- play: " + play);

        // perform local play action
        if (play == true) {
            start();

        } else {
            stop();

        }
        // report the actual state, not just what was requested
        if (isPlaying()) {
            showHUDMessage("Play", 2000);
        } else {
            showHUDMessage("Paused");
        }
    }

    public boolean isPlaying() {
        boolean playing = false;
        if (snapper != null) {
            // ask the player for the play state, this has the most
            // accurate status of what the user is seeing
            playing = snapper.getPlayerState() == Player.Started;
        }
        return playing;
    }

    public synchronized void requestPlay(boolean play) {
        if (synced == true) {
            // ask the server to send play/pause
            logger.info("--> requesting play: " + play);
            VideoModuleCellMessage msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                    getVideo(),
                    (play == true) ? VideoModuleCellMessage.Action.PLAY
                    : VideoModuleCellMessage.Action.PAUSE,
                    getPosition());
            logger.info("--- sending message: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    private void start() {
        if ((snapper == null) || (snapper.hasPlayer() == false)) {
            return;
        }
        if (isPlaying() && (frameRate != preferredFrameRate)) {
            // frame rate has changed, need to restart play
            stop();
        }

        if (!isPlaying()) {
            if (preferredFrameRate > 0) {
                logger.info("play video");
                snapper.startMovie();
                frameRate = preferredFrameRate;
                frameTimer = new Timer();
                frameUpdateTask = new FrameUpdateTask();
                frameTimer.scheduleAtFixedRate(frameUpdateTask, 0, (long) (1000 * 1f / frameRate));
            }
        }
    }

    private void stop() {
        if ((snapper == null) || (snapper.hasPlayer() == false)) {
            return;
        }

        logger.info("stop video");
        snapper.stopMovie();
        frameRate = 0;
        if (frameTimer != null) {
            frameTimer.cancel();
        }
        frameTimer = null;
    }

    public void cue(double time, double offset) {
        CueVideo cuer = new CueVideo(time, offset);
        new Thread(cuer).start();
    }

    private class CueVideo implements Runnable {

        private double start;
        private double cueLeadIn;
        private Timer cueTimer;

        public CueVideo(double start, double cueLeadIn) {
            this.start = start;
            this.cueLeadIn = cueLeadIn;
        }

        public void run() {
            if (!isPlaying()) {
                cueTimer = new Timer();
                setPosition(start - cueLeadIn);
                snapper.setStopTime(start);
                play(true);
                logger.fine("*** scheduling cue in from: " + (start - cueLeadIn) + " to " + start);
                cueTimer.schedule(new CueCompleteTask(), (long) cueLeadIn);
            }
        }

        private class CueCompleteTask extends TimerTask {

            public void run() {
                logger.fine("*** cue complete");
                stop();
                cueTimer.cancel();
            }
        }
    }

    private void dispatchKeyEvent(KeyEvent e) {
        VideoModuleCellMessage msg = null;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                logger.fine("zooming in");
                showHUDMessage("zoom in", 1000);
                // ask the server to zoom the camera in
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.ZOOM_IN);
                break;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_UNDERSCORE:
                logger.fine("zooming out");
                showHUDMessage("zoom out", 1000);
                // ask the server to zoom the camera out
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.ZOOM_OUT);
                break;
            case KeyEvent.VK_LEFT:
                logger.fine("panning right");
                showHUDMessage("pan right", 1000);
                // ask the server to pan the camera right
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.PAN_RIGHT);
                break;
            case KeyEvent.VK_RIGHT:
                logger.fine("panning left");
                showHUDMessage("pan left", 1000);
                // ask the server to pan the camera left
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.PAN_LEFT);
                break;
            case KeyEvent.VK_UP:
                logger.fine("tilt up");
                showHUDMessage("tilt up", 1000);
                // ask the server to tilt the camera up
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.TILT_UP);
                break;
            case KeyEvent.VK_DOWN:
                logger.fine("tilt down");
                showHUDMessage("tilt down", 1000);
                // ask the server to tilt the camera down
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.TILT_DOWN);
                break;
            case KeyEvent.VK_C:
                logger.fine("centering");
                // ask the server to center the camera
                msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                        VideoModuleCellMessage.Action.CENTER);
                break;
            case KeyEvent.VK_Z:
                if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                    logger.fine("zooming in fully");
                    showHUDMessage("max zoom", 1000);
                    // ask the server to zoom in fully
                    msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                            VideoModuleCellMessage.Action.ZOOM_IN_FULLY);
                } else {
                    logger.fine("zooming out fully");
                    showHUDMessage("min zoom", 1000);
                    // ask the server to zoom out fully
                    msg = new VideoModuleCellMessage(this.getCell().getCellID(),
                            VideoModuleCellMessage.Action.ZOOM_OUT_FULLY);
                }
                break;
            case KeyEvent.VK_F:
                if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                    logger.fine("increasing frame rate");
                    setFrameRate(preferredFrameRate + 1);
                    if (isPlaying() == true) {
                        // already playing, re-start
                        start();
                    }
                } else {
                    logger.fine("decreasing frame rate");
                    setFrameRate((preferredFrameRate > 0) ? preferredFrameRate - 1 : 0);
                    if (isPlaying() == true) {
                        // already playing, re-start
                        start();
                    }
                }
                break;
            case KeyEvent.VK_S:
                setSynced(!isSynced());
                break;
            case KeyEvent.VK_P:
                boolean willPlay = !isPlaying();
                // play/pause local player
                play(willPlay);
                // notify other clients of play state change
                requestPlay(willPlay);
                break;
        }

        if (msg != null) {
            logger.info("sending message: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    private void dispatchMouseEvent(MouseEvent e) {
        switch (e.getID()) {
            case MouseEvent.MOUSE_CLICKED:
                //play(!isPlaying());
                break;
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

    private class FrameUpdateTask extends TimerTask {

        public void run() {
            if (snapper != null) {
                if (snapper.getPlayerState() == Player.Started) {
                    VideoModuleApp.this.repaint();
                } else {
                    logger.info(">>> stopping frame update task because movie isn't playing <<");
                    stop();
                }
            }
        }
    }

    /**
     * Paint contents of window
     */
    @Override
    protected void paint(Graphics2D g) {
        if (drawingSurface != null) {
            // drawingSurface.paint(g);
            if (snapper != null) {
                BufferedImage frame = snapper.getFrame();

                if (frame != null) {
                    g.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
//                    g.drawImage(frame,
//                            getWidth(), 0, 0, getHeight(),
//                            0, 0, frame.getWidth(), frame.getHeight(),
//                            null);
                }
            }
        }
    }
}
