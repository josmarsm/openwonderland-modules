package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This interface represents an object which contains a float value.
 *
 * @author Carl Jokl
 */
public interface FloatValue extends Serializable {

    /**
     * Get the internal primitive value of the FloatValue.
     * 
     * @return The primitive float value of the FloatValue.
     */
    public float getFloat();
}
