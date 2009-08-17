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
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.SceneTitleChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ServerClientMessageTypes;
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

    // CMU file URI
    private String cmuURI;
    private final Serializable uriLock = new String();

    // Connection information
    private String hostName;
    private int port;
    private boolean socketInitialized = false;  // False until a CMU instance informs us with valid socket information.
    private final Serializable socketLock = new String();

    // Scene title
    private String sceneTitle;
    private final Serializable sceneTitleLock = new String();

    // Playback information
    private boolean playing;
    private float playbackSpeed;
    private final Serializable playbackSpeedLock = new String();

    // Ground plane information
    private boolean groundPlaneShowing;
    private final Serializable groundPlaneLock = new String();

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
            if (message instanceof PlaybackSpeedChangeMessage) {
                cellMO.setPlaybackInformationFromMessage(clientID, (PlaybackSpeedChangeMessage) message);
            }

            // Ground plane visibility change
            if (message instanceof GroundPlaneChangeMessage) {
                cellMO.setGroundPlaneShowingFromMessage(clientID, (GroundPlaneChangeMessage) message);
            }

            // Scene title change
            if (message instanceof SceneTitleChangeMessage) {
                cellMO.setSceneTitleFromMessage(clientID, (SceneTitleChangeMessage) message);
            }

            // Restart program
            if (message instanceof RestartProgramMessage) {
                cellMO.createProgram();
            }

            // Mouse button event
            if (message instanceof MouseButtonEventMessage) {
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
        cmuClientState.setSceneTitle(this.getSceneTitle());

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
        setSceneTitle(setup.getSceneTitle());

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
        cmuServerState.setSceneTitle(getSceneTitle());
        return super.getServerState(serverState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            ChannelComponentMO.ComponentMessageReceiver receiver =
                    (ChannelComponentMO.ComponentMessageReceiver) new CMUCellMessageReceiver(this);
            for (Class c : ServerClientMessageTypes.MESSAGE_TYPES_TO_RECEIVE) {
                channel.addMessageReceiver(c, receiver);
            }
        } else {
            for (Class c : ServerClientMessageTypes.MESSAGE_TYPES_TO_RECEIVE) {
                channel.removeMessageReceiver(c);
            }
            ProgramConnectionHandlerMO.removeProgram(this.getCellID());
        }
    }

    public void createProgram() {
        setPlaybackInformation(PlaybackDefaults.DEFAULT_START_PLAYING, PlaybackDefaults.DEFAULT_START_SPEED);
        // Create CMU instance
        ProgramConnectionHandlerMO.createProgram(getCellID(), getCmuURI());
    }

    /**
     * Get the URI of the loaded CMU file.
     * @return The URI of the loaded CMU file
     */
    public String getCmuURI() {
        synchronized(uriLock) {
            return cmuURI;
        }
    }

    /**
     * Sets the URI of the CMU file.
     * @param uri The URI of the CMU file
     */
    public void setCmuURI(String uri) {
        synchronized(uriLock) {
            cmuURI = uri;
        }
    }

    /**
     * Get current playback speed.
     * @return Current playback speed for the CMU instance
     */
    public float getPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return this.playbackSpeed;
        }
    }

    /**
     * Get whether the scene is currently playing.
     * @return The current play/pause state of the scene
     */
    public boolean isPlaying() {
        synchronized (playbackSpeedLock) {
            return playing;
        }
    }

    /**
     * We use two separate forms of playback speed control: a binary play/pause
     * control, and a many-valued playback speed control.  This method
     * gets the net playback speed, taking both controls into account.
     * If the scene is playing, get the stored playback speed; if it is paused,
     * get the default paused speed.
     * @return Actual playback speed
     */
    private float getActualPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return (isPlaying() ? this.playbackSpeed : PlaybackDefaults.PAUSE_SPEED);
        }
    }

    /**
     * Set the play/pause state of the scene, and its playback speed.  Also
     * send this information to all clients.
     * @param playing Whether the scene is playing or paused
     * @param playbackSpeed The playback speed of the scene
     */
    public void setPlaybackInformation(boolean playing, float playbackSpeed) {
        setPlaybackInformationFromMessage(null, new PlaybackSpeedChangeMessage(playbackSpeed, playing));
    }

    /**
     * Set the playback information from a PlaybackSpeedChangeMessage,
     * and send an update to all clients.
     * @param notifier The client which originally sent this message (null if none)
     * @param message The playback change message
     */
    private void setPlaybackInformationFromMessage(WonderlandClientID notifier, PlaybackSpeedChangeMessage message) {
        final float actualPlaybackSpeed;
        synchronized (playbackSpeedLock) {
            this.playbackSpeed = message.getPlaybackSpeed();
            this.playing = message.isPlaying();
            actualPlaybackSpeed = getActualPlaybackSpeed();
        }
        // Inform the associated program of the change
        ProgramConnectionHandlerMO.changePlaybackSpeed(getCellID(), actualPlaybackSpeed);
        // Send a message to clients
        sendCellMessage(notifier, message);
    }

    /**
     * Find out whether the ground plane of the CMU instance should be shown
     * by clients.
     * @return Whether the ground plane should be showing
     */
    public boolean isGroundPlaneShowing() {
        synchronized (groundPlaneLock) {
            return groundPlaneShowing;
        }
    }

    /**
     * Set whether the ground plane of the CMU instance should be shown by
     * clients; sends an update to all clients to notify them of the change.
     * @param groundPlaneShowing Whether the ground plane should be showing
     */
    public void setGroundPlaneShowing(boolean groundPlaneShowing) {
        setGroundPlaneShowingFromMessage(null, new GroundPlaneChangeMessage(groundPlaneShowing));
    }

    /**
     * Set the ground-showing state from a GroundPlaneChangeMessage, then
     * pass the message on to clients.
     * @param notifier The client who originally sent the message (null if none)
     * @param message The ground plane change message
     */
    private void setGroundPlaneShowingFromMessage(WonderlandClientID notifier, GroundPlaneChangeMessage message) {
        synchronized (groundPlaneLock) {
            this.groundPlaneShowing = message.isGroundPlaneShowing();
        }
        sendCellMessage(notifier, message);
    }

    /**
     * Get the port which clients should connect to in order to receive
     * scene updates.
     * @return Port to connect to
     */
    public int getPort() {
        synchronized (socketLock) {
            return port;
        }
    }

    /**
     * Get the hostname which clients should connect to in order to receive
     * scene updates.
     * @return Host to connect to
     */
    public String getHostname() {
        synchronized (socketLock) {
            return hostName;
        }
    }

    public void setHostnameAndPort(String hostname, int port) {
        setHostnameAndPortFromMessage(null, new ConnectionChangeMessage(hostname, port));
    }

    private void setHostnameAndPortFromMessage(WonderlandClientID notifier, ConnectionChangeMessage message) {
        synchronized (socketLock) {
            this.socketInitialized = true;
            this.hostName = message.getHostname();
            this.port = message.getPort();
        }
        sendCellMessage(notifier, message);
    }

    public String getSceneTitle() {
        synchronized (sceneTitleLock) {
            return sceneTitle;
        }
    }

    public void setSceneTitle(String sceneTitle) {
        setSceneTitleFromMessage(null, new SceneTitleChangeMessage(sceneTitle));
    }

    private void setSceneTitleFromMessage(WonderlandClientID notifier, SceneTitleChangeMessage message) {
        synchronized (sceneTitleLock) {
            this.sceneTitle = message.getSceneTitle();
        }
        sendCellMessage(notifier, message);
    }
}
