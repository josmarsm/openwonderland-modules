package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface represents an object which is a group of PathNode objects.
 * This can exist both on the server or the client where the objects
 * contained in the group all implement the PathNode interface even if
 * implementations may be quite different.
 *
 * @author Carl Jokl
 */
public interface PathNodeGroup {

    /**
     * Get the PathNode in the group at the specified index in the path.
     *
     * @param index The index of the PathNode in the path to be retrieved.
     * @return The PathNode at the specified index in the path.
     * @throws IndexOutOfBoundsException If the specified index is outside the range of PathNode indices.
     */
    public PathNode getPathNode(int index) throws IndexOutOfBoundsException;

    /**
     * Get the number of PathNodes in this PathNodeGroup.
     *
     * @return The number of PathNodes in this PathNodeGroup.
     */
    public int noOfNodes();

    /**
     * Whether this is an empty PathNodeGroup which contains no PathNodes.
     *
     * @return True if the PathNodeGroup is empty, false otherwise.
     */
    public boolean isEmpty();
}
