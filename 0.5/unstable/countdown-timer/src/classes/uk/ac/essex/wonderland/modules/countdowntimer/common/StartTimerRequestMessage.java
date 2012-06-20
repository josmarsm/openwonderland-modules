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
package uk.ac.essex.wonderland.modules.countdowntimer.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * A message sent to the SimulationCellConnectionHandler to set the
 * simulation of a particular cell.  The state of this message includes the
 * id of the cell to update and the simulation to change it to.
 *
 * @author Bernard Horan
 */
public class StartTimerRequestMessage extends Message {
    private final int cellID;
    private final int minutes;
    private final int seconds;

    /**
     *
     * @param cellID
     * @param minutes
     * @param seconds  
     */
    public StartTimerRequestMessage(int cellID, int minutes, int seconds) {
        super();
        this.cellID = cellID;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     *
     * @return
     */
    public int getCellID() {
        return cellID;
    }

    public int getMinutes() {
        return minutes;
    }
    
    public int getSeconds() {
        return seconds;
    }
    
}
