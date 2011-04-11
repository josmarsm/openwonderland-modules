package org.jdesktop.wonderland.modules.path.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;

/**
 * This cell server state holds state for a path cell.
 * @author Carl Jokl
 */
@XmlRootElement(name="path-cell")
@ServerState
public class PathCellServerState extends CellServerState implements PathCellState {

    /**
     * The name of the Server class for the PathCellServerState.
     */
    public static final String SERVER_CLASS_NAME = "org.jdesktop.wonderland.modules.path.server.PathCellMO";

    @XmlTransient
    private boolean editMode;
    @XmlTransient
    private boolean closedPath;
    @XmlTransient
    private PathStyle pathStyle;
    @XmlElements({
        @XmlElement(name="metadata")
    })
    private List<PathNodeCellServerState> nodes;

    /**
     * Create a default PathServerState with no style set.
     */
    public PathCellServerState() {
        nodes = new ArrayList<PathNodeCellServerState>();
    }

    /**
     * Create a new PathServerState with the specified PathStyle
     * and using the default NodeStyleType for the specified SegmentStyleType
     * for all the nodes.
     *
     * @param pathStyle The PathStyle of the PathCellServerState.
     * @param editMode Whether the PathCell is in edit mode.
     * @param closedPath Whether the path represented by this PathCell is a closed path.
     */
    public PathCellServerState(PathStyle pathStyle, boolean editMode, boolean closedPath) {
        this();
        this.pathStyle = pathStyle;
        this.editMode = editMode;
        this.closedPath = closedPath;
    }

    /**
     * {@inheritDoc}
     */
    @XmlAttribute(name="editMode")
    @Override
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    /**
     * {@inheritDoc}
     */
    @XmlAttribute(name="closedPath")
    @Override
    public boolean isClosedPath() {
        return closedPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClosedPath(boolean closedPath) {
        this.closedPath = closedPath;
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public PathStyle getPathStyle() {
        return pathStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPathStyle(PathStyle pathStyle) {
        this.pathStyle = pathStyle;
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public String getServerClassName() {
        return SERVER_CLASS_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodeStates() {
        return nodes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeState getPathNodeState(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < nodes.size()) {
            return nodes.get(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d is outside the range of node states! No of node states %d.", index, nodes.size()));
        }
    }
}
