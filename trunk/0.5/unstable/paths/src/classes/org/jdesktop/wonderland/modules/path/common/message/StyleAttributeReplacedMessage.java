package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;

/**
 * This is a message used to notify that a StyleAttribute has been replaced within an ItemStyle.
 *
 * @author Carl Jokl
 */
public class StyleAttributeReplacedMessage extends AbstractItemStyleMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final StyleAttribute replacementAttribute;

    /**
     * Create a new instance of a message to notify that a StyleAttribute has been replaced by the specified StyleAttribute
     * within the ItemStyle identified by the specified ItemStyleIdentifier.
     *
     * @param cellID The id of the cell which is the source of the change.
     * @param itemStyleID The ItemStyleIdentifier which identifies the ItemStyle for which a StyleAttribute was replaced.
     * @param replacementAttribute The StyleAttribute which is the replacement attribute to the StyleAttribute with the same name.
     */
    public StyleAttributeReplacedMessage(CellID cellID, final ItemStyleIdentifier itemStyleID, final StyleAttribute replacementAttribute) {
        super(cellID, itemStyleID);
        this.replacementAttribute = replacementAttribute;
    }

    /**
     * Get the StyleAttribute which is the replacement StyleAttribute within the ItemStyle.
     *
     * @return The StyleAttribute which is to replace the existing StyleAttribute (of the same name) within
     *         the ItemStyle.
     */
    public StyleAttribute getReplacementAttribute() {
        return replacementAttribute;
    }
}
