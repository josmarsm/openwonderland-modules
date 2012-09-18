package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * This is a message used to change the whether the PathCell is in edit mode.
 *
 * @author Carl Jokl
 */
public class EditModeChangeMessage extends CellMessage implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final boolean editMode;

    /**
     * This is a message to set the edit mode of the PathCell.
     *
     * @param cellID The id of the cell to be changed.
     * @param editMode Whether the PathCell should be in edit mode.
     */
    public EditModeChangeMessage(CellID cellID, final boolean editMode) {
        super(cellID);
        this.editMode = editMode;
    }

    /**
     * Get whether the PathCell should be in EditMode.
     *
     * @return True if the PathCell should be in edit mode. False otherwise.
     */
    public boolean isEditMode() {
        return editMode;
    }
}
