package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface represents an object which has a description and can have a description set.
 *
 * @author Carl Jokl
 */
public interface MutableDescribed extends Described {

    /**
     * Set the description of the item.
     *
     * @param description The description the item is to have or null if the item should have no description.
     */
    public void setDescription(String description);

    /**
     * Whether a description is set for the item.
     *
     * @return True if the description for the item is not null or false otherwise.
     */
    public boolean isDescriptionSet();
}
