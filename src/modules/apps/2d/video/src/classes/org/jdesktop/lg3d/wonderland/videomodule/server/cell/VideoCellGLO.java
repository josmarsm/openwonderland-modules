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
import org.jdesktop.lg3d.wonderland.darkstar.server.CellAccessControl;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.ClientIdentityManager;
import org.jdesktop.lg3d.wonderland.darkstar.server.UserGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.auth.WonderlandIdentity;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.AvatarCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.UserCellCacheGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOHelper;
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
    private boolean clientAccess = true;  // TW            

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

    @Override
    public void pingClients(){  // TW
        // Construct a (very) simple message first...  TW
        VideoCellMessage tempMsg = new VideoCellMessage (Action.PING);  // TW
   
        logger.fine("Privileges have changed; pinging all clients....");  // TW

        // We'll run into significant syncronization problems as
        // a result of privilege changes (the video cell ends up
        // getting reloaded, and sharing clients get wildly out
        // of sync with one another).  So, we'll send out a message
        // to pause everyone's video from playing (if it is).
        // TW
        pauseClients();  // TW
        
        // Ping all of our clients.
        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());  // TW
                
        getCellChannel().send(sessions, tempMsg.getBytes());  // TW              
    }

    @Override
    public void pingClient(ClientSession client){  // TW
        // Construct a (very) simple message first...  TW
        VideoCellMessage tempMsg = new VideoCellMessage (Action.PING);  // TW
   
        logger.fine("Privileges have changed; pinging a client....");  // TW

        if (getCellChannel().getSessions().contains(client))  // TW
            // Ping our client.
            getCellChannel().send(client, tempMsg.getBytes());  // TW
        else
            logger.severe("Attempting to ping a client outside of this GLO's channel:  " + client.getName());        
    }
     
    public void pauseClients(){  // TW
        // Construct a (very) simple message first...  TW
        VideoAppStateMO stateMO = getStateMO();
        VideoCellMessage tempMsg = new VideoCellMessage(cellID,
                                                       stateMO.getControllingCell(),
                                                       stateMO.getSource(),
                                                       Action.PAUSE,
                                                       stateMO.getPosition());       
   
        logger.fine("Pausing all clients....");  // TW

        // Stop all of our clients.
        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());  // TW
        
        // Start locally...
        stateMO.setPosition(tempMsg.getPosition());  // TW
        stateMO.setState(PlayerState.PAUSED);  // TW
        tempMsg.setState(PlayerState.PAUSED);  // TW
        
        // Now broadcast the stop request...
        getCellChannel().send(sessions, tempMsg.getBytes());  // TW        
    }

    public void stopClient(ClientSession client){  // TW
        // Construct a (very) simple message first...  TW
        VideoAppStateMO stateMO = getStateMO();
        VideoCellMessage tempMsg = new VideoCellMessage(cellID,
                                                       stateMO.getControllingCell(),
                                                       stateMO.getSource(),
                                                       Action.STOP,
                                                       stateMO.getPosition());       
   
        logger.fine("Stopping one client....");  // TW

        // Start locally...
        tempMsg.setState(PlayerState.STOPPED);  // TW
        
        // Now broadcast the stop request...
        getCellChannel().send(client, tempMsg.getBytes());  // TW        
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
            
            // Obtain a UserGLO object for use later.
            // TW
            UserGLO user = UserGLO.getUserGLO(client.getName());
            
            logger.fine("video GLO: received msg: " + vmcm);

            // the current state of the video application
            VideoAppStateMO stateMO = getStateMO();  // Relocated.  TW

            // client currently in control
            String controlling = stateMO.getControllingCell();  // Relocated.  TW
            
            // client making the request
            String requester = vmcm.getUID(); // Relocated.  TW

            // Does the user have interact permission?  If not, then they
            // can't even see the video cell.  If they have control, take
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

                // Get the user's video player to stop
                stopClient(client);  // TW

                // In case the client has a control panel open, 
                // let them know they've been cut off.
                VideoCellMessage msg = new VideoCellMessage (Action.NO_ALTER_PERM);  // TW
            
                // Send this off to the client.  TW
                logger.fine("Video GLO sending NO_ALTER_PERM msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW

                // Get the client to re-evaluate their surroundings.
                // TW
                user.getAvatarCellRef().get(AvatarCellGLO.class).getUserCellCacheRef().
                                        get(UserCellCacheGLO.class).refactor();

                return;  // TW
            }
            
            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
 
            // Does the user have permissions to alter the Video cell?  If not,
            // ignore most messages coming from them by setting 'clientAccess'
            // to false.
            //
            // TW            
            if (!CellAccessControl.canAlter(user.getUserIdentity(),this)) {  // TW
                
                clientAccess = false; // TW

                // Since this client does not have alter permissions, send
                // them a message to that effect.  The client can then
                // work to prevent the end user from making any changes
                // to its local Video cell.
                //
                // TW
                // Construct a (very) simple VideoCellMessage first...  TW
                VideoCellMessage msg = new VideoCellMessage (Action.NO_ALTER_PERM);  // TW
            
                // Send this off to the client.  TW
                logger.fine("Video GLO sending NO_ALTER_PERM msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW
            }
            else {
                clientAccess = true; // TW

                // Since this client has alter permissions, send
                // them a message to that effect.  The client can then
                // work to re-enable the end user's ability to make any changes
                // to their local Video cell.
                //
                // TW
                // Construct a (very) simple VideoCellMessage first...  TW
                VideoCellMessage msg = new VideoCellMessage (Action.ALTER_PERM);  // TW
            
                // Send this off to the client.  TW
                logger.fine("Video GLO sending ALTER_PERM msg: " + msg);  // TW
                getCellChannel().send(client, msg.getBytes());  // TW                
            }
            
            // No need to take further action on a PING from the client,
            if (vmcm.getAction() == Action.PING) { // TW
                return;  // TW
            }
                        
            // clone the message
            VideoCellMessage msg = new VideoCellMessage(vmcm);

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
                controlling = stateMO.getControllingCell();  // Re-assign the controller.  TW

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
                        if (clientAccess){
                            stateMO.setPosition(vmcm.getPosition());
                            stateMO.setState(PlayerState.PLAYING);
                            msg.setState(PlayerState.PLAYING);
                        }
                        break;
                    case PAUSE:
                        if (clientAccess){
                            stateMO.setPosition(vmcm.getPosition());
                            stateMO.setState(PlayerState.PAUSED);
                            msg.setState(PlayerState.PAUSED);
                        }
                        break;
                    case REWIND:
                    case FAST_FORWARD:
                        if (clientAccess){
                            stateMO.setPosition(vmcm.getPosition());
                            stateMO.setState(vmcm.getState());
                            msg.setState(vmcm.getState());
                        }
                        break;
                    case STOP:
                        if (clientAccess){
                            stateMO.setPosition(vmcm.getPosition());
                            stateMO.setState(PlayerState.STOPPED);
                            msg.setState(PlayerState.STOPPED);
                        }
                        break;
                    case SET_SOURCE:
                        if (clientAccess){
                            stateMO.setPosition(vmcm.getPosition());
                            stateMO.setSource(vmcm.getSource());
                        }
                        break;
                    case SET_PTZ:
                        if (clientAccess){
                            stateMO.setPan(msg.getPan());
                            stateMO.setTilt(msg.getTilt());
                            stateMO.setZoom(msg.getZoom());
                        }
                        break;
                    case REQUEST_COMPLETE:
                        // release control of camera by this client
                        stateMO.setControllingCell(null);
                        break;
                }
                // broadcast the message to all clients, including the requester
                logger.fine("video GLO: broadcasting msg: " + msg);
                
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
