package org.jdesktop.wonderland.modules.path.common;

import com.jme.math.Vector3f;
import java.io.Serializable;

/**
 * The Cell State of a Path Cell.
 *
 * @author Carl Jokl
 */
public interface PathCellState extends NodePath, Cloneable, Serializable {

    /**
     * Get the number of PathNodes in the PathNodes in the PathCell.
     *
     * @return The number of PathNodes in the PathCell.
     */
    public int noOfNodeStates();

    /**
     * Get the PathNode at the specified index.
     *
     * @param index The index of the PathNode to be retrieved.
     * @return The PathNode at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the range of PathNodes.
     */
    public PathNodeState getPathNodeState(int index) throws IndexOutOfBoundsException;

    /**
     * Add the specified PathNodeState to this PathCellState object.
     *
     * @param nodeState The PathNodeState object to be added to this PathCellState.
     * @return True if the PathNodeState was able to be added successfully.
     */
    public boolean addPathNodeState(PathNodeState nodeState);

    /**
     * Add a new PathNodeState to this PathCellState with the specified attributes.
     *
     * @param position The position of the PathNode.
     * @param name The name of the PathNode (optional).
     * @return True if the PathNode was able to be created and added, false otherwise.
     */
    public boolean addPathNodeState(Vector3f position, String name);

    /**
     * Add a new PathNodeState to this PathCellState with the specified attributes.
     *
     * @param x The x dimension position of the PathNode.
     * @param y The y dimension position of the PathNode.
     * @param z The z dimension position of the PathNode.
     * @param name The name of the PathNode (optional).
     * @return True if the PathNode was able to be created and added, false otherwise.
     */
    public boolean addPathNodeState(float x, float y, float z, String name);

    /**
     * Insert the specified PathNodeState at the specified index within the PathCellState.
     * If the index at which the PathNodeState is to be inserted is the same as the
     * number of path nodes then the method is equivalent to adding a PathNodeState.
     *
     * @param nodeState The PathNodeState to be inserted (this should not be null for the operation to succeed).
     * @param index The index at which to insert the PathNodeState.
     * @return The PathNodeState The PathNodeState which was previously at the specified index or null if the
     *         PathNodeState was added to the end of the PathNodeStates or the supplied PathNodeState was null.
     * @throws IndexOutOfBoundsException If the specified index at which to insert the PathNodeState was outside the
     *                                   valid range of PathNodeState indices.
     */
    public PathNodeState insertPathNodeState(PathNodeState nodeState, int index) throws IndexOutOfBoundsException;

    /**
     * Create a new PathNodeState  specified PathNodeState at the specified index within the PathCellState.
     * If the index at which the PathNodeState is to be inserted is the same as the
     * number of path nodes then the method is equivalent to adding a PathNodeState.
     *
     * @param position The position of the PathNode.
     * @param name The name of the PathNode.
     * @param index The index at which to insert the new PathNodeState.
     * @return
     * @throws IndexOutOfBoundsException If the specified index at which to insert the PathNodeState was outside the
     *                                   valid range of PathNodeState indices.
     */
    public PathNodeState insertPathNodeState(Vector3f position, String name, int index) throws IndexOutOfBoundsException;

    /**
     * Create a new PathNodeState  specified PathNodeState at the specified index within the PathCellState.
     * If the index at which the PathNodeState is to be inserted is the same as the
     * number of path nodes then the method is equivalent to adding a PathNodeState.
     *
     * @param x The x dimension position of the PathNode.
     * @param y The y dimension position of the PathNode.
     * @param z The z dimension position of the PathNode.
     * @param name The name of the PathNode.
     * @param index The index at which to insert the new PathNodeState.
     * @return
     * @throws IndexOutOfBoundsException If the specified index at which to insert the PathNodeState was outside the
     *                                   valid range of PathNodeState indices.
     */
    public PathNodeState insertPathNodeState(float x, float y, float z, String name, int index) throws IndexOutOfBoundsException;

    /**
     * Remove the specified PathNodeState from this PathCellState.
     *
     * @param nodeState The PathNodeState which is to be removed from this PathCellState.
     * @return True if the specified PathNodeState was not null and was present in this
     *         PathCellState and was able to be removed successfully.
     */
    public boolean removePathNodeState(PathNodeState nodeState);

    /**
     * Remove the PathNodeState at the specified index from this PathCellState.
     *
     * @param nodeIndex The index of the PathNodeState to be removed from this PathCellState.
     * @return The PathNodeState which was removed from this PathCellState.
     * @throws IndexOutOfBoundsException If the specified node index was outside the range of PathNodeState indices.
     */
    public PathNodeState removePathNodeStateAt(int nodeIndex) throws IndexOutOfBoundsException;

    /**
     * Remove all of the PathNodeStates within this PathCellState.
     */
    public void removeAllPathNodeStates();

    /**
     * Covariant overload to return a more specific clone of this object from the Object.clone() method,
     * i.e. return the PathCellState.
     *
     * @return A clone of this PathCellState.
     */
    @Override
    public PathCellState clone();

    /**
     * Set the PathCellState using the values from the specified PathCellState. This can be useful when restoring
     * the PathCellState back to a previous saved state.
     *
     * @param state The PathCellState to use to set the state of this PathCellState. If the specified PathCellState is null
     *              then the value is ignored and no changes are made.
     */
    public void setFrom(PathCellState state);

    /**
     * Get an array containing the positions of all the PathNodes which can be used for such things
     * as animations.
     *
     * @return An array of all the 3D positions of the PathNodes in the NodePath.
     */
    public Vector3f[] getNodePositions();
}
