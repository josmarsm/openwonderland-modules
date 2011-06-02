package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * This is a message used to notify that a ItemStyle has been removed from the specified index in a PathStyle.
 *
 * @author Carl Jokl
 */
public class ItemStyleSpanChangeMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int newSpan;

    /**
     * Create a new instance of a ItemStyleSpanChangeMessage to notify that a
     * ItemStyle has changed the number of items that it spans.
     *
     * @param cellID The id of the cell to be changed.
     * @param itemStyleID The ItemStyleIdentifier to identify the ItemStyle which changed the number of items which it spans.
     * @param newSpan The new style span i.e. the number of styled items for which this style will span.
     */
    public ItemStyleSpanChangeMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final int newSpan) {
        super(cellID, itemStyleID);
        this.newSpan = newSpan;
    }
    
    /**
     * Get the new ItemStyle span after the change which is being notified of in this message.
     * 
     * @return The new number of items for the style to span after the change.
     */
    public int getSpan() {
        return newSpan;
    }
}
