package org.jdesktop.wonderland.modules.path.common;

/**
 * The Cell State of a Path Cell.
 *
 * @author Carl Jokl
 */
public interface PathCellState extends NodePath {

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
}
