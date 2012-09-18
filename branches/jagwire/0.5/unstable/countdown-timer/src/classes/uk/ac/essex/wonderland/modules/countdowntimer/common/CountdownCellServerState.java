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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * 
 * @author Bernard Horan
 */
@XmlType(namespace = "countdown-timer")
@XmlRootElement(name = "countdown-cell")
@ServerState
public class CountdownCellServerState extends CellServerState {

    public CountdownCellServerState() {
        super();
    }

    @Override
    public String getServerClassName() {
        return "uk.ac.essex.wonderland.modules.countdowntimer.server.CountdownCellMO";
    }
}
