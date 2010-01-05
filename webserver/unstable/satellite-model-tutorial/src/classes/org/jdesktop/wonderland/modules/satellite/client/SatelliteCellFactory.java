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
package org.jdesktop.wonderland.modules.satellite.client;

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.satellite.common.SatelliteCellServerState;

@CellFactory
public class SatelliteCellFactory implements CellFactorySPI {

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
        SatelliteCellServerState state = new SatelliteCellServerState();
        state.setModelURI("wla://satellite-model-tutorial/satellite.kmz/satellite.kmz.dep");
        return (T)state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return "Satellite";
    }

    /**
     * {@inheritDoc}
     */
    public Image getPreviewImage() {
        return null;
   }
}
