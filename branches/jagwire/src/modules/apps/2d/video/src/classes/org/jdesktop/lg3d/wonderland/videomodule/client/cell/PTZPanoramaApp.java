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
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.RequestStatus;
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
    private static final float VIDEO_OVERLAY_Z = 0.04f;
    private float horizFOV = 0.0f;
    private float vertFOV = 0.0f;
    private float horizPixelsPerDegree = 0.0f;
    private float vertPixelsPerDegree = 0.0f;
    private float panoramaWidth;
    private float panoramaHeight;
    private float videoWidth;
    private float videoHeight;
    private BufferedImage snapshot = null;
    private PanoramaVideo video;

    public PTZPanoramaApp(SharedApp2DImageCell cell) {
        super(cell);
    }

    public PTZPanoramaApp(SharedApp2DImageCell cell, int x, int y, int width, int height,
            boolean decorated) {
        super(cell, x, y, width, height, decorated);
    }

    @Override
    public void setVideoInstance(VideoSource videoInstance) {
        logger.finest("ptz panorama: cell origin: " + cell.getCellOrigin());
        logger.finest("ptz panorama: cell bounds: " + cell.getBounds());

        this.videoInstance = videoInstance;
        if (videoInstance instanceof PTZCamera) {
            ptz = (PTZCamera) videoInstance;

            // visible horizontal field of view in degrees
            horizFOV = ptz.getMaxPan() - ptz.getMinPan() + ptz.getMinHorizontalFOV();
            // visible vertical field of view in degrees
            vertFOV = ptz.getMaxTilt() - ptz.getMinTilt() + ptz.getMinVerticalFOV();

            panoramaHeight = this.getHeight();
            panoramaWidth = this.getWidth();

            horizPixelsPerDegree = panoramaWidth / horizFOV;
            vertPixelsPerDegree = panoramaHeight / vertFOV;

            videoWidth = ptz.getMinHorizontalFOV() * horizPixelsPerDegree;
            videoHeight = ptz.getMinVerticalFOV() * vertPixelsPerDegree;

            setPreferredWidth(panoramaWidth);
            setPreferredHeight(panoramaHeight);

            logger.finest("ptz panorama: horizontal FOV: " + horizFOV);
            logger.finest("ptz panorama: vertical FOV: " + vertFOV);
            logger.finest("ptz panorama: horiz pixels per degree: " + horizPixelsPerDegree);
            logger.finest("ptz panorama: vert pixels per degree: " + vertPixelsPerDegree);
            logger.finest("ptz panorama: panorama w: " + panoramaWidth);
            logger.finest("ptz panorama: panorama h: " + panoramaHeight);
            logger.finest("ptz panorama: video w: " + videoWidth);
            logger.finest("ptz panorama: video h: " + videoHeight);
            logger.finest("ptz panorama: my preferred dimensions: " + panoramaWidth + "x" + panoramaHeight);
            logger.finest("ptz panorama: panorama native to physical width: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaWidth));
            logger.finest("ptz panorama: panorama native to physical height: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaHeight));
            logger.finest("ptz panorama: video native to physical width: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) videoWidth));
            logger.finest("ptz panorama: video native to physical height: " + Toolkit3D.getToolkit3D().widthNativeToPhysical((int) videoHeight));

            // force maximum zoom
            ptz.zoomTo(ptz.getMaxZoom());

            video = new PanoramaVideo(PTZToPhysical(ptz.getPan(), ptz.getTilt(), VIDEO_OVERLAY_Z),
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
                logger.info("ptz panorama: refreshing panorama");
                showHUDMessage("refreshing...", 45000);
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
        int pixels = (int) ((angle + ptz.getMaxPan()) * horizPixelsPerDegree);
        return pixels;
    }

    private int tiltAngleToPixels(float angle) {
        int pixels = (int) Math.abs((angle - ptz.getMaxTilt()) * vertPixelsPerDegree);
        return pixels;
    }

    private float xCoordToAngle(float x) {
        float w = getWidth();       // width of app in pixels

        float cx = w / 2f;
        float hf = horizFOV / 2f;   // visible field of view left or right of center

        float px = x - cx;          // distance in pixels from center

        float angle = (px / cx) * hf;     // angular offset from center

        logger.finest("ptz panorama: x: " + x + " = angle: " + angle);
        angle = (Math.abs(angle) > ptz.getMaxPan()) ? Math.signum(angle) * ptz.getMaxPan() : angle;

        return angle;
    }

    private float yCoordToAngle(float y) {
        float h = getHeight();          // height of app in pixels

        float cy = h / 2f;
        float vf = vertFOV / 2f;        // visible field of view above or below center

        float py = y - cy;              // distance in pixels from center

        float angle = -(py / cy) * vf;  // angular offset from center

        logger.finest("ptz panorama: y: " + y + " = angle: " + angle);
        angle = (Math.abs(angle) > ptz.getMaxTilt()) ? Math.signum(angle) * ptz.getMaxTilt() : angle;

        return angle;
    }

    public Point3f PTZToPhysical(float pan, float tilt, float zoom) {
        Point3f physical = new Point3f();
        float panoramaPhysicalWidth = Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaWidth);
        float panoramaPhysicalHeight = Toolkit3D.getToolkit3D().widthNativeToPhysical((int) panoramaHeight);

        // pan angular range (+/-) from center of panorama
        float panRange = ptz.getMaxPan() + ptz.getMinHorizontalFOV() / 2f;
        // tilt angular range (+/-) from center of panorama
        float tiltRange = ptz.getMaxTilt() + ptz.getMinVerticalFOV() / 2f;

        // x center of video window
        float centerX = 0.0f;
        // y center of video window
        float centerY = 0.0f;

        // percent of pan range
        float panPercent = pan / panRange;
        // percent of tilt range
        float tiltPercent = tilt / tiltRange;

        // physical pan position
        float panPhysical = centerX + panPercent * panoramaPhysicalWidth / 2f;
        // physical tilt position
        float tiltPhysical = centerY + tiltPercent * panoramaPhysicalHeight / 2f;

        physical.set(panPhysical, tiltPhysical, zoom);

        return physical;
    }

    protected void refreshPanorama() {
        int horizontalPasses = (int) Math.ceil((double) panoramaWidth / (double) videoWidth);
        int verticalPasses = (int) Math.ceil((double) panoramaHeight / (double) videoHeight);
        int vw = (int) (horizPixelsPerDegree * ptz.getMinHorizontalFOV());
        int vh = (int) (vertPixelsPerDegree * ptz.getMinVerticalFOV());

        for (int v = 0; v < verticalPasses; v++) {
            float vy = 0.5f * vh + (float) v * vh;
            for (int h = 0; h < horizontalPasses; h++) {
                float vx = 0.5f * vw + (float) h * vw;
                logger.info("ptz panorama: requesting refresh of location: " +
                        xCoordToAngle(vx) + ", " + yCoordToAngle(vy));

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
        float pan, tilt, zoom;

        if (isSynced()) {
            logger.fine("ptz panorama: " + myUID + " received message: " + msg);
            if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED) {
                // a client may send a request while another app has control.
                // the server denies the conflicting request and the app must
                // the re-issue the request when the app currently in control
                // relinquishes control                
                try {
                    logger.info("ptz panorama: scheduling retry of request: " + msg);
                    retryCameraRequest(msg.getAction(), new Point3f(msg.getPan(), msg.getTilt(), msg.getZoom()));
                } catch (Exception e) {
                    logger.warning("ptz panorama: failed to create retry request for: " + msg);
                }
            } else {
                switch (msg.getAction()) {
                    case SET_PTZ:
                        // a request to adjust the camera's pan, tilt, zoom is starting

                        // take a snapshot of the current position of the camera before
                        // commencing the move
                        logger.fine("ptz panorama: taking snapshot");
                        if (snapper != null) {
                            snapshot = snapper.getFrame();
                            repaint();
                        }

                        // new PTZ values
                        pan = msg.getPan();
                        tilt = msg.getTilt();
                        zoom = msg.getZoom();

                        // only adjust the camera if this cell has control of the camera
                        if (forMe == true) {
                            // change the camera's pan, tilt or zoom settings
                            logger.fine("ptz panorama: performing action: " + msg.getAction());
                            moveCamera(msg.getAction(), pan, tilt, zoom);
                        }
                        logger.fine("ptz panorama: moving video window to: " + pan + ", " + tilt);
                        moveVideo(pan, tilt, VIDEO_OVERLAY_Z);
                        break;
                    default:
                        super.handleResponse(msg);
                        break;
                }
            }
        }
        if (vcm != null) {
            logger.fine("ptz panorama: sending message: " + vcm);
            ChannelController.getController().sendMessage(vcm);
        }
    }

    @Override
    protected void updateMenu() {
    }

    /**
     * Moves the video window to the corresponding pan, tilt, zoom position
     * @param pan the pan angle
     * @param tilt the tilt angle
     * @param zoom the zoom angle
     */
    public void moveVideo(float pan, float tilt, float zoom) {
        video.moveTo(PTZToPhysical(pan, tilt, zoom));
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

            logger.info("ptz panorama: painting snapshot at: " + ptz.getPan() + ", " + ptz.getTilt());

            g.drawImage(snapshot, px, py, (int) videoWidth, (int) videoHeight, null);
            snapshot = null;
        }
    }
}
