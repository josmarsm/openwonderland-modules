package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface is used to denote an object which is named.
 *
 * @author Carl Jokl
 */
public interface Named {

    /**
     * Get the name of the item (if any).
     * 
     * @return The name of the item or null if the item has no name set.
     */
    public String getName();
}
