package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to notify that a PathNode has been removed from a NodePath.
 *
 * @author Carl Jokl
 */
public class PathNodeRemovedMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int nodeIndex;

    /**
     * This is a message to notify of a PathNode having been removed from a NodePath.
     *
     * @param cellID The id of the cell which is the source of the change.
     * @param nodeIndex The index of the PathNode which has been removed.
     */
    public PathNodeRemovedMessage(CellID cellID, final int nodeIndex) {
        super(cellID);
        this.nodeIndex = nodeIndex;
    }

    /**
     * Get the index of the PathNode which has been removed from the NodePath.
     *
     * @return The index of the PathNode which has been removed from the NodePath.
     */
    public int getNodeIndex() {
        return nodeIndex;
    }
}
