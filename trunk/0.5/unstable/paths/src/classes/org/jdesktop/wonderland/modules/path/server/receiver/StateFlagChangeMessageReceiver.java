package org.jdesktop.wonderland.modules.path.server.receiver;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;
import org.jdesktop.wonderland.modules.path.server.PathCellMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * This class is intended to receive messages about boolean flag state changes
 * to the NodePath for the server.
 *
 * @author Carl Jokl
 */
public class StateFlagChangeMessageReceiver extends AbstractComponentMessageReceiver {

    /**
     * Create a new instance of a StateFlagChangeMessageReceiver to receive messages
     * when boolean NodePath state values are changed.
     *
     * @param cellMO The PathCellMO server object for which to receive messages.
     */
    public StateFlagChangeMessageReceiver(PathCellMO cellMO) {
        super(cellMO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        CellMO cell = getCell();
        if (cell instanceof PathCellMO) {
            PathCellMO pathCell = (PathCellMO) cell;
            if (message instanceof EditModeChangeMessage) {
                pathCell.setEditMode(((EditModeChangeMessage) message).isEditMode());
                cell.sendCellMessage(clientID, message);
            }
            else if (message instanceof PathClosedChangeMessage) {
                pathCell.setClosedPath(((PathClosedChangeMessage) message).isPathClosed());
                cell.sendCellMessage(clientID, message);
            }
        }
    }

}
