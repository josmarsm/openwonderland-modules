/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.connectionsample.main;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.connectionsample.common.ChangeColorMessage;
import org.jdesktop.wonderland.modules.connectionsample.common.ColorChangeConnectionType;
import org.jdesktop.wonderland.modules.connectionsample.common.CreateCellRequestMessage;
import org.jdesktop.wonderland.modules.connectionsample.common.CreateCellResponseMessage;

/**
 * A custom connection for sending color change information.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ColorChangeConnection extends BaseConnection {
    public ConnectionType getConnectionType() {
        return ColorChangeConnectionType.TYPE;
    }

    /**
     * Request that the connection handler create a new cell.  Return the
     * cell ID of the created cell, or throw an exception if there is an
     * error creating the cell.
     *
     * @return the cell ID of the newly created cell
     */
    public CellID createCell() throws InterruptedException {
        // request cell creation, and block until we get a response
        ResponseMessage response = sendAndWait(new CreateCellRequestMessage());

        // the response should be either a CreateCellResponseMessage on
        // success, or an ErrorMessage on failure.  Handled these two cases
        // by returning the cell ID or throwing an exception.
        if (response instanceof CreateCellResponseMessage) {
            // success.  Return the cell ID.
            CreateCellResponseMessage ccrm = (CreateCellResponseMessage) response;
            return ccrm.getCellID();
        } else if (response instanceof ErrorMessage) {
            // error.  Throw an exception.
            ErrorMessage em = (ErrorMessage) response;
            throw new RuntimeException("Error creating cell: " + em.getErrorMessage(),
                                       em.getErrorCause());
        } else {
            // unexpected response.  Throw an exception.
            throw new RuntimeException("Unexpected message type: " + response);
        }
    }

    /**
     * Send a message to set the color of the given cell to the given color.
     * @param cellID the CellID to change the color of
     * @param color the new color to change it to
     */
    public void setColor(CellID cellID, int color) {
        send(new ChangeColorMessage(cellID, color));
    }

    @Override
    public void handleMessage(Message message) {
        // no messages to handle.  If the server sent any messages
        // we would handle them here (other than responses to our requests,
        // which are handled automatically).
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
