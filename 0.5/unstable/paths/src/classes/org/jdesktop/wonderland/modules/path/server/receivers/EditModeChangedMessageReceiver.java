package org.jdesktop.wonderland.modules.path.server.receivers;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
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
public class EditModeChangedMessageReceiver extends AbstractComponentMessageReceiver {

    /**
     * Create a new instance of a EditModeChangedMessageReceiver to receive
     *
     * @param cellMO
     */
    public EditModeChangedMessageReceiver(PathCellMO cellMO) {
        super(cellMO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        CellMO cell = getCell();
        if (cell instanceof PathCellMO && message instanceof EditModeChangeMessage) {
            ((PathCellMO) cell).setEditMode(((EditModeChangeMessage) message).isEditMode());
            cell.sendCellMessage(clientID, message);
        }
    }

}
