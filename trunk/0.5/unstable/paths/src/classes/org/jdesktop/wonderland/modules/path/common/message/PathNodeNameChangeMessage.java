package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to notify that a PathNode has changed name.
 *
 * @author Carl Jokl
 */
public class PathNodeNameChangeMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int nodeIndex;
    private final String name;

    /**
     * This is a message to notify of a PathNode having changed name.
     *
     * @param cellID The id of the cell which is the source of the change.
     * @param nodeIndex The index of the PathNode which has changed name.
     * @param name The new name of the PathNode.
     */
    public PathNodeNameChangeMessage(CellID cellID, final int nodeIndex, String name) {
        super(cellID);
        this.nodeIndex = nodeIndex;
        this.name = name;
    }

    /**
     * Get the index of the PathNode which has changed name.
     *
     * @return The index of the PathNode which has changed name.
     */
    public int getNodeIndex() {
        return nodeIndex;
    }

    /**
     * Get the new name of the PathNode.
     *
     * @return The new name of the PathNode.
     */
    public String getName() {
        return name;
    }
}
