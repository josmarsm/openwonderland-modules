package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which includes a primary radius value.
 *
 * @author Carl Jokl
 */
public interface RadiusHoldingStyle {

    /**
     * Get the primary radius value of this style.
     *
     * @return The primary radius value of this style.
     */
    public float getRadius();

    /**
     * Set the primary radius value of this style.
     *
     * @param radius The radius value of this style.
     * @throws IllegalArgumentException If the specified radius value is outside the supported range.
     */
    public void setRadius(float radius) throws IllegalArgumentException;

    /**
     * Get the supported range of radius values.
     *
     * @return The range of supported radius values.
     */
    public FloatValueRange getSupportedRadiusRange();
}
