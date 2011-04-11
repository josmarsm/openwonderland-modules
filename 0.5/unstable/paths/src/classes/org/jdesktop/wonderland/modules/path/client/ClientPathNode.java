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
}
