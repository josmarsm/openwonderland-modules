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
package org.jdesktop.wonderland.modules.cmu.client;

import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import com.jme.scene.Node;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.CMUCellRenderer;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.VisualNode;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.VisualParent;
import org.jdesktop.wonderland.modules.cmu.client.web.VisualDownloadManager;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.SceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.UnloadSceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.GroundPlaneChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.RestartProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.SceneTitleChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ServerClientMessageTypes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualRepoIdentifier;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    // Renderer info
    private CMUCellRenderer renderer = null;
    private final VisualParent sceneRoot = new VisualParent();      // We synchronize incoming visual changes and connection changes on this object.
    private boolean sceneLoaded = false;

    // Scene title info
    private String sceneTitle = null;
    private final Object sceneTitleLock = new Object();

    // Playback info
    private float playbackSpeed = 0.0f;
    private boolean playing = false;
    private final Object playbackSpeedLock = new Object();

    // Ground plane info
    private boolean groundPlaneShowing = false;
    private final Object groundPlaneLock = new Object();
    private final Collection<NodeID> groundPlaneIDs = new Vector<NodeID>();

    // Message receivers/listeners
    private MouseEventListener mouseListener = null;
    private VisualChangeReceiverThread cmuConnectionThread = null;
    private final CMUCellMessageReceiver messageReceiver = new CMUCellMessageReceiver();

    // Listener sets
    private final Set<PlaybackChangeListener> playbackChangeListeners = new HashSet<PlaybackChangeListener>();
    private final Set<GroundPlaneChangeListener> groundPlaneChangeListeners = new HashSet<GroundPlaneChangeListener>();
    private final Set<SceneTitleChangeListener> sceneTitleChangeListeners = new HashSet<SceneTitleChangeListener>();

    // HUD Stuff
    private final HUDControl hudControl = new HUDControl(this);

    /**
     * Listener to process mouse click events.
     */
    private class MouseEventListener extends EventClassListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        /**
         * Toggle HUD on/off.
         * @param event {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            hudControl.setHUDShowing(!hudControl.isHUDShowing());
        //sendCellMessage(new MouseButtonEventMessage(getCellID()));//, mbe));
        }

        /**
         * Nothing to compute.
         * @param event {@inheritDoc}
         */
        @Override
        public void computeEvent(Event event) {
        }
    }

    /**
     * Message receiver to receive socket information and playback speed
     * change messages.
     */
    private class CMUCellMessageReceiver implements ComponentMessageReceiver {

        /**
         * Process messages sent by the managed object on the server;
         * these messages can update connection information for this cell
         * (ConnectionChangeMessage), or change the displayed playback speed
         * (PlaybackSpeedChangeMessage).
         * @param message {@inheritDoc}
         */
        @Override
        public void messageReceived(CellMessage message) {
            final boolean fromMe = message.getSenderID() != null && message.getSenderID().equals(getCellCache().getSession().getID());

            // Socket information message
            if (ConnectionChangeMessage.class.isAssignableFrom(message.getClass())) {
                ConnectionChangeMessage changeMessage = (ConnectionChangeMessage) message;
                setHostnameAndPort(changeMessage.getHostname(), changeMessage.getPort());
            }

            // Playback speed message
            if (PlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())) {
                if (!fromMe) {
                    PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                    setPlaybackInformationInternal(change.getPlaybackSpeed(), change.isPlaying());
                }
            }

            // Ground plane visibility change message
            if (GroundPlaneChangeMessage.class.isAssignableFrom(message.getClass())) {
                if (!fromMe) {
                    GroundPlaneChangeMessage change = (GroundPlaneChangeMessage) message;
                    setGroundPlaneShowingInternal(change.isGroundPlaneShowing());
                }
            }

            // Scene title change message
            if (SceneTitleChangeMessage.class.isAssignableFrom(message.getClass())) {
                if (!fromMe) {
                    SceneTitleChangeMessage change = (SceneTitleChangeMessage) message;
                    setSceneTitleInternal(change.getSceneTitle());
                }
            }
        }
    }

    /**
     * Standard constructor.
     */
    public CMUCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Use the client state data to extract port/server information;
     * if none has been set (i.e. if the CMU instance on the server
     * has not yet been initialized), we don't do anything, and instead
     * wait for the CMUCellMO associated with this cell to inform
     * us explicitly of the socket information.
     * @param {@inheritDoc}
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);

        assert clientState instanceof CMUCellClientState;
        CMUCellClientState cmuClientState = (CMUCellClientState) clientState;
        this.setPlaybackInformationInternal(cmuClientState.getPlaybackSpeed(), cmuClientState.isPlaying());
        if (cmuClientState.isServerAndPortInitialized()) {
            this.setHostnameAndPort(cmuClientState.getServer(), cmuClientState.getPort());
        }
        this.setSceneTitle(cmuClientState.getSceneTitle());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new CMUCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }

    }

    /**
     * Get the root of the CMU scene represented by this node.
     * @return The root of the CMU scene as a jME node
     */
    public Node getSceneRoot() {
        return this.sceneRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);

        switch (status) {
            case DISK:
                // Remove mouse listener.
                if (mouseListener != null) {
                    mouseListener.removeFromEntity(renderer.getEntity());
                    mouseListener = null;
                }

                if (!increasing) {
                    // Remove message receivers
                    if (channel != null) {
                        for (Class messageClass : ServerClientMessageTypes.MESSAGE_TYPES_TO_RECEIVE) {
                            channel.removeMessageReceiver(messageClass);
                        }
                    }

                    // Clean up HUD and network stuff
                    hudControl.unloadHUD();
                    setConnectedState(false);
                }

                break;

            case INACTIVE:
                if (increasing) {
                    // Add message receivers
                    if (channel != null) {
                        for (Class messageClass : ServerClientMessageTypes.MESSAGE_TYPES_TO_RECEIVE) {
                            channel.addMessageReceiver(messageClass, messageReceiver);
                        }
                    }
                }

                break;

            case ACTIVE:
                // Add mouse listener.
                if (mouseListener == null) {
                    mouseListener = new MouseEventListener();
                    mouseListener.addToEntity(renderer.getEntity());
                }

                break;

            default:
                break;
        }
    }

    /**
     * We allow only one thread to provide scene graph updates at a time;
     * this checks whether the parameter is that thread.
     * @param receiver The thread to check
     * @return True if that thread is allowed to update this cell
     */
    public boolean allowsUpdatesFrom(VisualChangeReceiverThread receiver) {
        synchronized (this.sceneRoot) {
            return (this.cmuConnectionThread == receiver);
        }
    }

    /**
     * Check if the given thread is allowed to update this cell, and if
     * so, deal with the message.
     * @param message The message to apply
     * @param receiver The thread sending the message
     */
    public void applyMessage(Object message, VisualChangeReceiverThread receiver) {
        synchronized (sceneRoot) {
            if (allowsUpdatesFrom(receiver)) {
                // Transformation for existing visual
                if (TransformationMessage.class.isAssignableFrom(message.getClass())) {
                    this.applyTransformationMessage((TransformationMessage) message);
                } // New visual
                else if (VisualMessage.class.isAssignableFrom(message.getClass())) {
                    this.applyVisualMessage((VisualMessage) message);
                } // Visual deleted
                else if (VisualDeletedMessage.class.isAssignableFrom(message.getClass())) {
                    this.applyVisualDeletedMessage((VisualDeletedMessage) message);
                } // Load entire scene
                else if (SceneMessage.class.isAssignableFrom(message.getClass())) {
                    this.applySceneMessage((SceneMessage) message);
                } // Unload scene
                else if (UnloadSceneMessage.class.isAssignableFrom(message.getClass())) {
                    this.applyUnloadSceneMessage((UnloadSceneMessage) message);
                } // Unknown message
                else {
                    System.out.println("CMUCell WARNING: UNKOWN MESSAGE: " + message);
                }
            }
        }
    }

    /**
     * Processes a transformation message by applying it to the node
     * with the appropriate node ID.
     * @param message Message to apply
     */
    private void applyTransformationMessage(TransformationMessage message) {
        synchronized (sceneRoot) {
            if (this.isSceneLoaded()) {
                this.sceneRoot.applyTransformationToChild(message);
            }
        }
    }

    /**
     * Load a visual message by attaching it to the scene root, and
     * filtering out the ground plane if desired.
     * @param message Message to apply
     */
    private void applyVisualMessage(VisualMessage message) {
        //TODO: thread this?
        VisualNode visualNode = new VisualNode(message.getNodeID());
        VisualRepoIdentifier visualID = message.getVisualID();
        NodeID nodeID = message.getNodeID();

        VisualAttributes attributes = VisualDownloadManager.downloadVisual(visualID, this);
        if (attributes == null) {
            return;
        }
        visualNode.applyVisual(attributes);

        synchronized (sceneRoot) {
            sceneRoot.attachChild(visualNode);
            sceneRoot.applyTransformationToChild(message.getTransformation());
        }


        // Filter ground plane
        if (NodeNameClassifier.isGroundPlaneName(attributes.getName())) {
            synchronized (groundPlaneIDs) {
                groundPlaneIDs.add(nodeID);
            }
            synchronized (sceneRoot) {
                sceneRoot.applyVisibilityToChild(nodeID, isGroundPlaneShowing());
            }
        }
    }

    /**
     * Removes the node referenced by the message.
     * @param message Message to apply
     */
    private void applyVisualDeletedMessage(VisualDeletedMessage message) {
        synchronized (sceneRoot) {
            sceneRoot.removeChild(message);
        }
    }

    /**
     * Load an entire scene, and mark this scene as loaded.
     * @param message The scene to load
     */
    private void applySceneMessage(SceneMessage message) {
        for (VisualMessage visual : message.getVisuals()) {
            synchronized (sceneRoot) {
                applyVisualMessage(visual);
            }
        }
        synchronized (sceneRoot) {
            this.setSceneLoaded(true);
        }
    }

    /**
     * Unload an entire scene, and mark this scene as unloaded.
     * @param message Message to apply
     */
    private void applyUnloadSceneMessage(UnloadSceneMessage message) {
        // Note: message has no state
        synchronized (sceneRoot) {
            setConnectedState(false);
        }
    }

    /**
     * Called by receivers interacting with this cell to mark that their connection
     * has been obtained or lost.
     * @param connected True if the connection has been obtained, false if lost
     * @param receiver The thread for which the connected state aplies
     */
    public void updateConnectedState(boolean connected, VisualChangeReceiverThread receiver) {
        synchronized (sceneRoot) {
            if (allowsUpdatesFrom(receiver)) {
                setConnectedState(connected);
            }
        }
    }

    /**
     * Visually and practically set the connected state of the scene.  If
     * we're disconnecting, unload the scene visuals, and cut ties with the
     * current VisualChangeReceiverThread.  If we're connecting, clean up
     * any disconnect messages to prepare for incoming scene changes.
     * @param connected Whether we're connecting (true) or disconnecting (false)
     */
    private void setConnectedState(boolean connected) {
        synchronized (this.sceneRoot) {
            if (connected) {
                this.sceneRoot.detachAllChildren();
            } else {
                this.setSceneLoaded(false);
                this.cmuConnectionThread = null;

                //TODO: Show something noting that the scene is disconnected.
                this.sceneRoot.detachAllChildren();
            }
        }
    }

    /**
     * Determines whether this scene has been fully loaded.
     * @return True if this scene has been fully loaded, false otherwise
     */
    public boolean isSceneLoaded() {
        synchronized (sceneRoot) {
            return sceneLoaded;
        }
    }

    /**
     * Set the loaded state of the scene.
     * @param sceneLoaded Whether the scene is loaded
     */
    protected void setSceneLoaded(boolean sceneLoaded) {
        synchronized (sceneRoot) {
            this.sceneLoaded = sceneLoaded;
        }

    }

    /**
     * Set the server and port that will be used to connect to a CMU instance.
     * If we are already connected somewhere, disconnect and reestablish the
     * connection at the new location.
     * @param server The host address to connect to
     * @param port The port to connect to
     */
    public void setHostnameAndPort(String hostname, int port) {
        // Synchronize this on sceneRoot, so that no changes can happen to
        // the scene while this is occurring
        synchronized (this.sceneRoot) {
            // Clear any existing connections
            this.setConnectedState(false);

            // Set up a new receiver thread
            cmuConnectionThread = new VisualChangeReceiverThread(this, hostname, port);
            cmuConnectionThread.start();
        }
    }

    /**
     * Get the title of this scene.
     * @return The title of this scene
     */
    public String getSceneTitle() {
        synchronized (sceneTitleLock) {
            return sceneTitle;
        }
    }

    /**
     * Set the title of this scene, and send a message
     * notifying the server/other cells that this has been done.
     * @param sceneTitle The new scene title
     */
    public void setSceneTitle(String sceneTitle) {
        synchronized (sceneTitleLock) {
            setSceneTitleInternal(sceneTitle);
        }
        sendCellMessage(new SceneTitleChangeMessage(getSceneTitle()));
    }

    /**
     * Set the title of this scene locally and notify any listeners, but
     * don't send an associated cell message.
     * @param sceneTitle The new scene title
     */
    private void setSceneTitleInternal(String sceneTitle) {
        synchronized (sceneTitleLock) {
            this.sceneTitle = sceneTitle;
            hudControl.updateHUD();
        }
        fireSceneTitleChanged(new SceneTitleChangeEvent(sceneTitle));
    }

    /**
     * Get the stored playback speed, i.e. the playback speed that will
     * be used as long as this scene isn't paused.
     * @return The speed at which the associated CMU scene is playing
     */
    public float getPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return this.playbackSpeed;
        }

    }

    /**
     * True if the program is playing, e.g. not paused.  Note that this
     * has nothing to do with the playback speed; a program can be playing
     * at speed 0, or paused with speed 1.
     * @return True if the program is playing
     */
    public boolean isPlaying() {
        synchronized (playbackSpeedLock) {
            return playing;
        }
    }

    /**
     * Set the playback speed that will be used as long as this scene isn't
     * paused, and propagate this change to the server.
     * @param playbackSpeed New playback speed
     */
    public void setPlaybackSpeed(float playbackSpeed) {
        PlaybackSpeedChangeMessage message = null;
        if (isSceneLoaded()) {
            synchronized (playbackSpeedLock) {
                setPlaybackInformationInternal(playbackSpeed, this.isPlaying());
                message = new PlaybackSpeedChangeMessage(getPlaybackSpeed(), playing);
            }
        }
        sendCellMessage(message);
    }

    /**
     * Change the play/pause state of the scene, and send an update to the server
     * noting that this has been done.
     * @param playing The play/pause state of the scene
     */
    public void setPlaying(boolean playing) {
        if (isSceneLoaded()) {
            PlaybackSpeedChangeMessage message = null;
            synchronized (playbackSpeedLock) {
                setPlaybackInformationInternal(this.getPlaybackSpeed(), playing);
                message = new PlaybackSpeedChangeMessage(getPlaybackSpeed(), playing);
            }
            sendCellMessage(message);
        }
    }

    /**
     * Set playback speed and play/pause state without propagating this change
     * to the server (i.e. to respond to server commands).
     * @param playbackSpeed The speed at which the associated CMU scene is playing
     * @param playing The play/pause state of the scene
     */
    private void setPlaybackInformationInternal(float playbackSpeed, boolean playing) {
        synchronized (playbackSpeedLock) {
            this.playing = playing;
            this.playbackSpeed = playbackSpeed;
        }
        firePlaybackChanged(new PlaybackChangeEvent(playbackSpeed, playing));
    }

    public boolean isGroundPlaneShowing() {
        synchronized (groundPlaneLock) {
            return groundPlaneShowing;
        }

    }

    public void setGroundPlaneShowing(boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.setGroundPlaneShowingInternal(groundPlaneShowing);
        }
        sendCellMessage(new GroundPlaneChangeMessage(groundPlaneShowing));
    }

    private void setGroundPlaneShowingInternal(boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.groundPlaneShowing = groundPlaneShowing;
            synchronized (groundPlaneIDs) {
                for (NodeID id : groundPlaneIDs) {
                    sceneRoot.applyVisibilityToChild(id, groundPlaneShowing);
                }
            }
        }
        fireGroundPlaneChanged(new GroundPlaneChangeEvent(groundPlaneShowing));
    }

    /**
     * Add a listener for playback changes.
     * @param listener Listener to add
     */
    public void addPlaybackChangeListener(PlaybackChangeListener listener) {
        synchronized (this.playbackChangeListeners) {
            playbackChangeListeners.add(listener);
        }
    }

    /**
     * Remove a listener for playback changes.
     * @param listener Listener to remove
     */
    public void removePlaybackChangeListener(PlaybackChangeListener listener) {
        synchronized (playbackChangeListeners) {
            playbackChangeListeners.remove(listener);
        }
    }

    /**
     * Send playback information to playback change listeners.
     */
    private void firePlaybackChanged(PlaybackChangeEvent event) {
        synchronized (playbackChangeListeners) {
            for (PlaybackChangeListener listener : playbackChangeListeners) {
                listener.playbackChanged(event);
            }
        }
    }

    public void addGroundPlaneChangeListener(GroundPlaneChangeListener listener) {
        synchronized (groundPlaneChangeListeners) {
            groundPlaneChangeListeners.add(listener);
        }

    }

    public void removeGroundPlaneChangeListener(PlaybackChangeListener listener) {
        synchronized (playbackChangeListeners) {
            playbackChangeListeners.remove(listener);
        }

    }

    private void fireGroundPlaneChanged(GroundPlaneChangeEvent event) {
        synchronized (groundPlaneChangeListeners) {
            for (GroundPlaneChangeListener listener : groundPlaneChangeListeners) {
                listener.groundPlaneChanged(event);
            }
        }
    }

    public void addSceneTitleChangeListener(SceneTitleChangeListener listener) {
        synchronized (sceneTitleChangeListeners) {
            sceneTitleChangeListeners.add(listener);
            listener.sceneTitleChanged(new SceneTitleChangeEvent(this.getSceneTitle()));
        }
    }

    public void removeSceneTitleChangeListener(SceneTitleChangeListener listener) {
        synchronized (sceneTitleChangeListeners) {
            sceneTitleChangeListeners.remove(listener);
        }
    }

    private void fireSceneTitleChanged(SceneTitleChangeEvent event) {
        synchronized (sceneTitleLock) {
            event = new SceneTitleChangeEvent(this.getSceneTitle());
            synchronized (sceneTitleChangeListeners) {
                for (SceneTitleChangeListener listener : sceneTitleChangeListeners) {
                    listener.sceneTitleChanged(event);
                }
            }
        }
    }

    public void restart() {
        sendCellMessage(new RestartProgramMessage());
    }
}
