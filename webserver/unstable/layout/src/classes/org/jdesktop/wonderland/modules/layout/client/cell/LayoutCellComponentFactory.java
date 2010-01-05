/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.layout.client.cell;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.layout.api.common.spi.LayoutFactorySPI;
import org.jdesktop.wonderland.modules.layout.client.LayoutRegistry;
import org.jdesktop.wonderland.modules.layout.common.LayoutCellComponentServerState;

/**
 * The cell component factory for the layout cell component.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellComponentFactory
public class LayoutCellComponentFactory implements CellComponentFactorySPI {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(LayoutCellComponentFactory.class.getName());

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/layout/client/cell/resources/" +
            "Bundle");

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return BUNDLE.getString("Layout_Cell_Component");
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {

        // Go through the list of registered layouts and choose one as a default
        LayoutRegistry registry = LayoutRegistry.getInstance();
        Set<LayoutFactorySPI> factorySet = registry.getLayoutFactories();
        LayoutFactorySPI factory = factorySet.iterator().next();

        LayoutCellComponentServerState state =
                new LayoutCellComponentServerState();
        state.setLayoutConfig(factory.getDefaultLayoutConfig(null));

        LOGGER.warning("Layout Config: " + state.getLayoutConfig());
        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return BUNDLE.getString("Layout_Cell_Component_Description");
    }
}
