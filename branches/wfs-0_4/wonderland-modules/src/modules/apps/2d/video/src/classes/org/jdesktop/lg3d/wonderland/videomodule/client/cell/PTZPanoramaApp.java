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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.media.Player;
import javax.vecmath.Point3f;
import org.jdesktop.j3d.util.SceneGraphUtil;
import org.jdesktop.lg3d.wg.Toolkit3D;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.videomodule.common.PTZCamera;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.Action;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoSource;

/**
 *
 * Panoramic video application
 *
 * @author nsimpson
 */
public class PTZPanoramaApp extends PTZCameraApp {

    private static final Logger logger =
            Logger.getLogger(PTZPanoramaApp.class.getName());
    private static final float VIDEO_OVERLAY_Z = 0.01f;
    private float horizFOV = 0.0f;
    private float vertFOV = 0.0f;
    private float pixelsPerDegree = 0.0f;
    private float aspectRatio = 1.0f;
    private float panoramaWidth;
    private float panoramaHeight;
    private float videoWidth;
    private float videoHeight;
    private BufferedImage snapshot = null;
    private PanoramaVideo video;
    private static final Color inControlColor = Color.GREEN;
    private static final Color notInControlColor = Color.RED;

    public PTZPanoramaApp(SharedApp2DImageCell cell) {
        super(cell);
    }

    public PTZPanoramaApp(SharedApp2DImageCell cell, int x, int y, int width, int height) {
        super(cell, x, y, width, height);
    }

