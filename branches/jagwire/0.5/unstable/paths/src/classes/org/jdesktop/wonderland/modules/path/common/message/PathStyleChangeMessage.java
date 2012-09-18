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
public class PathStyleChangeMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final PathStyle pathStyle;

    /**
     * Create a new instance of PathStyleChangeMessage.
     *
     * @param cellID The id of the cell which has changed.
     * @param pathStyle The new PathStyle which the PathCell now has.
     */
    public PathStyleChangeMessage(CellID cellID, final PathStyle pathStyle) {
        super(cellID);
        this.pathStyle = pathStyle;
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
