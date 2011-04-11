package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which includes a primary length value.
 *
 * @author Carl Jokl
 */
public interface LengthHoldingStyle {

    /**
     * Get the primary length value of this style.
     *
     * @return The primary length value of this style.
     */
    public float getLength();

    /**
     * Set the primary length value of this style.
     *
     * @param length The length value of this style.
     * @throws IllegalArgumentException If the specified length value is outside the supported range.
     */
    public void setLength(float length) throws IllegalArgumentException;

    /**
     * Get the supported range of length values.
     *
     * @return The range of supported length values.
     */
    public FloatValueRange getSupportedLengthRange();
}
