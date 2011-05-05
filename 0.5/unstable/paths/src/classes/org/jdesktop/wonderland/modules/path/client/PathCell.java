package org.jdesktop.wonderland.modules.path.client;

import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.PathCellRenderer;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.PathNodeState;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;

/**
 * The client instance of a PathCell with client rendering.
 *
 * @author Carl Jokl
 */
public class PathCell extends Cell implements ClientNodePath {

    private boolean editMode;
    private boolean closedPath;
    private PathStyle pathStyle;
    private PathCellRenderer renderer;
    private List<ClientPathNode> pathNodes;

    /**
     * Create a new instance of a PathCell.
     *
     * @param cellID The id of the PathCell.
     * @param cellCache The CellCache used in the creation of the PathCell.
     */
    public PathCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        pathNodes = new ArrayList<ClientPathNode>();
    }

    /**
     * Set the state of this PathCell.
     *
     * @param state The state which this PathCell is going to be give.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        if (state instanceof PathCellState) {
            PathCellState pathState = (PathCellState) state;
            pathStyle = pathState.getPathStyle();
            closedPath = pathState.isClosedPath();
            editMode = pathState.isEditMode();
            removeAllNodes();
            final int noOfNodes = pathState.noOfNodeStates();
            PathNodeState currentNode = null;
            for (int nodeIndex = 0; nodeIndex < noOfNodes; nodeIndex++) {
                currentNode = pathState.getPathNodeState(nodeIndex);
                addNode(currentNode.getPosition(), currentNode.getName());
            }
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
    public void setClosedPath(boolean closedPath) {
        if (closedPath != this.closedPath) {
            this.closedPath = closedPath;
            if (renderer != null) {
                renderer.updateUI();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEditMode(boolean editMode) {
        if (editMode != this.editMode) {
            this.editMode = editMode;
            if (renderer != null) {
                renderer.updateUI();
            }
        }
    }

    /**
     * Get the number of PathNodes in this path.
     *
     * @return The number of PathNodes in this path.
     */
    public int getNoOfNodes() {
        return pathNodes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return pathNodes.isEmpty();
    }

    /**
     * Get the first PathNode of the PathCell.
     *
     * @return The first PathNode on the PathCell or null if the PathCell has no PathNodes.
     */
    @Override
    public ClientPathNode getFirstPathNode() {
        return pathNodes.isEmpty() ? null : pathNodes.get(0);
    }

    /**
     * Get the last PathNode of the PathCell.
     *
     * @return The last PathNode on the PathCell or null if the PathCell has not PathNodes.
     */
    @Override
    public ClientPathNode getLastPathNode() {
        return pathNodes.isEmpty() ? null : pathNodes.get(pathNodes.size() - 1);
    }

    /**
     * Get the PathNode at the specified index.
     *
     * @param nodeIndex The index of the PathNode to be retrieved.
     * @return The PathNode at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the range of valid node indices.
     */
    @Override
    public ClientPathNode getPathNode(int nodeIndex) throws IndexOutOfBoundsException {
        if (nodeIndex >= 0 && nodeIndex < pathNodes.size()) {
            return pathNodes.get(nodeIndex);
        }
        throw new IndexOutOfBoundsException(String.format("The index: %d was outside the valid range of path node indices!", nodeIndex));
    }

    /**
     * Get the PathNode with the specified label.
     *
     * @param label The label of the PathNode to be returned.
     * @return The PathNode with the specified label or null if no PathNode
     *         was found with that with that label.
     */
    @Override
    public ClientPathNode getPathNode(String label) {
        if (label != null) {
            for (ClientPathNode node : pathNodes) {
                if (label.equals(node.getName())) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Add a new node to this PathCell.
     *
     * @param node The node to be added to this PathCell.
     * @return True if the node was able to be added successfully.
     */
    @Override
    public boolean addNode(ClientPathNode node) {
        if (node != null && pathNodes.add(node)) {
            node.setSequenceIndex(pathNodes.size() - 1);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(Vector3f position, String name) {
        return position != null && addNode(new PathNode(this, position, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(float x, float y, float z, String name) {
        return addNode(new PathNode(this, new Vector3f(x, y, z), name));
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
        if (node == null) {
            throw new IllegalArgumentException("The specified path node to be inserted was null!");
        }
        else {
            final int noOfNodes = pathNodes.size();
            if(nodeIndex >= 0 && nodeIndex <= noOfNodes) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, Vector3f position, String name) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (position == null) {
            throw new IllegalArgumentException("The specified position of the path node to be insterted was null!");
        }
        else {
            return insertNode(nodeIndex, new PathNode(this, position, name));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode insertNode(int nodeIndex, float x, float y, float z, String name) throws IndexOutOfBoundsException {
        return insertNode(nodeIndex, new PathNode(this, new Vector3f(x, y, z), name));
    }

    /**
     * Remove the specified PathNode from the PathCell.
     *
     * @param node The PathNode to be removed from the PathCell.
     * @return True if the PathNode was not null and existed in the PathCell
     *         and was able to be removed successfully.
     */
    @Override
    public boolean removeNode(ClientPathNode node) {
        if (node != null && !pathNodes.isEmpty()) {
            final int nodeIndex = pathNodes.indexOf(node);
            if (nodeIndex >= 0) {
                removeNodeAt(nodeIndex);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the PathNode at the specified node index.
     *
     * @param nodeIndex The index of the PathNode to be removed.
     * @return The PathNode removed from the specified index.
     */
    @Override
    public ClientPathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException {
        ClientPathNode node = null;
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

    /**
     * Remove all the ClientPathNodes in this PathCell.
     */
    @Override
    public void removeAllNodes() {
        if (!pathNodes.isEmpty()) {
            ClientPathNode current = null;
            while (pathNodes.size() > 0) {
                current = pathNodes.remove(pathNodes.size() - 1);
                current.dispose();
            }
        }
    }

    /**
     * Get all of the PathNodes in this PathCell as an array.
     *
     * @return An array of all the PathNodes in this PathCell.
     */
    public ClientPathNode[] getNodes() {
        return pathNodes.toArray(new ClientPathNode[pathNodes.size()]);
    }

    /**
     * Get all of the PathNodes in this PathCell as a list.
     *
     * @return A List of all the PathNodes in this PathCell.
     */
    public List<ClientPathNode> getNodeList() {
        return new ArrayList(pathNodes);
    }

    /**
     * Get a ListIterator to iterate through the PathNodes within this PathCell.
     *
     * @return A ListIterator to iterate through the PathNodes within this PathCell.
     */
    public ListIterator<ClientPathNode> getNodeIterator() {
        List<Cell> children = getChildren();
        List<ClientPathNode> nodes = new ArrayList<ClientPathNode>(children.size());
        for (Cell child : children) {
            if (child instanceof ClientPathNode) {
                nodes.add((ClientPathNode) child);
            }
        }
        return nodes.listIterator();
    }

    /**
     * Create the CellRenderer of this PathCell.
     *
     * @param rendererType The type of renderer which needs to be created.
     * @return The CellRenderer of the PathCell to display this PathCell.
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        logger.warning("Creating path cell cell renderer!");
        if (rendererType == RendererType.RENDERER_JME) {
            renderer = new PathCellRenderer(this);
            return renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    /**
     * Set the status of this PathCell.
     *
     * @param status The status which this PathCell is to have.
     * @param increasing If the PathCell is moving from a lower activity state to a higher activity state.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        logger.warning(String.format("Status change to: %s increacing: %s in class: %s.", status.name(), Boolean.toString(increasing), getClass().getName()));
        super.setStatus(status, increasing);
        if (renderer != null) {
            renderer.setStatus(status, increasing);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodes() {
        return pathNodes.size();
    }

    /**
     * Simple client implementation of a PathNode.
     */
    private static class PathNode implements ClientPathNode {

        private ClientNodePath parent;
        private Vector3f position;
        private String label;
        private int sequenceIndex;

        /**
         * Create a new instance of a PathNode for use within this PathCell.
         *
         * @param parent The ClientNodePath which is the parent path to which the PathNode belongs.
         * @param position The 3D Position of the PathNode within the space.
         * @param label The label which describes what this PathNode represents (optional).
         */
        public PathNode(ClientNodePath parent, Vector3f position, String label) {
            this.parent = parent;
            this.position = position != null ? position : new Vector3f();
            this.label = label;
            sequenceIndex = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePath getPath() {
            return parent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ClientPathNode getLast() {
            return parent.getLastPathNode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ClientPathNode getNext() {
            final int noOfNodes = parent.noOfNodes();
            if (noOfNodes > 1 && sequenceIndex >= 0) {
                if ((sequenceIndex + 1) < noOfNodes) {
                    return parent.getPathNode(sequenceIndex + 1);
                }
                else if (sequenceIndex == (noOfNodes - 1) && parent.isClosedPath()) {
                    return parent.getFirstPathNode();
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            final int noOfNodes = parent.noOfNodes();
            return noOfNodes > 1 && sequenceIndex >= 0 && (((sequenceIndex + 1) < noOfNodes) || (parent.isClosedPath()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ClientPathNode getFirst() {
            return parent.getFirstPathNode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ClientPathNode getPrevious() {
            final int noOfNodes = parent.noOfNodes();
            if (noOfNodes > 1 && sequenceIndex < noOfNodes) {
                if (sequenceIndex > 0) {
                    return parent.getPathNode(sequenceIndex - 1);
                }
                else if (sequenceIndex == 0 && parent.isClosedPath()) {
                    return parent.getLastPathNode();
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            final int noOfNodes = parent.noOfNodes();
            return noOfNodes > 1 && sequenceIndex < noOfNodes && (sequenceIndex > 0 || (sequenceIndex == 0 && parent.isClosedPath()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSentinel() {
            return !parent.isEmpty() && parent.getFirstPathNode() == this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNamed() {
            return label != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getSequenceIndex() {
            return sequenceIndex;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSequenceIndex(int sequenceIndex) {
            this.sequenceIndex = sequenceIndex;
        }

        /**
         * Calculate the index by finding the index of this node within the parent path.
         */
        protected void recalculateIndex() {
            final int noOfNodes = parent.noOfNodes();
            sequenceIndex = -1;
            for (int index = 0; index < noOfNodes; index++) {
                if (parent.getPathNode(index) == this) {
                    sequenceIndex = index;
                    break;
                }
            }

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Vector3f getPosition() {
            return position;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            parent = null;
            position = null;
            label = null;
        }
    }
}
