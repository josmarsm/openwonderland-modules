package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which includes a primary height value.
 *
 * @author Carl Jokl
 */
public interface HeightHoldingStyle {

    /**
     * Get the primary height value of this style.
     *
     * @return The primary height value of this style.
     */
    public float getHeight();

    /**
     * Set the primary height value of this style.
     *
     * @param height The height value of this style.
     * @throws IllegalArgumentException If the specified height value is outside the supported range.
     */
    public void setHeight(float height) throws IllegalArgumentException;

    /**
     * Get the supported range of height values.
     *
     * @return The range of supported height values.
     */
    public FloatValueRange getSupportedHeightRange();
}
