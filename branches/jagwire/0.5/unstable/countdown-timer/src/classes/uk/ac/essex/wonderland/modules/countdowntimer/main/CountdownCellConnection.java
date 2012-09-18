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
package uk.ac.essex.wonderland.modules.countdowntimer.main;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import uk.ac.essex.wonderland.modules.countdowntimer.common.StartTimerRequestMessage;
import uk.ac.essex.wonderland.modules.countdowntimer.common.StartTimerResponseMessage;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownCellConnectionType;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownFailureException;

/**
 * A custom connection for setting a countdown cell.
 * @author Bernard Horan
 */
public class CountdownCellConnection extends BaseConnection {
    private static final Logger logger =
            Logger.getLogger(CountdownCellConnection.class.getName());
    public ConnectionType getConnectionType() {
        return CountdownCellConnectionType.TYPE;
    }

    /**
     * Set the simulation of a simulation cell
     * @param cellID
     * @param minutes 
     * @param seconds 
     * @throws CountdownFailureException
     */
    public void startTimer(int cellID, int minutes, int seconds) throws CountdownFailureException {
        try {
            StartTimerResponseMessage message = (StartTimerResponseMessage) sendAndWait(new StartTimerRequestMessage(cellID, minutes, seconds));
            CountdownFailureException ex = message.getException();
            if (ex != null) {
                throw message.getException();
            } else {
                logger.info(message.getText());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CountdownCellConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

    @Override
    public void handleMessage(Message message) {
        logger.log(Level.WARNING, "Message: {0}", message);
    }

    
}
