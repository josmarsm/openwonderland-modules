package org.jdesktop.wonderland.modules.path.client.receiver;

import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;

/**
 * This class is intended to receive PathCellEditModeChangeMessages for the server.
 *
 * @author Carl Jokl
 */
public class EditModeChangedMessageReceiver implements ComponentMessageReceiver {

    private PathCell cell;

    /**
     * Create a new instance of a EditModeChangedMessageReceiver to receive EditModeChangedMessages.
     *
     * @param cell The PathCell for which this message receiver is to receive messages.
     */
    public EditModeChangedMessageReceiver(PathCell cell) {
        this.cell = cell;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(CellMessage message) {
        if (cell != null && message instanceof EditModeChangeMessage && !message.getSenderID().equals(cell.getCellCache().getSession().getID())) {
            cell.setEditMode(((EditModeChangeMessage) message).isEditMode());
        }
    }

}
