package org.jdesktop.wonderland.modules.path.client;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.PathCellRenderer;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.PathNodeGroup;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;

/**
 * The client instance of a PathCell with client rendering.
 *
 * @author Carl Jokl
 */
public class PathCell extends Cell implements NodePath, PathNodeGroup {

    private boolean editMode;
    private boolean closedPath;
    private PathStyle pathStyle;
    private PathCellRenderer renderer;
    private MouseEventListener listener;

    /**
     * Create a new instance of a PathCell.
     *
     * @param cellID The id of the PathCell.
     * @param cellCache The CellCache used in the creation of the PathCell.
     */
    public PathCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        editMode = false;
        closedPath = false;
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
        return getNumChildren();
    }

    /**
     * Get the first PathNode of the PathCell.
     *
     * @return The first PathNode on the PathCell or null if the PathCell has no PathNodes.
     */
    public PathNode getFirst() {
        return getNumChildren() > 0 ? (PathNode) getChildren().get(0) : null;
    }

    /**
     * Get the last PathNode of the PathCell.
     *
     * @return The last PathNode on the PathCell or null if the PathCell has not PathNodes.
     */
    public PathNode getLast() {
        List<Cell> children = getChildren();
        if (!children.isEmpty()) {
            Cell last = children.get(children.size() - 1);
            if (last instanceof PathNode) {
                return (PathNode) last;
            }
        }
        return null;
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
        if (nodeIndex >= 0 && nodeIndex < getNumChildren()) {
            List<Cell> children = getChildren();
            Cell child = children.get(nodeIndex);
            ClientPathNode node = null;
            boolean after = true;
            if (child instanceof ClientPathNode) {
                node = (ClientPathNode) child;
                if (node.getSequenceIndex() == nodeIndex) {
                    return node;
                }
                else {
                    after = node.getSequenceIndex() < nodeIndex;
                }
            }
            //If this is reached then the node was not the desired one.
            final int noOfChildren = children.size();
            if (after) {
                for (int index = nodeIndex + 1; index < noOfChildren; index++) {
                    child = children.get(index);
                    if (child instanceof ClientPathNode) {
                        node = (ClientPathNode) child;
                        if (node.getSequenceIndex() == nodeIndex) {
                            return node;
                        }
                        else if (node.getSequenceIndex() > nodeIndex) {
                            after = false;
                            break;
                        }
                    }
                }
            }
            for (int index = nodeIndex - 1; index >= 0; index--) {
                child = children.get(index);
                if (child instanceof ClientPathNode) {
                    node = (ClientPathNode) child;
                    if (node.getSequenceIndex() == nodeIndex) {
                        return node;
                    }
                }
            }
            return null;
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
    public ClientPathNode getNode(String label) {
        if (label != null) {
            List<Cell> children = getChildren();
            for (Cell child : children) {
                if (label.equals(child.getName()) && child instanceof ClientPathNode) {
                    return (ClientPathNode) child;
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
    public boolean addNode(ClientPathNode node) {
        if (node instanceof Cell) {
            try {
                super.addChild((Cell) node);
                return true;
            }
            catch (MultipleParentException mpe) {
                logger.log(Level.SEVERE, "Failed to add node to the path as it already has a parent!", mpe);
            }
        }
        return false;
    }
    
    /**
     * Remove the specified PathNode from the PathCell.
     *
     * @param node The PathNode to be removed from the PathCell.
     * @return True if the PathNode was not null and existed in the PathCell
     *         and was able to be removed successfully.
     */
    public boolean removeNode(ClientPathNode node) {
        if (node instanceof Cell) {
           removeChild((Cell) node);
           return true;
        }
        return false;
    }

    /**
     * Remove the PathNode at the specified node index.
     *
     * @param nodeIndex The index of the PathNode to be removed.
     * @return The PathNode removed from the specified Index.
     */
    public PathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException {
        PathNode node = getPathNode(nodeIndex);
        if (node instanceof Cell) {
            removeChild((Cell) node);
        }
        return node;
    }

    /**
     * Get all of the PathNodes in this PathCell as an array.
     *
     * @return An array of all the PathNodes in this PathCell.
     */
    public ClientPathNode[] getNodes() {
        List<Cell> children = getChildren();
        List<ClientPathNode> nodes = new ArrayList<ClientPathNode>(children.size());
        for (Cell child : children) {
            if (child instanceof ClientPathNode) {
                nodes.add((ClientPathNode) child);
            }
        }
        return nodes.toArray(new ClientPathNode[nodes.size()]);
    }

    /**
     * Get all of the PathNodes in this PathCell as a list.
     *
     * @return A List of all the PathNodes in this PathCell.
     */
    public List<ClientPathNode> getNodeList() {
        List<Cell> children = getChildren();
        List<ClientPathNode> nodes = new ArrayList<ClientPathNode>(children.size());
        for (Cell child : children) {
            if (child instanceof ClientPathNode) {
                nodes.add((ClientPathNode) child);
            }
        }
        return nodes;
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
        super.setStatus(status, increasing);
        if (renderer != null) {
            if (status == CellStatus.INACTIVE && !increasing && listener != null) {
                listener.removeFromEntity(renderer.getEntity());
            }
            else if (status == CellStatus.RENDERING && increasing && listener == null) {
                listener = new MouseEventListener();
                listener.addToEntity(renderer.getEntity());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodes() {
        return getNumChildren();
    }

    /**
     * Add a child Cell to this PathCell. The only supported Cells which
     * can be added as children of this PathCell are PathNodes.
     *
     * @param child The child to be added to this PathCell which must be a PathNode.
     * @throws MultipleParentException If the added Child Cell already has a parent
     * @throws IllegalArgumentException If the specified Cell is not a PathNode.
     */
    @Override
    public void addChild(Cell child) throws MultipleParentException, IllegalArgumentException {
        if (child instanceof PathNode) {
            addChild(child);
        }
        else {
            throw new IllegalArgumentException("This PathCell only supports having children which are path nodes!");
        }
    }

    /**
     * Class to receive events which occur when the item is clicked.
     */
    private class MouseEventListener extends EventClassListener {

        public MouseEventListener() {
            
        }

        /**
         * Get the events to which this MouseEventListener listens.
         *
         * @return An array of classes of Events to which this mouse listener listens.
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D mouseButtonEvent = (MouseButtonEvent3D) event;
                if (mouseButtonEvent.isClicked() && mouseButtonEvent.getButton() == ButtonId.BUTTON1) {
                    
                    //renderer.updateShape();
                }
            }
        }
    }
}
