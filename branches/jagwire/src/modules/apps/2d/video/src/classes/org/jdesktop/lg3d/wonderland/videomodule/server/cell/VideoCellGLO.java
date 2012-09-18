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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;

import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.media.j3d.Bounds;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellSetup;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.Action;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.PlayerState;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.RequestStatus;

/**
 * @author nsimpson
 */
public class VideoCellGLO extends SharedApp2DImageCellGLO
        implements ManagedObject, BeanSetupGLO, CellMessageListener {

    private static final Logger logger =
            Logger.getLogger(VideoCellGLO.class.getName());
    private static long controlTimeout = 90 * 1000; // how long a client can retain control (ms)
    private ManagedReference stateRef = null;

    public VideoCellGLO() {
        this(null, null, null, null);
    }

    public VideoCellGLO(Bounds bounds, String appName, Matrix4d cellOrigin,
            Matrix4f viewRectMat) {
        super(bounds, appName, cellOrigin, viewRectMat, VideoCellGLO.class.getName());
    }

    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }

    @Override
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.videomodule.client.cell.VideoCell";
    }

    /**
     * Get the setup data for this cell
     * @return the cell setup data
     */
    @Override
    public VideoCellSetup getSetupData() {
        return getStateMO().getCellSetup();
    }

    public VideoAppStateMO getStateMO() {
        VideoAppStateMO stateMO = null;
        if (stateRef != null) {
            stateMO = stateRef.get(VideoAppStateMO.class);
        }

        return stateMO;
    }

    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup data) {
        BasicCellGLOSetup<VideoCellSetup> setupData = (BasicCellGLOSetup<VideoCellSetup>) data;
        super.setupCell(setupData);

        VideoCellSetup vcs = setupData.getCellSetup();
        controlTimeout = vcs.getControlTimeout();

        if (getStateMO() == null) {
            // create a new managed object containing the setup data
            VideoAppStateMO stateMO = new VideoAppStateMO(setupData.getCellSetup());

            // create a managed reference to the new state managed object
            DataManager dataMgr = AppContext.getDataManager();
            stateRef = dataMgr.createReference(stateMO);
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
        return new BasicCellGLOSetup<VideoCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    /**
     * Hack to get the Cell Channel from the private method
     * @return the Cell Channel
     */
    public Channel getCellChannel2() {
        return getCellChannel();
    }

    /*
     * Handle message
     * @param client the client that sent the message
     * @param message the message
     */
    @Override
    public void receivedMessage(ClientSession client, CellMessage message) {
        if (message instanceof VideoCellMessage) {
            VideoCellMessage vmcm = (VideoCellMessage) message;
            logger.fine("video GLO: received msg: " + vmcm);

            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());

            // clone the message
            VideoCellMessage msg = new VideoCellMessage(vmcm);

            // the current state of the video application
            VideoAppStateMO stateMO = getStateMO();

            // client currently in control
            String controlling = stateMO.getControllingCell();
            // client making the request
            String requester = vmcm.getUID();

            // time out requests from non-responsive clients
            if (controlling != null) {
                // clients may lose connectivity to the server while processing
                // requests. 
                // if this happens, release the controlling client lock so that
                // other clients can process their requests
                long controlDuration = stateMO.getControlOwnedDuration();

                if (controlDuration >= controlTimeout) {
                    logger.warning("video GLO: forcing control release of controlling cell: " + stateMO.getControllingCell());
                    stateMO.setControllingCell(null);
                    controlling = null;
                }
            }

            if (controlling == null) {
                // no cell has control, grant control to the requesting cell
                stateMO.setControllingCell(requester);

                // reflect the command to all clients
                // respond to a client that is (now) in control
                switch (vmcm.getAction()) {
                    case GET_STATE:
                        // return current state of video app
                        msg.setAction(Action.SET_STATE);
                        msg.setSource(stateMO.getSource());
                        msg.setState(stateMO.getState());
                        if (stateMO.getState() == PlayerState.PLAYING) {
                            Calendar now = Calendar.getInstance();
                            Calendar then = stateMO.getLastStateChange();
                            long ago = now.getTimeInMillis() - then.getTimeInMillis();
                            double predicted = stateMO.getPosition() + (ago / 1000);
                            msg.setPosition(predicted);
                            logger.fine("video GLO: predicted play position: " + predicted);
                        } else {
                            msg.setPosition(stateMO.getPosition());
                        }
                        msg.setPTZPosition(stateMO.getPan(), stateMO.getTilt(), stateMO.getZoom());
                        break;
                    case PLAY:
                        stateMO.setPosition(vmcm.getPosition());
                        stateMO.setState(PlayerState.PLAYING);
                        msg.setState(PlayerState.PLAYING);
                        break;
                    case PAUSE:
                        stateMO.setPosition(vmcm.getPosition());
                        stateMO.setState(PlayerState.PAUSED);
                        msg.setState(PlayerState.PAUSED);
                        break;
                    case REWIND:
                    case FAST_FORWARD:
                        stateMO.setPosition(vmcm.getPosition());
                        stateMO.setState(vmcm.getState());
                        msg.setState(vmcm.getState());
                        break;
                    case STOP:
                        stateMO.setPosition(vmcm.getPosition());
                        stateMO.setState(PlayerState.STOPPED);
                        msg.setState(PlayerState.STOPPED);
                        break;
                    case SET_SOURCE:
                        stateMO.setPosition(vmcm.getPosition());
                        stateMO.setSource(vmcm.getSource());
                        break;
                    case SET_PTZ:
                        stateMO.setPan(msg.getPan());
                        stateMO.setTilt(msg.getTilt());
                        stateMO.setZoom(msg.getZoom());
                        break;
                    case REQUEST_COMPLETE:
                        // release control of camera by this client
                        stateMO.setControllingCell(null);
                        break;
                }
                // broadcast the message to all clients, including the requester
                logger.fine("video GLO: broadcasting msg: " + msg);
                getCellChannel().send(sessions, msg.getBytes());
            } else {
                // one cell has control
                switch (vmcm.getAction()) {
                    case REQUEST_COMPLETE:
                        // release control of camera by this client
                        stateMO.setControllingCell(null);
                        // broadcast request complete to all clients
                        // broadcast the message to all clients, including the requester
                        logger.fine("video GLO: broadcasting msg: " + msg);
                        getCellChannel().send(sessions, msg.getBytes());
                        break;
                    default:
                        // send a denial to the requesting client
                        msg.setRequestStatus(RequestStatus.REQUEST_DENIED);
                        logger.info("video GLO: sending denial to client: " + msg);
                        getCellChannel().send(client, msg.getBytes());
                        break;
                }
            }
        } else {
            super.receivedMessage(client, message);
        }
    }
}
