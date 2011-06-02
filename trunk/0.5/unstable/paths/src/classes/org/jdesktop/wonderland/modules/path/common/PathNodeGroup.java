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
     * Add a new node to this PathCell.
     *
     * @param x The x position of the of the new node to be added to this PathNodeGroup.
     * @param y The y position of the of the new node to be added to this PathNodeGroup.
     * @param z The z position of the of the new node to be added to this PathNodeGroup.
     * @param name The name of the new node to be added to this PathNodeGroup (optional).
     * @return True if the node was able to be added successfully.
     */
    public boolean addNode(float x, float y, float z, String name);

    /**
     * Insert the specified PathNode at the specified node index.
     *
     * @param nodeIndex The index at which the PathNode is to be inserted. If the insertion index is the
     *                  same as the number of nodes before insertion then the method is essentially like addNode
     *                  except there is no PathNode to be returned by the method in that case.
     * @param x The x position of the of the new node to be added to this PathNodeGroup.
     * @param y The y position of the of the new node to be added to this PathNodeGroup.
     * @param z The z position of the of the new node to be added to this PathNodeGroup.
     * @param name The name of the new node to be added to this PathNodeGroup (optional).
     * @return The PathNode which used to be at the specified index (if any).
     * @throws IndexOutOfBoundsException If the specified nodeIndex at which to insert the node is invalid.
     */
    public PathNode insertNode(int nodeIndex, float x, float y, float z, String name) throws IndexOutOfBoundsException;

    /**
     * Remove the PathNode at the specified node index.
     *
     * @param nodeIndex The index of the PathNode to be removed.
     * @return The PathNode removed from the specified Index.
     * @throws IndexOutOfBoundsException If the specified node index of the PathNode
     *                                   to be removed is outside the range of valid node indices.
     */
    public PathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException;

    /**
     * Remove all of the PathNodes which are part of this PathNodeGroup.
     */
    public void removeAllNodes();

    /**
     * Whether this is an empty PathNodeGroup which contains no PathNodes.
     *
     * @return True if the PathNodeGroup is empty, false otherwise.
     */
    public boolean isEmpty();
}
