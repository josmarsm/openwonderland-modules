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
package org.jdesktop.wonderland.modules.cmu.client;

import org.jdesktop.wonderland.modules.cmu.client.events.cmu.PlaybackChangeEvent;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.PlaybackChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.SceneTitleChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.SceneTitleChangeEvent;
import org.jdesktop.wonderland.modules.cmu.client.ui.hud.HUDControl;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.ConnectionStateChangeEvent;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.ConnectionStateChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.SceneLoadedChangeEvent;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.SceneLoadedChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.VisibilityChangeEvent;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.VisibilityChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.CMUCellRenderer;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.VisualNode;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.VisualParent;
import org.jdesktop.wonderland.modules.cmu.client.web.VisualDownloadManager;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;
import org.jdesktop.wonderland.modules.cmu.common.VisualType;
import org.jdesktop.wonderland.modules.cmu.common.events.AvatarMovementEvent;
import org.jdesktop.wonderland.modules.cmu.common.events.ContextMenuEvent;
import org.jdesktop.wonderland.modules.cmu.common.events.ProximityEvent;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponsePair;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.SceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.UnloadSceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.AvailableResponsesChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.EventListMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.VisibilityChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.MouseButtonEventMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.RestartProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.SceneTitleChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ServerClientMessageTypes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualAttributesIdentifier;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    // Whether cell components can be treated as valid (e.g. whether setStatus() has been called)
    private boolean componentsValid = false;
    private final Object componentsValidLock = new Object();
    // Renderer info
    private CMUCellRenderer renderer = null;
    private final VisualParent sceneRoot = new VisualParent();      // We synchronize incoming visual changes and connection changes on this object.
    // Connection info
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
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
    // Message receivers/listeners
    private MouseEventListener mouseListener = null;
    private VisualChangeReceiverThread cmuConnectionThread = null;
    private final CMUCellMessageReceiver messageReceiver = new CMUCellMessageReceiver();
    // Event response information
    private EventResponseList eventList = null;
    private final Object eventListLock = new Object();
    private Collection<CMUResponseFunction> allowedResponses = null;
    // Context menu
    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;
    // Listener sets
    private final Set<PlaybackChangeListener> playbackChangeListeners = new HashSet<PlaybackChangeListener>();
    private final Set<VisibilityChangeListener> visibilityChangeListeners = new HashSet<VisibilityChangeListener>();
    private final Set<SceneTitleChangeListener> sceneTitleChangeListeners = new HashSet<SceneTitleChangeListener>();
    private final Set<SceneLoadedChangeListener> sceneLoadedChangeListeners = new HashSet<SceneLoadedChangeListener>();
    private final Set<ConnectionStateChangeListener> connectionStateChangeListeners = new HashSet<ConnectionStateChangeListener>();
    // HUD Stuff
    private final HUDControl hudControl = new HUDControl(this);
    // Content repository information
    private String visualRepoRoot = null;

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
            assert event instanceof MouseButtonEvent3D;
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;

            // We only process left clicks
            if (mbe.isClicked() && mbe.getButton() == ButtonId.BUTTON1) {
                TriMesh clickedMesh = mbe.getPickDetails().getTriMesh();

                // Walk up the scene graph until we find the VisualNode that was clicked
                Node clickedNode = clickedMesh.getParent();
                while (clickedNode != null && !(clickedNode instanceof VisualNode)) {
                    clickedNode = clickedNode.getParent();
                }

                if (clickedNode != null) {
                    VisualNode clickedVisual = (VisualNode) clickedNode;
                    sendCellMessage(new MouseButtonEventMessage(clickedVisual.getNodeID()));
                } else {
                    Logger.getLogger(CMUCell.MouseEventListener.class.getName()).log(Level.SEVERE, "Couldn't resolve mouse click");
                }
            }
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
            if (message instanceof ConnectionChangeMessage) {
                ConnectionChangeMessage changeMessage = (ConnectionChangeMessage) message;
                setHostnameAndPort(changeMessage.getHostname(), changeMessage.getPort());
            } // Playback speed message
            else if (message instanceof PlaybackSpeedChangeMessage) {
                if (!fromMe) {
                    PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                    setPlaybackInformationInternal(change.getPlaybackSpeed(), change.isPlaying());
                }
            } // Ground plane visibility change message
            else if (message instanceof VisibilityChangeMessage) {
                if (!fromMe) {
                    VisibilityChangeMessage change = (VisibilityChangeMessage) message;
                    if (change.getType().equals(VisualType.GROUND)) {
                        setGroundPlaneShowingInternal(change.isShowing());
                    } else {
                        Logger.getLogger(CMUCell.CMUCellMessageReceiver.class.getName()).
                                severe("Unrecognized visual type: " + change.getType());
                    }
                }
            } // Scene title change message
            else if (message instanceof SceneTitleChangeMessage) {
                if (!fromMe) {
                    SceneTitleChangeMessage change = (SceneTitleChangeMessage) message;
                    setSceneTitleInternal(change.getSceneTitle());
                }
            } // Change in available responses
            else if (message instanceof AvailableResponsesChangeMessage) {
                AvailableResponsesChangeMessage change = (AvailableResponsesChangeMessage) message;
                setAllowedResponses(change.getResponses());
            } // Change in events to respond to
            else if (message instanceof EventListMessage) {
                if (!fromMe) {
                    EventListMessage change = (EventListMessage) message;
                    setEventListInternal(change.getList());
                }
            } // Unknown message
            else {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, "Unkown message: " + message);
            }
        }
    }

    /**
     * Enumeration to represent the connection/loading state of a scene.
     */
    public enum ConnectionState {

        /**
         * Connection lost (for unknown reasons, or not expecting to
         * reconnect).
         */
        DISCONNECTED,
        /**
         * Connection lost, expecting to reconnect.
         */
        RECONNECTING,
        /**
         * Connected, waiting to load (i.e. waiting to receive a scene
         * message).
         */
        WAITING,
        /**
         * Connected, partially loaded.
         */
        LOADING,
        /**
         * Connected, fully loaded.
         */
        LOADED,
    }

    /**
     * Standard constructor.
     * @param cellID ID of this cell
     * @param cellCache Cache for this cell
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
     * @param clientState {@inheritDoc}
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
        this.setGroundPlaneShowingInternal(cmuClientState.isGroundPlaneShowing());
        this.setSceneTitle(cmuClientState.getSceneTitle());
        this.setEventListInternal(cmuClientState.getEventList());
        this.setAllowedResponses(cmuClientState.getAllowedResponses());
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
    @SuppressWarnings("unchecked")
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        this.setComponentsValid(true);

        ChannelComponent channel = getComponent(ChannelComponent.class);
        if (status == CellStatus.DISK) {
            if (!increasing) {
                // Remove mouse listener
                mouseListener.removeFromEntity(renderer.getEntity());
                mouseListener = null;

                // Remove message receivers
                if (channel != null) {
                    for (Class messageClass : ServerClientMessageTypes.MESSAGE_TYPES_TO_RECEIVE) {
                        channel.removeMessageReceiver(messageClass);
                    }
                }

                // Clean up HUD and network stuff
                hudControl.unloadHUD();
                setConnectionState(ConnectionState.DISCONNECTED);
            }

        } else if (status == CellStatus.INACTIVE) {
            if (increasing) {
                // Add message receivers
                if (channel != null) {
                    for (Class messageClass : ServerClientMessageTypes.MESSAGE_TYPES_TO_RECEIVE) {
                        channel.addMessageReceiver(messageClass, messageReceiver);
                    }
                }
            }

            if (this.menuFactory != null) {
                this.contextComp.removeContextMenuFactory(this.menuFactory);
                this.menuFactory = null;
            }
        } else if (status == CellStatus.RENDERING) {
            if (this.menuFactory == null) {
                this.menuFactory = new CMUContextFactory(this);
                this.contextComp.addContextMenuFactory(this.menuFactory);
            }
        } else if (status == CellStatus.ACTIVE) {
            // Add mouse listener.
            if (increasing) {
                mouseListener = new MouseEventListener();
                mouseListener.addToEntity(renderer.getEntity());
            }

        }
    }

    /**
     * We allow only one thread to provide scene graph updates at a time;
     * this checks whether the parameter is that thread.
     * @param receiver The thread to check
     * @return True if the thread is allowed to update this cell
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
        if (allowsUpdatesFrom(receiver)) {
            // Property/transformation update for existing visual
            if (message instanceof NodeUpdateMessage) {
                this.applyNodeUpdateMessage((NodeUpdateMessage) message);
            } // New visual
            else if (message instanceof VisualMessage) {
                this.applyVisualMessage((VisualMessage) message);
            } // Visual deleted
            else if (message instanceof VisualDeletedMessage) {
                this.applyVisualDeletedMessage((VisualDeletedMessage) message);
            } // Load entire scene
            else if (message instanceof SceneMessage) {
                this.applySceneMessage((SceneMessage) message);
            } // Unload scene
            else if (message instanceof UnloadSceneMessage) {
                this.applyUnloadSceneMessage((UnloadSceneMessage) message);
            } // Unknown message
            else {
                logger.severe("Unknown message: " + message);
            }
        }
    }

    /**
     * Processes a transformation message by applying it to the node
     * with the appropriate node ID.
     * @param message Message to apply
     */
    private void applyNodeUpdateMessage(final NodeUpdateMessage message) {
        synchronized (sceneRoot) {
            if (this.getConnectionState() == ConnectionState.LOADED) {
                ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

                    public void update(Object arg0) {
                        VisualParent visualNode = sceneRoot.applyUpdateToDescendant(message);
                        ClientContextJME.getWorldManager().addToUpdateList(visualNode);
                    }
                }, null);
            }
        }
    }

    /**
     * Load a visual message by attaching it to the scene root, and
     * filtering out the ground plane if desired.
     * @param message Message to apply
     * @return Whether the load was successful
     */
    private boolean applyVisualMessage(final VisualMessage message) {
        VisualAttributesIdentifier visualID = message.getVisualID();

        final VisualAttributes attributes = VisualDownloadManager.downloadVisual(visualID, getVisualRepoRoot(), this);
        if (attributes == null) {
            return false;
        }
        final VisualNode visualNode = new VisualNode(message.getNodeID(), this);

        synchronized (sceneRoot) {
            visualNode.applyVisualAttributes(attributes);
            ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

                public void update(Object arg0) {
                    sceneRoot.attachChild(visualNode);
                    sceneRoot.applyUpdateToDescendant(message.getTransformation());
                    sceneRoot.applyUpdateToDescendant(message.getVisualProperties());
                    sceneRoot.applyUpdateToDescendant(message.getAppearanceProperties());
                    sceneRoot.applyUpdateToDescendant(message.getChangingGeometries());
                    visualNode.updateVisibility();
                    ClientContextJME.getWorldManager().addToUpdateList(visualNode);
                }
            }, null);
        }
        return true;
    }

    /**
     * Removes the node referenced by the message.
     * @param message Message to apply
     */
    private void applyVisualDeletedMessage(final VisualDeletedMessage message) {
        synchronized (sceneRoot) {
            ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

                public void update(Object arg0) {
                    sceneRoot.removeDescendant(message);
                    ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                }
            }, null);
        }
    }

    /**
     * Load an entire scene, and mark this scene as loaded.
     * @param message The scene to load
     */
    private void applySceneMessage(SceneMessage message) {
        this.setConnectionState(ConnectionState.LOADING);
        setVisualRepoRoot(message.getRepoRoot());

        int visualNum = 0;
        fireSceneLoadedChanged(new SceneLoadedChangeEvent(this, (float) visualNum / (float) message.getVisuals().size()));
        for (VisualMessage visual : message.getVisuals()) {
            if (applyVisualMessage(visual)) {
                visualNum++;
                fireSceneLoadedChanged(new SceneLoadedChangeEvent(this, (float) visualNum / (float) message.getVisuals().size()));
            } else {
                setConnectionState(ConnectionState.DISCONNECTED);
                return;
            }
        }
        this.setConnectionState(ConnectionState.LOADED);
    }

    /**
     * Unload an entire scene, and set the connected state of the scene
     * according to information in the supplied message.
     * @param message Message to apply
     */
    private void applyUnloadSceneMessage(UnloadSceneMessage message) {
        if (message.getReason().equals(UnloadSceneReason.DISCONNECTING)) {
            setConnectionState(ConnectionState.DISCONNECTED);
        } else if (message.getReason().equals(UnloadSceneReason.RESTARTING)) {
            setConnectionState(ConnectionState.RECONNECTING);
        } else {
            logger.severe("Unrecognized reason for scene disconnect: " + message.getReason());
        }
    }

    /**
     * Find out whether the given node should currently be visible in the cell,
     * based on its visual type, and on the connection state of the scene.
     * Note that this may be separate from the node's visibility in the CMU
     * scene (i.e. ground plane nodes may be visible in the scene, but not in
     * the cell).
     * @param node The node to check
     * @return The current visibility of the node
     */
    public boolean isVisibleInCell(VisualNode node) {
        if (getConnectionState() != ConnectionState.LOADED) {
            return false;
        }
        if (node.isType(VisualType.GROUND)) {
            return isGroundPlaneShowing();
        }
        return true;
    }

    /**
     * Called by receivers interacting with this cell to mark that their connection
     * has been obtained or lost.
     * @param connected True if the connection has been obtained, false if lost
     * @param receiver The thread for which the connected state applies
     */
    public void updateConnectedState(boolean connected, VisualChangeReceiverThread receiver) {
        if (allowsUpdatesFrom(receiver)) {
            if (connected) {
                setConnectionState(ConnectionState.WAITING);
            } else {
                setConnectionState(ConnectionState.DISCONNECTED);
            }
        }
    }

    /**
     * Find out whether the components for this cell have been initialized,
     * e.g. whether setStatus() has been called.
     * @return Whether the components for this cell have been initialized
     */
    protected boolean isComponentsValid() {
        synchronized (componentsValidLock) {
            return componentsValid;
        }
    }

    /**
     * Set the
     * @param componentsValid
     */
    private void setComponentsValid(boolean componentsValid) {
        boolean oldComponentsValid = false;
        synchronized (componentsValidLock) {
            oldComponentsValid = this.componentsValid;
            this.componentsValid = componentsValid;
        }

        // If we're activating the components for the first time, perform any delayed processing
        if (componentsValid && !oldComponentsValid) {
            // Add listeners based on our event list
            this.setEventListInternal(this.getEventList());
        }
    }

    /**
     * Get the HUD control object for this cell, which can be used to get/set
     * the HUD state and visibility.
     * @return HUD control for this cell
     */
    public HUDControl getHudControl() {
        return hudControl;
    }

    /**
     * Convenience method to ind out whether a connection has been
     * established with a CMU instance on the server.
     * @return Whether this cell is connected to a CMU scene
     */
    private boolean isConnected() {
        return (getConnectionState() != ConnectionState.DISCONNECTED && getConnectionState() != ConnectionState.RECONNECTING);
    }

    /**
     * Determines whether this scene has been fully loaded.
     * @return The load state of the scene
     */
    public ConnectionState getConnectionState() {
        synchronized (sceneRoot) {
            return connectionState;
        }
    }

    /**
     * Visually and practically set the connection state of the scene.  If
     * we're disconnecting, unload the scene visuals, and cut ties with the
     * current VisualChangeReceiverThread.  If we're connecting, clean up
     * any disconnect messages to prepare for incoming scene changes.  Update
     * the HUD appropriately.
     * @param connectionState The connection/loading state of the scene
     */
    protected void setConnectionState(ConnectionState connectionState) {
        synchronized (sceneRoot) {
            this.connectionState = connectionState;

            if (!isConnected()) {
                this.cmuConnectionThread = null;
                fireSceneLoadedChanged(new SceneLoadedChangeEvent(this, 0.0f));

                ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

                    public void update(Object arg0) {

                        //TODO: If we're reconnecting, postpone detach until new visuals are loaded
                        synchronized (sceneRoot) {
                            sceneRoot.unloadVisualDescendantTextures();
                            sceneRoot.detachAllChildren();
                        }
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                }, null);
            } else if (connectionState == ConnectionState.LOADED) {
                // Set node visibility appropriately
                ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

                    public void update(Object arg0) {
                        synchronized (sceneRoot) {
                            sceneRoot.updateVisibility();
                        }
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                }, null);
            }
        }

        fireConnectionStateChanged(new ConnectionStateChangeEvent(this, connectionState));
    }

    /**
     * Set the server and port that will be used to connect to a CMU instance.
     * If we are already connected somewhere, disconnect and reestablish the
     * connection at the new location.
     * @param hostname The host address to connect to
     * @param port The port to connect to
     */
    public void setHostnameAndPort(String hostname, int port) {
        // Clear any existing connections
        this.setConnectionState(ConnectionState.DISCONNECTED);


        // Synchronize this on sceneRoot, so that no changes can happen to
        // the scene while this is occurring
        synchronized (this.sceneRoot) {
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
        }
        fireSceneTitleChanged(new SceneTitleChangeEvent(this, sceneTitle));
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
        if (isConnected()) {
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
        if (isConnected()) {
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
        firePlaybackChanged(new PlaybackChangeEvent(this, playbackSpeed, playing));
    }

    /**
     * Send a restart message to the server.
     */
    public void restart() {
        sendCellMessage(new RestartProgramMessage());
    }

    /**
     * Find out whether the ground plane should currently be showing.
     * @return Whether the ground plane is showing
     */
    public boolean isGroundPlaneShowing() {
        synchronized (groundPlaneLock) {
            return groundPlaneShowing;
        }
    }

    /**
     * Change the visibility of the ground plane, and propagate this change
     * to the server.
     * @param groundPlaneShowing Whether the ground plane should be showing
     */
    public void setGroundPlaneShowing(boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.setGroundPlaneShowingInternal(groundPlaneShowing);
        }
        sendCellMessage(new VisibilityChangeMessage(VisualType.GROUND, groundPlaneShowing));
    }

    /**
     * Change the visibility of the ground plane, but don't propagate the
     * change to the server (i.e. to respond to server requests).
     * @param groundPlaneShowing Whether the ground plane should be showing
     */
    private void setGroundPlaneShowingInternal(final boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.groundPlaneShowing = groundPlaneShowing;
            ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

                public void update(Object arg0) {
                    sceneRoot.updateVisibility();
                    ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                }
            }, null);
        }
        fireVisibilityChanged(new VisibilityChangeEvent(this, VisualType.GROUND, groundPlaneShowing));
    }

    /**
     * Set the root which should be used to download visuals from
     * the server; this information is attached to a SceneMessage when
     * a scene is loaded.
     * @param visualRepoRoot The root to use
     */
    private void setVisualRepoRoot(String visualRepoRoot) {
        synchronized (sceneRoot) {
            this.visualRepoRoot = visualRepoRoot;
        }
    }

    /**
     * Get the repo root which should be used to download visuals from
     * the server.
     * @return The root to use
     */
    private String getVisualRepoRoot() {
        synchronized (sceneRoot) {
            return visualRepoRoot;
        }
    }

    /**
     * Get the list of events (and responses) which this cell is listening for.
     * @return List of events we're listening for
     */
    public EventResponseList getEventList() {
        synchronized (this.eventListLock) {
            return this.eventList;
        }
    }

    /**
     * Set the list of events (and responses) which this cell should listen for,
     * and create the appropriate listeners/mechanisms (and remove old ones).
     * Propagate this change back to the server and other users.
     * @param eventList New list of events to listen for
     */
    public void setEventList(EventResponseList eventList) {
        this.setEventListInternal(eventList);
        sendCellMessage(new EventListMessage(eventList));
    }

    /**
     * Set the list of events (and responses) which this cell should listen for,
     * but do not propagate this change to the server.
     * @param eventList New list of events to listen for
     */
    private void setEventListInternal(EventResponseList eventList) {
        synchronized (this.eventListLock) {
            this.eventList = eventList;

            ///// Add necessary listeners, etc. /////

            // Only perform these actions if we know we have valid components
            if (this.isComponentsValid()) {

                for (EventResponsePair pair : eventList) {
                    // Proximity event
                    if (pair.getEvent() instanceof ProximityEvent) {
                        // Handled by server
                    } // Context menu event
                    else if (pair.getEvent() instanceof ContextMenuEvent) {
                        // Handled by context factory for this cell
                    } // Movement event
                    else if (pair.getEvent() instanceof AvatarMovementEvent) {
                        // Handled by server
                    } // Unrecognized event
                    else {
                        logger.severe("Unrecognized event: " + pair.getEvent());
                    }
                }
            }
        }
    }

    /**
     * Get the collection of responses which are available for the scene loaded
     * by this cell.
     * @return Collection of allowed responses for this cell
     */
    public Collection<CMUResponseFunction> getAllowedResponses() {
        synchronized (this.eventListLock) {
            return allowedResponses;
        }
    }

    /**
     * Set the collection of responses which are available to the scene loaded by
     * this cell.
     * @param allowedResponses New collection of allowed responses
     */
    public void setAllowedResponses(Collection<CMUResponseFunction> allowedResponses) {
        synchronized (this.eventListLock) {
            this.allowedResponses = allowedResponses;
        }
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
     * @param event The change event
     */
    private void firePlaybackChanged(PlaybackChangeEvent event) {
        synchronized (playbackChangeListeners) {
            for (PlaybackChangeListener listener : playbackChangeListeners) {
                listener.playbackChanged(event);
            }
        }
    }

    /**
     * Add a listener for ground plane changes.
     * @param listener Listener to add
     */
    public void addGroundPlaneChangeListener(VisibilityChangeListener listener) {
        synchronized (visibilityChangeListeners) {
            visibilityChangeListeners.add(listener);
        }
    }

    /**
     * Remove a listener for ground plane changes.
     * @param listener Listener to remove
     */
    public void removeVisibilityChangeListener(VisibilityChangeListener listener) {
        synchronized (playbackChangeListeners) {
            visibilityChangeListeners.remove(listener);
        }
    }

    /**
     * Send ground plane information to ground plane change listeners.
     * @param event The change event
     */
    private void fireVisibilityChanged(VisibilityChangeEvent event) {
        synchronized (visibilityChangeListeners) {
            for (VisibilityChangeListener listener : visibilityChangeListeners) {
                listener.visibilityChanged(event);
            }
        }
    }

    /**
     * Add a listener for scene title changes.
     * @param listener Listener to add
     */
    public void addSceneTitleChangeListener(SceneTitleChangeListener listener) {
        synchronized (sceneTitleChangeListeners) {
            sceneTitleChangeListeners.add(listener);
        }
    }

    /**
     * Remove a listener for scene title changes.
     * @param listener Listener to remove
     */
    public void removeSceneTitleChangeListener(SceneTitleChangeListener listener) {
        synchronized (sceneTitleChangeListeners) {
            sceneTitleChangeListeners.remove(listener);
        }
    }

    /**
     * Send scene title information to scene title listeners.
     * @param event The change event
     */
    private void fireSceneTitleChanged(SceneTitleChangeEvent event) {
        synchronized (sceneTitleChangeListeners) {
            for (SceneTitleChangeListener listener : sceneTitleChangeListeners) {
                listener.sceneTitleChanged(event);
            }
        }
    }

    /**
     * Add a listener for scene loaded changes.
     * @param listener Listener to add
     */
    public void addSceneLoadedChangeListener(SceneLoadedChangeListener listener) {
        synchronized (sceneTitleChangeListeners) {
            sceneLoadedChangeListeners.add(listener);
        }
    }

    /**
     * Remove a listener for scene loaded changes.
     * @param listener Listener to remove
     */
    public void removeSceneLoadedChangeListener(SceneLoadedChangeListener listener) {
        synchronized (sceneLoadedChangeListeners) {
            sceneLoadedChangeListeners.remove(listener);
        }
    }

    /**
     * Send scene loaded information to scene loaded listeners.
     * @param event The change event
     */
    private void fireSceneLoadedChanged(SceneLoadedChangeEvent event) {
        synchronized (sceneLoadedChangeListeners) {
            for (SceneLoadedChangeListener listener : sceneLoadedChangeListeners) {
                listener.sceneLoadedChanged(event);
            }
        }
    }

    /**
     * Add a listener for connection state changes.
     * @param listener Listener to add
     */
    public void addConnectionStateChangeListener(ConnectionStateChangeListener listener) {
        synchronized (connectionStateChangeListeners) {
            connectionStateChangeListeners.add(listener);
        }
    }

    /**
     * Remove a listener for connection state changes.
     * @param listener Listener to remove
     */
    public void removeConnectionStateChangeListener(ConnectionStateChangeListener listener) {
        synchronized (connectionStateChangeListeners) {
            connectionStateChangeListeners.remove(listener);
        }
    }

    /**
     * Send connection state information to state listeners.
     * @param event The change event
     */
    private void fireConnectionStateChanged(ConnectionStateChangeEvent event) {
        synchronized (connectionStateChangeListeners) {
            for (ConnectionStateChangeListener listener : connectionStateChangeListeners) {
                listener.connectionStateChanged(event);
            }
        }
    }
}
