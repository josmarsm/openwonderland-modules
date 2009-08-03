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
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.PlaybackDefaults;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.MouseButtonEventMessage;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    private CMUCellRenderer renderer;
    private MouseEventListener listener;
    private float playbackSpeed;
    private final Object playbackSpeedLock = new Object();
    private final VisualParent sceneRoot = new VisualParent();      // We synchronize incoming visual changes and connection changes on this object.
    private VisualChangeReceiverThread cmuConnectionThread = null;
    private final CMUCellMessageReceiver messageReceiver = new CMUCellMessageReceiver();

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
         * {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            sendCellMessage(new PlaybackSpeedChangeMessage(getCellID(), getPlaybackSpeed()));

            //TODO: send these more discriminately
            sendCellMessage(new MouseButtonEventMessage(getCellID()));//, mbe));
        }

        /**
         * Toggle play/pause on left click.
         * @param event {@inheritDoc}
         */
        @Override
        public void computeEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            //TODO: More complete playback speed functionality.
            // Toggle play/pause on click.
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
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
                if (!(message.getSenderID().equals(getCellCache().getSession().getID()))) {
                    PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                    setPlaybackSpeed(change.getPlaybackSpeed());
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
                    this.applyVisualDeletedMessage((VisualDeletedMessage)message);
                }
            }
        }
    }

    private void applyTransformationMessage(TransformationMessage message) {
        this.sceneRoot.applyTransformationToChild(message);
    }

    private void applyVisualMessage(VisualMessage message) {
        // Filter out ground plane
        if (!NodeNameClassifier.isGroundPlaneName(message.getName())) {
            sceneRoot.attachChild(new VisualNode(message));
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
        synchronized(this.sceneRoot) {
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
        synchronized(this.sceneRoot) {
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
            this.setPlaybackSpeed(PlaybackDefaults.DEFAULT_START_SPEED);
        }
    }

    /**
     * Get playback speed.
     * @return The speed at which the associated CMU scene is playing.
     */
    public float getPlaybackSpeed() {
        synchronized (playbackSpeedLock) {
            return this.playbackSpeed;
        }
    }

    /**
     * Set playback speed.
     * @param playbackSpeed The speed at which the associated CMU scene is playing.
     */
    public void setPlaybackSpeed(float playbackSpeed) {
        synchronized (playbackSpeedLock) {
            this.playbackSpeed = playbackSpeed;
        }
    }

    /**
     * True if the program is playing at any speed, i.e. not paused.
     * @return True if the program is playing at any speed
     */
    public boolean isPlaying() {
        return (getPlaybackSpeed() != PlaybackDefaults.PAUSE_SPEED);
    }

    /**
     * Just set the playback speed to the standard playback speed.
     */
    public void play() {
        this.setPlaybackSpeed(PlaybackDefaults.DEFAULT_PLAYBACK_SPEED);
    }

    /**
     * Just set the playback speed to the standard paused speed.
     */
    public void pause() {
        this.setPlaybackSpeed(PlaybackDefaults.PAUSE_SPEED);
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
                // Remove mouse listener.
                if (listener != null) {
                    listener.removeFromEntity(renderer.getEntity());
                    listener = null;
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

                // Stop receiving playback speed changes.
                if (!increasing) {
                    if (channel != null) {
                        channel.removeMessageReceiver(PlaybackSpeedChangeMessage.class);
                    }
                }
                break;

            case ACTIVE:
                // Add mouse listener.
                if (listener == null) {
                    listener = new MouseEventListener();
                    listener.addToEntity(renderer.getEntity());
                }

                // Start receiving playback speed changes.
                if (increasing) {
                    if (channel != null) {
                        channel.addMessageReceiver(PlaybackSpeedChangeMessage.class, messageReceiver);
                    }
                }
                break;

            default:
                break;
        }
    }

    //TODO: Show HUD
    protected void showHUD() {
    }

    protected void hideHUD() {
    }
}
