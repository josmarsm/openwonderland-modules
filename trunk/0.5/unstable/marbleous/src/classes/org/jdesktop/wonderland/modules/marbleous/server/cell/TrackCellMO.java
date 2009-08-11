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
package org.jdesktop.wonderland.modules.marbleous.server.cell;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.TrackCellClientState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * @author paulby
 */
public class TrackCellMO extends CellMO {

    private SimulationState simulationState = SimulationState.STOPPED;

    /** Default constructor, used when the cell is created via WFS */
    public TrackCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TrackCellClientState();
        }
        ((TrackCellClientState) cellClientState).setSimluationState(getSimulationState());
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
    }

    /**
     * Get the start/stopped state of the simulation
     * @return State of the simulation
     */
    public SimulationState getSimulationState() {
        return simulationState;
    }

    /**
     * Set the start/stopped state of the simulation
     * @param simulationState New state of the simulation
     */
    public void setSimulationState(SimulationState simulationState) {
        this.simulationState = simulationState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            ChannelComponentMO.ComponentMessageReceiver receiver =
                    (ChannelComponentMO.ComponentMessageReceiver) new TrackCellMessageReceiver(this);
            channel.addMessageReceiver(SimulationStateMessage.class, receiver);
        } else {
            channel.removeMessageReceiver(SimulationStateMessage.class);
        }
    }

    /**
     * Receives and passes on start/stop messages for the cell
     */
    private static class TrackCellMessageReceiver extends AbstractComponentMessageReceiver {

        /**
         * Standard constructor.
         * @param cellMO The associated TrackCellMO
         */
        public TrackCellMessageReceiver(TrackCellMO cellMO) {
            super(cellMO);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            TrackCellMO tcmo = (TrackCellMO) this.getCell();

            if (message instanceof SimulationStateMessage) {
                tcmo.setSimulationState(((SimulationStateMessage) message).getSimulationState());
                tcmo.sendCellMessage(clientID, message);
            }
        }
    }
}
