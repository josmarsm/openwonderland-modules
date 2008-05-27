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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import sun.misc.BASE64Encoder;

/**
 *
 * @author nsimpson
 */
public class Axis212PTZCamera implements PTZCamera {

    private static final Logger logger =
            Logger.getLogger(Axis212PTZCamera.class.getName());
    
    private static String CAMERA_USERNAME = "admin";
    private static String CAMERA_PASSWORD = "admin";
    private String cameraAddress;
    private String commandCGI = "axis-cgi/com/ptz.cgi?camera=1";
    private String panCommand = "pan";
    private String tiltCommand = "tilt";
    private String zoomCommand = "zoom";
    private static float MIN_PAN = -49;
    private static float MAX_PAN = 49;
    private static float MIN_TILT = -36;
    private static float MAX_TILT = 36;
    private static float MIN_ZOOM = 200;
    private static float MAX_ZOOM = 9740;
    private static final float MIN_HORIZONTAL_FOV = 46f;   // degrees
    private static final float MIN_VERTICAL_FOV = 33f;     // degrees
    private static float pan = 0;
    private static float tilt = 0;
    private static float zoom = 0;

    public Axis212PTZCamera() {

    }

    public Axis212PTZCamera(String cameraAddress) {
        setSource(cameraAddress);
    }

    public void setSource(String cameraAddress) {
        this.cameraAddress = cameraAddress;
    }

    public String getSource() {
        return this.cameraAddress;
    }

    public void resetCameraPosition() {
        center();
        zoomOutFully();
    }

    private String base64EncodeAuth(String user, String password) {
        String account = user + ":" + password;
        String encoding = new BASE64Encoder().encode(account.getBytes());
        return "Basic " + encoding;
    }

    private synchronized void sendCameraCommand(String url) {
        try {
            URL cameraURL = new URL(url);
            URLConnection connection = cameraURL.openConnection();
            connection.setRequestProperty("Authorization", 
                    base64EncodeAuth(CAMERA_USERNAME, CAMERA_PASSWORD));
            connection.connect();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            in.close();
        } catch (Exception e) {
            logger.warning("failed to connect to camera: " + e);
        }
    }

    public synchronized void tiltTo(float newtilt) {
        tilt = (newtilt < MIN_TILT) ? MIN_TILT : ((newtilt > MAX_TILT) ? MAX_TILT : newtilt);
        logger.fine("tilt to: " + tilt);

        sendCameraCommand(cameraAddress + "/" + commandCGI + "&" + tiltCommand + "=" + (int)tilt);
    }

    public synchronized void panTo(float newpan) {
        pan = (newpan < MIN_PAN) ? MIN_PAN : ((newpan > MAX_PAN) ? MAX_PAN : newpan);
        logger.fine("pan to: " + pan);

        sendCameraCommand(cameraAddress + "/" + commandCGI + "&" + panCommand + "=" + (int)pan);
    }

    public synchronized void zoomTo(float newzoom) {
        if ((newzoom < MIN_ZOOM) || (newzoom > MAX_ZOOM)) {
            logger.warning("attempt to set invalid zoom: " + newzoom + " valid range (" + MIN_ZOOM + "-" + MAX_ZOOM + ")");
            zoom = MAX_ZOOM;
        } else {
            zoom = newzoom;
        }
        //zoom = (newzoom < MIN_ZOOM) ? MIN_ZOOM : ((newzoom > MAX_ZOOM) ? MAX_ZOOM : newzoom);
        logger.fine("zoom to: " + zoom);

        sendCameraCommand(cameraAddress + "/" + commandCGI + "&" + zoomCommand + "=" + (int)zoom);
    }

    public void setPTZPosition(float p, float t, float z) {
        pan = p;
        tilt = t;
        zoom = z;
    }
    
    public float getMinPan() {
        return MIN_PAN;
    }

    public float getMaxPan() {
        return MAX_PAN;
    }

    public float getPan() {
        return pan;
    }

    public float getMinTilt() {
        return MIN_TILT;
    }

    public float getMaxTilt() {
        return MAX_TILT;
    }

    public float getTilt() {
        return tilt;
    }

    public float getMinZoom() {
        return MIN_ZOOM;
    }

    public float getMaxZoom() {
        return MAX_ZOOM;
    }

    public float getZoom() {
        return zoom;
    }

    public float getMinHorizontalFOV() {
        return MIN_HORIZONTAL_FOV;
    }

    public float getMinVerticalFOV() {
        return MIN_VERTICAL_FOV;
    }

    public synchronized void panBy(float delta) {
        panTo(pan + delta);
    }

    public synchronized void tiltBy(float delta) {
        tiltTo(tilt + delta);
    }

    public synchronized void zoomBy(float delta) {
        zoomTo(zoom + delta);
    }

    public synchronized void center() {
        panTo(0);
        tiltTo(0);
    }

    public void zoomOutFully() {
        zoomTo(MIN_ZOOM);
    }

    public void zoomInFully() {
        zoomTo(MAX_ZOOM);
    }

    public void zoomTest() {
        for (float z = MIN_ZOOM - 800; z <= MAX_ZOOM + 800; z += 200) {
            logger.fine("zoom to: " + z);
            zoomTo(z);
        }
    }

    public void panTest() {
        try {
            zoomInFully();
            Thread.sleep(1000);
            center();
            Thread.sleep(1000);
            panTo(MIN_PAN);
            Thread.sleep(1000);
            center();
            Thread.sleep(1000);
            panTo(MAX_PAN);
            Thread.sleep(1000);
            center();
            Thread.sleep(1000);
            zoomOutFully();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.warning("interrupted: " + e);
        }
    }

    public void tiltTest() {
        try {
            zoomInFully();
            Thread.sleep(1000);
            center();
            Thread.sleep(1000);
            tiltTo(MIN_TILT);
            Thread.sleep(1000);
            center();
            Thread.sleep(1000);
            tiltTo(MAX_TILT);
            Thread.sleep(1000);
            center();
            Thread.sleep(1000);
            zoomOutFully();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.warning("interrupted: " + e);
        }
    }

    public static void main(String[] args) {
        try {
            Axis212PTZCamera camera = new Axis212PTZCamera("labcam.east");
            camera.panTest();
            camera.tiltTest();
        } catch (Exception e) {
            logger.warning("camera test failed: " + e);
        }
    }
}

