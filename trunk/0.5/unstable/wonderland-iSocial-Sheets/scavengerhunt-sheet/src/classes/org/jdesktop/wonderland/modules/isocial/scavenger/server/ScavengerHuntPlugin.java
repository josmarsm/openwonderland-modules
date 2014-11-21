/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.server;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.EnvironmentCellMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;

/**
 * Server plugin for Scavenger Hunt component. This is used to initialize Shared state component.
 *
 * @author Vladimir Djurovic
 */
@Plugin
public class ScavengerHuntPlugin implements ServerPlugin {

    public void initialize() {
        CellManagerMO.getCellManager().registerCellComponent(EnvironmentCellMO.class, SharedStateComponentMO.class);
        CellManagerMO.getCellManager().registerCellComponent(EnvironmentCellMO.class, ProximityComponentMO.class);
    }
    
}
