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
package uk.ac.essex.wonderland.modules.postercontrol.web;

import uk.ac.essex.wonderland.modules.postercontrol.common.PosterCollectionRequestMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.ChangeContentsMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterCollectionResponseMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterControlConnectionType;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterContentsRequestMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterContentsResponseMessage;
import uk.ac.essex.wonderland.modules.postercontrol.common.PosterRemoveRequestMessage;

/**
 * A custom connection for sending poster control messages.
 * @author Bernard Horan
 */
public class PosterControlConnection extends BaseConnection {
    public ConnectionType getConnectionType() {
        return PosterControlConnectionType.TYPE;
    }

    @Override
    public void handleMessage(Message message) {
        // no messages to handle.  If the server sent any messages
        // we would handle them here (other than responses to our requests,
        // which are handled automatically).
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get the records of the poster cells that are in the server
     * @return
     * @throws InterruptedException
     */
    public Collection<PosterRecord> getPosterRecords() throws InterruptedException {
        //Get the IDs of the cells that are posters
        ResponseMessage response;
        Collection<PosterRecord> posterRecords = new HashSet<PosterRecord>();
        response = sendAndWait(new PosterCollectionRequestMessage());
        if (!(response instanceof PosterCollectionResponseMessage)) {
            //failed--throw an exception
            throw new RuntimeException("Unexpected message type: " + response);
        }
        //Success, get the cell contents for each cell id
        Set<Integer> cellIDs = ((PosterCollectionResponseMessage) response).getPosterIDs();
        for (int cellID : cellIDs) {
            response = sendAndWait(new PosterContentsRequestMessage(cellID));
            // the response should be either a PosterContentsResponseMessage on
            // success, or an ErrorMessage on failure.  Handled these two cases
            // by returning the cell ID or throwing an exception.
            if (response instanceof PosterContentsResponseMessage) {
                // success.  Return the cell ID.
                PosterContentsResponseMessage pcrm = (PosterContentsResponseMessage) response;
                PosterRecord record = new PosterRecord(cellID, pcrm.getCellName(), pcrm.getPosterContents());
                record.addAction(new PosterAction("delete", "delete&cellID=" + cellID));
                record.addAction(new PosterAction("edit", "edit&cellID=" + cellID));
                posterRecords.add(record);
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
        return posterRecords;
    }


    /**
     * Set the contents of a poster cell
     * @param cellID
     * @param string
     */
    public void setPosterContents(int cellID, String string) {
        send(new ChangeContentsMessage(cellID, string));
    }

    /**
     * Remove a specified poster cell
     * @param cellID
     */
    public void removePosterCell(int cellID) {
        send(new PosterRemoveRequestMessage(cellID));
    }

    

    
}
