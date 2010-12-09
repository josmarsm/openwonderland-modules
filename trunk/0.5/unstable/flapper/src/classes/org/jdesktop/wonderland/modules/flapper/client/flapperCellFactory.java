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
package org.jdesktop.wonderland.modules.flapper.client;

import java.awt.Image;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.flapper.common.flapperCellServerState;

@CellFactory
public class flapperCellFactory implements CellFactorySPI {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/flapper/client/resources/Bundle");

    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        return new String[] {};
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        flapperCellServerState state = new flapperCellServerState();
        state.setModelURI(bundle.getString("URL"));
        state.setName(bundle.getString("MODULATOR"));
        return (T)state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return bundle.getString("MODULATOR");
    }

    /**
     * {@inheritDoc}
     */
    public Image getPreviewImage() {
        return null;
   }
}
