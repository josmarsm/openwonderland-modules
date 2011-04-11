package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which has a given height offset to adjust vertical representation.
 *
 * @author Carl Jokl
 */
public interface LengthOffsetStyle {

    /**
     * Get the length offset value of this style.
     *
     * @return The length offset value of this style.
     */
    public float getLengthOffset();

    /**
     * Set the length offset value of this style.
     *
     * @param offset The length offset value of this style.
     * @throws IllegalArgumentException If the specified length offset value is outside the supported range.
     */
    public void setLengthOffset(float offset) throws IllegalArgumentException;

    /**
     * Get the supported range of length offset values.
     *
     * @return The range of supported length offset values.
     */
    public FloatValueRange getSupportedLengthOffsetRange();
}
