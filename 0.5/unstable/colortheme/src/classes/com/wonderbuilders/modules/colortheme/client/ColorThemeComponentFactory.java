/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.client;

import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import com.wonderbuilders.modules.colortheme.common.ColorThemeComponentServerState;

/**
 * The cell component factory for the color theme component.
 */
@CellComponentFactory
public class ColorThemeComponentFactory implements CellComponentFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com/wonderbuilders/modules/colortheme/client/resources/Bundle");

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return BUNDLE.getString("Color_Theme_Component");
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        ColorThemeComponentServerState state = new ColorThemeComponentServerState();
        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return BUNDLE.getString("Color_Theme_Component_Description");
    }
}
