package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;

/**
 * This class represents a message to inform of a change to PathStyle. The
 * complete updated path is included in this message.
 *
 * @author Carl Jokl
 */
public class PathStyleChangedMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private PathStyle pathStyle;

    /**
     * Create a new instance of PathStyleChangedMessage.
     *
     * @param cellID The id of the cell which has changed.
     */
    public PathStyleChangedMessage(CellID cellID) {
        super(cellID);
    }

    /**
     * Get the PathStyle which has changed.
     *
     * @return The PathStyle which has changed.
     */
    public PathStyle getPathStyle() {
        return pathStyle;
    }
}
