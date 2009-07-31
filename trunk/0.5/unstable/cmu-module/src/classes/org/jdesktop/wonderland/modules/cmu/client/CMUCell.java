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

import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import java.io.IOException;
import java.net.UnknownHostException;
import com.jme.scene.Node;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.TransformableParent;
import org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer.VisualNode;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.PlaybackDefaults;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    private CMUCellRenderer renderer;
    private MouseEventListener listener;
    private float playbackSpeed;
    private final Object playbackSpeedLock = new Object();
    private boolean playbackMessageReceiverAdded = false;
    private boolean connectionMessageReceiverAdded = false;
    private final TransformableParent sceneRoot = new TransformableParent();
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
            if (PlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())
                    && !(message.getCellID().equals(CMUCell.this.getCellID()))) {
                PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
                setPlaybackSpeed(change.getPlaybackSpeed());
            }
        }
    }

    /**
     * Thread to process incoming scene graph changes from a CMU instance.
     * Establishes a connection with the instance, and forwards messages
     * sent by the instance to the cell's scene graph.
     */
    private class VisualChangeReceiverThread extends Thread {

        private final String server;
        private final int port;

        /**
         * Standard constructor.
         * @param server The server on which the CMU instance is running.
         * @param port The port on which the CMU instance is running.
         */
        public VisualChangeReceiverThread(String server, int port) {
            super();
            this.server = server;
            this.port = port;
        }

        /**
         * Create a connection to the CMU instance, and wait for incoming
         * messages; these can be either transformation updates, or information
         * about new nodes.
         */
        @Override
        public void run() {
            try {
                ObjectInputStream fromServer;
                Socket connection = new Socket(server, port);

                // Get incoming stream from server
                fromServer = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    // Read messages as long as they're being sent
                    Object received = fromServer.readObject();

                    // Transformation for existing visual
                    if (TransformationMessage.class.isAssignableFrom(received.getClass())) {
                        sceneRoot.applyTransformationToChild((TransformationMessage) received);
                    }

                    // New visual
                    if (VisualMessage.class.isAssignableFrom(received.getClass())) {
                        VisualNode newNode = new VisualNode((VisualMessage) received);
                        synchronized (sceneRoot) {
                            sceneRoot.attachChild(newNode);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, null, ex);
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
     * Set the server and port that will be used to connect to a CMU instance.
     * This method should be called only once per cell.
     * @param server
     * @param port
     */
    public void setServerAndPort(String server, int port) {
        // Don't let the socket information be changed after it has been initialized.
        if (cmuConnectionThread == null) {
            cmuConnectionThread = new VisualChangeReceiverThread(server, port);
            cmuConnectionThread.start();
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

                // Stop receiving connection changes.
                if (!increasing) {
                    if (channel != null) {
                        channel.removeMessageReceiver(ConnectionChangeMessage.class);
                    }
                }
                break;

            case INACTIVE:
                //TODO: We always want to listen for connection changes; find out how to do this before setStatus is called?
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
