package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to notify that a PathNode has changed position.
 *
 * @author Carl Jokl
 */
public class PathNodePositionChangeMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int nodeIndex;
    private final float x;
    private final float y;
    private final float z;

    /**
     * This is a message to notify of a PathNode position change in the PathCell.
     *
     * @param cellID The id of the cell to be changed.
     * @param nodeIndex The index of the PathNode which has changed position.
     * @param x The new x position of the PathNode.
     * @param y The new y position of the PathNode.
     * @param z The new z position of the PathNode.
     */
    public PathNodePositionChangeMessage(CellID cellID, final int nodeIndex, final float x, final float y, final float z) {
        super(cellID);
        this.nodeIndex = nodeIndex;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the index of the PathNode which has changed position.
     *
     * @return The index of the PathNode which has changed position.
     */
    public int getNodeIndex() {
        return nodeIndex;
    }

    /**
     * Get the new X position of the PathNode.
     *
     * @return The new X position of the PathNode.
     */
    public float getX() {
        return x;
    }

    /**
     * Get the new Y position of the PathNode.
     *
     * @return The new Y position of the PathNode.
     */
    public float getY() {
        return y;
    }

    /**
     * Get the new Z position of the PathNode.
     *
     * @return The new Z position of the PathNode.
     */
    public float getZ() {
        return z;
    }
}
