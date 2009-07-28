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
package org.jdesktop.wonderland.modules.connectionsample.server;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.connectionsample.common.ChangeColorMessage;
import org.jdesktop.wonderland.modules.connectionsample.common.ColorChangeConnectionType;
import org.jdesktop.wonderland.modules.connectionsample.common.ColoredCubeConstants;
import org.jdesktop.wonderland.modules.connectionsample.common.CreateCellRequestMessage;
import org.jdesktop.wonderland.modules.connectionsample.common.CreateCellResponseMessage;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * A connection handler that implements the server-side of the
 * ColorChangeConnection.  This handler accepts requests to create cells, as
 * well as requests to change the color of an existing cell.
 * <p>
 * As described in the general ClientConnectionHandler javadoc, because this
 * handler is Serializable, a separate copy of the handler is created for
 * each client that uses the connection type.  Therefore we can store
 * per-client state, which in this cases is the list of all cells created
 * by the client.  When the client disconnects, only the cells created by
 * that client will be removed.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ColorChangeConnectionHandler 
        implements ClientConnectionHandler, Serializable
{
    /** A logger for output */
    private static final Logger logger =
            Logger.getLogger(ColorChangeConnectionHandler.class.getName());

    /**
     * List of cells created by this client, so they can be automatically
     * cleaned up.
     */
    private final List<CellID> cells = new LinkedList<CellID>();

    /**
     * Return the connection type used by this connection (in this case, the
     * ColorChangeConnectionType)
     * @return ColorChangeConnectionType.TYPE
     */
    public ConnectionType getConnectionType() {
        return ColorChangeConnectionType.TYPE;
    }

    /**
     * @{inheritDoc}
     */
    public void registered(WonderlandClientSender sender) {
        // do nothing
    }

    /**
     * @{inheritDoc}
     */
    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties)
    {
        // do nothing
    }

    /**
     * Handle requests from the client of this connection.  Requests will
     * be differentiated by message type.
     */
    public void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Message message)
    {
        if (message instanceof CreateCellRequestMessage) {
            // handle a cell creation request
            ResponseMessage response = handleCreateCell(
                    (CreateCellRequestMessage) message, clientID);
            sender.send(clientID, response);
        } else if (message instanceof ChangeColorMessage) {
            // handle a color change request
            handleChangeColor((ChangeColorMessage) message);
        } else {
            // unexpected request -- return an error
            Message error = new ErrorMessage(message.getMessageID(),
                    "Unexpected message type: " + message.getClass());
            sender.send(clientID, error);
        }
    }

    /**
     * Handle a request to create a cell.
     * @param request the request message
     * @param creator the client who sent the request
     * @return a response to the request, either a CreateCellResponseMessage
     * on success, or an ErrorMessage if there is an error
     */
    protected ResponseMessage handleCreateCell(CreateCellRequestMessage request,
                                               WonderlandClientID creator)
    {
        // create the cell
        CellMO cellMO = new ColoredCubeCellMO();

        try {
            // add the cell to the world
            WonderlandContext.getCellManager().insertCellInWorld(cellMO);
        } catch (MultipleParentException ex) {
            logger.log(Level.SEVERE, null, ex);
            ErrorMessage error = new ErrorMessage(request.getMessageID(),
                                                  "Cell creation error");
            return error;
        }

        // store the association between the cell and this client
        cells.add(cellMO.getCellID());

        // return the result
        return new CreateCellResponseMessage(request.getMessageID(),
                                             cellMO.getCellID());
    }

    /**
     * Handle a color change request.
     * @param message the request message.
     */
    public void handleChangeColor(ChangeColorMessage message) {
        // find the cell with the given ID
        CellMO cellMO = CellManagerMO.getCell(message.getCellID());

        // make sure the cell exists
        if (cellMO == null) {
            return;
        }

        // get the shared state component of this cell
        SharedStateComponentMO ssc = cellMO.getComponent(SharedStateComponentMO.class);
        SharedMapSrv map = ssc.get(ColoredCubeConstants.MAP_NAME);

        // set the value of color
        map.put(ColoredCubeConstants.COLOR_KEY,
                SharedInteger.valueOf(message.getColor()));
    }

    /**
     * Notification that a client has disconnected.
     * @param sender a sender that can be used to send messages to
     * other clients with the given connection type.
     * @param clientID the id of the client that disconnected.
     */
    public void clientDisconnected(WonderlandClientSender sender,
                                   WonderlandClientID clientID)
    {
        // remove all cells associated with this client
        for (CellID cellID : cells) {
            CellMO cellMO = CellManagerMO.getCell(cellID);
            if (cellMO != null) {
                WonderlandContext.getCellManager().removeCellFromWorld(cellMO);
            }
        }

        // remove all cells from our list
        cells.clear();
    }
}
