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

/**
 *
 * @author nsimpson
 */
public class Axis212PTZCamera implements PTZCamera {
    
    private static final Logger logger =
            Logger.getLogger(Axis212PTZCamera.class.getName());
    
    private String cameraAddress;
    private String commandCGI = "axis-cgi/com/ptz.cgi?camera=1";
    private String panCommand = "pan";
    private String tiltCommand = "tilt";
    private String zoomCommand = "zoom";
    
    private int MIN_PAN = -49;
    private int MAX_PAN = 49;
    
    private int MIN_TILT = -36;
    private int MAX_TILT = 36;
    
    private int MIN_ZOOM = 200;
    private int MAX_ZOOM = 9740;
    
    private int pan = 0;
    private int tilt = 0;
    private int zoom = 0;
    
    public Axis212PTZCamera() {
        
    }
    
    public Axis212PTZCamera(String cameraAddress) {
        this.cameraAddress = cameraAddress;
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
    
    private synchronized void sendCameraCommand(String url) {
        try {
            URL cameraURL = new URL(url);
            URLConnection connection = cameraURL.openConnection();
            connection.connect();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            in.close();
        } catch (Exception e) {
            logger.warning("failed to connect to camera: " + e);
        }
    }
    
    public synchronized void tiltTo(int tilt) {
        this.tilt = (tilt < MIN_TILT) ? MIN_TILT : ((tilt > MAX_TILT) ? MAX_TILT : tilt);
        
        sendCameraCommand(cameraAddress + "/" + commandCGI + "&" + tiltCommand + "=" + tilt);
    }
    
    public synchronized void panTo(int pan) {
        this.pan = (pan < MIN_PAN) ? MIN_PAN : ((pan > MAX_PAN) ? MAX_PAN : pan);
        
        sendCameraCommand(cameraAddress + "/" + commandCGI + "&" + panCommand + "=" + pan);
    }
    
    public synchronized void zoomTo(int zoom) {
        this.zoom = (zoom < MIN_ZOOM) ? MIN_ZOOM : ((zoom > MAX_ZOOM) ? MAX_ZOOM : zoom);
        
        sendCameraCommand(cameraAddress + "/" + commandCGI + "&" + zoomCommand + "=" + this.zoom);
    }
    
    public int getMinPan() {
        return MIN_PAN;
    }
    
    public int getMaxPan() {
        return MAX_PAN;
    }
    
    public int getPan() {
        return pan;
    }
    
    public int getMinTilt() {
        return MIN_TILT;
    }
    
    public int getMaxTilt() {
        return MAX_TILT;
    }
    
    public int getTilt() {
        return tilt;
    }
    
    public int getMinZoom() {
        return MIN_ZOOM;
    }
    
    public int getMaxZoom() {
        return MAX_ZOOM;
    }
    
    public int getZoom() {
        return zoom;
    }
    
    public synchronized void panBy(int delta) {
        panTo(pan + delta);
    }
    
    public synchronized void tiltBy(int delta) {
        tiltTo(tilt + delta);
    }
    
    public synchronized void zoomBy(int delta) {
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
        for (int z = MIN_ZOOM-800;z <= MAX_ZOOM + 800;z += 200) {
            logger.fine("zooming to: " + z);
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

