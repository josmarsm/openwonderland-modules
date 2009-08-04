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
package org.jdesktop.wonderland.modules.cmu.server;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellServerState;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.PlaybackDefaults;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.GroundPlaneChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.MouseButtonEventMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.RestartProgramMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * CellMO for the CMU module.  Interfaces both with the client-side cell instance
 * and the standalone CMU program runner (the latter via the 
 * ProgramConnectionHandlerMO), passing messages between the two and
 * helping to start the program instance initially.
 * @author kevin
 */
public class CMUCellMO extends CellMO {

    private String cmuURI;
    private String hostName;
    private int port;
    private boolean playing;
    private float playbackSpeed;
    private final Serializable playbackSpeedLock = new String();
    private boolean groundPlaneShowing;
    private final Serializable groundPlaneLock = new String();
    private boolean socketInitialized = false;  // False until a CMU instance informs us with valid socket information.
    private final Serializable socketLock = new String();

    /**
     * Receives and processes messages about playback speed change.
     */
    private static class CMUCellMessageReceiver extends AbstractComponentMessageReceiver {

        /**
         * Standard constructor.
         * @param cellMO The associated CMUCellMO
         */
        public CMUCellMessageReceiver(CMUCellMO cellMO) {
            super(cellMO);
        }

        /**
         * Process messages from CMU client cells; currently playback
         * speed changes and mouse clicks are received.
         * @param sender {@inheritDoc}
         * @param clientID {@inheritDoc}
         * @param message {@inheritDoc}
         */
        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            CMUCellMO cellMO = (CMUCellMO) getCell();

            // Playback speed change
            if (PlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())) {
                PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                cellMO.setPlaybackInformation(change.isPlaying(), change.getPlaybackSpeed());
                cellMO.sendCellMessage(clientID, message);
            }

            // Ground plane visibility change
            if (GroundPlaneChangeMessage.class.isAssignableFrom(message.getClass())) {
                GroundPlaneChangeMessage change = (GroundPlaneChangeMessage) message;
                cellMO.setGroundPlaneShowing(change.isGroundPlaneShowing());
                cellMO.sendCellMessage(clientID, message);
            }

            // Restart program
            if (RestartProgramMessage.class.isAssignableFrom(message.getClass())) {
                System.out.println("Received restart message");
                cellMO.createProgram();
            }

            // Mouse button event
            if (MouseButtonEventMessage.class.isAssignableFrom(message.getClass())) {
                MouseButtonEventMessage mouseMessage = (MouseButtonEventMessage) message;
            //TODO: forward mouse clicks
            }
        }
    }

    /** Default constructor. */
    public CMUCellMO() {
        super();
    }

    /**
     * Get the URI of the loaded CMU file.
     * @return The URI of the loaded CMU file
     */
    public String getCmuURI() {
        return cmuURI;
    }

    /**
     * Sets the URI of the CMU file.
     * @param uri The URI of the CMU file
     */
    public void setCmuURI(String uri) {
        cmuURI = uri;
    }

    /**
     * We use two separate forms of playback speed control: a binary play/pause
     * control, and a many-valued playback speed control.  This method
     * gets the net result of boht methods.
     * If the scene is playing, get the stored playback speed; if it is paused,
     * get the default paused speed.
     * @return Actual playback speed
     */
    private float getActualPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return (isPlaying() ? this.playbackSpeed : PlaybackDefaults.PAUSE_SPEED);
        }
    }

    public void setPlaybackInformation(boolean playing, float playbackSpeed) {
        synchronized (playbackSpeedLock) {
            this.playbackSpeed = playbackSpeed;
            this.playing = playing;
            // Inform the associated program of the change
            ProgramConnectionHandlerMO.changePlaybackSpeed(getCellID(), getActualPlaybackSpeed());
        }
    }

    /**
     * Get current playback speed.
     * @return Current playback speed for the CMU instance.
     */
    public float getPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return this.playbackSpeed;
        }
    }

    public boolean isPlaying() {
        synchronized (playbackSpeedLock) {
            return playing;
        }
    }

    public boolean isGroundPlaneShowing() {
        synchronized (groundPlaneLock) {
            return groundPlaneShowing;
        }
    }

    public void setGroundPlaneShowing(boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.groundPlaneShowing = groundPlaneShowing;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.cmu.client.CMUCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState clientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (clientState == null) {
            clientState = new CMUCellClientState();
        }

        CMUCellClientState cmuClientState = ((CMUCellClientState) clientState);
        synchronized (playbackSpeedLock) {
            cmuClientState.setPlaying(isPlaying());
            cmuClientState.setPlaybackSpeed(getPlaybackSpeed());
        }
        cmuClientState.setGroundPlaneShowing(isGroundPlaneShowing());
        synchronized (socketLock) {
            if (this.socketInitialized) {
                cmuClientState.setServerAndPort(getHostname(), getPort());
            }
        }

        return super.getClientState(clientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);

        CMUCellServerState setup = (CMUCellServerState) serverState;
        setCmuURI(setup.getCmuURI());
        setGroundPlaneShowing(setup.isGroundPlaneShowing());

        createProgram();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState serverState) {
        if (serverState == null) {
            serverState = new CMUCellServerState();
        }
        CMUCellServerState cmuServerState = (CMUCellServerState) serverState;
        cmuServerState.setCmuURI(getCmuURI());
        cmuServerState.setGroundPlaneShowing(isGroundPlaneShowing());
        return super.getServerState(serverState);
    }

    protected void createProgram() {
        // Create CMU instance
        ProgramConnectionHandlerMO.createProgram(getCellID(), getCmuURI());

        this.setPlaybackInformation(PlaybackDefaults.DEFAULT_START_PLAYING, PlaybackDefaults.DEFAULT_START_SPEED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void setLive(boolean live) {
        super.setLive(live);

        final Class[] messagesToReceive = {
            PlaybackSpeedChangeMessage.class,
            GroundPlaneChangeMessage.class,
            MouseButtonEventMessage.class,
            RestartProgramMessage.class
        };

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            ChannelComponentMO.ComponentMessageReceiver receiver =
                    (ChannelComponentMO.ComponentMessageReceiver) new CMUCellMessageReceiver(this);
            for (Class c : messagesToReceive) {
                channel.addMessageReceiver(c, receiver);
            }
        } else {
            for (Class c : messagesToReceive) {
                channel.removeMessageReceiver(c);
            }
            ProgramConnectionHandlerMO.removeProgram(this.getCellID());
        }
    }

    public int getPort() {
        synchronized (socketLock) {
            return port;
        }
    }

    public String getHostname() {
        synchronized (socketLock) {
            return hostName;
        }
    }

    public void setHostnameAndPort(String hostname, int port) {
        synchronized (socketLock) {
            this.socketInitialized = true;
            this.hostName = hostname;
            this.port = port;
            sendConnectionInformation();
        }
    }

    private void sendConnectionInformation() {
        synchronized (socketLock) {
            synchronized (playbackSpeedLock) {
                this.sendCellMessage(null, new ConnectionChangeMessage(this.getCellID(), getHostname(), getPort()));
                this.sendCellMessage(null, new PlaybackSpeedChangeMessage(this.getCellID(),
                        this.getPlaybackSpeed(), this.isPlaying()));
            }
        }
    }
}
