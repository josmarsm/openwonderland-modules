/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.client;


import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 * The cell component factory for the trigger gesture component.
 * 
 * @author Abhishek Upadhyay.
 */
@CellComponentFactory
public class TriggerGestureComponentFactory implements CellComponentFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/wonderbuilders/modules"
            + "/triggergesture/client/resources/Bundle");
    
    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        TriggerGestureComponentServerState serverState = new TriggerGestureComponentServerState();
        return (T) serverState;
    }

    public String getDisplayName() {
        return BUNDLE.getString("TriggerGestureFactory.DisplayName");
    }

    public String getDescription() {
        return BUNDLE.getString("TriggerGestureFactory.Description");
    }
    
}
