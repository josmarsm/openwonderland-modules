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
package org.jdesktop.wonderland.modules.cmu.player.connections;

import edu.cmu.cs.dennisc.scenegraph.event.ChildAddedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildRemovedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildrenListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.AbstractCamera;
import org.alice.apis.moveandturn.Model;
import org.alice.apis.moveandturn.Scene;
import org.alice.apis.moveandturn.Transformable;
import org.jdesktop.wonderland.common.NetworkAddress;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.ModelPropertyMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.SceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.UnloadSceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.player.ModelPropertyMessageListener;
import org.jdesktop.wonderland.modules.cmu.player.ModelWrapper;
import org.jdesktop.wonderland.modules.cmu.player.TransformationMessageListener;

/**
 * Wraps a CMU Scene object to extract transformation/geometry information
 * from its nodes.  Also sets up connections with clients wishing to receive
 * scene updates, and interacts with visual wrappers to send these updates
 * as necessary.
 * @author kevin
 */
public class SceneConnectionHandler implements ChildrenListener, TransformationMessageListener, ModelPropertyMessageListener {

    public final int DEFAULT_FPS = 30;
    private Scene sc = null;       // The scene to wrap.
    private final Set<ClientConnection> connections = new HashSet<ClientConnection>();
    private final Map<NodeID, ModelWrapper> visuals = new HashMap<NodeID, ModelWrapper>();
    private final ConnectionHandlerThread handlerThread;

    /**
     * Thread to set up a ServerSocket and listen for incoming connections
     * from clients, and then handle them appropriately.
     */
    private class ConnectionHandlerThread extends Thread {

        private ServerSocket socketListener = null;
        private final Object socketListenerLock = new Object();

