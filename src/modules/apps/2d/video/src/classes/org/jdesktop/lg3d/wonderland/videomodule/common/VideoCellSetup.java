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
package org.jdesktop.lg3d.wonderland.videomodule.common;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.SharedApp2DImageCellSetup;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.PlayerState;

/**
 *
 * @author nsimpson
 */
public class VideoCellSetup extends SharedApp2DImageCellSetup {

    private static final Logger logger =
            Logger.getLogger(VideoCellSetup.class.getName());
    private String source;
    private String videoClass;
    private VideoSource videoInstance;
    private long controlTimeout = 90 * 1000; // how long a client can retain control (ms)
    private float frameRate = 2.0f;     // frames per second
    private boolean playOnLoad = false; // don't auto play
    private boolean synced = true;      // sync with other clients by default
    private double preferredWidth = 0;  // none, use media width
    private double preferredHeight = 0; // none, use media height
    private boolean decorated = true;   // show window decorations
    private float pixelScale = 1.0f;    // scale factor when mapping from pixels to world units
    private boolean panoramic = false;  // for viewing pan/tilt/zoom camera video
    // video run time state
    private PlayerState playerState = PlayerState.STOPPED;
    private double position = 0.5;      // start position (seconds)
    // panorama run time state
    private float pan = 0.0f;
    private float tilt = 0.0f;
    private float zoom = 0.0f;
    private long requestThrottle = 2500; // limit requests to at most this often (milliseconds)

    public VideoCellSetup() {
        this(null, null, null);
    }

    public VideoCellSetup(String appName, Matrix4f viewRectMat, String clientClassName) {
        super(appName, viewRectMat, clientClassName);
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setPlayOnLoad(boolean playOnLoad) {
        this.playOnLoad = playOnLoad;
        this.playerState = (playOnLoad == true) ? PlayerState.PLAYING : PlayerState.STOPPED;
    }

    public boolean getPlayOnLoad() {
        return playOnLoad;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public boolean getSynced() {
        return synced;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setPreferredWidth(double preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredHeight(double preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    /** 
     * Set the window decoration status
     * @param decorated whether to show or hide the window decorations
     */
    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    /**
     * Get the window decoration status
     * @return true if the window decorations are enabled, false otherwise
     */
    public boolean getDecorated() {
        return decorated;
    }

    public void setPixelScale(float pixelScale) {
        this.pixelScale = pixelScale;
    }

    public float getPixelScale() {
        return pixelScale;
    }

    public void setVideoClass(String videoClass) {
        this.videoClass = videoClass;
        try {
            Class clss = Class.forName(this.videoClass);
            Constructor cons = clss.getConstructor(new Class[]{String.class});
            videoInstance = (VideoSource) cons.newInstance(this.source);
            logger.fine("video source: " + source);
        } catch (ClassNotFoundException e) {
            logger.warning("unrecognized camera class: " + this.videoClass + ": " + e);
        } catch (Exception e) {
            logger.severe("failed to create instance of: " + this.videoClass + ": " + e);
        }
    }

    public String getVideoClass() {
        return videoClass;
    }

    /**
     * Set the timeout for client requests
     * Clients that take longer than this time to process a request
     * will lose control
     * @param controlTimeout maximum time in milliseconds that a client can
     * retain control
     */
    public void setControlTimeout(long controlTimeout) {
        this.controlTimeout = controlTimeout;
    }

    /**
     * Get the control timeout (the length of time a client can have 
     * control before the server takes control away from the client)
     * @return the control timeout in milliseconds
     */
    public long getControlTimeout() {
        return controlTimeout;
    }

    public void setState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public PlayerState getState() {
        return playerState;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public double getPosition() {
        return position;
    }

    public void setPanoramic(boolean panoramic) {
        this.panoramic = panoramic;
    }

    public boolean getPanoramic() {
        return panoramic;
    }

    public void setPan(float pan) {
        this.pan = pan;
    }

    public float getPan() {
        return pan;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public float getTilt() {
        return tilt;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public VideoSource getVideoInstance() {
        return videoInstance;
    }

    public void setRequestThrottle(long requestThrottle) {
        this.requestThrottle = requestThrottle;
    }

    public long getRequestThrottle() {
        return requestThrottle;
    }
}
