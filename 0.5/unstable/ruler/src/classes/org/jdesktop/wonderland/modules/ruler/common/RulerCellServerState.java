/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ruler.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Shape Cell Server State
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="ruler-cell")
@ServerState
public class RulerCellServerState extends CellServerState implements RulerCellState {

    public static final String SERVER_CLASS_NAME = "org.jdesktop.wonderland.modules.ruler.server.RulerCellMO";

    @XmlTransient
    private RulerType rulerType;
    @XmlTransient
    private MeasurementUnits units;
    @XmlTransient
    private float scale;

    /**
     * Create a new instance of the CellState for a ruler.
     * By default the ruler will be strait and uses metric
     * meters and millimeters as the unit of measure.
     */
    public RulerCellServerState() {
        rulerType = RulerType.STRAIGHT;
        units = MeasurementUnits.METERS_CENTIMETERS_AND_MILLIMETERS;
    }

    /**
     * Create a new instance of RulerCellClientState with the specified
     * ruler type and units of measure.
     *
     * @param rulerType The type of ruler to use (if null the default straight ruler will be used).
     * @param units The units of measure which the ruler should use (if null metres and millimetres
     *                                                               will be used).
     */
    public RulerCellServerState(RulerType rulerType, MeasurementUnits units) {
        this.rulerType = rulerType != null ? rulerType : RulerType.STRAIGHT;
        this.units = units != null ? units : MeasurementUnits.METERS_CENTIMETERS_AND_MILLIMETERS;
    }

    /**
     * {@inheritDoc}
     */
    @XmlElement(name="type")
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
    @XmlElement(name="units")
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
    @XmlElement(name="scale")
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

    /**
     * Get the name of the server managed object class for the RulerCell.
     *
     * @return The name of the server RuleCell managed object.
     */
    @Override
    public String getServerClassName() {
        return SERVER_CLASS_NAME;
    }
}
