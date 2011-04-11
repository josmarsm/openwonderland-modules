package org.jdesktop.wonderland.modules.path.common;

import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;

/**
 * This class represents the CellState on the client for a PathCell.
 *
 * @author Carl Jokl
 */
public class PathCellClientState extends CellClientState implements PathCellState {

    private boolean editMode;
    private boolean closedPath;
    private PathStyle pathStyle;
    private List<PathNodeCellClientState> nodes;

    /**
     * Create a default PathServerState which has an
     * invisible style.
     */
    public PathCellClientState() {
        nodes = new ArrayList<PathNodeCellClientState>();
    }

    /**
     * Create a new PathServerState with the specified PathStyle
     * and using the default NodeStyleType for the specified SegmentStyleType
     * for all the nodes.
     *
     * @param pathStyle The PathStyle of the PathCellServerState.
     * @param editMode Whether the PathCell is in edit mode.
     * @param closedPath Whether the path that this PathCell represents is a closed path.
     */
    public PathCellClientState(PathStyle pathStyle, boolean editMode, boolean closedPath) {
        this.pathStyle = pathStyle;
        this.editMode = editMode;
        this.closedPath = closedPath;
    }

    /**
     * {@inheritDoc}
     */
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
