package org.jdesktop.wonderland.modules.path.server.receivers;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;
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
public class PathClosedChangedMessageReceiver extends AbstractComponentMessageReceiver {

    /**
     * Create a new instance of a PathCellEditModeMessageReceiver to receive
     *
     * @param cellMO The PathCellMO which will receive the message.
     */
    public PathClosedChangedMessageReceiver(PathCellMO cellMO) {
        super(cellMO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        CellMO cell = getCell();
        if (cell instanceof PathCellMO && message instanceof PathClosedChangeMessage) {
            ((PathCellMO) cell).setClosedPath(((PathClosedChangeMessage) message).isPathClosed());
            cell.sendCellMessage(clientID, message);
        }
    }

}
