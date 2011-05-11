package org.jdesktop.wonderland.modules.path.server;

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
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
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

    public PathCellMO() {
        nodes = new ArrayList<PathNodeState>();
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
            channel.addMessageReceiver(EditModeChangeMessage.class, stateFlagReceiver);
            channel.addMessageReceiver(PathClosedChangeMessage.class, stateFlagReceiver);
            channel.addMessageReceiver(PathNodePositionChangeMessage.class, new PathNodeChangeMessageReceiver(this));
            channel.addMessageReceiver(PathStyleChangeMessage.class, new PathStyleChangeMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(EditModeChangeMessage.class);
            channel.removeMessageReceiver(PathClosedChangeMessage.class);
            channel.removeMessageReceiver(PathNodePositionChangeMessage.class);
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
            throw new IndexOutOfBoundsException(String.format("The specified index: %d is outside the range of node states! No of node states %d.", index, nodes.size()));
        }
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
}

