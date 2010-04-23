/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.client;

import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common.ClickableLinkComponentServerState;

/**
 * Once again 99% boilerplate
 * @author Ben (shavnir)
 *
 */
@CellComponentFactory
public class ClickableLinkComponentFactory implements CellComponentFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/rockwellcollins/clickablelink/" +
            "client/Bundle");

    @Override
    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        ClickableLinkComponentServerState state = new ClickableLinkComponentServerState();
        state.setLinkURL("about:robots");
        return (T) state;
    }

    @Override
    public String getDescription() {
        return BUNDLE.getString("Description");
    }

    /**
     * This method actually adds it to the list of possible to add cellcomponents
     */
    @Override
    public String getDisplayName() {
        return BUNDLE.getString("Clickable_Link");
    }
}
