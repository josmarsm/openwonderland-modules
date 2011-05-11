package org.jdesktop.wonderland.modules.path.client.receiver;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;

/**
 * This class is intended to receive messages when the state of PathNodes changes.
 *
 * @author Carl Jokl
 */
public class PathNodeChangeMessageReceiver extends PathCellComponentMessageReceiver {

    /**
     * Create a new instance of a PathNodeChangeMessageReceiver to receive notifications when any
     * changes are made to PathNodes.
     *
     * @param cell The Cell for which this message receiver is to receive messages.
     * @param path The NodePath which is to be modified by the messages received.
     */
    public PathNodeChangeMessageReceiver(Cell cell, ClientNodePath path) {
        super(cell, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(CellMessage message) {
        if (handeMessage(message.getSenderID())) {
            if (message instanceof PathNodePositionChangeMessage) {
                PathNodePositionChangeMessage positionChangedMessage = (PathNodePositionChangeMessage) message;
                try {
                    path.setNodePosition(positionChangedMessage.getNodeIndex(), positionChangedMessage.getX(), positionChangedMessage.getY(), positionChangedMessage.getZ());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index of the path node (from the update message) which has changed position was outside the valid range!", ioobe);
                }
            }
        }
    }
}
