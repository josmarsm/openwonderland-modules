/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import com.wonderbuilders.modules.colortheme.common.ColorThemeComponentClientState;
import com.wonderbuilders.modules.colortheme.common.ColorThemeComponentServerState;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server-side color theme component.
 */
@DependsOnCellComponentMO(SharedStateComponentMO.class)
public class ColorThemeComponentMO extends CellComponentMO {
    
    /** Color theme currently in use. */
    private String currentColorTheme;
    
    /** Previously used color theme. */
    private String previousTheme;
    
    private Map<String,String> textureMapping;
    
    public ColorThemeComponentMO(CellMO cell) {
        super(cell);
        textureMapping = new HashMap<String, String>();
    }

    @Override
    protected String getClientClass() {
        return "com.wonderbuilders.modules.colortheme" +
               ".client.ColorThemeComponent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state, 
            WonderlandClientID clientID,
            ClientCapabilities capabilities) 
    {

        if (state == null) {
            state = new ColorThemeComponentClientState();
            ((ColorThemeComponentClientState)state).setCurrentColorTheme(currentColorTheme);
            ((ColorThemeComponentClientState)state).setPreviousTheme(previousTheme);
            ((ColorThemeComponentClientState)state).setTextureMapping(textureMapping);
        }
   
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState state) 
    {
        
        if (state == null) {
            state = new ColorThemeComponentServerState();
            ((ColorThemeComponentServerState)state).setCurrentColorTheme(currentColorTheme);
            ((ColorThemeComponentServerState)state).setPreviousTheme(previousTheme);
            ((ColorThemeComponentServerState)state).setTextureMapping(textureMapping);
        }
        
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        currentColorTheme = ((ColorThemeComponentServerState)state).getCurrentColorTheme();
        previousTheme = ((ColorThemeComponentServerState)state).getPreviousTheme();
        textureMapping = ((ColorThemeComponentServerState)state).getTextureMapping();
    }
    
}
