/*
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.server;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.EnvironmentCellMO;

/**
 *  Server side plugin for module initialization.
 *
 * @author Vladimir Djurovic
 */
@Plugin
public class ColorThemePlugin implements ServerPlugin {

    /**
     * Initializes the module. This will register shared state component for usage.
     */
    public void initialize() {
        CellManagerMO.getCellManager().registerCellComponent(EnvironmentCellMO.class, SharedStateComponentMO.class);
    }
    
    
}
