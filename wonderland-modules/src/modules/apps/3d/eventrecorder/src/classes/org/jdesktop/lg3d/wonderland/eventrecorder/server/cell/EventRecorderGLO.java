/**
 * Project Looking Glass
 * 
 * $RCSfile: RecordingDeviceGLO.java,v $
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
 * $Revision: 1.1.2.9 $
 * $Date: 2008/03/07 16:18:35 $
 * $State: Exp $ 
 */

package org.jdesktop.lg3d.wonderland.eventrecorder.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.media.j3d.Bounds;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellHierarchyMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.eventrecorder.common.EventRecorderCellMessage;
import org.jdesktop.lg3d.wonderland.eventrecorder.common.EventRecorderCellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.CellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.StationaryCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.UserCellCacheGLO;
import org.jdesktop.lg3d.wonderland.eventrecorder.RecordableCellGLO;
import org.jdesktop.lg3d.wonderland.eventrecorder.common.EventRecorderMessage;

public class EventRecorderGLO extends StationaryCellGLO 
        implements BeanSetupGLO, CellMessageListener {
    private static final Logger logger = Logger.getLogger(EventRecorderGLO.class.getName());
    
    private BasicCellGLOSetup<EventRecorderCellSetup> setup;
    
            
    public EventRecorderGLO() {
        this(null, null);
    }
    
    public EventRecorderGLO(Bounds bounds, Matrix4d center) {
        super(bounds, center);      
    }
        
    
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.eventrecorder.client.cell.EventRecorderCell";
    }
    
    public EventRecorderCellSetup getSetupData() {
        return setup.getCellSetup();
    }

    public void receivedMessage(ClientSession client, CellMessage message) {
        EventRecorderCellMessage ntcm = (EventRecorderCellMessage) message;
        switch (ntcm.getActionType()) {
            case RECORDING:
                getSetupData().setRecording(ntcm.isRecording());
                getSetupData().setUserName(ntcm.getUserName());

                // send a message to all clients except the sender to notify of 
                // the updated selection
                EventRecorderMessage msg = new EventRecorderMessage(getSetupData().isRecording(), getSetupData().getUserName());

                Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
                sessions.remove(client);
                getCellChannel().send(sessions, msg.getBytes());
                break;
            case SYNC_REQUEST:
                synchronize(client);
                break;
        }
    }

    private Collection<CellGLO> getChildren(CellGLO cell) {
        Collection<CellGLO> offspring = new ArrayList<CellGLO>();
        //offspring.add(cell);
        getChildren(cell, offspring);
        return offspring;
    }

    private void getChildren(CellGLO cell, Collection<CellGLO> cellCollection) {
        Collection<ManagedReference> offspring = cell.getChildren();
        for(ManagedReference mRef : offspring) {
            /* Fetech the cell GLO class associated with the visible cell */
            CellGLO cellGLO  = mRef.get(CellGLO.class);
            cellCollection.add(cellGLO);
            getChildren(cellGLO, cellCollection);
        }
    }
    
    
    private void synchronize(ClientSession client) {
        //Start Sync
        EventRecorderMessage erMsg = EventRecorderMessage.startSync();
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("Start sync");
        
        synchronizeCells(client);
        synchronizeState(client);
        
        //End sync
        erMsg = EventRecorderMessage.endSync();
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("End sync");
    }
    
    
    
    private void synchronizeCells(ClientSession client) {
        EventRecorderMessage erMsg;
        CellHierarchyMessage chMsg;
        
        //Start Sync
        erMsg = EventRecorderMessage.startSyncCells();
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("Start sync cells");
        
        // Setup Root Cell
        DataManager dataMgr = AppContext.getDataManager();
        CellGLO rootCell = dataMgr.getBinding("CELL_ROOT", CellGLO.class);
        
        chMsg = UserCellCacheGLO.newCreateCellMessage(rootCell);
        erMsg = EventRecorderMessage.cellHierarchyMessage(chMsg);
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("Sending CHmessage: " + chMsg.getActionType() + chMsg.getCellID() + " " + chMsg.getCellClassName());
        
        chMsg = UserCellCacheGLO.newRootCellMessage(rootCell);
        erMsg = EventRecorderMessage.cellHierarchyMessage(chMsg);
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("Sending CHmessage: " + chMsg.getActionType() + chMsg.getCellID() + " " + chMsg.getCellClassName());
        
        //Get rootCell's children
        Collection<CellGLO> offspring = getChildren(rootCell);
        //Send a CellHierarchyMessage for each child
        for(CellGLO cellGLO : offspring) {
            chMsg = UserCellCacheGLO.newCreateCellMessage(cellGLO);
            logger.info("Sending CHmessage: " + chMsg.getActionType() + chMsg.getCellID() + " " + chMsg.getCellClassName());
            erMsg = EventRecorderMessage.cellHierarchyMessage(chMsg);
            getCellChannel().send(client, erMsg.getBytes());
        }
        
        //End sync
        erMsg = EventRecorderMessage.endSyncCells();
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("End sync cells");
    }
    
    private void synchronizeState(ClientSession client) {
        EventRecorderMessage erMsg;
        CellHierarchyMessage chMsg;
        
        //Start Sync
        erMsg = EventRecorderMessage.startSyncState();
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("Start sync state");
        
        // Setup Root Cell
        DataManager dataMgr = AppContext.getDataManager();
        CellGLO rootCell = dataMgr.getBinding("CELL_ROOT", CellGLO.class);
        
        //Get rootCell's children
        Collection<CellGLO> offspring = getChildren(rootCell);
        //Send synchronise message for each child
        for(CellGLO cellGLO : offspring) {
            if (cellGLO instanceof RecordableCellGLO) {
                List<CellMessage> messages = ((RecordableCellGLO)cellGLO).getSynchronizeMessages();
                for (CellMessage message : messages) {
                    erMsg = EventRecorderMessage.synchronizeStateMessage(message, (RecordableCellGLO)cellGLO);
                    logger.info("Sending cell message: " + message + message.getCellID());
            
                    getCellChannel().send(client, erMsg.getBytes());
                }        
            }
        }
        //End sync
        erMsg = EventRecorderMessage.endSyncState();
        getCellChannel().send(client, erMsg.getBytes());
        logger.info("End sync state");
    }
    
    
    

    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup setupData) {
        setup = (BasicCellGLOSetup<EventRecorderCellSetup>) setupData;

        AxisAngle4d aa = new AxisAngle4d(setup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setup.getOrigin());

        Matrix4d o = new Matrix4d(rot, origin, setup.getScale());
        setOrigin(o);

        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float) setup.getBoundsRadius()));
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
        return new BasicCellGLOSetup<EventRecorderCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    

    
}