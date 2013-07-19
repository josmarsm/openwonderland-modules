/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.poster.common.PosterCellServerState;

/**
 * The cell factory for the Postercell.<br>
 * Adapted from the factory cell creaated for the "generic" Cell facility.<br>
 * originally written by Jordan Slott <jslott@dev.java.net>
 * 
 * @author Bernard Horan
 */
@CellFactory
public class PosterCellFactory implements CellFactorySPI {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/poster/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    @SuppressWarnings("unchecked")
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        CellServerState state = new PosterCellServerState();
        BoundingSphere box = new BoundingSphere(1.0f, new Vector3f(1f, 0.7f, 0.2f));
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);
        state.setName(bundle.getString("POSTER"));
        return (T) state;
    }

    public String getDisplayName() {
        return bundle.getString("POSTER");
    }

    public Image getPreviewImage() {
        URL url = PosterCellFactory.class.getResource("resources/Poster-icon.png");
        return Toolkit.getDefaultToolkit().getImage(url);
    }
}
