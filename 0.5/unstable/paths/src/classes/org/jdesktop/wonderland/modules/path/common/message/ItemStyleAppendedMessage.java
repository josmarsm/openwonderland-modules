package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;

/**
 * This is a message used to notify that an ItemStyle has been appended to a PathStyle.
 *
 * @author Carl Jokl
 */
public class ItemStyleAppendedMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final ItemStyle itemStyle;

    /**
     * Create a new instance of a ItemStyleAppendedMessage to notify that a new
     * ItemStyle has been appended to the PathStyle.
     *
     * @param cellID The id of the cell to be changed.
     * @param ItemStyle The NodeStyle which has been added to the PathStyle.
     */
    public ItemStyleAppendedMessage(CellID cellID, final ItemStyle itemStyle) {
        super(cellID);
        this.itemStyle = itemStyle;
    }

    /**
     * Get the ItemStyle which has been added to the PathStyle.
     *
     * @return The ItemStyle which has been added to the PathStyle.
     */
    public ItemStyle getItemStyle() {
        return itemStyle;
    }
}
