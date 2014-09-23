/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */


package com.wonderbuilders.modules.animation.client;

import com.wonderbuilders.modules.animation.common.AnimationComponentServerState;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 *  Factory for Animation components.
 *
 * @author Vladimir Djurovic
 */
@CellComponentFactory
public class AnimationComponentFactory implements CellComponentFactorySPI {
    
    /**
     * Static field for resource bundle.
     */
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com/wonderbuilders/modules/animation/client/resources/strings");

     /**
      * Returns default server state for this component.
      * 
      * @param <T> server state class
      * @return server state
      */
    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        AnimationComponentServerState state = new AnimationComponentServerState();
        return (T) state;
    }

    /**
     * Returns display name for component. Name is held in resource bundle.
     * 
     * @return display name
     */
    public String getDisplayName() {
        return BUNDLE.getString("Animation_Component");
    }

    /**
     * Returns component description. Description is held in resource bundle.
     * 
     * @return description string
     */
    public String getDescription() {
        return BUNDLE.getString("Animation_Component_Desc");
    }

}
