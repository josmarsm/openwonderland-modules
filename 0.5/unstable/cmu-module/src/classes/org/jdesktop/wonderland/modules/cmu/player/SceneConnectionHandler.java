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
package org.jdesktop.wonderland.modules.cmu.player;

import edu.cmu.cs.dennisc.scenegraph.Component;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.event.ChildAddedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildRemovedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildrenListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Scene;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;

/**
 * Wraps a CMU Scene object to extract transformation/geometry information
 * from its nodes.  Also sets up connections with clients wishing to receive
 * scene updates, and interacts with visual wrappers to send these updates
 * as necessary.
 * @author kevin
 */
public class SceneConnectionHandler implements ChildrenListener, TransformationMessageListener {

    private Scene sc = null;       // The scene to wrap.
    private final Collection<Connection> connections = new Vector<Connection>();
    private final Collection<VisualWrapper> visuals = new Vector<VisualWrapper>();
    private final ConnectionHandlerThread handlerThread;

    /**
     * A Socket/ObjectOutputStream pair, just used to store these two things
     * together.
     */
    protected class Connection {

        protected Socket socket;
        protected ObjectOutputStream outputStream;

        public Connection(Socket socket) {
            this.socket = socket;
            try {
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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
                    System.out.println("Connection accepted: " + incomingConnection);
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
         * Get the server set up by this thread's ServerSocket.
         * @return The server used to connect to this thread
         */
        public String getServer() {
            synchronized (socketListenerLock) {
                assert socketListener != null;
                return socketListener.getInetAddress().getHostAddress();
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
    public String getServer() {
        return this.handlerThread.getServer();
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
        this.sc = sc;
        this.processNode(sc.getSGComposite());
    }

    /**
     * Check this node and its children to see if visual data can be extracted,
     * and if so, create visual wrappers as appropriate.
     * @param c The component to parse
     */
    private synchronized void processNode(Component c) {
        assert c != null;

        // Check to see if it's a visual element, and if so, parse it and add it to the collection.
        if (Visual.class.isAssignableFrom(c.getClass())) {
            // Create the visual wrapper and fill it with the current connections,
            // not allowing new connections to be added while this is happening.
            synchronized (connections) {
                addVisual((Visual) c);
            }
        }

        // Process this node's children.
        if (Composite.class.isAssignableFrom(c.getClass())) {
            ((Composite) c).addChildrenListener(this);
            for (Component child : ((Composite) c).accessComponents()) {
                processNode(child);
            }
        }
    }

    /**
     * Add the given Socket as a Connection, and use it to send the current
     * state of the scene.
     * @param connection The connection to add
     */
    protected void addConnection(Socket connection) {
        try {
            // Don't add any new visuals until after this connection has been added
            // to the list and received all the current visuals; otherwise, we could
            // have visuals sent twice to this connection.
            synchronized (visuals) {
                synchronized (connections) {
                    // Store the connection and the associated stream.
                    Connection newConnection = new Connection(connection);
                    connections.add(newConnection);

                    // Broadcast setup data to this connection.
                    for (VisualWrapper visual : this.visuals) {
                        try {
                            newConnection.outputStream.writeObject(visual.getVisualMessage());
                            newConnection.outputStream.flush();
                        } catch (SocketException ex) {
                            removeConnection(newConnection);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Synchronously remove the given connection from our collection,
     * and perform any necessary cleanup.
     * @param connection The connection to remove
     */
    protected void removeConnection(Connection connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }

    /**
     * Synchronously remove the last Connection which the iterator pointed
     * to, and perform any necessary cleanup.  Provided as a convenience
     * for removing connections while iterating over them.
     * @param iterator
     */
    protected void removeConnection(Iterator<Connection> iterator) {
        synchronized (connections) {
            iterator.remove();
        }
    }

    /**
     * Create a wrapper for the given CMU visual, and broadcast its addition
     * to any connected clients.
     * @param visual The CMU visual to add
     */
    protected void addVisual(Visual visual) {
        synchronized (visuals) {
            synchronized (connections) {
                // Create and store a wrapper for this Visual.
                VisualWrapper visualWrapper = new VisualWrapper(visual);
                this.visuals.add(visualWrapper);
                visualWrapper.addTransformationMessageListener(this);

                // Broadcast it to each connected client.  Don't allow new
                // connections to be created during this process; this
                // would have this visual sent twice to these connections.
                Iterator<Connection> iterator = connections.iterator();
                while (iterator.hasNext()) {
                    Connection connection = iterator.next();
                    try {
                        connection.outputStream.writeObject(visualWrapper.getVisualMessage());
                        connection.outputStream.flush();
                    } catch (SocketException ex) {
                        removeConnection(iterator);
                    } catch (IOException ex) {
                        Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
            Iterator<Connection> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                try {
                    connection.outputStream.writeObject(message);
                    connection.outputStream.flush();
                } catch (SocketException ex) {
                    removeConnection(iterator);
                } catch (IOException ex) {
                    Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Unload the current scene, and inform all connected clients that this
     * is happening.
     */
    public void unloadScene() {
        if (this.getScene() != null) {
            synchronized (visuals) {
                synchronized (connections) {
                    for (VisualWrapper visual : visuals) {
                        // Clean up visuals individually
                        visual.unload();
                        visual.removeTransformationMessageListener(this);

                        Iterator<Connection> iterator = connections.iterator();
                        while (iterator.hasNext()) {
                            Connection connection = iterator.next();
                            try {
                                connection.outputStream.writeObject(new VisualDeletedMessage(visual.getNodeID()));
                                connection.outputStream.flush();
                            } catch (SocketException ex) {
                                removeConnection(iterator);
                            } catch (IOException ex) {
                                Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    // Remove visuals collectively
                    visuals.clear();
                }
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
