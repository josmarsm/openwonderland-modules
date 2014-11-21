/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.server;


import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntComponentClientState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedDataItem;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Vladimir Djurovic
 */
public class ScavengerHuntComponentMO extends CellComponentMO {
    
    private static final Logger LOGGER = Logger.getLogger(ScavengerHuntComponentMO.class.getName());
    
    /** Used to restore shared map state from snapshots. */
    @UsesCellComponentMO (SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;
    private ManagedReference<SharedMapSrv> sharedMapRef;
    
    private ScavengerHuntItem item;
    private String sheetId;
    
    public ScavengerHuntComponentMO(CellMO cellMO){
        super(cellMO);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.isocial.scavenger.client.components.ScavengerHuntComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if(state == null){
            state = new ScavengerHuntComponentClientState();
            ((ScavengerHuntComponentClientState)state).setItem(item);
            ((ScavengerHuntComponentClientState)state).setSheetId(sheetId);
        }
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if(state == null){
            state = new ScavengerHuntComponentServerState();
            ((ScavengerHuntComponentServerState)state).setItem(item);
            ((ScavengerHuntComponentServerState)state).setSheetId(sheetId);
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        item = ((ScavengerHuntComponentServerState) state).getItem();
        sheetId = ((ScavengerHuntComponentServerState) state).getSheetId();

        if (item != null) {
            // make sure the item has an updated cell id
            item.setCellId(this.cellID.toString());

            // try to load the shared data once the environment cell is loaded
            AppContext.getTaskManager().scheduleTask(new RegisterStateTask(sheetId, item));
        }
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        
        if(!live){
            SharedStateComponentMO ssc = CellManagerMO.getEnvironmentCell().getComponent(SharedStateComponentMO.class);
            SharedMapSrv sharedMap = ssc.get(sheetId);
            if(sharedMap != null && item != null){
                sharedMap.remove(item.getCellId());
            }
        }
    }

    
    private static class RegisterStateTask implements Task, Serializable {
        private final String sheetId;
        private final ScavengerHuntItem item;
        
        public RegisterStateTask(String sheetId, ScavengerHuntItem item) {
            this.sheetId = sheetId;
            this.item = item;
        }
        
        public void run() throws Exception {
            CellMO envCell = CellManagerMO.getEnvironmentCell();
            
            // if the environment cell is not loaded yet, reschedule
            // ourself for later
            if (envCell == null) {
                AppContext.getTaskManager().scheduleTask(this, 1000);
                return;
            }
            
            // environment cell exists, go ahead and register
            SharedStateComponentMO ssc = envCell.getComponent(SharedStateComponentMO.class);
            SharedMapSrv sharedMap = ssc.get(sheetId);
            LOGGER.log(Level.FINE, "local Shared map srv: {0}", sharedMap);
            LOGGER.log(Level.FINE, "item: {0}", item);
            if(sharedMap != null && item != null){
                sharedMap.put(item.getCellId(), new SharedDataItem(item));
                LOGGER.log(Level.FINE, "Shared map after : {0}", sharedMap);
            }
        }
    }
}
