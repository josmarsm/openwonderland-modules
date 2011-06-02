package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to notify that a PathNode has been added to a NodePath.
 *
 * @author Carl Jokl
 */
public class PathNodeAddedMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final float x;
    private final float y;
    private final float z;
    private final String name;

    /**
     * This is a message to notify that a PathNode has been added to the NodePath.
     *
     * @param cellID The id of the cell from which this message originates.
     * @param x The X position of the new PathNode.
     * @param y The Y position of the new PathNode.
     * @param z The Z position of the new PathNode.
     * @param name The name given to the new PathName (if any) this can be null.
     */
    public PathNodeAddedMessage(CellID cellID, final float x, final float y, final float z, final String name) {
        super(cellID);
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
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
     * Get the name of the added PathNode (if any)
     *
     * @return The name of the added PathNode or null if it does not have a name.
     */
    public String getName() {
        return name;
    }
}
