package org.jdesktop.wonderland.modules.path.server;

import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.PathCellClientState;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.PathCellServerState;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.PathNodeGroup;
import org.jdesktop.wonderland.modules.path.common.PathNodeState;
import org.jdesktop.wonderland.modules.path.common.message.AllPathNodesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeNameChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathStyleChangeMessage;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.server.receiver.StateFlagChangeMessageReceiver;
import org.jdesktop.wonderland.modules.path.server.receiver.PathNodeChangeMessageReceiver;
import org.jdesktop.wonderland.modules.path.server.receiver.PathStyleChangeMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The PathCell managed object for holding the PathCell state on the server.
 *
 * @author Carl Jokl
 */
public class PathCellMO extends CellMO implements NodePath, PathNodeGroup {

    /**
     * The fully qualified name of the client PathCell class.
     */
    public static final String CLIENT_CELL_CLASS_NAME = "org.jdesktop.wonderland.modules.path.client.PathCell";
    
    private boolean editMode;
    private boolean closedPath;
    private PathStyle pathStyle;
    private List<PathNodeState> nodes;

    /**
     * Create a new instance of a PathCell managed object.
     */
    public PathCellMO() {
        nodes = new ArrayList<PathNodeState>();
    }

    /**
     * Private constructor used in cloning instances of PathCellMO.
     *
     * @param nodes A List of the PathNodeStates to use in this PathCellMO.
     * @param editMode Whether the PathCell is in edit mode.
     * @param closedPath Whether the NodePath is a closed path.
     * @param pathStyle The PathStyle of the NodePath.
     * @throws IllegalArgumentException If the specified PathNodeState list is null.
     */
    private PathCellMO(final List<PathNodeState> nodes, boolean editMode, boolean closedPath, final PathStyle pathStyle) throws IllegalArgumentException {
        if (nodes == null) {
            throw new IllegalArgumentException("The path node states for this path cell state managed object cannot be null!");
        }
        this.nodes = nodes;
        this.editMode = editMode;
        this.closedPath = closedPath;
        this.pathStyle = pathStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return CLIENT_CELL_CLASS_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            PathCellServerState pathState = new PathCellServerState(pathStyle, editMode, closedPath);
            for (PathNodeState nodeState : nodes) {
                pathState.addPathNodeState(nodeState);
            }
            state = pathState;
        }
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        if (state instanceof PathCellState) {
            PathCellState pathState = (PathCellState) state;
            editMode = pathState.isEditMode();
            closedPath = pathState.isClosedPath();
            pathStyle = pathState.getPathStyle();
            final int noOfNodes = pathState.noOfNodeStates();
            nodes.clear();
            for (int nodeIndex = 0; nodeIndex < noOfNodes; nodeIndex++) {
                nodes.add(pathState.getPathNodeState(nodeIndex));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            PathCellClientState pathState = new PathCellClientState(pathStyle, editMode, closedPath);
            for (PathNodeState nodeState : nodes) {
                pathState.addPathNodeState(nodeState);
            }
            cellClientState = pathState;
        }
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        ChannelComponentMO channel = this.getComponent(ChannelComponentMO.class);
        if (live) {
            StateFlagChangeMessageReceiver stateFlagReceiver = new StateFlagChangeMessageReceiver(this);
            PathNodeChangeMessageReceiver nodeChangeReceiver = new PathNodeChangeMessageReceiver(this);
            channel.addMessageReceiver(EditModeChangeMessage.class, stateFlagReceiver);
            channel.addMessageReceiver(PathClosedChangeMessage.class, stateFlagReceiver);
            channel.addMessageReceiver(PathNodePositionChangeMessage.class, nodeChangeReceiver);
            channel.addMessageReceiver(PathNodeAddedMessage.class, nodeChangeReceiver);
            channel.addMessageReceiver(PathNodeInsertedMessage.class, nodeChangeReceiver);
            channel.addMessageReceiver(PathNodeRemovedMessage.class, nodeChangeReceiver);
            channel.addMessageReceiver(AllPathNodesRemovedMessage.class, nodeChangeReceiver);
            channel.addMessageReceiver(PathNodeNameChangeMessage.class, nodeChangeReceiver);
            channel.addMessageReceiver(PathStyleChangeMessage.class, new PathStyleChangeMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(EditModeChangeMessage.class);
            channel.removeMessageReceiver(PathClosedChangeMessage.class);
            channel.removeMessageReceiver(PathNodePositionChangeMessage.class);
            channel.removeMessageReceiver(PathNodeAddedMessage.class);
            channel.removeMessageReceiver(PathNodeInsertedMessage.class);
            channel.removeMessageReceiver(PathNodeRemovedMessage.class);
            channel.removeMessageReceiver(AllPathNodesRemovedMessage.class);
            channel.removeMessageReceiver(PathNodeNameChangeMessage.class);
            channel.removeMessageReceiver(PathStyleChangeMessage.class);
        }
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
    public boolean isEditMode() {
        return editMode;
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
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
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
    public PathNode getPathNode(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < nodes.size()) {
            return nodes.get(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d is outside the range of node states! No of node states: %d.", index, nodes.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(float x, float y, float z, String name) {
        return nodes.add(new PathNodeState(x, y, z, name, nodes.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNode insertNode(int nodeIndex, float x, float y, float z, String name) throws IndexOutOfBoundsException {
        final int noOfNodes = nodes.size();
        if (nodeIndex == noOfNodes) {
            addNode(x, y, z, name);
            return null;
        }
        else if (nodeIndex >= 0 && nodeIndex < noOfNodes) {
            PathNode existing = nodes.get(nodeIndex);
            nodes.add(nodeIndex, new PathNodeState(x, y, z, name, nodeIndex));
            //Upper limit is one greater due to inserted node.
            for (int currentNodeIndex = nodeIndex + 1; nodeIndex <= noOfNodes; currentNodeIndex++) {
                nodes.get(nodeIndex).setSequenceIndex(currentNodeIndex);
            }
            return existing;
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d at which to insert a path node was outside the range of valid node indices! No of node states: %d.", nodeIndex, noOfNodes));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException {
        final int noOfNodes = nodes.size();
        if (nodeIndex >= 0 && nodeIndex < noOfNodes) {
            return nodes.remove(nodeIndex);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d at which to remove a path node was outside the range of valid node indices! No of node states: %d.", nodeIndex, noOfNodes));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllNodes() {
        nodes.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodes() {
        return nodes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getCoordRange(Vector3f min, Vector3f max) {
        for (PathNodeState node : nodes) {
            if (min != null) {
                min.x = Math.min(min.x, node.getX());
                min.y = Math.min(min.y, node.getY());
                min.z = Math.min(min.z, node.getZ());
            }
            if (max != null) {
                max.x = Math.max(max.x, node.getX());
                max.y = Math.max(max.y, node.getY());
                max.z = Math.max(max.z, node.getZ());
            }
        }
    }

    /**
     * Create a clone of this PathCellMO.
     *
     * @return A clone of this PathCellMO.
     */
    @Override
    public PathCellMO clone() {
        List<PathNodeState> clonedNodes = new ArrayList<PathNodeState>(nodes.size());
        for (PathNodeState state : nodes) {
            clonedNodes.add(state);
        }
        return new PathCellMO(clonedNodes, editMode, closedPath, pathStyle != null ? pathStyle.clone() : null);
    }
}

