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
/**
 * This code derived from:
 *
 * JMFSnapper.java
 * Andrew Davison, May 2005, ad@fivedots.coe.psu.ac.th
 */
/* The specified movie is loaded into a JMF player, and
played continuously in a loop until stopMovie() is called.
The player is not displayed, instead the user accesses
the current frame in the movie by called getFrame(). It
returns the image as a BufferedImage object of type
BufferedImage.TYPE_3BYTE_BGR, and dimensions
FORMAT_SIZE x FORMAT_SIZE. The image has the current time
in hours:minutes.seconds.milliseconds written on top of it.
The original dimensions of the image in the movie can be
retrieved by calling getImageWidth() and getImageHeight().
----
For best performance, the movie should be in MPEG-1 format
with _no_ audio track.
If the movie does have an audio track, then the JMF player
used here will be slow to start, and frame grabbing (using
JMF's FrameGrabbingControl class) will be erratic --
e.g. there may be several seconds when the frame does not
change.
----
This code does not allow the user to retrieve a specific
frame. I did want to do this (e.g. see the Quicktime version
of this application), but I couldn't get JMF's
FramePositioningControl class to work in a reliable manner.
 */
package org.jdesktop.lg3d.wonderland.videomodule.common;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.media.*;
import javax.media.Time;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.util.*;

public class JMFSnapper implements ControllerListener {

    /** a logger */
    private static final Logger logger =
            Logger.getLogger(JMFSnapper.class.getName());
    // size of BufferedImage; should be a power of 2, less than 512,
    // or older graphic cards may get upset when using it as a texture
    private static final int FORMAT_SIZE = 512;
    // used while waiting for the BufferToImage object to be initialized
    private static final int MAX_TRIES = 5;
    private static final int TRY_PERIOD = 2000;   // ms
    // when getting the next frame, wait up to
    // PER_FRAME_MAX_TRIES*PER_FRAME_TRY_PERIOD milliseconds
    // The default is 50ms which allows for a maximum frame rate of 20fps
    private static final int PER_FRAME_MAX_TRIES = 10;
    private static final int PER_FRAME_TRY_PERIOD = 5;   // ms
    public static final int RESET_STOP_TIME = -1;
    private Player p;
    private FrameGrabbingControl fg;
    private BufferToImage bufferToImage = null;
    private int width,  height;          // frame dimensions
    // used for waiting until the player has started
    private Object waitSync = new Object();
    private boolean stateTransitionOK = true;
    private boolean showTimer = false;
    private BufferedImage bi = null;
    private Graphics graphics = null;

    public JMFSnapper(String fnm) {
        // utilise the native modular player so frame grabbing is available
        Manager.setHint(Manager.PLUGIN_PLAYER, new Boolean(true));
//      Manager.setHint(Manager.CACHING, new Boolean(true));
//      Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true));

        // create a realized player
        try {
            if ((new File(fnm)).exists()) {
                p = Manager.createRealizedPlayer(new URL("file:" + fnm));
            } else {
                p = Manager.createRealizedPlayer(new MediaLocator(fnm));
            }

            logger.fine("created player for: " + fnm);
        } catch (Exception e) {
            logger.severe("failed to create player for: " + fnm + ": " + e);
        }

        if (p != null) {
            p.addControllerListener(this);

            // create the frame grabber
            fg = (FrameGrabbingControl) p.getControl(
                    "javax.media.control.FrameGrabbingControl");
            if (fg == null) {
                logger.severe("frame grabber could not be created");
            } else {
                // check if the player has a visual component
                if (p.getVisualComponent() == null) {
                    logger.severe("no visual component found");
                } else {
                    // wait until the player has started
                    logger.fine("starting player...");
                    p.start();
                    if (!waitForStart()) {
                        logger.severe("failed to start the player.");
                    } else {
                        waitForBufferToImage(TRY_PERIOD, MAX_TRIES);
                    }
                }
            }
        }
    }

    /**
     * Get if a player exists
     * @return true if a player exists, false otherwise
     */
    public boolean hasPlayer() {
        return (p != null);
    }

    /**
     * wait for the player to enter its Started state
     */
    private boolean waitForStart() {
        synchronized (waitSync) {
            try {
                while (p.getState() != Controller.Started && stateTransitionOK) {
                    waitSync.wait();
                }
            } catch (Exception e) {
            }
        }
        return stateTransitionOK;
    }

