package org.jdesktop.wonderland.modules.ruler.common;

/**
 * An interface which shows the attributes which should be accessible for a given
 * RulerCellState object whether it be on the client or the server.
 *
 * @author Carl Jokl
 */
public interface RulerCellState extends RulerInfoHolder  {

    /**
     * Set the ruler type for this RulerCellState.
     *
     * @param rulerType The type of ruler which is being used.
     */
    public void setRulerType(RulerType rulerType);

    /**
     * Set the units of measure for this RulerCellState.
     *
     * @param units The units of measure for the ruler.
     */
    public void setUnits(MeasurementUnits units);

    /**
     * The scale of the ruler in the RulerCell. This is the number of large units
     * in length the ruler is.
     *
     * @param scale The scale of the ruler which determines its length.
     */
    public void setRulerScale(float scale);
}
