package org.jdesktop.wonderland.modules.path.server.receiver;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
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
        if (cell instanceof PathCellMO && message instanceof PathNodePositionChangeMessage) {
            PathNodePositionChangeMessage positionChangeMessage = (PathNodePositionChangeMessage) message;
            PathCellMO pathCellMO = (PathCellMO) cell;
            try {
                PathNode node = pathCellMO.getPathNode(positionChangeMessage.getNodeIndex());
                node.getPosition().set(positionChangeMessage.getX(), positionChangeMessage.getY(), positionChangeMessage.getZ());
                cell.sendCellMessage(clientID, message);
            }
            catch (IndexOutOfBoundsException ioobe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The path node index of the path node which has changed (from the update message) position was invalid!", ioobe);
            }
        }
    }

}
