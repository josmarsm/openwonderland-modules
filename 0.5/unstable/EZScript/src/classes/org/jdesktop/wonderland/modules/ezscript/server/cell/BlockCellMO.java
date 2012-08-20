/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.server.cell;

import com.jme.renderer.ColorRGBA;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.BlockClientState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.BlockServerState;
import org.jdesktop.wonderland.modules.ezscript.server.EZScriptComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Ryan
 */
public class BlockCellMO extends CellMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef;
    
    @UsesCellComponentMO(EZScriptComponentMO.class)
    private ManagedReference<EZScriptComponentMO> ezRef;
    private ColorRGBA material;
    private String textureURL;
    
    public BlockCellMO() {
        super();
    }
    
    
    
    
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.ezscript.client.globals.builder.cell.BlockCell";
    }
    
    @Override
    public CellClientState getClientState(CellClientState state,
                                          WonderlandClientID clientID,
                                          ClientCapabilities capabilities) {
        
        if(state == null) {
            state = new BlockClientState();
        }
        
        ((BlockClientState)state).setMaterial(material);
        ((BlockClientState)state).setTextureURL(textureURL);
        
        return super.getClientState(state, clientID, capabilities);
    }
    
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        
        this.material = ((BlockServerState)state).getMaterial();
        this.textureURL = ((BlockServerState)state).getTextureURL();
    }
    
    @Override
    public CellServerState getServerState(CellServerState state) {
        if(state == null) {
            state = new BlockServerState();
        }
        
        ((BlockServerState)state).setMaterial(material);
        ((BlockServerState)state).setTextureURL(textureURL);
        
        return super.getServerState(state);
    }
    
    
}
