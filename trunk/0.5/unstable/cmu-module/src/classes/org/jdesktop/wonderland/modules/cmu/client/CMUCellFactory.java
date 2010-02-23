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
package org.jdesktop.wonderland.modules.cmu.client;

import com.jme.bounding.BoundingBox;
import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellServerState;

/**
 * Factory for CMU cells; cells can be created by dropping .a3p files
 * onto the Wonderland window.
 * @author kevin
 */
@CellFactory
public class CMUCellFactory implements CellFactorySPI {

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getExtensions() {
        return new String[]{"a3p"};
    }

    /**
     * {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        CMUCellServerState state = new CMUCellServerState();

        state.setSceneTitle("CMU Scene");
        if (props != null) {
            String uri = props.getProperty("content-uri");
            state.setCmuURI(uri);
            state.setSceneTitle(uri.substring(uri.lastIndexOf('/') + 1));
        }
        state.setGroundPlaneShowing(false);

        // Create a bounding box so that scenes match vertically with the world
        BoundingBox defaultBounds = new BoundingBox();
        defaultBounds.yExtent = 0;
        state.setBoundingVolumeHint(new BoundingVolumeHint(true, defaultBounds));

        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        // Cells can't be created without an associated .a3p file
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getPreviewImage() {
        return null;
    }
}
