package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface is used to denote an object which is named and can be named.
 *
 * @author Carl Jokl
 */
public interface MutableNamed extends Named {

    /**
     * Set the name of the item or null if the item is not named.
     *
     * @param name The name the item is to have or null if the item is to be unnamed. 
     */
    public void setName(String name);

     /**
     * Whether the item is named.
     *
     * @return True if the item has a name or false otherwise.
     */
    public boolean isNamed();
}
