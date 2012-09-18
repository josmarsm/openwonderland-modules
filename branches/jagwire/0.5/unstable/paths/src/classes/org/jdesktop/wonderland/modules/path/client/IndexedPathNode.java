package org.jdesktop.wonderland.modules.path.client;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * Simple client implementation of a IndexedPathNode.
 */
class IndexedPathNode implements ClientPathNode {

    private ClientNodePath parent;
    private Vector3f position;
    private String name;
    private int sequenceIndex;

    /**
     * Create a new IndexedPathNode to act as part of the ClientNodePath.
     *
     * @param parent The owning ClientNodePath of this ClientPathNode.
     * @param position The 3D position of the ClientPathNode.
     * @param name The name (if any) of the ClientPathNode.
     * @param sequenceIndex The sequence index of this ClientPathNode.
     */
    public IndexedPathNode(ClientNodePath parent, Vector3f position, String name, int sequenceIndex) {
        this.parent = parent;
        this.position = position != null ? position : new Vector3f();
        this.name = name;
        this.sequenceIndex = sequenceIndex;
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
    public int getNextIndex() {
        final int noOfNodes = parent.noOfNodes();
        if (noOfNodes > 1 && sequenceIndex >= 0) {
            if ((sequenceIndex + 1) < noOfNodes) {
                return sequenceIndex + 1;
            } else if (sequenceIndex == (noOfNodes - 1) && parent.isClosedPath()) {
                return 0;
            }
        }
        return -1;
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
            } else if (sequenceIndex == 0 && parent.isClosedPath()) {
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
    public int getPreviousIndex() {
        final int noOfNodes = parent.noOfNodes();
        if (noOfNodes > 1 && sequenceIndex < noOfNodes) {
            if (sequenceIndex > 0) {
                return sequenceIndex - 1;
            } else if (sequenceIndex == 0 && parent.isClosedPath()) {
                return noOfNodes - 1;
            }
        }
        return -1;
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
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNamed() {
        return name != null;
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

    @Override
    public Vector3f getPosition() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getStyle() {
        if (parent != null && sequenceIndex >= 0 && sequenceIndex < parent.noOfNodes()) {
            return parent.getNodeStyle(sequenceIndex);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getStyleType() {
        if (parent != null && sequenceIndex >= 0 && sequenceIndex < parent.noOfNodes()) {
            return parent.getNodeStyleType(sequenceIndex);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode clone() {
        return new IndexedPathNode(parent, position.clone(), name, sequenceIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode clone(ClientNodePath parent) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (parent == null) {
            ClientPathNode clonedNode = new IndexedPathNode(parent, position.clone(), name, parent.noOfNodes());
            if (parent.addNode(clonedNode)) {
                return clonedNode;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        parent = null;
        position = null;
        name = null;
    }
}
