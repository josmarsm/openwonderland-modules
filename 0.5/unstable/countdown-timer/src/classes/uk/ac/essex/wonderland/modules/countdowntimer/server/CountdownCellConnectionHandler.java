/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2012, University of Essex, UK, 2012, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.countdowntimer.server;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownCellConnectionType;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownFailureException;
import uk.ac.essex.wonderland.modules.countdowntimer.common.StartTimerRequestMessage;
import uk.ac.essex.wonderland.modules.countdowntimer.common.StartTimerResponseMessage;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * A connection handler that implements the server-side of the
 * CountdownCellConnection.  This handler accepts requests to set the time of a cell,
 * as well a events from the cell.
 * <p>
 * As described in the general ClientConnectionHandler javadoc, because this
 * handler is Serializable, a separate copy of the handler is created for
 * each client that uses the connection type.  Therefore we can store
 * per-client state, which in this cases is the list of all cells created
 * by the client.  When the client disconnects, only the cells created by
 * that client will be removed.
 *
 * @author Bernard Horan
 */
public class CountdownCellConnectionHandler
        implements ClientConnectionHandler, Serializable, ManagedObject
{
    /** A logger for output */
    private static final Logger logger =
            Logger.getLogger(CountdownCellConnectionHandler.class.getName());

    private WonderlandClientSender sender;
    private WonderlandClientID clientID;

    /**
     * Return the connection type used by this connection (in this case, the
     * CountdownCellConnectionType)
     * @return CountdownCellConnectionType.TYPE
     */
    public ConnectionType getConnectionType() {
        return CountdownCellConnectionType.TYPE;
    }

    /**
     * @{inheritDoc}
     */
    public void registered(WonderlandClientSender sender) {
        this.sender = sender;
    }

    /**
     * @{inheritDoc}
     */
    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties)
    {
        this.clientID = clientID;
        if (this.sender != sender) {
            logger.warning("resetting sender");
            this.sender = sender;
        }
    }

    /**
     * Handle requests from the client of this connection.  Requests will
     * be differentiated by message type.
     */
    public void messageReceived(WonderlandClientSender sender,
            WonderlandClientID clientID,
            Message message) {
        if (message instanceof StartTimerRequestMessage) {
            //handle a set simulation message
            ResponseMessage response = handleStartTimer(
                    (StartTimerRequestMessage) message, clientID);
            sender.send(clientID, response);
        } else  {
            logger.severe("Unknown message: " + message);
        }
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
        logger.severe("Client disconnected: " + sender.toString());
        this.sender = null;
        this.clientID = null;
    }

    private ResponseMessage handleStartTimer(StartTimerRequestMessage message, WonderlandClientID clientID) {
        CellID cellID = new CellID(message.getCellID());
        CellMO countdownCell = CellManagerMO.getCell(cellID);
        CountdownFailureException ex = null;
        if (countdownCell instanceof CountdownCellMO) {
            try {
                CountdownCellMO countdownCellMO = (CountdownCellMO) countdownCell;
                countdownCellMO.startTimer(message.getMinutes(), message.getSeconds());
            } catch (CountdownFailureException pfe) {
                ex = pfe;
            }
        } else {
            ex = new CountdownFailureException("Invalid cellID: " + cellID);
        }
        ResponseMessage responseMessage;
        if (ex == null) {
            responseMessage = new StartTimerResponseMessage(message.getMessageID(), "Succeeded in setting cellId: " + cellID, null );
        } else {
            responseMessage = new StartTimerResponseMessage(message.getMessageID(), "Failed to set time of cellId: " + cellID, ex);
        }
        return responseMessage;
    }

    
}
