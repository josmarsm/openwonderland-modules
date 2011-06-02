package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;

/**
 * This is a message used to notify that a StyleAttribute has been added to an ItemStyle.
 *
 * @author Carl Jokl
 */
public class StyleAttributeAddedMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final StyleAttribute addedAttribute;

    /**
     * This is a message to notify that a PathNode has been added to the NodePath.
     *
     * @param cellID The id of the cell from which this message originates.
     * @param itemStyleID The ItemStyleIdentifier which identifies the ItemStyle to which a StyleAttribute was added.
     * @param addedAttribute The StyleAttribute that was added to the ItemStyle.
     */
    public StyleAttributeAddedMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final StyleAttribute addedAttribute) {
        super(cellID, itemStyleID);
        this.addedAttribute = addedAttribute;
    }

    /**
     * Get the StyleAttribute which was added to the ItemStyle.
     *
     * @return The StyleAttribute which was added to the ItemStyle.
     */
    public StyleAttribute getAddedAttribute() {
        return addedAttribute;
    }
}
