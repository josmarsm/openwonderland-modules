/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This class is an abstract base class for all messages which relate to ItemStyle.
 *
 * @author Carl Jokl
 */
public abstract class AbstractItemStyleMessage extends CellMessage implements Serializable {

    private final ItemStyleIdentifier itemStyleID;

    /**
     * Protected constructor for initializing this AbstractItemStyleMessage.
     *
     * @param cellID The id of the Cell to which the message relates.
     * @param itemStyleID The identifier of the ItemStyle which has been altered.
     */
    protected AbstractItemStyleMessage(CellID cellID, ItemStyleIdentifier itemStyleID) {
        super(cellID);
        if (itemStyleID == null) {
            throw new IllegalArgumentException("The item style identifier for the item style message cannot be null!");
        }
        this.itemStyleID = itemStyleID;
    }

    /**
     * Get the ItemStyleIdentifier which identifies the ItemStyle associated with this message.
     *
     * @return The ItemStyleIdentifier which is used to identify the ItemStyleAssociated with this message.
     */
    public ItemStyleIdentifier getItemStyleID() {
        return itemStyleID;
    }
}
