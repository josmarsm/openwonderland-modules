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
import org.jdesktop.lg3d.wonderland.darkstar.server.CellAccessControl;  // TW
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.ClientIdentityManager;  // TW
import org.jdesktop.lg3d.wonderland.darkstar.server.UserGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.auth.WonderlandIdentity; // TW
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.AvatarCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.UserCellCacheGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOHelper;  // TW
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
    private boolean clientAccess = true;  // TW
                       
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

        // Handle configuring this cell's discretionary access controls
        //
        // TW
        setCellAccessOwner(BasicCellGLOHelper.getCellAccessOwner(setupData));  // TW
        setCellAccessGroup(BasicCellGLOHelper.getCellAccessGroup(setupData));  // TW
        setCellAccessGroupPermissions(BasicCellGLOHelper.getCellAccessGroupPermissions(setupData)); // TW
        setCellAccessOtherPermissions(BasicCellGLOHelper.getCellAccessOtherPermissions(setupData)); // TW
        
        // Also, setup the name of the cell (this is passed to the
        // client for display in the WonderDAC GUI).
        // TW
        setCellName(BasicCellGLOHelper.getCellName(setupData));  // TW   
        
        // And, setup the cell's width and height.  TW
        setCellWidth(BasicCellGLOHelper.getCellWidth(setupData)); // TW
        setCellHeight(BasicCellGLOHelper.getCellHeight(setupData)); // TW        
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
    public void pingClients(){  // TW
        // Construct a (very) simple message first...  TW
        TightVNCModuleCellMessage tempMsg = new TightVNCModuleCellMessage (cellID, Action.PING);  // TW
   
        logger.fine("Privileges have changed; pinging all clients....");  // TW
        
        // Ping all of our clients.
        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());  // TW
                
        getCellChannel().send(sessions, tempMsg.getBytes());  // TW              
    }

    @Override
    public void pingClient(ClientSession client){  // TW
        // Construct a (very) simple message first...  TW
        TightVNCModuleCellMessage tempMsg = new TightVNCModuleCellMessage (cellID, Action.PING);  // TW
   
        logger.fine("Privileges have changed; pinging a client....");  // TW
        
        if (getCellChannel().getSessions().contains(client))  // TW
            // Ping our client.
            getCellChannel().send(client, tempMsg.getBytes());  // TW
        else
            logger.severe("Attempting to ping a client outside of this GLO's channel:  " + client.getName());           
    }
    
    @Override
    public void receivedMessage(ClientSession client, CellMessage message) {
        if (message instanceof TightVNCModuleCellMessage) {
            TightVNCModuleCellMessage vnccm = (TightVNCModuleCellMessage) message;

            // Obtain a UserGLO object for use later.
            // TW
            UserGLO user = UserGLO.getUserGLO(client.getName());
            
            logger.fine("vnc GLO: received msg: " + vnccm);

            // the current state of the application
            TightVNCModuleStateMO stateMO = getStateMO();  // Relocated.  TW

            // client currently in control
            String controlling = stateMO.getControllingCell(); // Relocated.  TW
            
            // client making the request
            String requester = vnccm.getUID();  // Relocated.  TW

            // Does the user have interact permission?  If not, then they
            // can't even see the VNC cell.  If they have control, take
            // it away, then drop them like yesterday's cheese!
            // TW
            if (!CellAccessControl.canInteract(user.getUserIdentity(),this)){  // TW
                
                // If the client does not have 'alter' or 'interact'
                // access, make sure they didn't just have control.
                // TW
                if ((controlling != null) && (requester != null) && requester.equals(controlling)) {  // TW
                    logger.fine("Forcing user to relinquish control due to lost privileges.");  // TW
                   stateMO.setControllingCell(null);  // TW
                   controlling = null; // TW
                }     
                
                // In case the client has a control panel open,
                // let them know that they're about to be cut off.
                // TW
                TightVNCModuleCellMessage msg = new TightVNCModuleCellMessage (Action.NO_ALTER_PERM);  // TW
            
                // Send this off to the client.  TW
                logger.fine("TightVNC GLO sending NO_ALTER_PERM msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW

                // Close the client's session.

                msg = new TightVNCModuleCellMessage (Action.CLOSE_SESSION);  // TW
                
                // Send the close-session message off to the client.
                
                logger.fine("TightVNC GLO sending CLOSE_SESSION msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW
                
                // Get the client to re-evaluate their surroundings.
                // TW
                user.getAvatarCellRef().get(AvatarCellGLO.class).getUserCellCacheRef().
                                        get(UserCellCacheGLO.class).refactor();
                            
                return;  // TW
            }
            
            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());

            // Does the user have permissions to alter the VNC cell?  If not,
            // ignore most messages coming from them by setting 'clientAccess'
            // to false.
            //
            // TW            
            if (!CellAccessControl.canAlter(user.getUserIdentity(),this)) {  // TW
                
                clientAccess = false;  // TW
                
                // Since this client does not have alter permissions, send
                // them a message to that effect.  The client can then
                // work to prevent the end user from making any changes
                // to its local TightVNCModule cell.
                //
                // TW
                // Construct a (very) simple TightVNCModuleCellMessage first...  TW
                TightVNCModuleCellMessage msg = new TightVNCModuleCellMessage (Action.NO_ALTER_PERM);  // TW
            
                // Send this off to the client.  TW
                logger.fine("TightVNC GLO sending NO_ALTER_PERM msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW
            }
            else {
                clientAccess = true;
                  
                // Since this client has alter permissions, send
                // them a message to that effect.  The client can then
                // work to re-enable the end user's ability to make any changes
                // to their local PDFviewer cell.
                //
                // TW
                // Construct a (very) simple TightVNCModuleCellMessage first...  TW
                TightVNCModuleCellMessage msg = new TightVNCModuleCellMessage (Action.ALTER_PERM);  // TW
            
                // Send this off to the client.  TW
                logger.fine("TightVNC GLO sending ALTER_PERM msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW
            }

            // No need to take further action on a PING from the client,
            if (vnccm.getAction() == Action.PING) { // TW
                return;  // TW
            }
            
            // clone the message
            TightVNCModuleCellMessage msg = new TightVNCModuleCellMessage(vnccm);

            // time out requests from non-responsive clients
            if (controlling != null) {
                // clients may lose connectivity to the server while processing
                // requests. 
                // If this happens, release the controlling client lock so that
                // other clients can process their requests
                long controlDuration = stateMO.getControlOwnedDuration();

                if (controlDuration >= controlTimeout) {
                    logger.warning("VNC GLO: forcing control release of controlling cell: " + stateMO.getControllingCell());
                    stateMO.setControllingCell(null);
                    controlling = null;
                }
            }

            if (controlling == null) {
                // no cell has control, grant control to the requesting cell
                stateMO.setControllingCell(requester);
                controlling = stateMO.getControllingCell();  // Re-assign the controller.  TW

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
                        if (clientAccess) {  // TW
                            stateMO.setServer(vnccm.getServer());
                            stateMO.setPort(vnccm.getPort());
                            stateMO.setUsername(vnccm.getUsername());
                            stateMO.setPassword(vnccm.getPassword());
                        }
                        break;
                    case CLOSE_SESSION:
                        if (clientAccess) {  // TW
                            stateMO.setControllingCell(null);
                            stateMO.setPort(5900);
                            stateMO.setUsername(null);
                            stateMO.setPassword(null);
                        }
                        break;
                    case REQUEST_COMPLETE:
                        // release control of VNC session state by this client
                        stateMO.setControllingCell(null);
                        break;
                    default:
                        break;
                }
                logger.fine("VNC GLO broadcasting msg: " + msg);               
                
                // If the user has access, let them sail on through to sending
                // their message.  If they do not, however, the only sailing they
                // are allowed to do is with SET_STATE and REQUEST_COMPLETE!
                // TW
                if (clientAccess || 
                    (msg.getAction() == Action.SET_STATE) ||
                    (msg.getAction() == Action.REQUEST_COMPLETE))
                    getCellChannel().send(sessions, msg.getBytes());
                
                // If the user has no 'alter' permission and has attempted to
                // do something other get the state of the PDF viewer or send
                // a REQUEST_COMPLETE message, then remove their control and
                // dump them.
                // TW
                else if ((requester != null) && requester.equals(controlling)) {  // TW
                    logger.warning ("Forcing the release of user's control due to lack of privilege.");
                    stateMO.setControllingCell(null);  // TW                  
                }                
            } else {
                // one cell has control
                switch (vnccm.getAction()) {
                    case REQUEST_COMPLETE:
                        // release control of camera by this client
                        stateMO.setControllingCell(null);
                        // broadcast request complete to all clients
                        // broadcast the message to all clients, including the requester
                        logger.fine("VNC GLO: broadcasting msg: " + msg);
                        getCellChannel().send(sessions, msg.getBytes());
                        break;
                    default:
                        // send a denial to the requesting client
                        msg.setRequestStatus(RequestStatus.REQUEST_DENIED);
                        logger.info("VNC GLO: sending denial to client: " + msg);
                        getCellChannel().send(client, msg.getBytes());
                        break;
                }
            }
        } else {
            super.receivedMessage(client, message);
        }
    }
}
