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
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Scene;
import org.jdesktop.wonderland.modules.cmu.common.TransformationMessage;

/**
 * Wraps a CMU Scene object to extract transformation/geometry information
 * from its nodes.  Also sets up connections with clients wishing to receive
 * scene updates, and interacts with visual wrappers to send these updates
 * as necessary.
 * @author kevin
 */
public class SceneConnectionHandler implements ChildrenListener, TransformationMessageListener {

    private Scene sc;       // The scene to wrap.
    private final Collection<Socket> connections = new Vector<Socket>();
    private final Collection<ObjectOutputStream> streams = new Vector<ObjectOutputStream>();
    private final Collection<VisualWrapper> visuals = new Vector<VisualWrapper>();
    private final ConnectionHandlerThread handlerThread;

    private class ConnectionHandlerThread extends Thread {

        private ServerSocket socketListener = null;
        private final Object socketListenerLock = new Object();

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

        public int getPort() {
            synchronized (socketListenerLock) {
                assert socketListener != null;
                return socketListener.getLocalPort();
            }
        }

        public String getServer() {
            synchronized (socketListenerLock) {
                assert socketListener != null;
                return socketListener.getInetAddress().getHostAddress();
            }
        }
    }

    public SceneConnectionHandler() {
        handlerThread = new ConnectionHandlerThread();
        handlerThread.start();
    }

    public SceneConnectionHandler(Scene sc) {
        this();
        this.setScene(sc);
    }

    public int getPort() {
        return this.handlerThread.getPort();
    }

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
     * @param sc The scene to wrap
     */
    public synchronized void setScene(Scene sc) {
        //TODO: Cleanup
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
                System.out.println("Added: " + c);
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

    protected void addConnection(Socket connection) {
        ObjectOutputStream newStream = null;
        try {
            newStream = new ObjectOutputStream(connection.getOutputStream());
            // Don't add any new visuals until after this connection has been added
            // to the list and received all the current visuals; otherwise, we could
            // have visuals sent twice to this connection.
            synchronized (visuals) {
                synchronized (connections) {
                    // Store the connection and the associated stream.
                    connections.add(connection);
                    streams.add(newStream);

                    // Broadcast setup data to this connection.
                    for (VisualWrapper visual : this.visuals) {
                        System.out.println("Writing object from added connection : " + visual.getVisualMessage());
                        newStream.writeObject(visual.getVisualMessage());
                        newStream.flush();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void addVisual(Visual visual) {
        synchronized (visuals) {
            synchronized (connections) {
                // Create and store a wrapper for this Visual.
                VisualWrapper wrapper = new VisualWrapper(visual);
                this.visuals.add(wrapper);
                wrapper.addTransformationMessageListener(this);

                // Broadcast it to each connected client.  Don't allow new
                // connections to be created during this process; this
                // would have this visual sent twice to these connections.
                for (ObjectOutputStream stream : this.streams) {
                    try {
                        System.out.println("Writing object from added visual: " + wrapper.getVisualMessage());
                        stream.writeObject(wrapper.getVisualMessage());
                        stream.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public void transformationMessageChanged(TransformationMessage message) {
        synchronized (connections) {
            for (ObjectOutputStream stream : this.streams) {
                try {
                    stream.writeObject(message);
                    stream.flush();
                } catch (IOException ex) {
                    Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        System.out.println("\nChild added or removed\n");
    }
}
