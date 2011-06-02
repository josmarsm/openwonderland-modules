package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This class represents a simple default implementation of a TextValue. The indirection which instances of this
 * object provides means that more than one object can share a reference to the same text value which will be updated
 * in both places when a change is made.
 *
 * @author Carl Jokl
 */
public class SimpleTextValue implements MutableTextValue, Comparable<TextValue>, Cloneable, Serializable {

    private String text;

    /**
     * Create a new instance of a blank / unset TextValue.
     */
    public SimpleTextValue() {

    }

    /**
     * Create a new instance of a TextVale with the specified text set within it.
     *
     * @param text A String value which is the value which this TextValue is to have.
     */
    public SimpleTextValue(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTextSet() {
        return text != null;
    }

    /**
     * Compare the specified TextValue to this TextValue.
     */
    @Override
    public int compareTo(TextValue otherValue) {
        if (otherValue != null) {
            if (text != null) {
                return text.compareTo(otherValue.getText());
            }
            else {
                return otherValue.isTextSet() ? 1 : 0;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SimpleTextValue(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleTextValue) {
            SimpleTextValue otherValue = (SimpleTextValue) obj;
            return (text != null ? text.equals(otherValue.getText()) : !otherValue.isTextSet());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return text;
    }
}
