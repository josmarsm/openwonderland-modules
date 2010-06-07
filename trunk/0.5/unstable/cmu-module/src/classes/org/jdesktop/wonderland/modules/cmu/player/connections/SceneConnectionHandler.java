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
package org.jdesktop.wonderland.modules.cmu.player.connections;

import edu.cmu.cs.dennisc.scenegraph.event.ChildAddedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildRemovedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildrenListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.AbstractCamera;
import org.alice.apis.moveandturn.Model;
import org.alice.apis.moveandturn.Scene;
import org.alice.apis.moveandturn.Transformable;
import org.jdesktop.wonderland.common.NetworkAddress;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.SceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.UnloadSceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.player.conversions.scenegraph.ModelConverter;
import org.jdesktop.wonderland.modules.cmu.player.NodeUpdateListener;

/**
 * Wraps a CMU Scene object to extract transformation/geometry information
 * from its nodes.  Also sets up connections with clients wishing to receive
 * scene updates, and interacts with visual wrappers to send these updates
 * as necessary.
 * @author kevin
 */
public class SceneConnectionHandler implements ChildrenListener, NodeUpdateListener {

    private static final String PUBLIC_HOSTNAME_PROP = "cmu.player.hostname";
    private static final String MIN_PORT_PROP = "cmu.player.minPort";
    private static final String MAX_PORT_PROP = "cmu.player.maxPort";

    private Scene sc = null;       // The scene to wrap.
    private final Set<ClientConnection> connections = new HashSet<ClientConnection>();
    private final Map<NodeID, ModelConverter> visuals = new HashMap<NodeID, ModelConverter>();
    private final ConnectionHandlerThread handlerThread;

    /**
     * Thread to set up a ServerSocket and listen for incoming connections
     * from clients, and then handle them appropriately.
     */
    private class ConnectionHandlerThread extends Thread {

        private ServerSocket socketListener = null;
        private final Object socketListenerLock = new Object();
        private final String publicHostname;

