package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which has a given height offset to adjust vertical representation.
 *
 * @author Carl Jokl
 */
public interface HeightOffsetStyle {

    /**
     * Get the height offset value of this style.
     *
     * @return The height offset value of this style.
     */
    public float getHeightOffset();

    /**
     * Set the height offset value of this style.
     *
     * @param offset The height offset value of this style.
     * @throws IllegalArgumentException If the specified height offset value is outside the supported range.
     */
    public void setHeightOffset(float offset) throws IllegalArgumentException;

    /**
     * Get the supported range of height offset values.
     *
     * @return The range of supported height offset values.
     */
    public FloatValueRange getSupportedHeightOffsetRange();
}
