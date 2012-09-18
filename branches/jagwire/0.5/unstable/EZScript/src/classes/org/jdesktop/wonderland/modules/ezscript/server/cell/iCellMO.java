/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.server.cell;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.iCellClientState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.iCellServerState;
import org.jdesktop.wonderland.modules.ezscript.server.EZScriptComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Ryan
 */
public class iCellMO extends CellMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef;
    
    @UsesCellComponentMO(EZScriptComponentMO.class)
    private ManagedReference<EZScriptComponentMO> ezRef;
    
    private String rendererClassName = null;
    private String cellClassName = null;
    
    public iCellMO() {
        super();
    }
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return cellClassName;
    }
    
    @Override
    public CellClientState getClientState(CellClientState state,
                                          WonderlandClientID clientID,
                                          ClientCapabilities capabilities) {
        if(state == null) {
            state = new iCellClientState();
        }
        
        ((iCellClientState)state).setRendererClassName(rendererClassName);
        return super.getClientState(state, clientID, capabilities);
    }
    
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        
        this.rendererClassName = ((iCellServerState)state).getRendererClassName();
        this.cellClassName = ((iCellServerState)state).getCellClassName();
    }
    
    @Override
    public CellServerState getServerState(CellServerState state) {
        if(state == null) {
            state = new iCellServerState();
        }
        
        ((iCellServerState)state).setRendererClassName(rendererClassName);
        ((iCellServerState)state).setCellClassName(cellClassName);
        return super.getServerState(state);
        
    }
    
    
    
    
}
