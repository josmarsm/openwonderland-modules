package org.jdesktop.wonderland.modules.path.client;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This interface extends the basic PathNode interface to provide ease of access to
 * previous and next nodes in order to simplify rendering of nodes and segments.
 *
 * @author Carl Jokl
 */
public interface ClientPathNode extends PathNode, Serializable {

    /**
     * Get the parent NodePath of the ClientPathNode.
     *
     * @return The parent NodePath of this ClientPathNode.
     */
    public NodePath getPath();

    /**
     * Whether this ClientPathNode is expected to be visible.
     * This can be used for optimization to avoid drawing segments
     * between ClientPathNodes both of the ClientPathNodes between the are too
     * segments are too far away to be visible.
     *
     * @return True if the ClientPathNode is in a location close enough to the viewer
     *         to be in a visible state.
     */
    public boolean isVisible();

    /**
     * Get the last ClientPathNode in the chain of ClientPathNodes. This method
     * traverses all the next node references until it finds a node which
     * has no next node (which is assumed to be the last node.
     * 
     * @return The last ClientPathNode in the chain of ClientPathNodes.
     */
    public ClientPathNode getLast();

    /**
     * Get the next node in the sequence for this PathCell.
     *
     * @return The next ClientPathNode in the sequence for this PathCell.
     */
    public ClientPathNode getNext();

    /**
     * Set the next node in the sequence for the PathCell.
     *
     * @param next The next ClientPathNode in the sequence for the PathCell.
     */
    public void setNext(ClientPathNode next);

    /**
     * Whether the ClientPathNode which is next after this ClientPathNode
     * has been set.
     * 
     * @return True if the next ClientPathNode is set.
     */
    public boolean hasNext();

    /**
     * Whether the next ClientPathNode is set and is visible.
     * 
     * @return True if the next ClientPathNode is set and is visible.
     */
    public boolean isNextVisible();

    /**
     * Get the first node in the chain of ClientPathNodes. This method
     * will use the previous node and keep linking back unit a node
     * with no previous is encountered. This is assumed to be the first
     * ClientPathNode.
     * 
     * @return The first ClientPathNode in the chain of ClientPathNode.
     */
    public ClientPathNode getFirst();

    /**
     * Get the previous ClientPathNode in the sequence for the PathCell.
     *
     * @return The previous ClientPathNode in the sequence for the PathCell.
     */
    public ClientPathNode getPrevious();

    /**
     * Set the previous ClientPathNode in the sequence for the PathCell.
     *
     * @param previous The previous ClientPathNode in the sequence of the PathCell.
     */
    public void setPrevious(ClientPathNode previous);

    /**
     * Whether the ClientPathNode which is before this ClientPathNode
     * has been set.
     *
     * @return True if the previous ClientPathNode is set.
     */
    public boolean hasPrevious();

    /**
     * Whether the previous ClientPathNode is set and is visible.
     *
     * @return True if the previous ClientPathNode is set and is visible.
     */
    public boolean isPreviousVisible();

    /**
     * Whether this ClientPathNode is a sentinel node.
     * This is relevant to closed paths where the links from previous to next
     * ClientPathNodes loop around. In these cases another means is needed
     * to identify the start of the path to stop certain operations looping forever.
     * 
     * @return True if this node is a sentinel node in a ClosedPath. 
     */
    public boolean isSentinel();

    /**
     * Flag this ClientPathNode to be the sentinel ClientPathNode in a loop.
     */
    public void flagSentinel();

    /**
     * Clear this ClientPathNode from being the sentinel ClientPathNode in a loop
     * (if it was currently set to be a sentinel).
     */
    public void clearSentinel();

    /**
     * This method removes this ClientPathNode from the chain of ClientPathNodes.
     * If this ClientPathNode has a next and a previous then these two will
     * be linked together as next and previous.
     */
    public void unlink();

    /**
     * Clear out the internal state of this ClientPathNode prior to deletion.
     * This helps the node or attached references be garbage collected.
     */
    public void dispose();
}
