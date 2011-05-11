package org.jdesktop.wonderland.modules.path.client.receiver;

import java.math.BigInteger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This is a common abstract base class for all the client message receivers for the PathCell.
 *
 * @author Carl Jokl
 */
public abstract class PathCellComponentMessageReceiver implements ComponentMessageReceiver, Disposable {

    protected Cell cell;
    protected ClientNodePath path;

    /**
     * Initialize this PathCellComponentMessageReceiver to work for the specified
     * Cell and NodePath.
     *
     * @param cell The Cell for which this MessageReceiver is receiving messages.
     * @param path The ClientNodePath which is to be updated by the messages received by this PathCellComponentMessageReceiver.
     */
    protected PathCellComponentMessageReceiver(Cell cell, ClientNodePath path) {
        this.cell = cell;
        this.path = path;
    }

    /**
     * Check whether this PathCellComponentMessageReceiver can receive a message.
     * This will be based on making sure the required internal components are set
     * and that the source Cell session id of the message is not the same as the id of the
     * Cell session to which this message listener belongs.
     *
     * @param id The session id of the source Cell session from which the message originates.
     * @return True if the required components are set to handle a message and the
     *         message does not originate from the same Cell.
     */
    protected boolean handeMessage(BigInteger id) {
        return path != null && cell != null && !cell.getCellCache().getSession().getID().equals(id);
    }

    /**
     * Dispose of the internal state of this PathNodeChangeMessageReceiver in order to aid garbage collection.
     */
    @Override
    public void dispose() {
        cell = null;
        path = null;
    }
}
