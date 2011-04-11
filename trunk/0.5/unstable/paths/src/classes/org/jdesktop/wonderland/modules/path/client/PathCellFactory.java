package org.jdesktop.wonderland.modules.path.client;

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.path.common.PathCellServerState;

/**
 * This factory is used to create PathCells.
 *
 * @author Carl Jokl
 */
@CellFactory
public class PathCellFactory implements CellFactorySPI {

    /**
     * The display name for the PathCell.
     */
    public static final String DISPLAY_NAME = "Path of Nodes Cell";

    /**
     * The file extensions which the PathCell supports.
     *
     * @return An empty array of Strings because file extensions are not supported by
     *         this PathCell.
     */
    @Override
    public String[] getExtensions() {
        return new String[] {};
    }

    /**
     * Get the default PathCellServerState for a PathCell.
     *
     * @param <T> The specific subclass of CellServerState which is returned i.e. PathCellServerState.
     * @param properties The properties used to create the default PathCellServerState.
     * @return A default instance of the PathCellServerState.
     */
    @Override
    public <T extends CellServerState> T getDefaultCellServerState(Properties properties) {
        return (T) new PathCellServerState();
    }

    /**
     * The display name of the PathCell for use in the UI.
     *
     * @return The display name of the PathCell.
     */
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * The preview image for the PathCell.
     *
     * @return The preview image for the PathCell.
     */
    @Override
    public Image getPreviewImage() {
        return null;
    }

}
