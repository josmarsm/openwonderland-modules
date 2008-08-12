/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.lg3d.wonderland.tightvncmodule.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage.Action;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage.RequestStatus;

/**
 * @author nsimpson
 */
public class TightVNCModuleCellGLO extends SharedApp2DImageCellGLO
        implements BeanSetupGLO, CellMessageListener {

    private static final Logger logger =
            Logger.getLogger(TightVNCModuleCellGLO.class.getName());
    private static long controlTimeout = 90 * 1000; // how long a client can retain control (ms)

    // The setup object contains the current state of the VNC application.
    // It's updated every time a client makes a change so that when new 
    // clients join, they receive the current state.
    private ManagedReference stateRef = null;
    private BasicCellGLOSetup<TightVNCModuleCellSetup> setup;

    public TightVNCModuleCellGLO() {
        this(null, null, null, null);
    }

    public TightVNCModuleCellGLO(Bounds bounds, String appName, Matrix4d cellOrigin,
            Matrix4f viewRectMat) {
        super(bounds, appName, cellOrigin, viewRectMat, TightVNCModuleCellGLO.class.getName());
    }

    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }

    @Override
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell.TightVNCModuleCell";
    }

    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup data) {
        BasicCellGLOSetup<TightVNCModuleCellSetup> setupData = (BasicCellGLOSetup<TightVNCModuleCellSetup>) data;
        super.setupCell(setupData);

        TightVNCModuleCellSetup vnccs = setupData.getCellSetup();
        controlTimeout = vnccs.getControlTimeout();

        if (getStateMO() == null) {
            // create a new managed object containing the setup data
            TightVNCModuleStateMO stateMO = new TightVNCModuleStateMO(setupData.getCellSetup());

            // create a managed reference to the new state managed object
            DataManager dataMgr = AppContext.getDataManager();
            stateRef = dataMgr.createReference(stateMO);
        }
    }

    /**
     * Get the setup data for this cell
     * @return the cell setup data
     */
    @Override
    public TightVNCModuleCellSetup getSetupData() {
        return stateRef.get(TightVNCModuleStateMO.class).getCellSetup();
    }

    public TightVNCModuleStateMO getStateMO() {
        TightVNCModuleStateMO stateMO = null;
        if (stateRef != null) {
            stateMO = stateRef.get(TightVNCModuleStateMO.class);
        }

        return stateMO;
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
        return new BasicCellGLOSetup<TightVNCModuleCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    @Override
    public void receivedMessage(ClientSession client, CellMessage message) {
        if (message instanceof TightVNCModuleCellMessage) {
            TightVNCModuleCellMessage vnccm = (TightVNCModuleCellMessage) message;
            logger.fine("vnc GLO: received msg: " + vnccm);

            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());

            // clone the message
            TightVNCModuleCellMessage msg = new TightVNCModuleCellMessage(vnccm);

            // the current state of the application
            TightVNCModuleStateMO stateMO = getStateMO();

            // client currently in control
            String controlling = stateMO.getControllingCell();
            // client making the request
            String requester = vnccm.getUID();

            // time out requests from non-responsive clients
            if (controlling != null) {
                // clients may lose connectivity to the server while processing
                // requests. 
                // If this happens, release the controlling client lock so that
                // other clients can process their requests
                long controlDuration = stateMO.getControlOwnedDuration();

                if (controlDuration >= controlTimeout) {
                    logger.warning("vnc GLO: forcing control release of controlling cell: " + stateMO.getControllingCell());
                    stateMO.setControllingCell(null);
                    controlling = null;
                }
            }

            if (controlling == null) {
                // no cell has control, grant control to the requesting cell
                stateMO.setControllingCell(requester);

                // reflect the command to all clients
                // respond to a client that is (now) in control
                switch (vnccm.getAction()) {
                    case GET_STATE:
                        // return current state of VNC app
                        msg.setAction(Action.SET_STATE);
                        msg.setServer(stateMO.getServer());
                        msg.setPort(stateMO.getPort());
                        msg.setUsername(stateMO.getUsername());
                        msg.setPassword(stateMO.getPassword());
                        break;
                    case SET_STATE:
                        break;
                    case OPEN_SESSION:
                        stateMO.setServer(vnccm.getServer());
                        stateMO.setPort(vnccm.getPort());
                        stateMO.setUsername(vnccm.getUsername());
                        stateMO.setPassword(vnccm.getPassword());
                        break;
                    case CLOSE_SESSION:
                        stateMO.setControllingCell(null);
                        stateMO.setPort(5900);
                        stateMO.setUsername(null);
                        stateMO.setPassword(null);
                        break;
                    case REQUEST_COMPLETE:
                        // release control of VNC session state by this client
                        stateMO.setControllingCell(null);
                        break;
                    default:
                        break;
                }
                logger.fine("vnc GLO: broadcasting msg: " + msg);
                getCellChannel().send(sessions, msg.getBytes());
            } else {
                // one cell has control
                switch (vnccm.getAction()) {
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
