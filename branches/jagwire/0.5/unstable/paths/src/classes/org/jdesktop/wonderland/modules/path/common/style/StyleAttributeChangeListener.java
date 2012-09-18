package org.jdesktop.wonderland.modules.path.common.style;

/**
 * Simple interface for a ValueChangeListener which can be notified when the value
 * of a StyleAttribute is changed.
 */
public interface StyleAttributeChangeListener {

    /**
     * This method is called when a StyleAttribute changes. This should only
     * be fired when a value actually changes and not if it is set again but to
     * the same value.
     *
     * @param source The source StyleAttribute which has changed.
     */
    public void attributeChanged(StyleAttribute source);
}
