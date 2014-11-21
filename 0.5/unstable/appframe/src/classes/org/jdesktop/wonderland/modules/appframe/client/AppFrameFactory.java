/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerStateFactory;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
/**
 *
 * @author jkaplan
 */
@CellFactory
public class AppFrameFactory implements CellFactorySPI {
    private static final Logger LOGGER =
            Logger.getLogger(AppFrameFactory.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.appframe.client.resources.Bundle");

    public String[] getExtensions() {
        return null;
    }

    public CellServerState getDefaultCellServerState(Properties props) {
        try {
            // load the server state from the prototype in the resources/
            // directory
            Unmarshaller u = CellServerStateFactory.getUnmarshaller((ScannedClassLoader) getClass().getClassLoader());
            InputStream is = AppFrameFactory.class.getResourceAsStream("resources/appframe-wlc.xml");
            CellServerState state = CellServerState.decode(new InputStreamReader(is),u);
            
            // update the bounds hint based on the size of the model. This
            // guarantees the frame will be placed correctly when it is
            // dropped into the world
            PositionComponentServerState pcss = (PositionComponentServerState)
                    state.getComponentServerState(PositionComponentServerState.class);
            if (pcss != null) {
                BoundingVolume bv = new BoundingBox(Vector3f.ZERO,
                        (float) pcss.getBounds().x,
                        (float) pcss.getBounds().y,
                        (float) pcss.getBounds().z);
                state.setBoundingVolumeHint(new BoundingVolumeHint(true, bv));
            }

            return state;
        } catch (JAXBException je) {
            LOGGER.log(Level.WARNING, "Error decoding server state", je);
            return null;
        }
    }
   // the name that appears on the insert object dialog
    public String getDisplayName() {
        return BUNDLE.getString("AppFrame");
    }

    // the preview image in the insert object dialog
    public Image getPreviewImage() {
        URL url = AppFrameFactory.class.getResource("resources/appframe-icon.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