        /**
         * Standard constructor.
         */
        public ConnectionHandlerThread() {
            super();

            publicHostname = System.getProperty(PUBLIC_HOSTNAME_PROP);

            // Initialize connection listener.
            synchronized (socketListenerLock) {
                try {
                    InetAddress addr = NetworkAddress.getPrivateLocalAddress();
                    socketListener = createServerSocket(addr);
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
            // if there is a public hostname, return that
            if (publicHostname != null) {
                return publicHostname;
            }

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

        /**
         * Find a valid server socket with the range specified by the
         * cmu.player.minPort and cmu.player.maxPort values. If the range
         * is not specified, a random port will be opened.
         * @param inetAddr the address to open a socket on
         * @return a server socket bound to an open port with the given
         * port range
         * @throws IOException if no ports are available in the given range
         */
        private ServerSocket createServerSocket(InetAddress addr)
                throws IOException
        {
            // read properties
            String minPortStr = System.getProperty(MIN_PORT_PROP);
            String maxPortStr = System.getProperty(MAX_PORT_PROP);

            if (minPortStr == null || maxPortStr == null) {
                // no values specified
                return new ServerSocket(0, 0, addr);
            }

            // parse ports into a range
            int minPort = Integer.parseInt(minPortStr);
            int maxPort = Integer.parseInt(maxPortStr);

            // Find a valid socket in the range.  We choose a random port in
            // the range, and record the ones we hit that are bad.  This is
            // better than just going in order for smaller numbers of connections,
            // but might take a long time for longer collections.
            int rangeSize = maxPort - minPort;
            Set<Integer> tried = new TreeSet<Integer>();
            while (tried.size() < rangeSize) {
                // generate a random number in the given range
                int port = minPort + (int) (Math.random() * rangeSize);

                // check if we have already tried it
                if (!tried.contains(new Integer(port))) {
                    try {
                        return new ServerSocket(port, 0, addr);
                    } catch (SocketException se) {
                        // port in use, just record it and go on
                        tried.add(new Integer(port));
                    }
                }
            }

            // no ports available -- throw an exception
            throw new IOException("No ports available in range " + minPort
                    + " - " + maxPort + ".");
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
        this.unloadScene(UnloadSceneReason.RESTARTING);
        if (sc != null) {
            this.sc = sc;
            this.processModel(sc);
        }
    }

    /**
     * Simulate a click in this scene on the given node.  Only left-clicks
     * are supported.
     * @param id ID representing the node which has been clicked
     */
    public void click(NodeID id) {
        ModelConverter model = null;
        synchronized (visuals) {
            model = visuals.get(id);
        }
        if (model != null) {
            model.click();
        }
    }

    /**
     * Recursively process a CMU Composite and its descendants, creating
     * wrappers as appropriate and sending parsed data to any clients which
     * are already connected.
     * @param c The Composite to process
     */
    protected synchronized void processModel(org.alice.apis.moveandturn.Composite c) {
        assert c != null;

        //TODO: Process camera
        if (c instanceof AbstractCamera) {
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
            for (ModelConverter visual : this.visuals.values()) {
                visualMessages.add(visual.getVisualMessage());
            }
            message = new SceneMessage(visualMessages, ContentManager.getVisualRepoRoot());

            // Store the connection.
            newConnection = new ClientConnection(this, connection);
            synchronized (connections) {
                connections.add(newConnection);
            }
            newConnection.start();
        }

        // Broadcast setup data to this connection.
        newConnection.queueMessage(message);
    }

    /**
     * Respond to a SocketException thrown by a client connection
     * by closing the connection.
     * @param connection The connection which threw the exception
     * @param ex The ex The exception which was thrown
     */
    protected void handleSocketException(ClientConnection connection, SocketException ex) {
        try {
            connection.close(new UnloadSceneMessage(UnloadSceneReason.DISCONNECTING));
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
    @SuppressWarnings("unchecked")
    protected void addModel(Model model) {
        synchronized (visuals) {
            synchronized (connections) {
                // Create and store a wrapper for this Visual.
                ModelConverter visualWrapper = new ModelConverter(model);
                this.visuals.put(visualWrapper.getNodeID(), visualWrapper);
                visualWrapper.addNodeUpdateListener(this);
                ContentManager.uploadVisual(visualWrapper.getVisualAttributes());

                // Broadcast it to each connected client.  Don't allow new
                // connections to be created during this process; this
                // would have this visual sent twice to these connections.
                for (ClientConnection connection : connections) {
                    connection.queueMessage(visualWrapper.getVisualMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modelUpdated(NodeUpdateMessage message) {
        synchronized (connections) {
            for (ClientConnection connection : connections) {
                connection.queueMessage(message);
            }
        }
    }

    /**
     * Unload the current scene, and inform all connected clients that this
     * is happening.
     * @param reason The reason for unloading the scene
     */
    public void unloadScene(UnloadSceneReason reason) {
        if (this.getScene() != null) {
            synchronized (connections) {
                for (ClientConnection connection : connections) {
                    connection.queueMessage(new UnloadSceneMessage(reason));
                }
            }
            synchronized (visuals) {
                for (ModelConverter visual : visuals.values()) {
                    // Clean up visuals individually
                    visual.unload();
                    visual.removeNodeUpdateListener(this);
                }
                // Remove visuals collectively
                visuals.clear();
            }
        }
    }

    //TODO: Listen for scene graph changes.
    /**
     * {@inheritDoc}
     * @param childrenEvent {@inheritDoc}
     */
    public void childAdded(ChildAddedEvent childrenEvent) {
        Logger.getLogger(SceneConnectionHandler.class.getName()).warning("added: " + childrenEvent);
    }

    /**
     * {@inheritDoc}
     * @param childrenEvent {@inheritDoc}
     */
    public void childRemoved(ChildRemovedEvent childrenEvent) {
        Logger.getLogger(SceneConnectionHandler.class.getName()).warning("removed: " + childrenEvent);
    }
}
