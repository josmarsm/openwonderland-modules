package org.jdesktop.wonderland.modules.path.client.ui;

/**
 * This interface represents a command which uses a position. Both moving PathNodes and creating new PathNodes involve
 * selecting a destination position. This interface allows that position to be selected with reusable functionality.
 *
 * @author Carl Jokl
 */
public interface Positionable {

    /**
     * This method is invoked when a position has been chosen either to move an item or at which to create the item.
     *
     * @param x The X coordinate to use.
     * @param y The Y coordinate to use.
     * @param z The Z coordinate to use.
     */
    public void usePosition(float x, float y, float z);
}
