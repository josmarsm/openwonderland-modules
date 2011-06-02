package org.jdesktop.wonderland.modules.path.client.receiver;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.message.AllPathNodesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeNameChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeRemovedMessage;

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
            else if (message instanceof PathNodeNameChangeMessage) {
                PathNodeNameChangeMessage nameChangeMessage = (PathNodeNameChangeMessage) message;
                try {
                    PathNode node = path.getPathNode(nameChangeMessage.getNodeIndex());
                    node.setName(nameChangeMessage.getName());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index of the path node (from the update message) which has changed name was outside the valid range!", ioobe);
                }
            }
            else if (message instanceof PathNodeAddedMessage) {
                PathNodeAddedMessage nodeAddedMessage = (PathNodeAddedMessage) message;
                path.addNode(nodeAddedMessage.getX(), nodeAddedMessage.getY(), nodeAddedMessage.getZ(), nodeAddedMessage.getName());
            }
            else if (message instanceof PathNodeInsertedMessage) {
                PathNodeInsertedMessage nodeInsertedMessage = (PathNodeInsertedMessage) message;
                try {
                    path.insertNode(nodeInsertedMessage.getNodeIndex(), nodeInsertedMessage.getX(), nodeInsertedMessage.getY(), nodeInsertedMessage.getZ(), nodeInsertedMessage.getName());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index at which the path node was to be inserted (from the update message) was outside the valid range!", ioobe);
                }
            }
            else if (message instanceof PathNodeRemovedMessage) {
                PathNodeRemovedMessage nodeRemovedMessage = (PathNodeRemovedMessage) message;
                try {
                    path.removeNodeAt(nodeRemovedMessage.getNodeIndex());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index at which the path node was to be removed (from the update message) was outside the valid range!", ioobe);
                }
            }
            else if (message instanceof AllPathNodesRemovedMessage) {
                path.removeAllNodes();
            }
        }
    }
}
