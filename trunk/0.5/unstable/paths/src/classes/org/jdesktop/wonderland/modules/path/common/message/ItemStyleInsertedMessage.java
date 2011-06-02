package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;

/**
 * This is a message used to notify that a ItemStyle has been inserted at a specified index in a PathStyle.
 *
 * @author Carl Jokl
 */
public class ItemStyleInsertedMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final ItemStyle itemStyle;
    private final int index;

    /**
     * Create a new instance of a ItemStyleInsertedMessage to notify that a new
     * ItemStyle has been inserted at the specified index in the PathStyle.
     *
     * @param cellID The id of the cell to be changed.
     * @param itemStyle The ItemStyle which has been inserted in the PathStyle.
     * @param index The index at which the new ItemStyle has been inserted in the PathStyle.
     */
    public ItemStyleInsertedMessage(CellID cellID, final ItemStyle itemStyle, final int index) {
        super(cellID);
        this.itemStyle = itemStyle;
        this.index = index;
    }

    /**
     * Get the ItemStyle which has been inserted in the PathStyle.
     *
     * @return The ItemStyle which has been inserted in the PathStyle.
     */
    public ItemStyle getItemStyle() {
        return itemStyle;
    }

    /**
     * Get the index at which the new ItemStyle was inserted.
     *
     * @return The index at which the new ItemStyle was inserted.
     */
    public int getIndex() {
        return index;
    }
}
