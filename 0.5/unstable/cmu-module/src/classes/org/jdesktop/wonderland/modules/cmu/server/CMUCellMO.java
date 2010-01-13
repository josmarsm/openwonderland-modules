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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellServerState;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.PlaybackDefaults;
import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;
import org.jdesktop.wonderland.modules.cmu.common.VisualType;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;
import org.jdesktop.wonderland.modules.cmu.common.events.WonderlandResponse;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.AvailableResponsesChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.EventListMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.EventResponseMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.VisibilityChangeMessage;
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

    /**
     * Filename to store Wonderland-specific data inside Alice files
     */
    private static final String wonderlandDataFileName = "wonderland.xml";
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
    // Wonderland events
    private EventResponseList eventList = null;
    private ArrayList<WonderlandResponse> allowedEventResponses = null;
    private final Serializable eventListLock = new String();

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
            } // Ground plane visibility change
            else if (message instanceof VisibilityChangeMessage) {
                cellMO.setGroundPlaneShowingFromMessage(clientID, (VisibilityChangeMessage) message);
            } // Scene title change
            else if (message instanceof SceneTitleChangeMessage) {
                cellMO.setSceneTitleFromMessage(clientID, (SceneTitleChangeMessage) message);
            } // Restart program
            else if (message instanceof RestartProgramMessage) {
                cellMO.createProgram();
            } // Mouse button event
            else if (message instanceof MouseButtonEventMessage) {
                cellMO.sendMouseClick(((MouseButtonEventMessage) message).getNodeID());
            } // Event list update
            else if (message instanceof EventListMessage) {
                cellMO.setEventListFromMessage(clientID, (EventListMessage) message);
            } // Generic Wonderland event
            else if (message instanceof EventResponseMessage) {
                cellMO.processEventResponse(((EventResponseMessage) message).getResponse());
            } // Unknown message
            else {
                Logger.getLogger(CMUCellMO.CMUCellMessageReceiver.class.getName()).log(Level.SEVERE, "Unknown message: " + message);
            }
        }
    }

    /** Default constructor. */
    public CMUCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     * @param clientID {@inheritDoc}
     * @param capabilities {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.cmu.client.CMUCell";
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new CMUCellClientState();
        }

        CMUCellClientState cmuClientState = ((CMUCellClientState) cellClientState);
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
        cmuClientState.setEventList(this.getEventList());
        cmuClientState.setAllowedResponses(this.getAllowedEventResponses());

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        CMUCellServerState setup = (CMUCellServerState) state;
        setCmuURI(setup.getCmuURI());
        setGroundPlaneShowing(setup.isGroundPlaneShowing());
        setSceneTitle(setup.getSceneTitle());

        createProgram();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState setup) {
        if (setup == null) {
            setup = new CMUCellServerState();
        }
        CMUCellServerState cmuServerState = (CMUCellServerState) setup;
        cmuServerState.setCmuURI(getCmuURI());
        cmuServerState.setGroundPlaneShowing(isGroundPlaneShowing());
        cmuServerState.setSceneTitle(getSceneTitle());
        return super.getServerState(setup);
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
            ProgramConnectionHandlerMO.removeProgram(this.getCellID(), UnloadSceneReason.DISCONNECTING);
        }
    }

    /**
     * Apply playback defaults to this cell, and then send a message to the CMU
     * program manager notifying it to create a program with this cell's URI.
     */
    public void createProgram() {
        setPlaybackInformation(PlaybackDefaults.DEFAULT_START_PLAYING, PlaybackDefaults.DEFAULT_START_SPEED);

        // Create CMU instance
        ProgramConnectionHandlerMO.createProgram(getCellID(), getCmuURI(), getActualPlaybackSpeed());
    }

    /**
     * Forward a mouse click on the given node to the appropriate CMU program.
     * @param nodeID ID for the node which has been clicked
     */
    public void sendMouseClick(NodeID nodeID) {
        ProgramConnectionHandlerMO.sendClick(getCellID(), nodeID);
    }

    /**
     * Process a response to a Wonderland event by passing it on to the
     * CMU player.
     * @param response The response to propagate
     */
    public void processEventResponse(WonderlandResponse response) {
        System.out.println("Server received event response: " + response);
        ProgramConnectionHandlerMO.sendEventResponse(getCellID(), response);
    }

    /**
     * Get the URI of the loaded CMU file.
     * @return The URI of the loaded CMU file
     */
    public String getCmuURI() {
        synchronized (uriLock) {
            return cmuURI;
        }
    }

    /**
     * Sets the URI of the CMU file.
     * @param uri The URI of the CMU file
     */
    public void setCmuURI(String uri) {
        synchronized (uriLock) {
            cmuURI = uri;
        }

        if (this.eventList == null) {
            this.eventList = new EventResponseList();
        }

        //TODO: Get zip file from URI
        // Read Wonderland-specific data from CMU file
        /*
        try {
            URL url = AssetUtils.getAssetURL(uri);
            Asset a = AssetManager.getAssetManager().getAsset(new ContentURI(url.toString()));
            if (AssetManager.getAssetManager().waitForAsset(a)) {
                // Create program
                File localFile = a.getLocalCacheFile();
                ZipFile zipFile = new ZipFile(localFile, ZipFile.OPEN_READ);

                ZipEntry wonderlandData = zipFile.getEntry(wonderlandDataFileName);
                synchronized (eventListLock) {
                    if (wonderlandData != null) {
                        this.eventList = WonderlandEventList.readFromStream(zipFile.getInputStream(wonderlandData));
                    } else {
                        this.eventList = new WonderlandEventList();
                    }
                }

                zipFile.close();
            }
        } catch (ZipException ex) {
            Logger.getLogger(CMUCellMO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CMUCellMO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CMUCellMO.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
        
    }

    /**
     * Get the list of possible event responses for this cell.
     * @return List of possible event responses
     */
    public ArrayList<WonderlandResponse> getAllowedEventResponses() {
        return allowedEventResponses;
    }

    /**
     * Set the possible event responses for this cell.  Note that these responses
     * are not enforced to be the only possible responses; this is merely a
     * convenience to aid in displaying the responses which the player will be
     * able to handle gracefully.
     * @param allowedEventResponses List of possible event responses
     */
    public void setAllowedEventResponses(ArrayList<WonderlandResponse> allowedEventResponses) {
        this.allowedEventResponses = allowedEventResponses;
        sendCellMessage(null, new AvailableResponsesChangeMessage(allowedEventResponses));
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
            actualPlaybackSpeed = this.getActualPlaybackSpeed();
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
        setGroundPlaneShowingFromMessage(null, new VisibilityChangeMessage(VisualType.GROUND, groundPlaneShowing));
    }

    /**
     * Set the ground-showing state from a GroundPlaneChangeMessage, then
     * pass the message on to clients.
     * @param notifier The client who originally sent the message (null if none)
     * @param message The ground plane change message
     */
    private void setGroundPlaneShowingFromMessage(WonderlandClientID notifier, VisibilityChangeMessage message) {
        synchronized (groundPlaneLock) {
            this.groundPlaneShowing = message.isShowing();
        }
        sendCellMessage(notifier, message);
    }

    /**
     * Get the list of Wonderland events that this cell should respond to (with
     * the appropriate responses).
     * @return List of events to respond to
     */
    public EventResponseList getEventList() {
        synchronized (eventListLock) {
            return this.eventList;
        }
    }

    /**
     * Set the list of Wonderland events that this cell should respond to.
     * @param eventList List of events to respond to
     */
    public void setEventList(EventResponseList eventList) {
        setEventListFromMessage(null, new EventListMessage(eventList));
    }

    /**
     * Set the list of Wonderland events that the cell should respond to,
     * then pass this message on to clients.
     * @param notifier The client who originally notified of the change
     * @param message The message to send
     */
    private void setEventListFromMessage(WonderlandClientID notifier, EventListMessage message) {
        synchronized (eventListLock) {
            this.eventList = message.getList();
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

    /**
     * Set the connection information for this cell to receive scene information
     * and updates, and propagate this information to connected clients.
     * @param hostname The hostname to connect to
     * @param port The port to connect to
     */
    public void setHostnameAndPort(String hostname, int port) {
        synchronized (socketLock) {
            this.socketInitialized = true;
            this.hostName = hostname;
            this.port = port;
        }
        sendCellMessage(null, new ConnectionChangeMessage(hostname, port));
    }

    /**
     * Get the title of this scene.
     * @return Title of this scene
     */
    public String getSceneTitle() {
        synchronized (sceneTitleLock) {
            return sceneTitle;
        }
    }

    /**
     * Set the title of this scene.
     * @param sceneTitle Title of this scene
     */
    public void setSceneTitle(String sceneTitle) {
        setSceneTitleFromMessage(null, new SceneTitleChangeMessage(sceneTitle));
    }

    /**
     * Set the scene title internally, as a response to a scene title change
     * message.
     * @param notifier The client who sent the message
     * @param message The scene title change message
     */
    private void setSceneTitleFromMessage(WonderlandClientID notifier, SceneTitleChangeMessage message) {
        synchronized (sceneTitleLock) {
            this.sceneTitle = message.getSceneTitle();
        }
        sendCellMessage(notifier, message);
    }
}
