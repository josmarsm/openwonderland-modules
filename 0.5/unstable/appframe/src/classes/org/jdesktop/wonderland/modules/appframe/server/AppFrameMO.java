/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.server;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameServerState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.*;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Server side of app frame
 */
@DependsOnCellComponentMO({SharedStateComponentMO.class,MovableComponentMO.class})
public class AppFrameMO extends CellMO {
    
    @UsesCellComponentMO(value = ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelRef;
    
    public AppFrameMO() {
        
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities){
        return "org.jdesktop.wonderland.modules.appframe.client.AppFrame";
    }

    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);
        
    }

    @Override
    public CellServerState getServerState(CellServerState serverState) {
        if (serverState == null) {
            serverState = new AppFrameServerState();
        }
        return super.getServerState(serverState);
    }
}





