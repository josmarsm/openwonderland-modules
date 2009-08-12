/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.marbleous.common.cell;

import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;

/**
 * Container for Marbleous track client cell client state data.
 *
 * @author paulby
 */
public class TrackCellClientState extends CellClientState {

    private SimulationState simulationState;
    private Track track;

    /**
     * Get the start/stop state of the simulation
     * @return Start/stop state of the simulation
     */
    public SimulationState getSimulationState() {
        return simulationState;
    }

    /**
     * Set the start/stop state of the simulation
     * @param simulationState State of the simulation
     */
    public void setSimluationState(SimulationState simulationState) {
        this.simulationState = simulationState;
    }

    public void setTrack(Track aTrack) {
        track = aTrack;
    }

    public Track getTrack() {
        return track;
    }
}
