package org.jdesktop.wonderland.modules.path.server.receiver;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathStyleChangeMessage;
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
public class PathStyleChangeMessageReceiver extends AbstractComponentMessageReceiver {

    /**
     * Create a new instance of a PathStyleChangeMessageReceiver to receive PathStyleChangeMessages.
     *
     * @param cellMO The PathCellMO server object for which to receive the messages.
     */
    public PathStyleChangeMessageReceiver(PathCellMO cellMO) {
        super(cellMO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        CellMO cell = getCell();
        if (cell instanceof PathCellMO && message instanceof PathStyleChangeMessage) {
            ((PathCellMO) cell).setPathStyle(((PathStyleChangeMessage) message).getPathStyle());
            cell.sendCellMessage(clientID, message);
        }
    }

}
