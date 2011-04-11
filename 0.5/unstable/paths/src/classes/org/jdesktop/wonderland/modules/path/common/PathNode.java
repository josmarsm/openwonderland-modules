package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * A node which represents a way-point along a path.
 *
 * @author Carl Jokl
 */
public interface PathNode extends Positioned, Serializable {

    /**
     * Get the label of the node (if any).
     *
     * @return The label of the node (if any).
     */
    public String getName();

    /**
     * Whether the node is named.
     *
     * @return True if the node has a name or false otherwise.
     */
    public boolean isNamed();

    /**
     * Get the sequence index of this node in the path.
     *
     * @return The index of the node on the path starting where
     *         node indexes start from zero.
     */
    public int getSequenceIndex();

    /**
     * Set the sequence index of the node in its parent path.
     *
     * @param sequenceIndex The sequence index of the node in its
     *                      parent path.
     */
    public void setSequenceIndex(int sequenceIndex);
}