        /**
         * Standard constructor.
         */
        public ConnectionHandlerThread() {
            super();

            // Initialize connection listener.
            synchronized (socketListenerLock) {
                try {
                    socketListener = new ServerSocket();
                    socketListener.bind(null);
                } catch (IOException ex) {
                    Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.setName("CMU Connection Handler " + socketListener);
        }

        /**
         * Listen for incoming connections and add them to the connection
         * list as they arrive.
         */
        @Override
        public void run() {
            try {
                Socket incomingConnection = null;
                while (true) {
                    // Accept client connections and add them.
                    incomingConnection = socketListener.accept();
                    Logger.getLogger(SceneConnectionHandler.class.getName()).info("Connection accepted: " + incomingConnection);
                    addConnection(incomingConnection);
                }
            } catch (IOException ex) {
                Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Get the port set up by this thread's ServerSocket.
         * @return The port used to connect to this thread
         */
        public int getPort() {
            synchronized (socketListenerLock) {
                assert socketListener != null;
                return socketListener.getLocalPort();
            }
        }

        /**
         * Get the hostname for this thread's ServerSocket.
         * @return The hostname used to connect to this thread
         */
        public String getHostname() {
            synchronized (socketListenerLock) {
                assert socketListener != null;
                try {
                    return NetworkAddress.getPrivateLocalAddress().getHostAddress();
                } catch (UnknownHostException ex) {
                    Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        }
    }

    /**
     * Basic constructor; creates a ConnectionHandlerThread immediately.
     */
    public SceneConnectionHandler() {
        handlerThread = new ConnectionHandlerThread();
        handlerThread.start();
    }

    /**
     * Creates a ConnectionHandlerThread, and wraps/parses the given scene.
     * @param sc The scene to wrap
     */
    public SceneConnectionHandler(Scene sc) {
        this();
        this.setScene(sc);
    }

    /**
     * Get the port used to connect to this scene.
     * @return The port on which this scene is listening for connections
     */
    public int getPort() {
        return this.handlerThread.getPort();
    }

    /**
     * Get the address used to connect to this scene.
     * @return The address on which this scene is listening for connections
     */
    public String getHostname() {
        return this.handlerThread.getHostname();
    }

    /**
     * Get the wrapped scene.
     * @return The wrapped scene
     */
    public Scene getScene() {
        return sc;
    }

    /**
     * Set the wrapped scene, and parse it to extract the JME nodes.
     * Also clean up data from any existing scene.
     * @param sc The scene to wrap
     */
    public synchronized void setScene(Scene sc) {
        this.unloadScene();
        if (sc != null) {
            this.sc = sc;
            this.processModel(sc);
        }
    }

    public void click(NodeID id) {
        ModelWrapper model = visuals.get(id);
        if (model != null) {
            model.click();
        }
    }

    private synchronized void processModel(org.alice.apis.moveandturn.Composite c) {
        assert c != null;

        //TODO: Process camera
        if (c instanceof AbstractCamera) {
            System.out.println("Camera: " + c);
        }

        if (c instanceof Model) {
            synchronized (connections) {
                addModel((Model) c);
            }
        }
        for (Transformable child : c.getComponents()) {
            processModel(child);
        }
    }

    /**
     * Add the given Socket as a Connection, and use it to send the current
     * state of the scene.
     * @param connection The connection to add
     */
    protected void addConnection(Socket connection) {
        SceneMessage message = null;
        ClientConnection newConnection = null;

        // Don't add any new visuals until after this connection has been added
        // to the list and received all the current visuals; otherwise, this
        // connection could fail to receive the new visuals added.
        synchronized (visuals) {
            // Create the scene message with all current visuals (might be none)
            Collection<VisualMessage> visualMessages = new Vector<VisualMessage>();
            for (ModelWrapper visual : this.visuals.values()) {
                visualMessages.add(visual.getVisualMessage());
            }
            message = new SceneMessage(visualMessages, VisualUploadManager.getUsername());

            // Store the connection.
            newConnection = new ClientConnection(this, DEFAULT_FPS, connection);
            synchronized (connections) {
                connections.add(newConnection);
            }
            newConnection.start();
        }

        // Broadcast setup data to this connection.
        newConnection.queueMessage(message);
    }

    /**
     * Synchronously remove the given connection from our collection,
     * and perform any necessary cleanup.
     * @param connection The connection to remove
     */
    protected void handleSocketException(ClientConnection connection, SocketException ex) {
        try {
            connection.close();
        } catch (SocketException ex1) {
            // No action, we're already closing
        }
        synchronized (connections) {
            if (connections.contains(connection)) {
                Logger.getLogger(SceneConnectionHandler.class.getName()).info("Closing connection: " + connection);
                connections.remove(connection);
            }
        }
    }

    /**
     * Create a wrapper for the given CMU visual, and broadcast its addition
     * to any connected clients.
     * @param model The CMU visual to add
     */
    protected void addModel(Model model) {
        synchronized (visuals) {
            synchronized (connections) {
                // Create and store a wrapper for this Visual.
                ModelWrapper visualWrapper = new ModelWrapper(model);
                this.visuals.put(visualWrapper.getNodeID(), visualWrapper);
                visualWrapper.addTransformationMessageListener(this);
                visualWrapper.addPropertiesMessageListener(this);
                VisualUploadManager.uploadVisual(visualWrapper.getVisualAttributes());

                // Broadcast it to each connected client.  Don't allow new
                // connections to be created during this process; this
                // would have this visual sent twice to these connections.
                Iterator<ClientConnection> iterator = connections.iterator();
                while (iterator.hasNext()) {
                    ClientConnection connection = iterator.next();
                    connection.queueMessage(visualWrapper.getVisualMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transformationMessageChanged(TransformationMessage message) {
        synchronized (connections) {
            for (ClientConnection connection : connections) {
                connection.queueMessage(message);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void modelPropertyMessageChanged(ModelPropertyMessage message) {
        synchronized (connections) {
            for (ClientConnection connection : connections) {
                connection.queueMessage(message);
            }
        }
    }

    /**
     * Unload the current scene, and inform all connected clients that this
     * is happening.
     */
    public void unloadScene() {
        if (this.getScene() != null) {
            synchronized (connections) {
                for (ClientConnection connection : connections) {
                    connection.queueMessage(new UnloadSceneMessage());
                }
            }
            synchronized (visuals) {
                for (ModelWrapper visual : visuals.values()) {
                    // Clean up visuals individually
                    visual.unload();
                    visual.removeTransformationMessageListener(this);
                    visual.removePropertiesMessageListener(this);
                }
                // Remove visuals collectively
                visuals.clear();
            }
        }
    }

    //TODO: Listen for scene graph changes.
    public void childAdded(ChildAddedEvent childrenEvent) {
        printChildWarning();
        System.out.println("added: " + childrenEvent);
    }

    public void childRemoved(ChildRemovedEvent childrenEvent) {
        printChildWarning();
        System.out.println("removed: " + childrenEvent);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void printChildWarning() {
        System.out.println("Child added or removed");
    }
}
