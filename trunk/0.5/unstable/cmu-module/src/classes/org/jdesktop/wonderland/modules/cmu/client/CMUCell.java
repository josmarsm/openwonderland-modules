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

import org.jdesktop.wonderland.modules.cmu.common.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.TransformationMessage;
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
import org.jdesktop.wonderland.modules.cmu.common.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.ConnectionChangeMessage;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    private CMUCellRenderer renderer;
    private MouseEventListener listener;
    private boolean playbackMessageReceiverAdded = false;
    private boolean connectionMessageReceiverAdded = false;
    private final TransformableParent sceneRoot = new TransformableParent();
    private VisualChangeReceiver cmuConnectionThread = null;
    private final CMUCellMessageReceiver messageReceiver = new CMUCellMessageReceiver();

    private class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
        /*
        CMUCellChangeMessage msg = new CMUCellChangeMessage(getCellID(), getPlaybackSpeed());
        sendCellMessage(msg);
         */
        }

        @Override
        public void computeEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
        /*
        if (program.isPlaying()) {
        program.pause();
        } else {
        program.play();
        }
         * */
        }
    }

    private class CMUCellMessageReceiver implements ComponentMessageReceiver {

        /**
         * Process messages sent by the managed object on the server;
         * these messages can update connection information for this cell
         * (ConnectionChangeMessage), or change the displayed playback speed
         * (PlaybackSpeedChangeMessage).
         * @param message Message sent by server
         */
        public void messageReceived(CellMessage message) {

            System.out.println("Client-side message received: " + message);

            // Socket information message
            if (ConnectionChangeMessage.class.isAssignableFrom(message.getClass())) {
                ConnectionChangeMessage changeMessage = (ConnectionChangeMessage) message;
                CMUCell.this.setServerAndPort(changeMessage.getServer(), changeMessage.getPort());
            }

            // Playback speed message
            if (PlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())) {
                PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
            //TODO: process playback speed changes.
            }
        }
    }

    /**
     * Thread to process incoming scene graph changes from a CMU instance.
     * Establishes a connection with the instance, and forwards messages
     * sent by the instance to the cell's scene graph.
     */
    private class VisualChangeReceiver extends Thread {

        private final String server;
        private final int port;

        /**
         * Standard constructor.
         * @param server The server on which the CMU instance is running.
         * @param port The port on which the CMU instance is running.
         */
        public VisualChangeReceiver(String server, int port) {
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
                System.out.println("Connecting cell....");

                ObjectInputStream fromServer;
                Socket connection = new Socket(server, port);

                System.out.println("Cell connected: " + server + ":" + port);

                fromServer = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    Object received = fromServer.readObject();
                    System.out.println("Received object: " + received);

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
     * Get the root of the CMU scene represented by this node.
     * @return The root of the CMU scene as a jME node.
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
            System.out.println("Setting server and port on CMUCell");
            cmuConnectionThread = new VisualChangeReceiver(server, port);
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

        System.out.println("\n\n\n\nSETTING STATUS: " + status + ", " + increasing + "\n\n\n\n");
        ChannelComponent channel = getComponent(ChannelComponent.class);

        //TODO: We always want to listen for connection changes; find out how to do this before setStatus is called.
        if (!this.connectionMessageReceiverAdded) {
            connectionMessageReceiverAdded = true;
            channel.addMessageReceiver(ConnectionChangeMessage.class, messageReceiver);
        }

        switch (status) {
            case DISK:
                if (listener != null) {
                    listener.removeFromEntity(renderer.getEntity());
                    listener = null;
                }

                // Don't care about playback speed changes at this point.
                //TODO: Eliminate need for the boolean.
                if (playbackMessageReceiverAdded) {
                    if (channel != null) {
                        channel.removeMessageReceiver(PlaybackSpeedChangeMessage.class);
                    }
                    playbackMessageReceiverAdded = false;
                }
                break;

            case ACTIVE:

                if (listener == null) {
                    listener = new MouseEventListener();
                    listener.addToEntity(renderer.getEntity());
                }
                if (!playbackMessageReceiverAdded) {
                    if (channel != null) {
                        channel.addMessageReceiver(PlaybackSpeedChangeMessage.class, messageReceiver);
                    }
                    playbackMessageReceiverAdded = true;
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
