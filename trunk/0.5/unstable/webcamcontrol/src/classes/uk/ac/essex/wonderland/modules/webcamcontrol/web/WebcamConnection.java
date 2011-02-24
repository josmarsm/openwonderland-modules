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
package uk.ac.essex.wonderland.modules.webcamcontrol.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.ChangeSettingsMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamAction;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamCollectionRequestMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamCollectionResponseMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamConnectionType;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamRecord;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamSettingsRequestMessage;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamSettingsResponseMessage;

/**
 * A custom connection for sending Wecam viewer control messages.
 * @author Bernard Horan
 */
public class WebcamConnection extends BaseConnection {
    private static final Logger logger = Logger.getLogger(WebcamConnection.class.getName());

    public ConnectionType getConnectionType() {
        return WebcamConnectionType.TYPE;
    }

    @Override
    public void handleMessage(Message message) {
        // no messages to handle.  If the server sent any messages
        // we would handle them here (other than responses to our requests,
        // which are handled automatically).
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get the records of the tight webcam viewer cells that are in the server
     * @return
     * @throws InterruptedException
     */
    public Collection<WebcamRecord> getWebcamRecords() throws InterruptedException {
        //logger.warning("getting records");
        //Get the IDs of the cells that are webcam viewer cells
        ResponseMessage response;
        Collection<WebcamRecord> webcamRecords = new HashSet<WebcamRecord>();
        response = sendAndWait(new WebcamCollectionRequestMessage());
        if (!(response instanceof WebcamCollectionResponseMessage)) {
            //failed--throw an exception
            throw new RuntimeException("Unexpected message type: " + response);
        }
        //Success, get the cell contents for each cell id
        Set<Integer> cellIDs = ((WebcamCollectionResponseMessage) response).getCellIDs();
        //logger.warning("got records: " + cellIDs);

        for (int cellID : cellIDs) {
            //logger.warning("getting record for cellid: " + cellID);
            response = sendAndWait(new WebcamSettingsRequestMessage(cellID));
            // the response should be either a WebcamSettingsResponseMessage on
            // success, or an ErrorMessage on failure.  Handled these two cases
            // by returning the cell ID or throwing an exception.
            if (response instanceof WebcamSettingsResponseMessage) {
                // success.  Return the cell ID.
                WebcamSettingsResponseMessage pcrm = (WebcamSettingsResponseMessage) response;
                WebcamRecord record = pcrm.getRecord();
                record.addAction(new WebcamAction("delete", "delete&cellID=" + cellID));
                record.addAction(new WebcamAction("edit", "edit&cellID=" + cellID));
                webcamRecords.add(record);
            } else if (response instanceof ErrorMessage) {
                // error.  Throw an exception.
                ErrorMessage em = (ErrorMessage) response;
                throw new RuntimeException("Error getting contents: " + em.getErrorMessage(),
                        em.getErrorCause());
            } else {
            // unexpected response.  Throw an exception.
            throw new RuntimeException("Unexpected message type: " + response);
        }
        }
        return webcamRecords;
    }


    /**
     * Change the settings of a webcam viewer cell
     * @param cellID
     * @param string
     */
    public void changeSettings(int cellID, WebcamRecord record) {
        send(new ChangeSettingsMessage(cellID, record));
    }
    
}
