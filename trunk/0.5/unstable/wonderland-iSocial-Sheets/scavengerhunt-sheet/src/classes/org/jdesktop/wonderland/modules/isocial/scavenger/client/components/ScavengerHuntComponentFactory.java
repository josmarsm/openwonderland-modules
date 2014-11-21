/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client.components;

import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;

/**
 * Cell component factory for Scavenger Hunt.
 *
 * @author Vladimir Djurovic
 */
@CellComponentFactory
public class ScavengerHuntComponentFactory implements CellComponentFactorySPI {
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ScavengerHuntConstants.BUNDLE_PATH);

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        ScavengerHuntComponentServerState state = new ScavengerHuntComponentServerState();
        return (T)state;
    }

    public String getDisplayName() {
        return BUNDLE.getString(ScavengerHuntConstants.PROP_SCAVENGER_HUNT_NAME);
    }

    public String getDescription() {
        return BUNDLE.getString(ScavengerHuntConstants.PROP_SCAVENGER_HUNT_DESC);
    }
    
}
