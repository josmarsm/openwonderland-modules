package org.jdesktop.wonderland.modules.ruler.common;

/**
 * Defines and interface for all objects which provide at least
 * read access to basic ruler information.
 *
 * @author Carl Jokl
 */
public interface RulerInfoHolder {

    /**
     * Get the type of the ruler being used.
     *
     * @return The type of ruler being used.
     */
    public RulerType getRulerType();

    /**
     * Get the measurement units of the ruler.
     *
     * @return The measurement units of the ruler.
     */
    public MeasurementUnits getUnits();

    /**
     * Get the scale of the ruler i.e. the number of
     * large units in length the ruler should be.
     *
     * @return The number of large MeasurementUnits unit lengths to which
     *         the length of the ruler is set.
     */
    public float getRulerScale();
}
