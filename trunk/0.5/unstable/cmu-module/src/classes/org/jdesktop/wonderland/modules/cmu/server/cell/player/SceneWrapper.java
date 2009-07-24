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
package org.jdesktop.wonderland.modules.cmu.server.cell.player;

import edu.cmu.cs.dennisc.scenegraph.Component;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.event.ChildAddedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildRemovedEvent;
import edu.cmu.cs.dennisc.scenegraph.event.ChildrenListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Scene;

/**
 * Wraps a CMU Scene object to extract transformation/geometry information
 * from its nodes.  Also sets up connections with clients to receive scene
 * updates.
 * @author kevin
 */
public class SceneWrapper implements ChildrenListener {

    private Scene sc;
    private final Collection<ConnectionListener> connectionListeners = new Vector<ConnectionListener>();
    private final Vector<Socket> connections = new Vector<Socket>();
    
    private class ConnectionHandlerThread extends Thread {

        private ServerSocket socketListener;

        public ConnectionHandlerThread() {
            try {
                socketListener = new ServerSocket(5555);
            } catch (IOException ex) {
                Logger.getLogger(SceneWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            Socket incomingConnection = null;
            while (true) {
                try {
                    incomingConnection = socketListener.accept();
                } catch (IOException ex) {
                    Logger.getLogger(SceneWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
                synchronized(connections) {
                    connections.add(incomingConnection);
                }
            }
        }
    }

    public SceneWrapper() {
        new ConnectionHandlerThread().start();
    }

    public SceneWrapper(Scene sc) {
        this();
        this.setScene(sc);
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
     * Recursively parse scene tree components, ending at the visible leaves.
     * @param c The component to parse
     */
    private synchronized void processNode(Component c) {
        assert c != null;

        // Check to see if it's a visual element, and if so, parse it and add it to the collection.
        if (Visual.class.isAssignableFrom(c.getClass())) {
            VisualWrapper visual;
            synchronized (connections) {
                Collection<OutputStream> streams = new ArrayList<OutputStream>();
                visual = new VisualWrapper((Visual) c, streams);
                this.addConnectionListener(visual);
            }
        }

        // Process children.
        if (Composite.class.isAssignableFrom(c.getClass())) {
            ((Composite) c).addChildrenListener(this);
            for (Component child : ((Composite) c).accessComponents()) {
                processNode(child);
            }
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        synchronized (connectionListeners) {
            connectionListeners.add(listener);
        }
    }

    public void fireConnectionAdded(OutputStream stream) {
        synchronized (connectionListeners) {
            for (ConnectionListener listener : connectionListeners) {
                listener.connectionAdded(stream);
            }
        }
    }

    //TODO: Listen for scene graph changes.
    public void childAdded(ChildAddedEvent childrenEvent) {
        printChildWarning();
        System.out.println("added: " + childrenEvent);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void childRemoved(ChildRemovedEvent childrenEvent) {
        printChildWarning();
        System.out.println("removed: " + childrenEvent);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void printChildWarning() {
        System.out.println("\n\n\n\n\nChild added or removed!!!\n\n\n\n");
    }
}
