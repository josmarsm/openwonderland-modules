/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.navigateto.client;

import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.modules.navigateto.common.NavigateToServerState;

/**
 * Factory for best view component
 *
 * @author nilang shah
 */
@CellComponentFactory
public class NavigateToComponentFactory implements CellComponentFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.navigateto.client.Bundle");

    /**
     * Return the display name of this component
     */
    public String getDisplayName() {
        return BUNDLE.getString("Navigate_To");
    }

    /**
     * Return the description of this component
     */
    public String getDescription() {
        return BUNDLE.getString("Navigate_To_Description");
    }

    /**
     * Create an instance of the component's server state.
     *
     * @return a configured instance of the component's server state.
     */
    public NavigateToServerState getDefaultCellComponentServerState() {
        NavigateToServerState out = new NavigateToServerState();
        return out;
    }
}
