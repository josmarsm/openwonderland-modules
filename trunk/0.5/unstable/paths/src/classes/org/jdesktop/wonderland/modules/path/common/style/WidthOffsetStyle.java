package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which has a given width offset to adjust vertical representation.
 *
 * @author Carl Jokl
 */
public interface WidthOffsetStyle {

    /**
     * Get the width offset value of this style.
     *
     * @return The width offset value of this style.
     */
    public float getWidthOffset();

    /**
     * Set the width offset value of this style.
     *
     * @param offset The width offset value of this style.
     * @throws IllegalArgumentException If the specified width offset value is outside the supported range.
     */
    public void setWidthOffset(float offset) throws IllegalArgumentException;

    /**
     * Get the supported range of width offset values.
     *
     * @return The range of supported width offset values.
     */
    public FloatValueRange getSupportedWidthOffsetRange();
}
