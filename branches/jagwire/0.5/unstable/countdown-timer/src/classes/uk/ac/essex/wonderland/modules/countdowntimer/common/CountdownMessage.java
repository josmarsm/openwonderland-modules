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

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message sent <em>from server to client</em> to inform client to update a countdown cell.
 * @author Bernard Horan
 */
public class CountdownMessage extends CellMessage {
    private final Time time;

    public CountdownMessage(CellID cellID, Time time) {
        super(cellID);
        this.time = time;
    }

    public Time getTime() {
        return time;
    }
    
}
