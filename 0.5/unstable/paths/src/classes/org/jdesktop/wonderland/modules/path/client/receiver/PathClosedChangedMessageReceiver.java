package org.jdesktop.wonderland.modules.path.client.receiver;

import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;

/**
 * This class is intended to receive PathCellEditModeChangeMessages for the server.
 *
 * @author Carl Jokl
 */
public class PathClosedChangedMessageReceiver implements ComponentMessageReceiver {

    private PathCell cell;

    /**
     * Create a new instance of a PathClosedMessageReceiver to receive
     * messages when the closed path state of the PathCell changes.
     *
     * @param cellMO The PathCellMO which will receive the message.
     */
    public PathClosedChangedMessageReceiver(PathCell cell) {
        this.cell = cell;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(CellMessage message) {
        if (cell != null && message instanceof PathClosedChangeMessage && !message.getSenderID().equals(cell.getCellCache().getSession().getID())) {
            cell.setClosedPath(((PathClosedChangeMessage) message).isPathClosed());
        }
    }

}
