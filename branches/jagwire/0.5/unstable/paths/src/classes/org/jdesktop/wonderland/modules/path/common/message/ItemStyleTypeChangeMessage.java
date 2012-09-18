package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.path.common.style.StyleType;

/**
 * This is a message used to notify that a ItemStyle has changed StyleType in a PathStyle.
 *
 * @author Carl Jokl
 */
public class ItemStyleTypeChangeMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final StyleType newStyleType;

    /**
     * Create a new instance of a ItemStyleTypeChangeMessage to notify that an
     * ItemStyle has changed StyleType in the PathStyle.
     *
     * @param cellID The id of the cell to be changed.
     * @param itemStyleID The ItemStyleIdentifier which is used to identify the ItemStyle which has changed StyleType.
     * @param newStyleType The new StyleType to which the specified ItemStyle has changed.
     */
    public ItemStyleTypeChangeMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final StyleType newStyleType) {
        super(cellID, itemStyleID);
        this.newStyleType = newStyleType;
    }

    /**
     * Get the new StyleType to which the ItemStyle was changed.
     *
     * @return The StyleType to which the ItemStyle was changed.
     */
    public StyleType getStyleType() {
        return newStyleType;
    }
}
