/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameServerState;

/**
 * cell factory for image frame
 */
@CellFactory
public class ImageFrameCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        ImageFrameServerState state = new ImageFrameServerState();
        if (props != null) {
            int aspectRatio = Integer.parseInt((String) props.get("aspectRatio"));
            int orientation = -1;
            if (props.get("orientation") != null) {
                orientation = Integer.parseInt((String) props.get("orientation"));
            }

            String width = (String) props.get("frameWidth");
            String height = (String) props.get("frameHeight");
            String imageURL = (String) props.get("imageURL");

            if (width != null) {
                state.setFrameHeight(Integer.parseInt(height));
                state.setFrameWidth(Integer.parseInt(width));
                state.setImageURL(imageURL);
            }
            state.setAspectRatio(aspectRatio);
            state.setOrientation(orientation);
        }
        BoundingVolume bv = new BoundingBox(new Vector3f(0f, 0f, 0f),
                3f,
                2f,
                -3f);
        Quaternion rot = new Quaternion(new float[]{0f, 0f, 0f});
        BoundingVolumeHint hint = new BoundingVolumeHint(true, bv);
        hint.getBoundsHint().transform(rot, Vector3f.ZERO, new Vector3f(3f, 2f, -3f));
        state.setBoundingVolumeHint(hint);
        state.setName("ImageFrame");
        return (T) state;
    }

    public String getDisplayName() {
        return "Image Frame";
    }

    public Image getPreviewImage() {
        URL url = ImageFrameCellFactory.class.getResource("resources/ImageFrameIcon1.jpg");
        return Toolkit.getDefaultToolkit().createImage(url);
    }

}
