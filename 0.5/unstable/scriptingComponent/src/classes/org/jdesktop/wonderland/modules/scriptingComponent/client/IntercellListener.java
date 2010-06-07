package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Listener for notification of cell status changes
 * 
 * @author Morris Ford
 */
@ExperimentalAPI
public interface IntercellListener {

    /**
     * Notification that the cells status has changed.
     * @param cell the cells whoes status has changed
     * @param status the new status for the cell
     */
    public void intercellMessage(String payload);
}
