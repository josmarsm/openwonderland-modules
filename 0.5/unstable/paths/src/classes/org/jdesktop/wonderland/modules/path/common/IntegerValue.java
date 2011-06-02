package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This interface represents an object which contains an integer value.
 *
 * @author Carl Jokl
 */
public interface IntegerValue extends Serializable {

    /**
     * Get the primitive value of this IntegerValue.
     * 
     * @return The primitive value of this IntegerValue.
     */
    public int getInteger();
}
