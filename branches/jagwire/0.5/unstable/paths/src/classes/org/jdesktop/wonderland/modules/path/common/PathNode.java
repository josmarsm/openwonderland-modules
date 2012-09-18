package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * A node which represents a way-point along a path.
 *
 * @author Carl Jokl
 */
public interface PathNode extends Positioned, MutableNamed, Cloneable, Serializable {

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

    /**
     * Create a copy of this PathNode.
     *
     * @return A copy of this PathNode.
     */
    public PathNode clone();
}
