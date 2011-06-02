package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * This is a message used to notify that a PathNode has been removed from a NodePath.
 *
 * @author Carl Jokl
 */
public class StyleAttributeRemovedMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final StyleAttributeIdentifier id;

    /**
     * Create a new instance of this StyleAttributeRemovedMessage to remove the StyleAttribute at the specified index
     * within the ItemStyle identified by the specified ItemStyleIdentifier.
     *
     * @param cellID The id of the cell which is the source of the change.
     * @param itemStyleID The ItemStyleIdentifier which identifies the ItemStyle for which a StyleAttribute was removed.
     * @param index The index of the StyleAttribute in the list of ItemStyle attributes which was removed.
     */
    public StyleAttributeRemovedMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final int index) {
        super(cellID, itemStyleID);
        id = new IndexStyleAttributeIdentifier(index);
    }

    /**
     * Create a new instance of this StyleAttributeRemovedMessage to remove the StyleAttribute with the specified name
     * within the ItemStyle identified by the specified ItemStyleIdentifier.
     *
     * @param cellID The id of the cell which is the source of the change.
     * @param itemStyleID The ItemStyleIdentifier which identifies the ItemStyle for which a StyleAttribute was removed.
     * @param index The index of the StyleAttribute in the list of ItemStyle attributes which was removed.
     */
    public StyleAttributeRemovedMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final String name) {
        super(cellID, itemStyleID);
        id = new NameStyleAttributeIdentifier(name);
    }

    /**
     * Get the id which identifies the StyleAttribute to be removed.
     *
     * @return The StyleAttributeIdentifier which identifies the StyleAttribute to be removed in the ItemStyle.
     */
    public StyleAttributeIdentifier getID() {
        return id;
    }
}
