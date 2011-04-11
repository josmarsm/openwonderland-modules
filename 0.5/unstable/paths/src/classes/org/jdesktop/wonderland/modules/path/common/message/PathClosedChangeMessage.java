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

    private boolean pathClosed;

    /**
     * This is a message to set the edit mode of the PathCell.
     *
     * @param cellID The id of the cell to be changed.
     * @param pathClosed Whether the PathCell should be a closed path.
     */
    public PathClosedChangeMessage(CellID cellID, boolean pathClosed) {
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

    /**
     * Set whether the PathCell should be a closed path.
     *
     * @param pathClosed True if the PathCell should be a closed path.
     */
    public void setPathClosed(boolean pathClosed) {
        this.pathClosed = pathClosed;
    }
}
