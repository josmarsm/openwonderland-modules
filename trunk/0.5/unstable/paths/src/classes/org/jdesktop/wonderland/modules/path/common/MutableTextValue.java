package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface represents a TextValue for which the value can be set / changed.
 *
 * @author Carl Jokl
 */
public interface MutableTextValue extends TextValue {

    /**
     * Set the String value of this TextValue.
     * 
     * @param text The new String which will be the value of this TextValue.
     */
    public void setText(String text);
}
