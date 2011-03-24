package org.jdesktop.wonderland.modules.ruler.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message class used to send a message between the client
 * and server to indicate the unit of measure for the ruler should
 * be the specified value.
 *
 * @author Carl Jokl
 */
public class RulerCellUnitChangeMessage extends CellMessage {

    private MeasurementUnits units;

    /**
     * Create a new instance of RulerCellUnitChangeMessage with the specified
     * target cell id and MeasurementUnits to be set for the RulerCell.
     *
     * @param cellID The id of the cell for which this message is intended.
     * @param units The new MeasurementUnits which are to be used for the ruler.
     */
    public RulerCellUnitChangeMessage(CellID cellID, MeasurementUnits units) {
        super(cellID);
        this.units = units;
    }

    /**
     * Get the new MeasurementUnits which the ruler is to have.
     *
     * @return An enumeration instance representing the type of measurement units
     *         which the ruler is to use.
     */
    public MeasurementUnits getUnits() {
        return units;
    }

    /**
     * Get the new MeasurementUnits which the ruler is to have.
     *
     * @param units An enumeration instance representing the type of measurement units
     *        which the ruler is to use.
     */
    public void setShapeType(MeasurementUnits units) {
        this.units = units;
    }
}
