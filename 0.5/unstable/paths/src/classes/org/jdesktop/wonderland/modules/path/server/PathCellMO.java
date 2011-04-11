package org.jdesktop.wonderland.modules.path.server;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.PathCellClientState;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.PathCellServerState;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.PathNodeGroup;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.server.receivers.EditModeChangedMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The PathCell managed object for holding the PathCell state on the server.
 *
 * @author Carl Jokl
 */
public class PathCellMO extends CellMO implements NodePath, PathNodeGroup {

    public static final String CLIENT_CELL_CLASS_NAME = "org.jdesktop.wonderland.modules.path.client.PathCell";
    
    private boolean editMode;
    private boolean closedPath;
    private PathStyle pathStyle;

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
            state = new PathCellServerState(pathStyle, editMode, closedPath);
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
            PathCellState shapeState = (PathCellState) state;
            editMode = shapeState.isEditMode();
            closedPath = shapeState.isClosedPath();
            pathStyle = shapeState.getPathStyle();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new PathCellClientState(pathStyle, editMode, closedPath);
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
            channel.addMessageReceiver(EditModeChangeMessage.class, new EditModeChangedMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(EditModeChangeMessage.class);
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
        if (index >= 0 && index < getNumChildren()) {
            CellMO currentChild = null;
            PathNode currentNode = null;
            for (ManagedReference<CellMO> childMO : getAllChildrenRefs()) {
                currentChild = childMO.get();
                if (currentChild instanceof PathNode) {
                    currentNode = (PathNode) childMO;
                    if (currentNode.getSequenceIndex() == index) {
                        return currentNode;
                    }
                }
            }
        }
        throw new IndexOutOfBoundsException(String.format("The specified index: %d is outside the range of valid node indices! No of nodes: %d.", index, getNumChildren()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodes() {
        return getNumChildren();
    }

    @Override
    public void addChild(CellMO child) throws MultipleParentException {
        if (child instanceof PathNode) {
            addChild(child);
        }
        else {
            throw new IllegalArgumentException("Only path node managed objects can be added as children of a path managed object!");
        }
    }
}

