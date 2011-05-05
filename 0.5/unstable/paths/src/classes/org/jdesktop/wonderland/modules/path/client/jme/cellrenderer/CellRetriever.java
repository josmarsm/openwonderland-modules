package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.cell.Cell;

/**
 * This is an interface for an object which is used to get hold of the 
 * Cell in which a component is contained.
 *
 * @author Carl Jokl
 */
public interface CellRetriever {

    /**
     * Get the overall encompassing Cell which contains this component.
     *
     * @return The Cell which contains this component.
     */
    public Cell getContainingCell();
}