    /**
     * Wait for the BufferToImage object to be initialized.
     * Movies with an audio track may take several seconds to
     * initialize this object, so this method makes up to 'tries' attempts.
     * @param wait the amount of time to wait between tries in milliseconds
     * @param tries the number of times to try waiting for the image
     */
    private synchronized boolean waitForBufferToImage(int wait, int tries) {
        boolean hasImage = false;
        int tryCount = tries;

        while (tryCount > 0) {
            if (hasBufferToImage()) {   // initialization succeeded
                hasImage = true;
                break;
            }
            try {   // initialization failed so wait a while and try again
                logger.fine("waiting for image...");
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                logger.fine("failed to initialize buffer for image: " + e);
            }
            tryCount--;
        }

        if (tryCount == 0) {
            logger.severe("timed out waiting for image");
        }
        return hasImage;
    }

    /**
     * The BufferToImage object is initialized here, so that when
     * getFrame() is called later, the snap can be quickly changed to
     * an image.
     * The object is initialized by taking a snap, which
     * may be an actual picture or be 'empty'.
     * An 'empty' snap is a Buffer object with no video information,
     * as detected by examining its component VideoFormat data.
     * An 'empty' snap is caused by the delay in the player, which
     * although in its started state still takes several seconds to
     * start playing the movie. This delay occurs when the movie has a
     * video and audio track.
     * There's no delay if the movie only has a video track.
     */
    private boolean hasBufferToImage() {
        Buffer buf = fg.grabFrame();     // take a snap

        if (buf == null) {
            logger.warning("no grabbed frame");
            return false;
        }

        // there is a buffer, but check if it's empty or not
        VideoFormat vf = (VideoFormat) buf.getFormat();
        if (vf == null) {
            logger.warning("no video format");
            return false;
        }

        width = vf.getSize().width;     // extract the image's dimensions
        height = vf.getSize().height;

        // initialize bufferToImage with the video format info.
        bufferToImage = new BufferToImage(vf);
        return true;
    }

    /**
     * stopMovie() and getFrame() are synchronized so that it's not
     * possible to close down the player while a frame is being
     * copied from its movie.
     */
    public synchronized void stopMovie() {
        if (p != null) {
            p.stop();
        }
    }

    /**
     * startMovie() and getFrame() are synchronized so that it's not
     * possible to close down the player while a frame is being
     * copied from its movie.
     */
    public synchronized void startMovie() {
        if (p != null) {
            p.start();
            if (!waitForStart()) {
                logger.severe("failed to start the player.");
            }
        }
    }

    /**
     * Set the play position
     * @param time the position in seconds
     */
    public synchronized void setPosition(double time) {
        setPositionSeconds(time);
    }

    /**
     * Set the play position
     * @param time the position in nanoseconds
     */
    public synchronized void setPositionNanoseconds(long time) {
        if (p != null) {
            // can only set time on a stopped player
            if (p.getState() == Player.Started) {
                p.stop();
            // REMIND: restart now?
            }
            p.setMediaTime(new Time(time));
        }
    }

    /**
     * Set the play position
     * @param time the position in seconds
     */
    public synchronized void setPositionSeconds(double time) {
        if (p != null) {
            boolean resume = false;
            // can only set time on a stopped player
            if (p.getState() == Player.Started) {
                resume = true;
                p.stop();
            }
            p.setMediaTime(new Time(time));
            if (resume) {
                p.start();
            }
        }
    }

    /**
     * Get the play position
     * @return the play position in seconds
     */
    public synchronized double getPosition() {
        return getPositionSeconds();
    }

    /**
     * Get the play position
     * @return the play position in seconds
     */
    public synchronized double getPositionSeconds() {
        double position = 0;

        if (p != null) {
            position = p.getMediaTime().getSeconds();
        }

        return position;
    }

    /**
     * Get the play position
     * @return the play position in nanoseconds
     */
    public synchronized long getPositionNanoseconds() {
        long position = 0;

        if (p != null) {
            position = p.getMediaTime().getNanoseconds();
        }

        return position;
    }

