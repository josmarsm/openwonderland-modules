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
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.GroundPlaneChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.RestartProgramMessage;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    private CMUCellRenderer renderer;
    private MouseEventListener mouseListener;
    private float playbackSpeed;
    private boolean playing;
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
                CMUCell.this.setServerAndPort(changeMessage.getServer(), changeMessage.getPort());
            }

            // Playback speed message
            if (PlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())) {
                if (message.getSenderID() == null || !(message.getSenderID().equals(getCellCache().getSession().getID()))) {
                    PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                    setPlayingWithoutUpdate(change.isPlaying());
                    setPlaybackSpeedWithoutUpdate(change.getPlaybackSpeed());
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
                }

                // New visual
                if (VisualMessage.class.isAssignableFrom(message.getClass())) {
                    this.applyVisualMessage((VisualMessage) message);
                }

                // Visual deleted
                if (VisualDeletedMessage.class.isAssignableFrom(message.getClass())) {
                    this.applyVisualDeletedMessage((VisualDeletedMessage) message);
                }
            }
        }
    }

    private void applyTransformationMessage(TransformationMessage message) {
        this.sceneRoot.applyTransformationToChild(message);
    }

    private void applyVisualMessage(VisualMessage message) {
        sceneRoot.attachChild(new VisualNode(message));

        // Filter ground plane
        if (NodeNameClassifier.isGroundPlaneName(message.getName())) {
            synchronized (this.groundPlaneIDs) {
                groundPlaneIDs.add(message.getNodeID());
            }
            sceneRoot.applyVisibilityToChild(message.getNodeID(), isGroundPlaneShowing());
        }
    }

    private void applyVisualDeletedMessage(VisualDeletedMessage message) {
        sceneRoot.removeChild(message);
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
    public void setServerAndPort(String server, int port) {
        // Synchronize this on sceneRoot, so that no changes can
        synchronized (this.sceneRoot) {
            this.disconnect();
            cmuConnectionThread = new VisualChangeReceiverThread(this, server, port);
            cmuConnectionThread.start();
        }
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
        synchronized (playbackSpeedLock) {
            setPlaybackSpeedWithoutUpdate(playbackSpeed);
            this.sendPlaybackData();
        }
    }

    /**
     * Set playback speed.
     * @param playbackSpeed The speed at which the associated CMU scene is playing.
     */
    private void setPlaybackSpeedWithoutUpdate(float playbackSpeed) {
        synchronized (playbackSpeedLock) {
            this.playbackSpeed = playbackSpeed;
            firePlaybackChanged();
        }
    }

    public void setPlaying(boolean playing) {
        synchronized (playbackSpeedLock) {
            setPlayingWithoutUpdate(playing);
            this.sendPlaybackData();
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

    private void setPlayingWithoutUpdate(boolean playing) {
        synchronized (playbackSpeedLock) {
            this.playing = playing;
            firePlaybackChanged();
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
        this.setPlayingWithoutUpdate(cmuClientState.isPlaying());
        this.setPlaybackSpeedWithoutUpdate(cmuClientState.getPlaybackSpeed());
        if (cmuClientState.isServerAndPortInitialized()) {
            this.setServerAndPort(cmuClientState.getServer(), cmuClientState.getPort());
        }
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
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);

        switch (status) {
            case DISK:
                //TODO: Weird stuff happening when going to disk and back
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
                break;

            case INACTIVE:
                if (increasing) {
                    if (channel != null) {
                        channel.addMessageReceiver(ConnectionChangeMessage.class, messageReceiver);
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
                break;

            case ACTIVE:
                // Add mouse listener.
                if (mouseListener == null) {
                    mouseListener = new MouseEventListener();
                    mouseListener.addToEntity(renderer.getEntity());
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
        System.out.println("Sending restart message...");
        sendCellMessage(new RestartProgramMessage(getCellID()));
    }

    protected void setHUDShowing(boolean showing) {
        Runnable toRun;
        if (showing) {
            if (hudComponent == null) {
                // Set up the UI
                toRun = new Runnable() {

                    public void run() {
                        // Create the panel
                        hudPanel = new CMUJPanel(CMUCell.this);

                        // Create the HUD component
                        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                        hudComponent = mainHUD.createComponent(hudPanel);
                        hudComponent.setPreferredTransparency(0.0f);
                        hudComponent.setName("CMU Player");
                        hudComponent.setPreferredLocation(Layout.NORTHWEST);
                        mainHUD.addComponent(hudComponent);
                        hudComponent.setVisible(true);
                    }
                };
            } else {
                toRun = new Runnable() {

                    public void run() {
                        hudComponent.setVisible(true);
                    }
                };
            }
        } else {
            toRun = new Runnable() {

                public void run() {
                    hudComponent.setVisible(false);
                }
            };
        }

        SwingUtilities.invokeLater(toRun);

        synchronized (hudShowingLock) {
            this.hudShowing = showing;
        }
    }

    protected boolean isHUDShowing() {
        synchronized (hudShowingLock) {
            return hudShowing;
        }
    }
}
