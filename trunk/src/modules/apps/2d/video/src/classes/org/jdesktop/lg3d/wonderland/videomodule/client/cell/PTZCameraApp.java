/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import org.jdesktop.j3d.util.SceneGraphUtil;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.videomodule.common.PTZCamera;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.Action;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoSource;

/**
 *
 * @author nsimpson
 */
public class PTZCameraApp extends VideoApp implements CameraListener {

    private static final Logger logger =
            Logger.getLogger(PTZCameraApp.class.getName());
    protected PTZCamera ptz;
    protected float horizOverlapPercent = 0.1f;
    protected float vertOverlapPercent = 0.1f;

    public PTZCameraApp(SharedApp2DImageCell cell) {
        super(cell);
    }

    public PTZCameraApp(SharedApp2DImageCell cell, int x, int y, int width, int height) {
        super(cell, x, y, width, height);
    }

    @Override
    public void setVideoInstance(VideoSource videoInstance) {
        this.videoInstance = videoInstance;
        if (videoInstance instanceof PTZCamera) {
            ptz = (PTZCamera) videoInstance;
            SceneGraphUtil.setCapabilitiesGraph(((SharedApp2DImageCell) cell).getCellLocal(), false);
        } else {
            super.setVideoInstance(videoInstance);
        }
    }

    @Override
    protected void dispatchKeyEvent(KeyEvent e) {
        float pan = ptz.getPan();
        float tilt = ptz.getTilt();
        float zoom = ptz.getZoom();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                logger.info("zooming in");
                showHUDMessage("zoom in", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(pan, tilt, zoom + 1000));
                break;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_UNDERSCORE:
                logger.info("zooming out");
                showHUDMessage("zoom out", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(pan, tilt, zoom - 1000));
                break;
            case KeyEvent.VK_LEFT:
                logger.info("panning left");
                showHUDMessage("pan left", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(pan - (1.0f - horizOverlapPercent) * ptz.getMinHorizontalFOV(), tilt, zoom));
                break;
            case KeyEvent.VK_RIGHT:
                logger.info("panning right");
                showHUDMessage("pan right", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(pan + (1.0f - horizOverlapPercent) * ptz.getMinHorizontalFOV(), tilt, zoom));
                break;
            case KeyEvent.VK_UP:
                logger.info("tilting up");
                showHUDMessage("tilt up", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(pan, tilt + (1.0f - vertOverlapPercent) * ptz.getMinVerticalFOV(), zoom));
                break;
            case KeyEvent.VK_DOWN:
                logger.info("tilting down");
                showHUDMessage("tilt down", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(pan, tilt - (1.0f - vertOverlapPercent) * ptz.getMinVerticalFOV(), zoom));
                break;
            case KeyEvent.VK_C:
                logger.info("centering");
                showHUDMessage("centering", 1000);
                sendCameraRequest(Action.SET_PTZ,
                        new Point3f(0, 0, ptz.getZoom()));
                break;
            case KeyEvent.VK_Z:
                if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                    logger.info("zooming in fully");
                    showHUDMessage("max zoom", 1000);
                    sendCameraRequest(Action.SET_PTZ,
                            new Point3f(pan, tilt, ptz.getMaxZoom()));
                } else {
                    logger.info("zooming out fully");
                    showHUDMessage("min zoom", 1000);
                    sendCameraRequest(Action.SET_PTZ,
                            new Point3f(pan, tilt, ptz.getMinZoom()));
                }
                break;
            case KeyEvent.VK_TAB:
                logger.fine("manual repaint!");
                repaint();
                break;
            default:
                // not a PTZ action, perhaps a generic video action
                super.dispatchKeyEvent(e);
                break;
        }
    }

    @Override
    protected void sendCameraRequest(Action action, Point3f position) {
        VideoCellMessage msg = null;

        switch (action) {
            case SET_PTZ:
                float p = position.getX();
                float t = position.getY();
                float z = position.getZ();
                // bounds check the angles to honor min/max angle contraints of camera
                p = (Math.abs(p) > ptz.getMaxPan()) ? Math.signum(p) * ptz.getMaxPan() : p;
                t = (Math.abs(t) > ptz.getMaxTilt()) ? Math.signum(t) * ptz.getMaxTilt() : t;
                z = (z > ptz.getMaxZoom()) ? ptz.getMaxZoom() : ((z < ptz.getMinZoom()) ? ptz.getMinZoom() : z);

                msg = new VideoCellMessage(getCell().getCellID(),
                        ((VideoCell) cell).getUID(), Action.SET_PTZ);
                msg.setState(PTZCameraApp.this.getState());
                msg.setPTZPosition(p, t, z);
                break;
            default:
                super.sendCameraRequest(action, position);
                break;
        }

        if (msg != null) {
            // send request to server
            logger.fine("sending camera request: " + msg);
            ChannelController.getController().sendMessage(msg);
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
            case REQUEST_DENIED:
                // this request was denied, create a retry thread
                retryCameraRequest(Action.SET_PTZ, new Point3f(msg.getPan(), msg.getTilt(), msg.getZoom()));
                break;
            case SET_PTZ:
                // a request to adjust the camera's pan, tilt, zoom is starting
                // new PTZ values
                float pan = msg.getPan();
                float tilt = msg.getTilt();
                float zoom = msg.getZoom();

                // only adjust the camera if this cell has control of the camera
                if (forMe == true) {
                    // change the camera's pan, tilt or zoom settings
                    logger.info("setting PTZ to pan: " + pan + ", tilt: " + tilt + ", zoom: " + zoom);
                    moveCamera(msg.getAction(), pan, tilt, zoom);
                }
                break;
            case REQUEST_COMPLETE:
                // save the camera's current pan, tilt, zoom values
                ptz.setPTZPosition(msg.getPan(), msg.getTilt(), msg.getZoom());

                // when another client's request completes, wakeup any threads
                // with pending actions, so they can resubmit their requests
                synchronized (actionLock) {
                    try {
                        logger.fine("waking retry threads");
                        actionLock.notify();
                    } catch (Exception e) {
                        logger.fine("exception notifying retry threads: " + e);
                    }
                }
                break;
            default:
                super.handleResponse(msg);
                break;
        }
        if (vcm != null) {
            logger.fine("sending message: " + vcm);
            ChannelController.getController().sendMessage(vcm);
        }
    }

    public void cameraActionComplete(Action action, float pan, float tilt, float zoom) {
        String myUID = ((VideoCell) cell).getUID();

        // notify everyone that the request has completed
        VideoCellMessage vcm = new VideoCellMessage(cell.getCellID(), myUID, action);
        vcm.setAction(Action.REQUEST_COMPLETE);
        vcm.setPTZPosition(pan, tilt, zoom);
        logger.fine("sending message: " + vcm);
        ChannelController.getController().sendMessage(vcm);
    }

    /**
     * Moves the camera to the specified pan, tilt, zoom position
     * @param pan the pan ange
     * @param tilt the tilt angle
     * @param zoom the zoom quotient
     */
    public void moveCamera(Action action, float pan, float tilt, float zoom) {
        CameraTask ptzTask = new CameraTask(ptz, action, pan, tilt, zoom, this);
        ptzTask.start();
    }
}
