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
package org.jdesktop.wonderland.modules.proximitytest.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.proximitytest.common.ProximityCellServerState;

/**
 * Cell factory for proximity test
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@CellFactory
public class ProximityCellFactory implements CellFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/proximitytest/client/resources/Bundle");

    static final BoundingVolume[] clientBV = new BoundingVolume[] {
        new BoundingBox(new Vector3f(), 5, 5, 5),
        new BoundingBox(new Vector3f(), 3, 3, 3),
        new BoundingBox(new Vector3f(), 1, 1, 1)
    };

    static final BoundingVolume[] serverBV = new BoundingVolume[] {
        new BoundingSphere(6, new Vector3f()),
        new BoundingSphere(4, new Vector3f()),
        new BoundingSphere(2, new Vector3f()),
    };

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        ProximityCellServerState state = new ProximityCellServerState();
        
        state.setClientBounds(Arrays.asList(clientBV));
        state.setServerBounds(Collections.EMPTY_LIST);

        // Give the hint for the bounding volume for initial Cell placement
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 2, 2, 2);
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);

        return (T) state;
    }

    public String getDisplayName() {
        return BUNDLE.getString("Proximity_Cell");
    }

    public Image getPreviewImage() {
        URL url = ProximityCellFactory.class.getResource("resources/sample_preview.jpg");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
