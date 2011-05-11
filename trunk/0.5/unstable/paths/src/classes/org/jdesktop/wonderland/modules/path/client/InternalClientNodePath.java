package org.jdesktop.wonderland.modules.path.client;

import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.PathNodeState;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This class holds the internal ClientNodePath with the actual ClientNodePath model. Any changes
 * made to this ClientNodePath update the local model but do not send messages to the server.
 * This ClientNodePath should be modified when messages to the server should not be sent or
 * when changes to the model are being made as a result of a message from the server or
 * sending a message about the change is taking place elsewhere.
 */
class InternalClientNodePath implements ClientNodePath {

    private final PathCell parent;
    private final List<ClientPathNode> pathNodes;
    private volatile boolean closedPath;
    private volatile boolean editMode;
    private PathStyle pathStyle;

    /**
     * Create a new InternalClientNodePath for the following PathCell.
     *
     * @param parent The owning PathCell to which requests will be made
     *               to perform graphical updates as needed.
     */
    public InternalClientNodePath(final PathCell parent) {
        this.parent = parent;
        pathNodes = new ArrayList<ClientPathNode>();
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
    public boolean isClosedPath() {
        return closedPath;
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
    public synchronized void setClosedPath(boolean closedPath) {
        if (closedPath != this.closedPath) {
            this.closedPath = closedPath;
            parent.updatePathUI();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setEditMode(boolean editMode) {
        if (editMode != this.editMode) {
            this.editMode = editMode;
            parent.updatePathUI();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        synchronized(pathNodes) {
            return pathNodes.isEmpty();
        }
    }

    /**
     * Get the first IndexedPathNode of the PathCell.
     *
     * @return The first IndexedPathNode on the PathCell or null if the PathCell has no PathNodes.
     */
    @Override
    public ClientPathNode getFirstPathNode() {
        synchronized(pathNodes) {
            return pathNodes.isEmpty() ? null : pathNodes.get(0);
        }
    }

    /**
     * Get the last IndexedPathNode of the PathCell.
     *
     * @return The last IndexedPathNode on the PathCell or null if the PathCell has not PathNodes.
     */
    @Override
    public ClientPathNode getLastPathNode() {
        synchronized(pathNodes) {
            return pathNodes.isEmpty() ? null : pathNodes.get(pathNodes.size() - 1);
        }
    }

    /**
     * Get the IndexedPathNode at the specified index.
     *
     * @param nodeIndex The index of the IndexedPathNode to be retrieved.
     * @return The IndexedPathNode at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the range of valid node indices.
     */
    @Override
    public ClientPathNode getPathNode(int nodeIndex) throws IndexOutOfBoundsException {
        synchronized(pathNodes) {
            if (nodeIndex >= 0 && nodeIndex < pathNodes.size()) {
                return pathNodes.get(nodeIndex);
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The index: %d was outside the valid range of path node indices!", nodeIndex));
            }
        }
    }

    /**
     * Get the IndexedPathNode with the specified label.
     *
     * @param label The label of the IndexedPathNode to be returned.
     * @return The IndexedPathNode with the specified label or null if no IndexedPathNode
     *         was found with that with that label.
     */
    @Override
    public ClientPathNode getPathNode(String label) {
        synchronized(pathNodes) {
            if (label != null) {
                for (ClientPathNode node : pathNodes) {
                    if (label.equals(node.getName())) {
                        return node;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Add a new node to this PathCell.
     *
     * @param node The node to be added to this PathCell.
     * @return True if the node was able to be added successfully.
     */
    @Override
    public boolean addNode(ClientPathNode node) {
        synchronized(pathNodes) {
            if (node != null && pathNodes.add(node)) {
                node.setSequenceIndex(pathNodes.size() - 1);
                return true;
            }
            else {
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(Vector3f position, String name) {
        synchronized(pathNodes) {
            return position != null && pathNodes.add(new IndexedPathNode(this, position, name, pathNodes.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(float x, float y, float z, String name) {
        synchronized(pathNodes) {
            return pathNodes.add(new IndexedPathNode(this, new Vector3f(x, y, z), name, pathNodes.size()));
        }
    }

    /**
     * Insert the specified ClientPathNode at the specified node index.
     *
     * @param nodeIndex The index at which the ClientPathNode is to be inserted. If the insertion index is the
     *                  same as the number of nodes before insertion then the method is essentially like addNode
     *                  except there is no ClientPathNode to be returned by the method in that case.
     * @param node The ClientPathNode to be inserted at the specified index.
     * @return The ClientPathNode which used to be at the specified index (if any).
     * @throws IndexOutOfBoundsException If the specified nodeIndex at which to insert the node is invalid.
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, ClientPathNode node) throws IllegalArgumentException, IndexOutOfBoundsException {
        synchronized(pathNodes) {
            if (node == null) {
                throw new IllegalArgumentException("The specified path node to be inserted was null!");
            }
            else {
                final int noOfNodes = pathNodes.size();
                if (nodeIndex >= 0 && nodeIndex <= noOfNodes) {
                    if (nodeIndex == noOfNodes) {
                        addNode(node);
                        return null;
                    }
                    else {
                        ClientPathNode previous = pathNodes.get(nodeIndex);
                        pathNodes.add(nodeIndex, node);
                        node.setSequenceIndex(nodeIndex);
                        nodeIndex++;
                        previous.setSequenceIndex(nodeIndex);
                        nodeIndex++;
                        while (nodeIndex < noOfNodes) {
                            pathNodes.get(nodeIndex).setSequenceIndex(nodeIndex);
                            nodeIndex++;
                        }
                        return previous;
                    }
                }
                else {
                    throw new IndexOutOfBoundsException(String.format("The node index: %d at which the path node was to be inserted was outside the range of valid indices at which to insert! No of path nodes: %d.", nodeIndex, pathNodes.size()));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, Vector3f position, String name) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (position == null) {
            throw new IllegalArgumentException("The specified position of the path node to be insterted was null!");
        }
        else {
            return insertNode(nodeIndex, new IndexedPathNode(this, position, name, nodeIndex));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, float x, float y, float z, String name) throws IndexOutOfBoundsException {
        return insertNode(nodeIndex, new IndexedPathNode(this, new Vector3f(x, y, z), name, nodeIndex));
    }

    /**
     * Remove the specified IndexedPathNode from the PathCell.
     *
     * @param node The IndexedPathNode to be removed from the PathCell.
     * @return True if the IndexedPathNode was not null and existed in the PathCell
     *         and was able to be removed successfully.
     */
    @Override
    public boolean removeNode(ClientPathNode node) {
        synchronized(pathNodes) {
            if (node != null && !pathNodes.isEmpty()) {
                final int nodeIndex = pathNodes.indexOf(node);
                if (nodeIndex >= 0) {
                    removeNodeAt(nodeIndex);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Remove the IndexedPathNode at the specified node index.
     *
     * @param nodeIndex The index of the IndexedPathNode to be removed.
     * @return The IndexedPathNode removed from the specified index.
     */
    @Override
    public ClientPathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException {
        synchronized(pathNodes) {
            ClientPathNode node = null;
            //ToDo synch UI with changes
            //Based on the number after removing one.
            final int noOfNodes = pathNodes.size() - 1;
            if (nodeIndex >= 0 && nodeIndex <= noOfNodes) {
                node = pathNodes.remove(nodeIndex);
                while (nodeIndex < noOfNodes) {
                    pathNodes.get(nodeIndex).setSequenceIndex(nodeIndex);
                    nodeIndex++;
                }
            }
            return node;
        }
    }

    /**
     * Remove all the ClientPathNodes in this PathCell.
     */
    @Override
    public void removeAllNodes() {
        synchronized(pathNodes) {
            if (!pathNodes.isEmpty()) {
                //ToDo synch UI with changes.
                ClientPathNode current = null;
                while (pathNodes.size() > 0) {
                    current = pathNodes.remove(pathNodes.size() - 1);
                    current.dispose();
                }
            }
        }
    }

    /**
     * Set the IndexedPathNode position and send a message to the server to notify of the change.
     *
     * @param index The index of the IndexedPathNode for which to set the position.
     * @param x The new X position of the IndexedPathNode.
     * @param y The new Y position of the IndexedPathNode.
     * @param z The new Z position of the IndexedPathNode.
     * @return True of the new IndexedPathNode position was able to be set and a notification was sent to the server.
     */
    @Override
    public boolean setNodePosition(int index, float x, float y, float z) {
        synchronized(pathNodes) {
            if (index >= 0 && index < pathNodes.size()) {
                ClientPathNode node = pathNodes.get(index);
                node.getPosition().set(x, y, z);
                parent.updateNodeUI(index, true);
                return true;
            }
            return false;
        }
    }

    /**
     * Get all of the PathNodes in this PathCell as an array.
     *
     * @return An array of all the PathNodes in this PathCell.
     */
    public ClientPathNode[] getNodes() {
        synchronized(pathNodes) {
            return pathNodes.toArray(new ClientPathNode[pathNodes.size()]);
        }
    }

    /**
     * Get all of the PathNodes in this PathCell as a list.
     *
     * @return A List of all the PathNodes in this PathCell.
     */
    public List<ClientPathNode> getNodeList() {
        synchronized(pathNodes) {
            return new ArrayList(pathNodes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public  int noOfNodes() {
        synchronized(pathNodes) {
            return pathNodes.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getNodeStyle(int nodeIndex) throws IndexOutOfBoundsException {
        return pathStyle != null ? pathStyle.getNodeStyle(nodeIndex, true) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getNodeStyleType(int nodeIndex) throws IndexOutOfBoundsException {
        if (pathStyle != null) {
            NodeStyle style = pathStyle.getNodeStyle(nodeIndex, true);
            if (style != null) {
                style.getStyleType();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getSegmentStyle(int segmentIndex) throws IndexOutOfBoundsException {
        return pathStyle != null ? pathStyle.getSegmentStyle(segmentIndex, true) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getSegmentStyleType(int segmentIndex) throws IndexOutOfBoundsException {
        if (pathStyle != null) {
            SegmentStyle style = pathStyle.getSegmentStyle(segmentIndex, true);
            if (style != null) {
                return style.getStyleType();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setFrom(PathCellState state) {
        if (state != null) {
            closedPath = state.isClosedPath();
            editMode = state.isEditMode();
            replacePathStyle(state);
            replaceNodes(state);
            parent.updatePathUI();
        }
    }

    /**
     * Replace the PathStyle of this ClientNodePath using the specified PathCellState.
     * 
     * @param state The PathCellState to be used to set the PathStyle.
     */
    private void replacePathStyle(PathCellState state) {
        pathStyle = state.getPathStyle();
    }

    /**
     * Replace the nodes in this ClientNodePath using the specified PathCellState.
     *
     * @param state The PathCellState to use to set the PathNodes in this ClientNodePath. This value must not be null.
     */
    private void replaceNodes(PathCellState state) {
        synchronized(pathNodes) {
            pathNodes.clear();
            final int noOfNodes = state.noOfNodeStates();
            PathNodeState currentNodeState = null;
            for (int nodeIndex = 0; nodeIndex < noOfNodes; nodeIndex++) {
                currentNodeState = state.getPathNodeState(nodeIndex);
                pathNodes.add(new IndexedPathNode(this, currentNodeState.getPosition(), currentNodeState.getName(), nodeIndex));
            }
        }
    }
}
