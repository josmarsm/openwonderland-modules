/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.attachto.client;

import com.wonderbuilders.modules.attachto.common.AttachToComponentServerState;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 * Factory for Attach-To components.
 *
 * @author Abhishek Upadhyay
 */
@CellComponentFactory
public class AttachToComponentFactory implements CellComponentFactorySPI {

    @Override
    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        return (T) new AttachToComponentServerState();
    }

    @Override
    public String getDisplayName() {
        return "Attach-To";
    }

    @Override
    public String getDescription() {
        return "attach an object to another object";
    }
}
