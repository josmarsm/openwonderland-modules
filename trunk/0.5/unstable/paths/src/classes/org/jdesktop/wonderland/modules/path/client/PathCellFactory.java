package org.jdesktop.wonderland.modules.path.client;

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.path.common.PathCellServerState;
import org.jdesktop.wonderland.modules.path.common.style.DefaultStyleFactory;
import org.jdesktop.wonderland.modules.path.common.style.StyleFactory;

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
    public static final String DISPLAY_NAME = "Fence / Cordon / Path";

    private StyleFactory styleFactory;

    /**
     * Create a new instance of a PathCellFactory.
     */
    public PathCellFactory() {
        //ToDo Change Style Factory as needed.
        styleFactory = new DefaultStyleFactory();
    }

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
        PathCellServerState serverState = new PathCellServerState(styleFactory.createDefaultPathStyle(), true, true);
        double angle = 0.0;
        final int noOfNodes = 6;
        final double twoPi = Math.PI * 2.0;
        final double segmentAngle = twoPi / ((double) noOfNodes);
        final float radius = 2.5f;
        for (int index = 0; index < noOfNodes; index++) {
            angle = segmentAngle * index;
            serverState.addPathNodeState((float) Math.sin(angle) * radius, 0.0f, (float) Math.cos(angle) * radius, Integer.toString(index));
        }
        return (T) serverState;
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
