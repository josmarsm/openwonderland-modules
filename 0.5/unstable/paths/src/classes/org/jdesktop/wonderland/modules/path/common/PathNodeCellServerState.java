package org.jdesktop.wonderland.modules.path.common;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * This class represents the ServerState of a PathNodeCell.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="path-node-cell")
@ServerState
public class PathNodeCellServerState extends CellServerState implements PathNodeState, Serializable {

    @XmlTransient
    private int sequenceIndex;

    /**
     * The name of the Server class for the PathCellServerState.
     */
    public static final String SERVER_CLASS_NAME = "org.jdesktop.wonderland.modules.path.server.PathNodeCellMO";

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public String getServerClassName() {
        return SERVER_CLASS_NAME;
    }

    /**
     * Get the sequence index for the PathNodeCell which denotes where on the path of nodes it is located.
     *
     * @return The sequence index of the PathNodeCell within the PathCell. Indices begin at zero.
     */
    @XmlAttribute(name="index")
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
