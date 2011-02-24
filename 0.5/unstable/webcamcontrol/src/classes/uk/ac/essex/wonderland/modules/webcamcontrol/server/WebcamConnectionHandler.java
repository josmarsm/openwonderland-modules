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
package uk.ac.essex.wonderland.modules.webcamcontrol.server;

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
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerConstants;
import org.jdesktop.wonderland.modules.webcamviewer.server.cell.WebcamViewerCellMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.ChangeSettingsMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamCollectionRequestMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamCollectionResponseMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamConnectionType;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamRecord;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamSettingsRequestMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamSettingsResponseMessage;

/**
 * A connection handler that implements the server-side of the
 * WebcamControlConnection.  This handler accepts requests to
 * disover the ids of Webcam viewer cells, get the settings of a cell, and change the
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
public class WebcamConnectionHandler
        implements ClientConnectionHandler, Serializable
{
    /** A logger for output */
    private static final Logger logger =
            Logger.getLogger(WebcamConnectionHandler.class.getName());

    /**
     * Return the connection type used by this connection (in this case, the
     * WebcamConnectionType)
     * @return WebcamConnectionType.TYPE
     */
    public ConnectionType getConnectionType() {
        return WebcamConnectionType.TYPE;
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
        if (message instanceof WebcamSettingsRequestMessage) {
            // handle a request for the settings of a webcam
            ResponseMessage response = handleGetContents(
                    (WebcamSettingsRequestMessage) message, clientID);
            sender.send(clientID, response);
        } else if (message instanceof WebcamCollectionRequestMessage) {
            // handle a request for a collection of ids of webcam viewer cells
            ResponseMessage response = handleGetCollection((WebcamCollectionRequestMessage) message);
            sender.send(clientID, response);
        } else if (message instanceof ChangeSettingsMessage) {
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
     * Handle a request to get the settings of a Webcam cell.
     * @param request the request message
     * @param creator the client who sent the request
     * @return a response to the request, either a WebcamSettingsResponseMessage
     * on success, or an ErrorMessage if there is an error
     */
    protected ResponseMessage handleGetContents(WebcamSettingsRequestMessage request,
                                               WonderlandClientID creator)
    {

        int cellID = request.getCellID();
        //logger.log(Level.WARNING, "CellID: {0}", cellID);
        
        //get the cell
        CellMO cellMO = CellManagerMO.getCell(new CellID(cellID));
        if (cellMO instanceof WebcamViewerCellMO) {
            SharedStateComponentMO sharedStateComp = cellMO.getComponent(SharedStateComponentMO.class);
            SharedMapSrv sharedMap = sharedStateComp.get(WebcamViewerConstants.STATUS_MAP);
            String cameraURI = sharedMap.get(WebcamViewerConstants.CAMERA_URI, SharedString.class).getValue();
            String cameraUsername = sharedMap.get(WebcamViewerConstants.CAMERA_USERNAME, SharedString.class).getValue();
            String cameraPassword = sharedMap.get(WebcamViewerConstants.CAMERA_PASSWORD, SharedString.class).getValue();
            String cameraStateStr = sharedMap.get(WebcamViewerConstants.CAMERA_STATE, SharedString.class).getValue();
            WebcamRecord record = new WebcamRecord(cellID, cellMO.getName(), cameraURI, cameraUsername, cameraPassword, cameraStateStr);
            // return the result
            return new WebcamSettingsResponseMessage(request.getMessageID(), record);
        } else {
            logger.log(Level.SEVERE, "Can''t find Webcam cell for cellID: {0} found: {1}", new Object[]{cellID, cellMO});
            ErrorMessage error = new ErrorMessage(request.getMessageID(),
                                                  "Webcam settings retrieval error");
            return error;
        }

    }

    private ResponseMessage handleGetCollection(WebcamCollectionRequestMessage request) {
       Set<Integer> cellIDs = new HashSet<Integer>();
       Set<CellID> rootCells = CellManagerMO.getCellManager().getRootCells();
        for (CellID cellID1 : rootCells) {
            CellMO rootCell = CellManagerMO.getCell(cellID1);
            //logger.log(Level.WARNING, "CellID: {0} -> {1}", new Object[]{cellID1, rootCell});
            if (rootCell instanceof WebcamViewerCellMO) {
                cellIDs.add(cellID1.hashCode());
            }
            Collection<ManagedReference<CellMO>> children = rootCell.getAllChildrenRefs();
            int i = 0;
            for (ManagedReference<CellMO> managedReference : children) {
                CellMO child = managedReference.get();
                //logger.log(Level.WARNING, "Child: {0} -> {1}", new Object[]{i++, child});
                if (child instanceof WebcamViewerCellMO) {
                    cellIDs.add(child.getCellID().hashCode());
                }
            }
        }
        return new WebcamCollectionResponseMessage(request.getMessageID(), cellIDs);
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
        WebcamRecord record = message.getRecord();

        // get the shared state component of this cell
        SharedStateComponentMO ssc = cellMO.getComponent(SharedStateComponentMO.class);
        SharedMapSrv map = ssc.get(WebcamViewerConstants.STATUS_MAP);

        // set the values
        map.put(WebcamViewerConstants.CAMERA_URI, SharedString.valueOf(record.getCameraURI()));
        map.put(WebcamViewerConstants.CAMERA_USERNAME, SharedString.valueOf(record.getCameraUsername()));
        map.put(WebcamViewerConstants.CAMERA_PASSWORD, SharedString.valueOf(record.getCameraPassword()));
        map.put(WebcamViewerConstants.CAMERA_STATE, SharedString.valueOf(record.getCameraState()));
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