    /**
     * Set a stop time which is when the playback will stop
     * @param time the time in seconds or RESET_STOP_TIME to clear the
     * stop time
     */
    public synchronized void setStopTime(double time) {
        if (p != null) {
            if (time == RESET_STOP_TIME) {
                if (p.getStopTime() != Clock.RESET) {
                    p.setStopTime(Clock.RESET);
                }
            } else {
                p.setStopTime(new Time(time));
            }
        }
    }

    /**
     * Mute or unmute the player audio
     * @param muting if true mutes audio, else unmutes audio
     */
    public void mute(boolean muting) {
        if (hasPlayer() == true) {
            if (p.getGainControl() != null) {
                p.getGainControl().setMute(muting);
            }
        }
    }

    /**
     * Gets whether the audio is muted
     * @return true if audio is muted, false otherwise
     */
    public boolean isMuted() {
        boolean muted = false;
        if (hasPlayer() == true) {
            if (p.getGainControl() != null) {
                muted = p.getGainControl().getMute();
            }
        }
        return muted;
    }

    /** 
     * Get the duraiton of the media
     * @return the medai duration in nanoseconds
     */
    public long getDuration() {
        long duration = 0;
        if (p != null) {
            duration = p.getDuration().getNanoseconds();
        }
        return duration;
    }

    /**
     * Show or hide timer
     */
    public void showTimer(boolean show) {
        showTimer = show;
    }

    /**
     * Get the internal state of the player
     * States are:
     *   Unrealized   (100)
     *   Realizing    (200)
     *   Realized     (300)
     *   Prefetching  (400)
     *   Prefetched   (500) "stopped"
     *   Started      (600) "playing"
     * @return the player state as an integer
     */
    public int getPlayerState() {
        int state = 0;
        if (p != null) {
            state = p.getState();
        }
        return state;
    }

    /**
     * Grab a frame from the movie.
     * The frame must be converted from Buffer object to Image,
     * and finally to BufferedImage. The current time is written
     * on top of the image when it's converted to a BufferedImage.
     */
    synchronized public BufferedImage getFrame() {
        if (fg != null) {
            // grab the current frame as a buffer object
            Buffer buf = fg.grabFrame();

//            waitForBufferToImage(PER_FRAME_TRY_PERIOD, PER_FRAME_MAX_TRIES);

            if (buf == null) {
                logger.warning("no grabbed buffer");
                return null;
            }

            // use JMF to decode frame into a java.awt.Image
            Image im = bufferToImage.createImage(buf);

            if (im == null) {
                // if there was no luck converting the data using JMF, try imageIO
                // XXX this could be very expensive XXX
                try {
                    bi = ImageIO.read(new ByteArrayInputStream((byte[]) buf.getData()));
                } catch (IOException ioe) {
                    logger.warning("failed to read image: " + ioe);
                }
                if (bi == null) {
                    // oh well, can't be converted
                    logger.warning("unable to decode frame: " + buf.getFormat());
                }
            } else {
                // convert the Image to a BufferedImage
                if ((bi == null) || (im.getWidth(null) != bi.getWidth()) || (im.getHeight(null) != bi.getHeight())) {
                    bi = new BufferedImage(im.getWidth(null), im.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB);
                    graphics = bi.createGraphics();
                }
                graphics.drawImage(im, 0, 0, im.getWidth(null), im.getHeight(null), null);
            }

            // Overlay current time on top of the image
            if ((bi != null) && (showTimer == true)) {
                Graphics g = bi.createGraphics();
                g.drawImage(im, 0, 0, im.getWidth(null), im.getHeight(null), null);
                g.setColor(Color.RED);
                g.setFont(new Font("Helvetica", Font.BOLD, 12));
                g.drawString(timeNow(), 5, 14);
                g.dispose();
            }
        }
        return bi;
    }

    /**
     * return hours:minutes.seconds.milliseconds
     */
    private String timeNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss.SSS");
        Calendar now = Calendar.getInstance();
        return (sdf.format(now.getTime()));
    }

    /**
     * respond to events
     */
    public void controllerUpdate(ControllerEvent evt) {
        if (evt instanceof StartEvent) {
            // the player has started
            synchronized (waitSync) {
                stateTransitionOK = true;
                waitSync.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
                // there was a problem getting a player resource
                stateTransitionOK = false;
                waitSync.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            p.setMediaTime(new Time(0));
            p.stop();
        }
    }

    public int getImageWidth() {
        return width;
    }

    public int getImageHeight() {
        return height;
    }
}

