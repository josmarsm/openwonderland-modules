package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to change the whether the PathCell is in edit mode.
 *
 * @author Carl Jokl
 */
public class PathClosedChangeMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final boolean pathClosed;

    /**
     * This is a message to set whether the PathCell should be represented as a closed path..
     *
     * @param cellID The id of the cell to be changed.
     * @param pathClosed Whether the PathCell should be a closed path.
     */
    public PathClosedChangeMessage(CellID cellID, final boolean pathClosed) {
        super(cellID);
        this.pathClosed = pathClosed;
    }

    /**
     * Get whether the PathCell should be a closed path.
     *
     * @return True if the PathCell should be a closed path. False otherwise.
     */
    public boolean isPathClosed() {
        return pathClosed;
    }
}
