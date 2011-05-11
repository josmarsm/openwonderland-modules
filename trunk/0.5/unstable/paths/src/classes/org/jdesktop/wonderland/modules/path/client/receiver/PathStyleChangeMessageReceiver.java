package org.jdesktop.wonderland.modules.path.client.receiver;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.message.PathStyleChangeMessage;

/**
 * This class is intended to receive PathStyleChangeMessages from the server.
 *
 * @author Carl Jokl
 */
public class PathStyleChangeMessageReceiver extends PathCellComponentMessageReceiver {

    /**
     * Create a new instance of a PathStyleChangeMessageReceiver to receive PathStyleChangedMessages.
     *
     * @param cell The Cell for which this message receiver is to receive messages.
     * @param path The NodePath which is to be modified and updated by the messages received.
     */
    public PathStyleChangeMessageReceiver(Cell cell, ClientNodePath path) {
        super(cell, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(CellMessage message) {
        if (handeMessage(message.getSenderID())) {
            if (message instanceof PathStyleChangeMessage) {
                path.setPathStyle(((PathStyleChangeMessage) message).getPathStyle());
            }
        }
    }

}