    @Override
    public void setVideoInstance(VideoSource videoInstance) {
        logger.finest("cell origin: " + cell.getCellOrigin());
        logger.finest("cell bounds: " + cell.getBounds());

        this.videoInstance = videoInstance;
        if (videoInstance instanceof PTZCamera) {
            ptz = (PTZCamera) videoInstance;

            // visible horizontal field of view in degrees
            horizFOV = ptz.getMaxPan() - ptz.getMinPan() + ptz.getMinHorizontalFOV();
            // visible vertical field of view in degrees
            vertFOV = ptz.getMaxTilt() - ptz.getMinTilt() + ptz.getMinVerticalFOV();

            pixelsPerDegree = (((float) this.getHeight()) / vertFOV);
            aspectRatio = horizFOV / vertFOV;
            panoramaHeight = this.getHeight();
            panoramaWidth = this.getWidth(); //(int) (horizFOV * pixelsPerDegree);

            videoWidth = ptz.getMinHorizontalFOV() * pixelsPerDegree;
            videoHeight = ptz.getMinVerticalFOV() * pixelsPerDegree;

            setPreferredWidth(panoramaWidth);
            setPreferredHeight(panoramaHeight);

            logger.fine("horizontal FOV: " + horizFOV);
            logger.fine("vertical FOV: " + vertFOV);
            logger.fine("aspect ratio: " + aspectRatio);
            logger.fine("pixels per degree: " + pixelsPerDegree);
            logger.fine("panorama w: " + panoramaWidth);
            logger.fine("panorama h: " + panoramaHeight);
            logger.fine("video w: " + videoWidth);
            logger.fine("video h: " + videoHeight);
            logger.fine("my preferred dimensions: " + panoramaWidth + "x" + panoramaHeight);
            logger.fine("panorama native to physical width: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaWidth));
            logger.fine("panorama native to physical height: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaHeight));
            logger.fine("video native to physical width: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) videoWidth));
            logger.fine("video native to physical height: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) videoHeight));

            // force maximum zoom
            ptz.zoomTo(ptz.getMaxZoom());

            video = new PanoramaVideo(panTiltToPhysical(ptz.getPan(), ptz.getTilt()),
                    Toolkit3D.getToolkit3D().widthNativeToPhysical((int) videoWidth));
            video.setVideo(null);
            ((SharedApp2DImageCell) cell).getCellLocal().addChild(video);
            SceneGraphUtil.setCapabilitiesGraph(((SharedApp2DImageCell) cell).getCellLocal(), false);
        } else {
            super.setVideoInstance(videoInstance);
        }
    }

    @Override
    protected void dispatchKeyEvent(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R:
                logger.fine("refresh panorama");
                showHUDMessage("refreshing...", 30000);
                refreshPanorama();
                break;
            default:
                // not a panorama action, perhaps a PTZ camera action
                super.dispatchKeyEvent(e);
                break;
        }
    }

    @Override
    protected void dispatchMouseEvent(MouseEvent e) {
        switch (e.getID()) {
            case MouseEvent.MOUSE_CLICKED:
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(xCoordToAngle(e.getX()), yCoordToAngle(e.getY()), ptz.getZoom()));
                break;
        }
    }

    private int panAngleToPixels(float angle) {
        int pixels = (int)(((horizFOV/2f) + (angle - ptz.getMinHorizontalFOV()/2f)) * pixelsPerDegree);
        logger.fine("+++ horizFOV/2f = " + horizFOV/2f);
        logger.fine("+++ angle = " + angle);
        logger.fine("+++ ptz.getMinHorizontalFOV = " + ptz.getMinHorizontalFOV());
        logger.fine("+++ horiz angle: " + angle + " = " + pixels);
        return pixels;
    }

    private int tiltAngleToPixels(float angle) {
        int pixels = (int) ((getHeight() / 2) - (angle * pixelsPerDegree) - ((ptz.getMinVerticalFOV() * pixelsPerDegree) / 2));
        logger.fine("+++ vert angle: " + angle + " = " + pixels);
        return pixels;
    }

    private float xCoordToAngle(float x) {
        float w = getWidth();       // width of app in pixels
        float cx = w / 2f;
        float hf = horizFOV / 2f;   // visible field of view left or right of center
        float px = x - cx;          // distance in pixels from center
        float angle = (px / cx) * hf;     // angular offset from center

        logger.finest("x: " + x + " = angle: " + angle);
        angle = (Math.abs(angle) > ptz.getMaxPan()) ? Math.signum(angle) * ptz.getMaxPan() : angle;

        return angle;
    }

    private float yCoordToAngle(float y) {
        float h = getHeight();      // height of app in pixels
        float cy = h / 2f;
        float vf = vertFOV / 2f;    // visible field of view above or below center
        float py = y - cy;          // distance in pixels from center
        float angle = -(py / cy) * vf;  // angular offset from center

        logger.finest("y: " + y + " = angle: " + angle);
        angle = (Math.abs(angle) > ptz.getMaxTilt()) ? Math.signum(angle) * ptz.getMaxTilt() : angle;

        return angle;
    }

    public Point3f panTiltToPhysical(float pan, float tilt) {
        Point3f physical = new Point3f();
        float panoramaPhysicalWidth = Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaWidth);
        float panoramaPhysicalHeight = Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaHeight);

        // pan angular range (+/-) from center of panorama
        float panRange = ptz.getMaxPan() + ptz.getMinHorizontalFOV() / 2;
        // tilt angular range (+/-) from center of panorama
        float tiltRange = ptz.getMaxTilt() + ptz.getMinVerticalFOV() / 2;

        // x center of video window
        float centerX = 0.0f;
        // y center of video window
        float centerY = 0.0f;

        // percent of pan range
        float panPercent = pan / panRange;
        // percent of tilt range
        float tiltPercent = tilt / tiltRange;

        // physical pan position
        float panPhysical = centerX + panPercent * panoramaPhysicalWidth / 2;
        // physical tilt position
        float tiltPhysical = centerY + tiltPercent * panoramaPhysicalHeight / 2;

        physical.set(panPhysical, tiltPhysical, VIDEO_OVERLAY_Z);

        logger.finest("pan: " + pan + ", tilt: " + tilt);
        logger.finest("pan range: " + panRange);
        logger.finest("tilt range: " + tiltRange);
        logger.finest("pan percent: " + panPercent);
        logger.finest("tilt percent: " + tiltPercent);
        logger.finest("panPhysical: " + panPhysical);
        logger.finest("tiltPhysical: " + tiltPhysical);
        logger.finest("physical: " + physical);

        return physical;
    }

