/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package uk.ac.essex.wonderland.modules.tightvnccontrol.server;

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
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerConstants;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerState;
import org.jdesktop.wonderland.modules.tightvncviewer.server.cell.TightVNCViewerCellMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.ChangeSettingsMessage;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.TightVNCCollectionRequestMessage;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.TightVNCSettingsRequestMessage;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.TightVNCSettingsResponseMessage;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.TightVNCCollectionResponseMessage;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.TightVNCConnectionType;
import uk.ac.essex.wonderland.modules.tightvnccontrol.common.TightVNCRecord;

/**
 * A connection handler that implements the server-side of the
 * TightVNCControlConnection.  This handler accepts requests to
 * disover the ids of TightVNC viewer cells, get the settings of a cell, and change the
 * settings of a cell.
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
public class TightVNCConnectionHandler
        implements ClientConnectionHandler, Serializable
{
    /** A logger for output */
    private static final Logger logger =
            Logger.getLogger(TightVNCConnectionHandler.class.getName());

    /**
     * Return the connection type used by this connection (in this case, the
     * TightVNCConnectionType)
     * @return TightVNCConnectionType.TYPE
     */
    public ConnectionType getConnectionType() {
        return TightVNCConnectionType.TYPE;
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
        if (message instanceof TightVNCSettingsRequestMessage) {
            // handle a request for the settings of a vnc viewer
            ResponseMessage response = handleGetSettings(
                    (TightVNCSettingsRequestMessage) message, clientID);
            sender.send(clientID, response);
        } else if (message instanceof TightVNCCollectionRequestMessage) {
            // handle a request for the ids of all the tight vnc cells
            ResponseMessage response = handleGetCollection((TightVNCCollectionRequestMessage) message);
            sender.send(clientID, response);
        } else if (message instanceof ChangeSettingsMessage) {
            // handle a request to change the settings of a tightvnc cell
            handleChangeSettings((ChangeSettingsMessage) message);
        }

        else {
            // unexpected request -- return an error
            Message error = new ErrorMessage(message.getMessageID(),
                    "Unexpected message type: " + message.getClass());
            sender.send(clientID, error);
        }
    }

    /**
     * Handle a request to get the settings of a TightVNC cell.
     * @param request the request message
     * @param creator the client who sent the request
     * @return a response to the request, either a TightVNCSettingsResponseMessage
     * on success, or an ErrorMessage if there is an error
     */
    protected ResponseMessage handleGetSettings(TightVNCSettingsRequestMessage request,
                                               WonderlandClientID creator)
    {

        int cellID = request.getCellID();
        //logger.log(Level.WARNING, "CellID: {0}", cellID);
        
        //get the cell
        CellMO cellMO = CellManagerMO.getCell(new CellID(cellID));
        if (cellMO instanceof TightVNCViewerCellMO) {
            SharedStateComponentMO sharedStateComp = cellMO.getComponent(SharedStateComponentMO.class);
            SharedMapSrv sharedMap = sharedStateComp.get(TightVNCViewerConstants.STATUS_MAP);
            String vncServer = sharedMap.get(TightVNCViewerConstants.VNC_SERVER, SharedString.class).getValue();
            int vncPort = sharedMap.get(TightVNCViewerConstants.VNC_PORT, SharedInteger.class).getValue();
            String vncUsername = sharedMap.get(TightVNCViewerConstants.VNC_USERNAME, SharedString.class).getValue();
            String vncPassword = sharedMap.get(TightVNCViewerConstants.VNC_PASSWORD, SharedString.class).getValue();
            TightVNCRecord record = new TightVNCRecord(cellID, cellMO.getName(), vncServer, vncPort, vncUsername, vncPassword);
            // return the result
            return new TightVNCSettingsResponseMessage(request.getMessageID(), record);
        } else {
            logger.log(Level.SEVERE, "Can''t find tight vnc cell for cellID: {0} found: {1}", new Object[]{cellID, cellMO});
            ErrorMessage error = new ErrorMessage(request.getMessageID(),
                                                  "tightVNC settings retrieval error");
            return error;
        }

    }

    private ResponseMessage handleGetCollection(TightVNCCollectionRequestMessage request) {
       Set<Integer> cellIDs = new HashSet<Integer>();
       Set<CellID> rootCells = CellManagerMO.getCellManager().getRootCells();
        for (CellID cellID1 : rootCells) {
            CellMO rootCell = CellManagerMO.getCell(cellID1);
            //logger.log(Level.WARNING, "CellID: {0} -> {1}", new Object[]{cellID1, rootCell});
            if (rootCell instanceof TightVNCViewerCellMO) {
                cellIDs.add(cellID1.hashCode());
            }
            Collection<ManagedReference<CellMO>> children = rootCell.getAllChildrenRefs();
            int i = 0;
            for (ManagedReference<CellMO> managedReference : children) {
                CellMO child = managedReference.get();
                //logger.log(Level.WARNING, "Child: {0} -> {1}", new Object[]{i++, child});
                if (child instanceof TightVNCViewerCellMO) {
                    cellIDs.add(child.getCellID().hashCode());
                }
            }
        }
        return new TightVNCCollectionResponseMessage(request.getMessageID(), cellIDs);
    }

    /**
     * Handle a contents change request.
     * @param message the request message.
     */
    public void handleChangeSettings(ChangeSettingsMessage message) {
        // find the cell with the given ID
        CellMO cellMO = CellManagerMO.getCell(new CellID(message.getCellID()));

        // make sure the cell exists
        if (cellMO == null) {
            return;
        }
        //Get the record containing the settings
        TightVNCRecord record = message.getRecord();

        // get the shared state component of this cell
        SharedStateComponentMO ssc = cellMO.getComponent(SharedStateComponentMO.class);
        SharedMapSrv map = ssc.get(TightVNCViewerConstants.STATUS_MAP);

        // set the values
        map.put(TightVNCViewerConstants.VNC_SERVER, SharedString.valueOf(record.getVncServer()));
        map.put(TightVNCViewerConstants.VNC_PORT,SharedInteger.valueOf(record.getVncPort()));
        map.put(TightVNCViewerConstants.VNC_USERNAME, SharedString.valueOf(record.getVncUsername()));
        map.put(TightVNCViewerConstants.VNC_PASSWORD, SharedString.valueOf(record.getVncPassword()));
        map.put(TightVNCViewerConstants.VNC_VIEWER_STATE, SharedString.valueOf(TightVNCViewerState.CONNECTED.name()));
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

    
}
