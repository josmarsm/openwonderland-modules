/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.marbleous.common.BigDropTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.BumpTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.LoopTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.StraightLevelTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;
import org.jdesktop.wonderland.modules.marbleous.common.cell.TrackCellClientState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.TrackCellServerState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SelectedSampleMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimTraceMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.TrackCellMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * @author paulby, Bernard Horan
 */
public class TrackCellMO extends CellMO {
    private final static Logger trackLogger = Logger.getLogger(TrackCellMO.class.getName());

    @UsesCellComponentMO(AudioPlaybackComponentMO.class)
    private ManagedReference<AudioPlaybackComponentMO> audioPlaybackCompRef;
    private SimulationState simulationState = SimulationState.STOPPED;
    private TrackCellServerState serverState;

    /** Default constructor, used when the cell is created via WFS */
    public TrackCellMO() {
        super();
        serverState = new TrackCellServerState();
        Track track = new Track();
        track.addTrackSegment(new BigDropTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
        track.addTrackSegment(new LoopTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
        track.addTrackSegment(new BumpTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
        track.buildTrack();
        serverState.setTrack(track);
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
        ((TrackCellClientState) cellClientState).setTrack(serverState.getTrack());
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
    }

    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        if (cellServerState == null) {
            cellServerState = serverState;
        }
        return super.getServerState(cellServerState);
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
                    (ChannelComponentMO.ComponentMessageReceiver) new SimulationStateMessageReceiver(this);
            channel.addMessageReceiver(SimulationStateMessage.class, receiver);
            receiver =
                    (ChannelComponentMO.ComponentMessageReceiver) new TrackCellMessageReceiver(this);
            channel.addMessageReceiver(TrackCellMessage.class, receiver);
            channel.addMessageReceiver(SimTraceMessage.class, new SimTraceMessageReceiver(this));
            channel.addMessageReceiver(SelectedSampleMessage.class, new SelectedSampleMessageReceiver(this));

        } else {
            channel.removeMessageReceiver(SimulationStateMessage.class);
            channel.removeMessageReceiver(SimTraceMessage.class);
            channel.removeMessageReceiver(SelectedSampleMessage.class);
        }
    }

    private void processAddSegmentMessage(WonderlandClientID clientID, TrackCellMessage tcm) {
        TrackSegment segment = tcm.getTrackSegment();
        //trackLogger.info("adding " + segment);
        serverState.getTrack().addTrackSegment(segment);
        sendCellMessage(clientID, tcm);
    }

    private void processModifySegmentMessage(WonderlandClientID clientID, TrackCellMessage tcm) {
        TrackSegment segment = tcm.getTrackSegment();
        trackLogger.info("modifying " + segment);
        serverState.getTrack().replaceTrackSegment(segment);
        sendCellMessage(clientID, tcm);
    }

    private void processRemoveSegmentMessage(WonderlandClientID clientID, TrackCellMessage tcm) {
        TrackSegment segment = tcm.getTrackSegment();
        //trackLogger.info("removing " + segment);
        serverState.getTrack().removeTrackSegment(segment);
        sendCellMessage(clientID, tcm);
    }

    /**
     * Receives selected time messages and passed it onto the clients
     */
    private static class SelectedSampleMessageReceiver extends AbstractComponentMessageReceiver {
        public SelectedSampleMessageReceiver(TrackCellMO cellMO) {
            super(cellMO);
        }

        /**
         * {@inheritDoc}
         */
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            getCell().sendCellMessage(clientID, message);
        }
    }

    /**
     * Receives sim traces messages and passed it onto the clients
     */
    private static class SimTraceMessageReceiver extends AbstractComponentMessageReceiver {
        public SimTraceMessageReceiver(TrackCellMO cellMO) {
            super(cellMO);
        }

        /**
         * {@inheritDoc}
         */
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            getCell().sendCellMessage(clientID, message);
        }
    }

    /**
     * Receives and passes on start/stop messages for the cell
     */
    private static class SimulationStateMessageReceiver extends AbstractComponentMessageReceiver {

        /**
         * Standard constructor.
         * @param cellMO The associated TrackCellMO
         */
        public SimulationStateMessageReceiver(TrackCellMO cellMO) {
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

    /**
     * Receives and acts on addSegment, deleteSegment, etc. messages
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

            if (message instanceof TrackCellMessage) {
                TrackCellMessage tcm = (TrackCellMessage) message;
                switch (tcm.getAction()) {
                    case ADD_SEGMENT:
                        tcmo.processAddSegmentMessage(clientID, tcm);
                        break;
                    case REMOVE_SEGMENT:
                        tcmo.processRemoveSegmentMessage(clientID, tcm);
                        break;
                    case MODIFY_SEGMENT:
                        tcmo.processModifySegmentMessage(clientID, tcm);
                        break;
                }
            }
        }
    }
}
