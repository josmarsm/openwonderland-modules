package org.jdesktop.wonderland.modules.path.server.receiver;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.message.AllPathNodesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeNameChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeRemovedMessage;
import org.jdesktop.wonderland.modules.path.server.PathCellMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * This class is intended to receive PathCellEditModeChangeMessages for the server.
 *
 * @author Carl Jokl
 */
public class PathNodeChangeMessageReceiver extends AbstractComponentMessageReceiver {

    /**
     * Create a new instance of PathNodeChangeMessageReceiver to receive PathNodePositionChangeMessages.
     *
     * @param cellMO The owning PathCellMO for which to receive messages.
     */
    public PathNodeChangeMessageReceiver(PathCellMO cellMO) {
        super(cellMO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        CellMO cell = getCell();
        if (cell instanceof PathCellMO) {
            PathCellMO pathCellMO = (PathCellMO) cell;
            if (message instanceof PathNodePositionChangeMessage) {
                PathNodePositionChangeMessage positionChangeMessage = (PathNodePositionChangeMessage) message;
                try {
                    PathNode node = pathCellMO.getPathNode(positionChangeMessage.getNodeIndex());
                    node.getPosition().set(positionChangeMessage.getX(), positionChangeMessage.getY(), positionChangeMessage.getZ());
                    cell.sendCellMessage(clientID, message);
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The path node index of the path node which has changed (from the update message) position was invalid!", ioobe);
                }
            }
            else if (message instanceof PathNodeNameChangeMessage) {
                PathNodeNameChangeMessage nameChangeMessage = (PathNodeNameChangeMessage) message;
                try {
                    PathNode node = pathCellMO.getPathNode(nameChangeMessage.getNodeIndex());
                    node.setName(nameChangeMessage.getName());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index of the path node (from the update message) which has changed name was outside the valid range!", ioobe);
                }
            }
            else if (message instanceof PathNodeAddedMessage) {
                PathNodeAddedMessage nodeAddedMessage = (PathNodeAddedMessage) message;
                pathCellMO.addNode(nodeAddedMessage.getX(), nodeAddedMessage.getY(), nodeAddedMessage.getZ(), nodeAddedMessage.getName());
            }
            else if (message instanceof PathNodeInsertedMessage) {
                PathNodeInsertedMessage nodeInsertedMessage = (PathNodeInsertedMessage) message;
                try {
                    pathCellMO.insertNode(nodeInsertedMessage.getNodeIndex(), nodeInsertedMessage.getX(), nodeInsertedMessage.getY(), nodeInsertedMessage.getZ(), nodeInsertedMessage.getName());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index at which the path node was to be inserted (from the update message) was outside the valid range!", ioobe);
                }
            }
            else if (message instanceof PathNodeRemovedMessage) {
                PathNodeRemovedMessage nodeRemovedMessage = (PathNodeRemovedMessage) message;
                try {
                    pathCellMO.removeNodeAt(nodeRemovedMessage.getNodeIndex());
                }
                catch (IndexOutOfBoundsException ioobe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The index at which the path node was to be removed (from the update message) was outside the valid range!", ioobe);
                }
            }
            else if (message instanceof AllPathNodesRemovedMessage) {
                pathCellMO.removeAllNodes();
            }
        }
    }

}
