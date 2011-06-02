package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * This is a message used to notify that a ItemStyle has been removed from the specified index in a PathStyle.
 *
 * @author Carl Jokl
 */
public class ItemStyleRemovedMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance of a ItemStyleRemovedMessage to notify that a
     * ItemStyle has been removed from the specified index in the PathStyle.
     *
     * @param cellID The id of the cell to be changed.
     * @param itemStyleID The ItemStyleIdentifier to identify the ItemStyle to be removed.
     */
    public ItemStyleRemovedMessage(CellID cellID, final ItemStyleIdentifier itemStyleID) {
        super(cellID, itemStyleID);
    }
    
}
