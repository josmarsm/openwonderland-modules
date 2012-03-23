/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
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
package uk.ac.essex.wonderland.modules.postercontrol.server;

import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.poster.server.PosterCellMO;
import uk.ac.essex.wonderland.modules.postercontrol.common.ChangeContentsMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterCollectionRequestMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterCollectionResponseMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterControlConnectionType;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterContentsRequestMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterContentsResponseMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterRemoveRequestMessage;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * A connection handler that implements the server-side of the
 * PosterControlConnection.  This handler accepts requests to
 * disover the ids of posters, get the contents of a poster, change the
 * contents of a poster and delete a poster.
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
public class PosterControlConnectionHandler
        implements ClientConnectionHandler, Serializable
{

    /** A logger for output */
    private static final Logger logger =
            Logger.getLogger(PosterControlConnectionHandler.class.getName());

    /**
     * Return the connection type used by this connection (in this case, the
     * PosterControlConnectionType)
     * @return PosterControlConnectionType.TYPE
     */
    public ConnectionType getConnectionType() {
        return PosterControlConnectionType.TYPE;
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
        if (message instanceof PosterContentsRequestMessage) {
            // handle a get contents request
            ResponseMessage response = handleGetContents(
                    (PosterContentsRequestMessage) message, clientID);
            sender.send(clientID, response);
        } else if (message instanceof PosterCollectionRequestMessage) {
            // handle a collection request
            ResponseMessage response = handleGetCollection((PosterCollectionRequestMessage) message);
            sender.send(clientID, response);
        } else if (message instanceof PosterRemoveRequestMessage) {
            //handle a request to remove a poster cell
            handleRemovePoster((PosterRemoveRequestMessage) message);
        } else if (message instanceof ChangeContentsMessage) {
            handleChangeContents((ChangeContentsMessage) message);
        }

        else {
            // unexpected request -- return an error
            Message error = new ErrorMessage(message.getMessageID(),
                    "Unexpected message type: " + message.getClass());
            sender.send(clientID, error);
        }
    }

    /**
     * Handle a request to get the contents of a poster.
     * @param request the request message
     * @param creator the client who sent the request
     * @return a response to the request, either a PosterContentsResponseMessage
     * on success, or an ErrorMessage if there is an error
     */
    protected ResponseMessage handleGetContents(PosterContentsRequestMessage request,
                                               WonderlandClientID creator)
    {

        int cellID = request.getCellID();
        
        //get the cell
        CellMO cellMO = CellManagerMO.getCell(new CellID(cellID));
        if (cellMO instanceof PosterCellMO) {
            PosterCellMO posterCell = (PosterCellMO) cellMO;
            // return the result
            return new PosterContentsResponseMessage(request.getMessageID(), cellMO.getName(), posterCell.getPosterText());
        } else {
            logger.log(Level.SEVERE, "Can''t find poster for cellID: {0} found: {1}", new Object[]{cellID, cellMO});
            ErrorMessage error = new ErrorMessage(request.getMessageID(),
                                                  "Poster contents retrieval error");
            return error;
        }

    }

    private ResponseMessage handleGetCollection(PosterCollectionRequestMessage request) {
       Set<Integer> posterIDs = new HashSet<Integer>();
       Set<CellID> rootCells = CellManagerMO.getCellManager().getRootCells();
        for (CellID cellID1 : rootCells) {
            CellMO rootCell = CellManagerMO.getCell(cellID1);
            if (rootCell instanceof PosterCellMO) {
                posterIDs.add(cellID1.hashCode());
            }
            Collection<ManagedReference<CellMO>> children = rootCell.getAllChildrenRefs();
            int i = 0;
            for (ManagedReference<CellMO> managedReference : children) {
                CellMO child = managedReference.get();
                if (child instanceof PosterCellMO) {
                    posterIDs.add(child.getCellID().hashCode());
                }
            }
        }
        return new PosterCollectionResponseMessage(request.getMessageID(), posterIDs);
    }

    /**
     * Handle a contents change request.
     * @param message the request message.
     */
    public void handleChangeContents(ChangeContentsMessage message) {
        // find the cell with the given ID
        CellMO cellMO = CellManagerMO.getCell(new CellID(message.getCellID()));

        // make sure the cell exists
        if (cellMO == null) {
            return;
        }

        PosterCellMO posterCell = (PosterCellMO) cellMO;
        posterCell.setPosterText(message.getContents());
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
        //Do nothing
    }

    private void handleRemovePoster(PosterRemoveRequestMessage posterRemoveRequestMessage) {
        // find the cell with the given ID
        CellMO cellMO = CellManagerMO.getCell(new CellID(posterRemoveRequestMessage.getCellID()));

        // make sure the cell exists
        if (cellMO == null) {
            return;
        }

        if (!(cellMO instanceof PosterCellMO)) {
            logger.log(Level.SEVERE, "Trying to remove wrong cell: {0}", cellMO);
        }
        CellManagerMO.getCellManager().removeCellFromWorld(cellMO);
    }
}
