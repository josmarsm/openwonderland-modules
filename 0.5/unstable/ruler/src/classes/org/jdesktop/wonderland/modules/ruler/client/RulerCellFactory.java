package org.jdesktop.wonderland.modules.ruler.client;

import com.jme.renderer.ColorRGBA;
import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.RulerDisplayMetaData;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.RulerImageHeight;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.RulerStyleMetaData;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellServerState;

/**
 * This class is a factory used to create RulerCell instances for the ruler module.
 *
 * @author Carl Jokl
 */
@CellFactory
public class RulerCellFactory implements CellFactorySPI {

    public static final String DISPLAY_NAME = "Ruler";

    /**
     * Get the filename extensions associated with this model.
     *
     * @return An empty array of Strings given that the RulerCell does not deal with files.
     */
    @Override
    public String[] getExtensions() {
        return new String[] {};
    }

    /**
     * Get the default RulerCellServerState.
     *
     * @param <T> The specific subtype of CellServerState to be returned.
     * @param properties The properties used in the creation of a new RulerCellServerState.
     * @return A new default instance of a RulerCellServerState.
     */
    @Override
    public <T extends CellServerState> T getDefaultCellServerState(Properties properties) {
        return (T) new RulerCellServerState();
    }

    /**
     * Get the display name for the RulerCell.
     *
     * @return The display name for the UI to use for the RulerCell.
     */
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * A preview image used for this module.
     *
     * @return An image containing a preview image for this module.
     */
    @Override
    public Image getPreviewImage() {
        return RulerImageFactory.createImage(new RulerDisplayMetaData(2, 16, RulerImageHeight.TALL, 0, false, 1.0f, 8, 4, 2),
                                             new RulerStyleMetaData(2, ColorRGBA.black.clone(), ColorRGBA.lightGray.clone(), ColorRGBA.white.clone(), ColorRGBA.black.clone()),
                                             null);
    }

}
