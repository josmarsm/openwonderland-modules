package org.jdesktop.wonderland.modules.path.client.receiver;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;

/**
 * This class is intended to receive messages to notify of changed NodePath
 * state boolean flags such as edit mode or whether the NodePath is a closed path.
 *
 * @author Carl Jokl
 */
public class FlagStateChangeMessageReceiver extends PathCellComponentMessageReceiver {

    /**
     * Create a new instance of a FlagStateChangeMessageReceiver to receive messages
     * about changes to any boolean state flags for the NodePath.
     *
     * @param cell The Cell for which this message receiver is to receive messages.
     * @param path The ClientNodePath to be modified by the state change messages.
     */
    public FlagStateChangeMessageReceiver(Cell cell, ClientNodePath path) {
        super(cell, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(CellMessage message) {
        if (handeMessage(message.getSenderID())) {
            if (message instanceof EditModeChangeMessage ) {
                path.setEditMode(((EditModeChangeMessage) message).isEditMode());
            }
            else if (message instanceof PathClosedChangeMessage) {
                path.setClosedPath(((PathClosedChangeMessage) message).isPathClosed());
            }
        }
    }
}
