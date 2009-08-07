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

import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import com.jme.scene.Node;
import java.util.Collection;
import java.util.Vector;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
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

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell implements HUDEventListener {

    private CMUCellRenderer renderer;
    private MouseEventListener mouseListener;
    private boolean sceneLoaded = false;
    private float playbackSpeed;
    private boolean playing;
    private String sceneTitle;
    private final Object playbackSpeedLock = new Object();
    private boolean groundPlaneShowing;
    private final Object groundPlaneLock = new Object();
    private final Collection<NodeID> groundPlaneIDs = new Vector<NodeID>();
    private final VisualParent sceneRoot = new VisualParent();      // We synchronize incoming visual changes and connection changes on this object.
    private VisualChangeReceiverThread cmuConnectionThread = null;
    private final CMUCellMessageReceiver messageReceiver = new CMUCellMessageReceiver();
    private final Collection<PlaybackChangeListener> playbackChangeListeners = new Vector<PlaybackChangeListener>();
    private final Collection<GroundPlaneChangeListener> groundPlaneChangeListeners = new Vector<GroundPlaneChangeListener>();
    private CMUJPanel hudPanel;
    private HUDComponent hudComponent;
    private boolean hudShowing = false;
    private final Object hudShowingLock = new Object();

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
         * {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            setHUDShowing(!isHUDShowing());
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
            // Socket information message
            if (ConnectionChangeMessage.class.isAssignableFrom(message.getClass())) {
                ConnectionChangeMessage changeMessage = (ConnectionChangeMessage) message;
                CMUCell.this.setHostnameAndPort(changeMessage.getServer(), changeMessage.getPort());
            }

            // Playback speed message
            if (PlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())) {
                if (message.getSenderID() == null || !(message.getSenderID().equals(getCellCache().getSession().getID()))) {
                    PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                    //System.out.println("Setting playback: " + change.getPlaybackSpeed() + ", " + change.isPlaying());
                    setPlaybackInformationWithoutUpdate(change.getPlaybackSpeed(), change.isPlaying());
                }
            }

            // Ground plane visibility change message
            if (GroundPlaneChangeMessage.class.isAssignableFrom(message.getClass())) {
                if (message.getSenderID() == null || !(message.getSenderID().equals(getCellCache().getSession().getID()))) {
                    GroundPlaneChangeMessage change = (GroundPlaneChangeMessage) message;
                    setGroundPlaneShowingWithoutUpdate(change.isGroundPlaneShowing());
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

    private void applyTransformationMessage(TransformationMessage message) {
        synchronized (sceneRoot) {
            if (this.isSceneLoaded()) {
                this.sceneRoot.applyTransformationToChild(message);
            }
        }
    }

    private void applyVisualMessage(VisualMessage message) {
        synchronized (sceneRoot) {
            sceneRoot.attachChild(new VisualNode(message));

            // Filter ground plane
            if (NodeNameClassifier.isGroundPlaneName(message.getName())) {
                synchronized (this.groundPlaneIDs) {
                    groundPlaneIDs.add(message.getNodeID());
                }
                sceneRoot.applyVisibilityToChild(message.getNodeID(), isGroundPlaneShowing());
            }
        }
    }

    private void applyVisualDeletedMessage(VisualDeletedMessage message) {
        sceneRoot.removeChild(message);
    }

    private void applySceneMessage(SceneMessage message) {
        synchronized (sceneRoot) {
            this.setSceneLoaded(true);
            for (VisualMessage visual : message.getVisuals()) {
                applyVisualMessage(visual);
            }
        }
    }

    private void applyUnloadSceneMessage(UnloadSceneMessage message) {
        synchronized (sceneRoot) {
            this.disconnect();
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
            return this.cmuConnectionThread == receiver;
        }

    }

    public boolean isSceneLoaded() {
        synchronized (sceneRoot) {
            return sceneLoaded;
        }

    }

    public void setSceneLoaded(boolean sceneLoaded) {
        synchronized (sceneRoot) {
            this.sceneLoaded = sceneLoaded;
        }

    }

    /**
     * Called by threads interacting with this cell to mark that their connection
     * has been lost.
     * @param receiver The thread whose connection has been lost
     */
    public void markDisconnected(VisualChangeReceiverThread receiver) {
        synchronized (this.sceneRoot) {
            if (allowsUpdatesFrom(receiver)) {
                this.disconnect();
            }

        }
    }

    /**
     * Called by threads interacting with this cell to mark that their connection
     * has been obtained.
     * @param receiver The thread whose connection has been obtained
     */
    public void markConnected(VisualChangeReceiverThread receiver) {
        synchronized (this.sceneRoot) {
            if (allowsUpdatesFrom(receiver)) {
                this.markConnected();
            }

        }
    }

    /**
     * Prevent any further incoming changes from the current connection
     * thread, and detach the scene which it had set up, replacing it
     * with a disconnect message.
     */
    private void disconnect() {
        synchronized (this.sceneRoot) {
            this.setSceneLoaded(false);
            this.cmuConnectionThread = null;

            //TODO: Show something noting that the scene is disconnected.
            this.sceneRoot.detachAllChildren();
        }

    }

    /**
     * Clean up any disconnect messages that had been placed in the scene,
     * to prepare for incoming visuals via a valid connection.
     */
    private void markConnected() {
        this.sceneRoot.detachAllChildren();
    }

    /**
     * Set the server and port that will be used to connect to a CMU instance.
     * If we are already connected somewhere, disconnect and reestablish the
     * connection at the new location.
     * @param server The host address to connect to
     * @param port The port to connect to
     */
    public void setHostnameAndPort(String server, int port) {
        // Synchronize this on sceneRoot, so that no changes can happen to
        // the scene while this is occurring
        synchronized (this.sceneRoot) {
            this.disconnect();
            cmuConnectionThread =
                    new VisualChangeReceiverThread(this, server, port);
            cmuConnectionThread.start();
        }

    }

    public String getSceneTitle() {
        return sceneTitle;
    }

    public void setSceneTitle(String sceneTitle) {
        this.sceneTitle = sceneTitle;
    }

    /**
     * Get the stored playback speed, i.e. the playback speed that will
     * be used as long as this scene isn't paused.
     * @return The speed at which the associated CMU scene is playing.
     */
    public float getPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return this.playbackSpeed;
        }

    }

    public void setPlaybackSpeed(float playbackSpeed) {
        if (isSceneLoaded()) {
            synchronized (playbackSpeedLock) {
                setPlaybackInformationWithoutUpdate(playbackSpeed, this.isPlaying());
                this.sendPlaybackData();
            }
        }
    }

    /**
     * Set playback speed and play/pause state without propagating this change
     * to the server (i.e. to respond to server commands).
     * @param playbackSpeed The speed at which the associated CMU scene is playing
     * @param playing The play/pause state of the scene
     */
    private void setPlaybackInformationWithoutUpdate(float playbackSpeed, boolean playing) {
        synchronized (playbackSpeedLock) {
            this.playing = playing;
            this.playbackSpeed = playbackSpeed;
            firePlaybackChanged();
        }


    }

    /**
     * Change the play/pause state of the scene, and send an update to the server
     * noting that this has been done.
     * @param playing The play/pause state of the scene
     */
    public void setPlaying(boolean playing) {
        if (isSceneLoaded()) {
            synchronized (playbackSpeedLock) {
                setPlaybackInformationWithoutUpdate(this.getPlaybackSpeed(), playing);
                this.sendPlaybackData();
            }
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
     * Get the root of the CMU scene represented by this node.
     * @return The root of the CMU scene as a jME node
     */
    public Node getSceneRoot() {
        return this.sceneRoot;
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
        this.setPlaybackInformationWithoutUpdate(cmuClientState.getPlaybackSpeed(), cmuClientState.isPlaying());
        if (cmuClientState.isServerAndPortInitialized()) {
            this.setHostnameAndPort(cmuClientState.getServer(), cmuClientState.getPort());
        }
        this.setSceneTitle(cmuClientState.getSceneTitle());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(
            RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new CMUCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }

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

                // Stop receiving connection changes; they'll be loaded
                // from the client state later if necessary.
                if (!increasing) {
                    if (channel != null) {
                        channel.removeMessageReceiver(ConnectionChangeMessage.class);
                    }
                }

                // Stop receiving playback speed changes
                if (!increasing) {
                    if (channel != null) {
                        channel.removeMessageReceiver(PlaybackSpeedChangeMessage.class);
                    }
                }

                // Stop receiving ground plane changes
                if (!increasing) {
                    if (channel != null) {
                        channel.removeMessageReceiver(GroundPlaneChangeMessage.class);
                    }
                }

                // Clean up HUD and network stuff
                if (!increasing) {
                    unloadHUD();
                    disconnect();
                }

                break;

            case INACTIVE:
                if (increasing) {
                    if (channel != null) {
                        channel.addMessageReceiver(ConnectionChangeMessage.class, messageReceiver);
                    }
                }

                // Start receiving playback speed changes.
                if (increasing) {
                    if (channel != null) {
                        channel.addMessageReceiver(PlaybackSpeedChangeMessage.class, messageReceiver);
                    }
                }

                // Start receiving ground plane changes.
                if (increasing) {
                    if (channel != null) {
                        channel.addMessageReceiver(GroundPlaneChangeMessage.class, messageReceiver);
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

    public void addPlaybackChangeListener(PlaybackChangeListener listener) {
        synchronized (this.playbackChangeListeners) {
            playbackChangeListeners.add(listener);
            listener.playbackChanged(new PlaybackChangeEvent(this.getPlaybackSpeed(), this.isPlaying()));
        }
    }

    public void removePlaybackChangeListener(PlaybackChangeListener listener) {
        synchronized (playbackChangeListeners) {
            playbackChangeListeners.remove(listener);
        }
    }

    private void firePlaybackChanged() {
        PlaybackChangeEvent event;
        synchronized (playbackSpeedLock) {
            event = new PlaybackChangeEvent(this.getPlaybackSpeed(), this.isPlaying());
        }

        synchronized (playbackChangeListeners) {
            for (PlaybackChangeListener listener : playbackChangeListeners) {
                listener.playbackChanged(event);
            }
        }
    }

    public void addGroundPlaneChangeListener(GroundPlaneChangeListener listener) {
        synchronized (groundPlaneChangeListeners) {
            groundPlaneChangeListeners.add(listener);
            listener.groundPlaneChanged(new GroundPlaneChangeEvent(this.isGroundPlaneShowing()));
        }

    }

    public void removeGroundPlaneChangeListener(PlaybackChangeListener listener) {
        synchronized (playbackChangeListeners) {
            playbackChangeListeners.remove(listener);
        }

    }

    private void fireGroundPlaneChanged() {
        GroundPlaneChangeEvent event;
        synchronized (groundPlaneLock) {
            event = new GroundPlaneChangeEvent(this.isGroundPlaneShowing());
        }

        synchronized (groundPlaneChangeListeners) {
            for (GroundPlaneChangeListener listener : groundPlaneChangeListeners) {
                listener.groundPlaneChanged(event);
            }

        }
    }

    public boolean isGroundPlaneShowing() {
        synchronized (groundPlaneLock) {
            return groundPlaneShowing;
        }

    }

    public void setGroundPlaneShowing(boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.setGroundPlaneShowingWithoutUpdate(groundPlaneShowing);
            sendGroundPlaneData();
        }
    }

    private void setGroundPlaneShowingWithoutUpdate(boolean groundPlaneShowing) {
        synchronized (groundPlaneLock) {
            this.groundPlaneShowing = groundPlaneShowing;
            synchronized (groundPlaneIDs) {
                for (NodeID id : groundPlaneIDs) {
                    sceneRoot.applyVisibilityToChild(id, groundPlaneShowing);
                }
            }
            fireGroundPlaneChanged();
        }

    }

    private void sendPlaybackData() {
        sendCellMessage(new PlaybackSpeedChangeMessage(getCellID(), getPlaybackSpeed(), isPlaying()));
    }

    private void sendGroundPlaneData() {
        sendCellMessage(new GroundPlaneChangeMessage(getCellID(), isGroundPlaneShowing()));
    }

    public void restart() {
        sendCellMessage(new RestartProgramMessage(getCellID()));
    }

    private class HUDDisplayer implements Runnable {

        private final boolean showing;

        public HUDDisplayer(boolean showing) {
            this.showing = showing;
        }

        @Override
        public void run() {
            synchronized (hudShowingLock) {
                // Set up UI
                if (showing && hudComponent == null) {
                    // Create the panel
                    hudPanel = new CMUJPanel(CMUCell.this);

                    // Create the HUD component
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                    assert mainHUD != null;
                    hudComponent = mainHUD.createComponent(hudPanel);
                    hudComponent.setPreferredTransparency(0.0f);
                    hudComponent.setName(getSceneTitle());
                    hudComponent.setPreferredLocation(Layout.NORTHWEST);
                    hudComponent.addEventListener(CMUCell.this);
                    mainHUD.addComponent(hudComponent);
                }
                if (hudComponent != null) {
                    hudComponent.setVisible(showing);
                }
                hudShowing = showing;
            }
        }
    }

    private class HUDKiller extends HUDDisplayer {
        public HUDKiller() {
            super(false);
        }

        @Override
        public void run() {
            synchronized(hudShowingLock) {
                super.run();

                //TODO: Remove from HUD manager?
                hudComponent = null;
            }
        }
    }

    protected void setHUDShowing(boolean showing) {
        SwingUtilities.invokeLater(new HUDDisplayer(showing));
    }

    protected boolean isHUDShowing() {
        synchronized (hudShowingLock) {
            return hudShowing;
        }
    }

    private void unloadHUD() {
        SwingUtilities.invokeLater(new HUDKiller());
    }

    public void HUDObjectChanged(HUDEvent event) {
        synchronized (hudShowingLock) {
            if (event.getObject().equals(this.hudComponent)) {
                if (event.getEventType().equals(HUDEventType.DISAPPEARED) || event.getEventType().equals(HUDEventType.CLOSED)) {
                    this.setHUDShowing(false);
                }
            }
        }
    }
}
