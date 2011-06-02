package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to notify that a PathNode has been inserted into a NodePath.
 *
 * @author Carl Jokl
 */
public class PathNodeInsertedMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int nodeIndex;
    private final float x;
    private final float y;
    private final float z;
    private final String name;

    /**
     * This is a message to notify that a PathNode has been inserted into a NodePath.
     *
     * @param cellID The id of the cell which was the origin of the message.
     * @param nodeIndex The index at which the PathNode has been inserted.
     * @param x The X position of the new PathNode.
     * @param y The Y position of the new PathNode.
     * @param z The Z position of the new PathNode.
     * @param name The name of the inserted PathNode (if any).
     */
    public PathNodeInsertedMessage(CellID cellID, final int nodeIndex, final float x, final float y, final float z, final String name) {
        super(cellID);
        this.nodeIndex = nodeIndex;
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    /**
     * Get the index at which the new PathNode has been inserted.
     *
     * @return The index at which the new PathNode has been inserted.
     */
    public int getNodeIndex() {
        return nodeIndex;
    }

    /**
     * Get the X position of the new PathNode.
     *
     * @return The X position of the new PathNode.
     */
    public float getX() {
        return x;
    }

    /**
     * Get the Y position of the new PathNode.
     *
     * @return The Y position of the new PathNode.
     */
    public float getY() {
        return y;
    }

    /**
     * Get the Z position of the new PathNode.
     *
     * @return The Z position of the new PathNode.
     */
    public float getZ() {
        return z;
    }

    /**
     * Get the name of the inserted PathNode (if any)
     *
     * @return The name of the inserted PathNode or null if it does not have a name.
     */
    public String getName() {
        return name;
    }
}
