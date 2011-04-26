package org.jdesktop.wonderland.modules.path.common;

import com.jme.math.Vector3f;
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
    private List<PathNodeState> nodes;

    /**
     * Create a default PathCellCllientState which has an
     * invisible style.
     */
    public PathCellClientState() {
        nodes = new ArrayList<PathNodeState>();
    }

    /**
     * Create a new PathCellClientState with the specified PathStyle
     * and using the default NodeStyleType for the specified SegmentStyleType
     * for all the nodes.
     *
     * @param pathStyle The PathStyle of the PathCellServerState.
     * @param editMode Whether the PathCell is in edit mode.
     * @param closedPath Whether the path that this PathCell represents is a closed path.
     */
    public PathCellClientState(PathStyle pathStyle, boolean editMode, boolean closedPath) {
        nodes = new ArrayList<PathNodeState>();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addPathNodeState(PathNodeState nodeState) {
        if (nodeState != null && nodes.add(nodeState)) {
            nodeState.setSequenceIndex(nodes.size() - 1);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addPathNodeState(Vector3f position, String name) {
        return addPathNodeState(new PathNodeState(position, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addPathNodeState(float x, float y, float z, String name) {
        return addPathNodeState(new PathNodeState(x, y, z, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeState insertPathNodeState(PathNodeState nodeState, int index) throws IndexOutOfBoundsException {
        final int limit = nodes.size();
        if (index >= 0 && index <= limit) {
            if (nodeState != null) {
                if (index == limit) {
                    addPathNodeState(nodeState);
                }
                else {
                    PathNodeState previous = nodes.get(index);
                    nodes.add(index, nodeState);
                    nodeState.setSequenceIndex(index);
                    index++;
                    previous.setSequenceIndex(index);
                    index++;
                    for (;index <= limit; index++) {
                        nodes.get(index).setSequenceIndex(index);
                    }
                    return previous;
                }
            }
            return null;
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The index: %d at which a path node state was to be inserted is outsize the valid range! No of path node states: %d.", index, nodes.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeState insertPathNodeState(Vector3f position, String name, int index) throws IndexOutOfBoundsException {
        return insertPathNodeState(new PathNodeState(position, name), index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeState insertPathNodeState(float x, float y, float z, String name, int index) throws IndexOutOfBoundsException {
        return insertPathNodeState(new PathNodeState(x, y, z, name), index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePathNodeState(PathNodeState nodeState) {
        if (nodeState != null) {
            if (nodes.remove(nodeState)) {
                int index = nodeState.getSequenceIndex();
                final int limit = nodes.size();
                if (index >= 0 && index < limit) {
                    for (;index < limit; index++) {
                        nodes.get(index).setSequenceIndex(index);
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeState removePathNodeStateAt(int nodeIndex) throws IndexOutOfBoundsException {
        final int limit = nodes.size();
        if (nodeIndex >= 0 && nodeIndex < limit) {
            PathNodeState removedNode = nodes.remove(nodeIndex);
            for (;nodeIndex < limit; nodeIndex++) {
                nodes.get(nodeIndex).setSequenceIndex(nodeIndex);
            }
            return removedNode;
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d at which to remove the path node state was not within the valid range! No of path node states: %d.", nodeIndex, limit));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllPathNodeStates() {
        nodes.clear();
    }
}
