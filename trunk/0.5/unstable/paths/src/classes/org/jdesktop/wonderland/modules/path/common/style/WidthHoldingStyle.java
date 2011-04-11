package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which includes a primary width value.
 *
 * @author Carl Jokl
 */
public interface WidthHoldingStyle {

    /**
     * Get the primary width value of this style.
     *
     * @return The primary width value of this style.
     */
    public float getWidth();

    /**
     * Set the primary width value of this style.
     *
     * @param width The width value of this style.
     * @throws IllegalArgumentException If the specified width value is outside the supported range.
     */
    public void setWidth(float width) throws IllegalArgumentException;

    /**
     * Get the supported range of width values.
     *
     * @return The range of supported width values.
     */
    public FloatValueRange getSupportedWidthRange();
}
