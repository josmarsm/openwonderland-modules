package org.jdesktop.wonderland.modules.ruler.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * This class represents a RulerCellState which is intended to be used
 * on the client.
 *
 * @author Carl Jokl
 */
public class RulerCellClientState extends CellClientState implements RulerCellState {

    private RulerType rulerType;
    private MeasurementUnits units;
    private float scale;

    /**
     * Create a new instance of the CellState for a ruler.
     * By default the ruler will be strait and uses metric
     * meters and millimeters as the unit of measure.
     */
    public RulerCellClientState() {
        rulerType = RulerType.STRAIGHT;
        units = MeasurementUnits.METERS_CENTIMETERS_AND_MILLIMETERS;
        scale = 1.0f;
    }

    /**
     * Create a new instance of RulerCellClientState with the specified
     * ruler type and units of measure.
     *
     * @param rulerType The type of ruler to use (if null the default straight ruler will be used).
     * @param units The units of measure which the ruler should use (if null meters and millimeters
     *                                                               will be used).
     */
    public RulerCellClientState(RulerType rulerType, MeasurementUnits units) {
        this.rulerType = rulerType != null ? rulerType : RulerType.STRAIGHT;
        this.units = units != null ? units : MeasurementUnits.METERS_CENTIMETERS_AND_MILLIMETERS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RulerType getRulerType() {
        return rulerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRulerType(RulerType rulerType) {
        this.rulerType = rulerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementUnits getUnits() {
        return units;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnits(MeasurementUnits units) {
        this.units = units;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getRulerScale() {
        return scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRulerScale(float scale) {
        this.scale = scale;
    }
}
