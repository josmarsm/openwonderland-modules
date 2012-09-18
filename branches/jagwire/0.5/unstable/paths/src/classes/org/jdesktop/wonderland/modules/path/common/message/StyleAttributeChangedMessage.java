package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;

/**
 * This is a message used to notify that a StyleAttribute has been changed within an ItemStyle.
 *
 * @author Carl Jokl
 */
public class StyleAttributeChangedMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final StyleAttribute changedAttribute;

    /**
     * This is a message to notify of a PathNode having been removed from a NodePath.
     *
     * @param cellID The id of the cell which is the source of the change.
     * @param itemStyleID The ItemStyleIdentifier which identifies the ItemStyle for which a StyleAttribute was changed.
     * @param changedAttribute The StyleAttribute which was changed within the ItemStyle.
     */
    public StyleAttributeChangedMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final StyleAttribute changedAttribute) {
        super(cellID, itemStyleID);
        this.changedAttribute = changedAttribute;
    }

    /**
     * Get the StyleAttribute which has been changed.
     *
     * @return The StyleAttribute which has been changed and which should be updated on other computers where it is present.
     */
    public StyleAttribute getAttribute() {
        return changedAttribute;
    }
}
