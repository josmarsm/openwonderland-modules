package org.jdesktop.wonderland.modules.ruler.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message sent between the client and the server to 
 * notify of a change of RulerType.
 *
 * @author Carl Jokl
 */
public class RulerCellTypeChangeMessage extends CellMessage {

    private RulerType rulerType;

    /**
     * Create a new RulerCellTypeChangeMessage with the specified attributes.
     *
     * @param cellID The id of the cell which is the target of the message.
     * @param shapeType The new ruler type to be used for the cell.
     */
    public RulerCellTypeChangeMessage(CellID cellID, RulerType rulerType) {
        super(cellID);
        this.rulerType = rulerType;
    }

    /**
     * Get the ruler type for this RulerCellTypeChangeMessage indicating
     * the type of ruler which should be displayed.
     *
     * @return A ruler type enumeration instance indicating the type of ruler which should be displayed.
     */
    public RulerType getRulerType() {
        return rulerType;
    }

    /**
     * Set the ruler type for this RulerCellTypeChangeMessage indicating the units
     * of measure to be used for the this ruler.
     *
     * @param rulerType A ruler type enumeration instance indicating the type of ruler which should be displayed.
     */
    public void setRulerType(RulerType rulerType) {
        this.rulerType = rulerType;
    }
}