    protected void refreshPanorama() {
        int horizontalPasses = (int) Math.ceil((double) panoramaWidth / (double) videoWidth);
        int verticalPasses = (int) Math.ceil((double) panoramaHeight / (double) videoHeight);

        for (int v = 0; v < verticalPasses; v++) {
            float vy = 0.5f * videoHeight + (float)v * videoHeight;
            for (int h = 0; h < horizontalPasses; h++) {
                float vx = 0.5f * videoWidth + (float)h * videoWidth;
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(xCoordToAngle(vx), yCoordToAngle(vy),
                        ptz.getZoom()));
            }
        }
    }

    @Override
    protected void handleResponse(VideoCellMessage msg) {
        String controlling = msg.getUID();
        String myUID = ((VideoCell) cell).getUID();
        boolean forMe = (myUID.equals(controlling));
        VideoCellMessage vcm = null;

        // a client may send a request while another camera has control.
        // the server denies the conflicting request and the client must
        // the re-issue the request when the client currently in control
        // relinquishes control
        switch (msg.getAction()) {
            case SET_PTZ:
                // a request to adjust the camera's pan, tilt, zoom is starting

                // take a snapshot of the current position of the camera before
                // commencing the move
                logger.fine("--- ptz changing: taking snapshot");
                snapshot = snapper.getFrame();
                repaint();

                // new PTZ values
                float pan = msg.getPan();
                float tilt = msg.getTilt();
                float zoom = msg.getZoom();

                // only adjust the camera if this cell has control of the camera
                if (forMe == true) {
                    // change the camera's pan, tilt or zoom settings
                    logger.fine("--- performing action: " + msg.getAction());
                    moveCamera(msg.getAction(), pan, tilt, zoom);

                    // notify everyone that the request has completed
                    vcm = new VideoCellMessage(msg);
                    vcm.setAction(Action.REQUEST_COMPLETE);
                }

                logger.fine("--- moving video window to: " + pan + ", " + tilt);
                moveVideo(pan, tilt, zoom);
                break;
            default:
                super.handleResponse(msg);
                break;
        }
        if (vcm != null) {
            logger.info("--- sending message: " + vcm);
            ChannelController.getController().sendMessage(vcm);
        }
    }

    /**
     * Moves the video window to the corresponding pan, tilt, zoom position
     * @param pan the pan angle
     * @param tilt the tilt angle
     * @param zoom the zoom angle
     */
    public void moveVideo(float pan, float tilt, float zoom) {
        video.moveTo(panTiltToPhysical(pan, tilt));
    }

    /**
     * Updates the video window with the current video frame
     */
    @Override
    protected void doFrameUpdate() {
        if ((snapper != null) && (snapper.getPlayerState() == Player.Started)) {
            video.setFrame(snapper.getFrame());
        }
    }

    /**
     * Paint panorama
     */
    @Override
    protected void paint(Graphics2D g) {
        if (snapshot != null) {
            int px = panAngleToPixels(ptz.getPan());
            int py = tiltAngleToPixels(ptz.getTilt());
            int vw = (int) (pixelsPerDegree * ptz.getMinHorizontalFOV());
            int vh = (int) (pixelsPerDegree * ptz.getMinVerticalFOV());
            
            logger.fine("+++ pan angle: " + ptz.getPan());
            logger.fine("+++ snapshot: " + snapshot.getWidth() + "x" + snapshot.getHeight());
            logger.fine("+++ position: " + px + ", " + py);
            logger.fine("+++ dimensions: " + videoWidth + "x" + videoHeight);
            
            g.drawImage(snapshot, px, py, (int)videoWidth + 12, (int)videoHeight, null);
            snapshot = null;
        }
    }
}
