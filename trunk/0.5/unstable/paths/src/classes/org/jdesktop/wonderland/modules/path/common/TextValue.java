package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This interface represents an object which holds a TextValue (i.e. a String).
 *
 * @author Carl Jokl
 */
public interface TextValue extends Serializable {

    /**
     * Get the text String from this TextValue object.
     * 
     * @return A String representing the text of the object or null if the text is not set.
     */
    public String getText();

    /**
     * Whether the String value is set within this TextValue.
     *
     * @return True if the String value within this TextValue is not null or false otherwise.
     */
    public boolean isTextSet();
   
}
