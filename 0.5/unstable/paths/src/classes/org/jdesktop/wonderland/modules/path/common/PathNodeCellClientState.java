package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * This class represents CellClientState for a PathNodeCell.
 *
 * @author Carl Jokl
 */
public class PathNodeCellClientState extends CellClientState implements PathNodeState, Serializable {

    private int sequenceIndex;

    /**
     * Get the sequence index for the PathNodeCell which denotes where on the path of nodes it is located.
     *
     * @return The sequence index of the PathNodeCell within the PathCell. Indices begin at zero.
     */
    public int getSequenceIndex() {
        return sequenceIndex;
    }

    /**
     * Set the sequence index of the PathNodeCell which denote where on the path of nodes it is located.
     *
     * @param sequenceIndex The sequence index of the PathNodeCell within the PathCell. Indices begin at zero.
     */
    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }
}
