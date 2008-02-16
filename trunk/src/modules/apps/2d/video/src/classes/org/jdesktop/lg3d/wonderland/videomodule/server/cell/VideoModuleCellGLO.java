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
package org.jdesktop.lg3d.wonderland.videomodule.server.cell;

import com.sun.sgs.app.ClientSession;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.media.j3d.Bounds;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellSetup;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.videomodule.common.PTZCamera;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellMessage.Action;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoSourcer;

/**
 * @author nsimpson
 */
public class VideoModuleCellGLO extends SharedApp2DImageCellGLO
        implements BeanSetupGLO, CellMessageListener {
    
    private static final Logger logger =
            Logger.getLogger(VideoModuleCellGLO.class.getName());
    
    private BasicCellGLOSetup<VideoModuleCellSetup> setup;
    private transient VideoSourcer videoInstance;
    
    public VideoModuleCellGLO() {
        this(null, null, null, null);
    }
    
    public VideoModuleCellGLO(Bounds bounds, String appName, Matrix4d cellOrigin,
            Matrix4f viewRectMat) {
        super(bounds, appName, cellOrigin, viewRectMat, VideoModuleCellGLO.class.getName());
    }
    
    
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
    @Override
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.videomodule.client.cell.VideoModuleCell";
    }
    
    @Override
    public VideoModuleCellSetup getSetupData() {
        return setup.getCellSetup();
    }
    
    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup setupData) {
        setup = (BasicCellGLOSetup<VideoModuleCellSetup>) setupData;
        
        AxisAngle4d aa = new AxisAngle4d(setup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setup.getOrigin());
        
        Matrix4d o = new Matrix4d(rot, origin, setup.getScale() );
        setOrigin(o);
        
        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float)setup.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
        }
    }
    
    /**
     * Called when the properties of a cell have changed.
     *
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellGLOSetup setupData) {
        setupCell(setupData);
    }
    
    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellGLOSetup getCellGLOSetup() {
        return new BasicCellGLOSetup<VideoModuleCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }
    
    private class PTZAction implements Runnable {
        private PTZCamera ptz;
        private Action action;
        
        public PTZAction(PTZCamera ptz, Action action) {
            this.ptz = ptz;
            this.action = action;
        }
        
        public void run() {
            if (ptz != null) {
                switch (action) {
                    case PAN_LEFT:
                        ptz.panBy(-10);
                        break;
                    case PAN_RIGHT:
                        ptz.panBy(10);
                        break;
                    case PAN_CENTER:
                        ptz.panTo(0);
                        break;
                    case TILT_UP:
                        ptz.tiltBy(10);
                        break;
                    case TILT_DOWN:
                        ptz.tiltBy(-10);
                        break;
                    case TILT_CENTER:
                        ptz.tiltTo(0);
                        break;
                    case ZOOM_IN:
                        ptz.zoomBy(1000);
                        break;
                    case ZOOM_IN_FULLY:
                        ptz.zoomInFully();
                        break;
                    case ZOOM_OUT:
                        ptz.zoomBy(-1000);
                        break;
                    case ZOOM_OUT_FULLY:
                        ptz.zoomOutFully();
                        break;
                    case CENTER:
                        ptz.center();
                        break;
                }
            }
        }
    }
    
    private void processCameraAction(VideoSourcer videoInstance, Action action) {
        if (videoInstance instanceof PTZCamera) {
            logger.info("--- processing PTZ action: " + action);
            
            PTZCamera ptz = (PTZCamera)videoInstance;
            PTZAction ptzAction = new PTZAction(ptz, action);
            new Thread(ptzAction).start();
        }
    }
    
    public void receivedMessage(ClientSession client, CellMessage message) {
        VideoModuleCellMessage vmcm = (VideoModuleCellMessage) message;
        logger.fine("--- received msg: " + vmcm);
        
        Action action = vmcm.getAction();
        videoInstance = setup.getCellSetup().getVideoInstance();
        VideoModuleCellMessage msg = null;
        
        // generic video actions
        if (action == Action.SET_SOURCE) {
            msg = new VideoModuleCellMessage(action);
            msg.setVideoURL(vmcm.getVideoURL());
        } else if (action == Action.SET_POSITION) {
            msg = new VideoModuleCellMessage(action);
            msg.setVideoURL(vmcm.getVideoURL());
            msg.setState(vmcm.getState());
            msg.setPosition(vmcm.getPosition());
        } else if ((action == Action.PLAY) || (action == Action.PAUSE) ||
                (action == Action.STOP)) {
            msg = new VideoModuleCellMessage(action);
            msg.setVideoURL(vmcm.getVideoURL());
            msg.setState(vmcm.getState());
            msg.setPosition(vmcm.getPosition());
        } else if (action == Action.GET_STATUS) {
            msg = new VideoModuleCellMessage(action);
        } else if (action == Action.STATUS) {
            msg = new VideoModuleCellMessage(action);
            msg.setVideoURL(vmcm.getVideoURL());
            msg.setState(vmcm.getState());
            msg.setPosition(vmcm.getPosition());
        } else {
            // pan/tilt/zoom camera specific actions?
            if (videoInstance instanceof PTZCamera) {
                processCameraAction(videoInstance, action);
            }
        }
        
        if (msg != null) {
            logger.fine("--- sending msg: " + msg);
            // notify all clients execpt the sender
            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
            sessions.remove(client);
            getCellChannel().send(sessions, msg.getBytes());
        }
    }
}
