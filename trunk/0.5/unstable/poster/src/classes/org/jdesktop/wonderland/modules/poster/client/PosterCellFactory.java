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
package org.jdesktop.wonderland.modules.poster.client;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.poster.common.PosterCellServerState;

/**
 * The cell factory for the sample cell that users the "generic" Cell facility.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellFactory
public class PosterCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        CellServerState state =  new PosterCellServerState();
         BoundingSphere box = new BoundingSphere(1.0f, new Vector3f(1f, 0.7f, 0.2f));
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);
        state.setName("Poster");
        return (T) state;
    }



    public String getDisplayName() {
        return "Poster";
    }

    public Image getPreviewImage() {
        return null;
    }
}
