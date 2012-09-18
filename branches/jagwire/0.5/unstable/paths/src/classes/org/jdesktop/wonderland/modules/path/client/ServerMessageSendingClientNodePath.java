package org.jdesktop.wonderland.modules.path.client;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.message.AllPathNodesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeNameChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This implementation of a ClientNodePath is used to send state synchronization messages to the server when changes
 * are made which change the path state. This class internally relays the calls to an internal ClientNodePath
 * which makes the actual modifications to the local state.
 */
class ServerMessageSendingClientNodePath implements ClientNodePath {

    private PathCell parent;
    private ClientNodePath internalPath;

    /**
     * This class is a wrapper / decorator around and internal ClientNodePath which
     * sends messages to synchronize the state of the ClientNodePath where needed.
     *
     * @param parent The parent PathCell to be used to send messages to the server.
     * @param internalPath The internal ClientNodePath to which methods will be relayed
     *                     in order to modify the actual model.
     */
    public ServerMessageSendingClientNodePath(PathCell parent, ClientNodePath internalPath) {
        this.parent = parent;
        this.internalPath = internalPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode getPathNode(int index) throws IndexOutOfBoundsException {
        return internalPath.getPathNode(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode getFirstPathNode() {
        return internalPath.getFirstPathNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode getLastPathNode() {
        return internalPath.getLastPathNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode getPathNode(String label) {
        return internalPath.getPathNode(label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(ClientPathNode node) {
        if (internalPath.addNode(node)) {
            ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                Vector3f position = node.getPosition();
                channelComponent.send(new PathNodeAddedMessage(parent.getCellID(), position.x, position.y, position.z, node.getName()));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(Vector3f position, String name) {
        if (internalPath.addNode(position, name)) {
            ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new PathNodeAddedMessage(parent.getCellID(), position.x, position.y, position.z, name));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(float x, float y, float z, String name) {
        if (internalPath.addNode(x, y, z, name)) {
            ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new PathNodeAddedMessage(parent.getCellID(), x, y, z, name));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, ClientPathNode node) throws IllegalArgumentException, IndexOutOfBoundsException {
        ClientPathNode replacedNode = internalPath.insertNode(nodeIndex, node);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            Vector3f position = node.getPosition();
            channelComponent.send(new PathNodeInsertedMessage(parent.getCellID(), nodeIndex, position.x, position.y, position.z, node.getName()));
        }
        return replacedNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, Vector3f position, String name) throws IllegalArgumentException, IndexOutOfBoundsException {
        ClientPathNode replacedNode = internalPath.insertNode(nodeIndex, position, name);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new PathNodeInsertedMessage(parent.getCellID(), nodeIndex, position.x, position.y, position.z, name));
        }
        return replacedNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, float x, float y, float z, String name) throws IndexOutOfBoundsException {
        ClientPathNode replacedNode = internalPath.insertNode(nodeIndex, x, y, z, name);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new PathNodeInsertedMessage(parent.getCellID(), nodeIndex, x, y, z, name));
        }
        return replacedNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeNode(ClientPathNode node) {
        if (internalPath.removeNode(node)) {
            ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new PathNodeRemovedMessage(parent.getCellID(), node.getSequenceIndex()));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException {
        ClientPathNode removedNode = internalPath.removeNodeAt(nodeIndex);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new PathNodeRemovedMessage(parent.getCellID(), nodeIndex));
        }
        return removedNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllNodes() {
        internalPath.removeAllNodes();
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new AllPathNodesRemovedMessage(parent.getCellID()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getNodeStyleType(int nodeIndex) throws IndexOutOfBoundsException {
        return internalPath.getNodeStyleType(nodeIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getNodeStyle(int nodeIndex) throws IndexOutOfBoundsException {
        //ToDo wrap with change detecting decorator?
        return internalPath.getNodeStyle(nodeIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getSegmentStyleType(int segmentIndex) throws IndexOutOfBoundsException {
        return internalPath.getSegmentStyleType(segmentIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getSegmentStyle(int segmentIndex) throws IndexOutOfBoundsException {
        //ToDo wrap with change detecting decorator?
        return internalPath.getSegmentStyle(segmentIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle getPathStyle() {
        //ToDo wrap with change detecting decorator?
        return internalPath.getPathStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditMode() {
        return internalPath.isEditMode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosedPath() {
        return internalPath.isClosedPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEditMode(boolean editMode) {
        internalPath.setEditMode(editMode);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new EditModeChangeMessage(parent.getCellID(), editMode));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClosedPath(boolean closedPath) {
        internalPath.setClosedPath(closedPath);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new PathClosedChangeMessage(parent.getCellID(), closedPath));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPathStyle(PathStyle pathStyle) {
        internalPath.setPathStyle(pathStyle);
        //ToDo send message.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodes() {
        return internalPath.noOfNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return internalPath.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodePosition(int index, float x, float y, float z) throws IndexOutOfBoundsException {
        internalPath.setNodePosition(index, x, y, z);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new PathNodePositionChangeMessage(parent.getCellID(), index, x, y, z));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeName(int index, String name) {
        internalPath.setNodeName(index, name);
        ChannelComponent channelComponent = parent.getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.send(new PathNodeNameChangeMessage(parent.getCellID(), index, name));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFrom(PathCellState state) {
        internalPath.setFrom(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getCoordRange(Vector3f min, Vector3f max) {
        internalPath.getCoordRange(min, max);
    }

    /**
     * Create a clone of this ServerMessageSengingClientNodePath.
     * As this class is a wrapper only the wrapper will be cloned but
     * the wrapped ClientNodePath will not be cloned.
     *
     * @return A clone of this ServerMessageSendingClientNodePath.
     */
    @Override
    public ClientNodePath clone() {
        return new ServerMessageSendingClientNodePath(parent, internalPath);
    }
}
