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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.CMUClientMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.SceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.UnloadSceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;

/**
 * A connection to a client which wishes to receive updates for a particular
 * CMU scene, implemented as a Thread which sends updates at regular intervals.
 * Receives updates from a SceneConnectionHandler directly, queues them, and
 * coalesces them intelligently between send intervals.
 * @author kevin
 */
public class ClientConnection extends Thread {

    /**
     * The default framerate (in frames per second) for client connections.
     */
    public final static int DEFAULT_FPS = 30;

    private final MessageQueue queue = new MessageQueue();
    private final SceneConnectionHandler parentHandler;
    private final Socket socket;
    private final long burstLength;    // The length of time (in ms) that should be taken to send a single "frame" of the scene
    private boolean closed = false;
    private final Object closedLock = new Object();
    private ObjectOutputStream outputStream = null;
    private Logger logger = Logger.getLogger(ClientConnection.class.getName());

    /**
     * Standard constructor; creates a connection with the default framerate.
     * @param parentHandler The handler for the scene which this connection is associated with
     * @param socket The Socket on which the client for this connection is listening
     */
    public ClientConnection(SceneConnectionHandler parentHandler, Socket socket) {
        this(parentHandler, DEFAULT_FPS, socket);
    }

    /**
     * Standard constructor plus specific framerate.
     * @param parentHandler The handler for the scene which this connection is associated with
     * @param targetFPS The target rate at which to send updates, in updates per second
     * (we view an update as a "burst" of queued messages).
     * @param socket The Socket on which the client for this connection is listening
     */
    public ClientConnection(SceneConnectionHandler parentHandler, int targetFPS, Socket socket) {
        this.parentHandler = parentHandler;
        this.socket = socket;
        this.burstLength = (long) (1000.0f / (float) targetFPS);

        try {
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(SceneConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.setName("CMU Client Connection: " + socket);
    }

    /**
     * Repeatedly checks the total number of messages in the queue,
     * sends this number of messages in a "burst," then sleeps for the
     * duration specified by the target frame rate.  When the queue becomes
     * empty, waits to be notified that it has been filled.
     */
    @Override
    public void run() {
        while (!isClosed()) {
            long startTime = System.currentTimeMillis();
            // "Current" number of queued messages; send all of these
            // before sleeping, to preserve frame rate
            int numMessagesInBurst = 0;
            synchronized (queue) {
                while (queue.empty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                numMessagesInBurst = queue.size();
            }
            assert (numMessagesInBurst > 0 || isClosed());

            for (int i = 0; i < numMessagesInBurst; i++) {
                CMUClientMessage nextMessage = queue.getNext();
                try {
                    this.writeToOutputStream(nextMessage);
                } catch (SocketException ex) {
                    parentHandler.handleSocketException(this, ex);
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (burstLength > elapsed) {
                try {
                    sleep(burstLength - elapsed);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Place a message in the queue, ordering it and coalescing it appropriately.
     * @param message The message to queue
     */
    public void queueMessage(CMUClientMessage message) {
        synchronized (queue) {
            if (message instanceof NodeUpdateMessage) {
                queue.queueNodeUpdateMessage((NodeUpdateMessage) message);
            } else if (message instanceof SceneMessage) {
                queue.queueSceneMessage((SceneMessage) message);
            } else if (message instanceof VisualMessage) {
                queue.queueVisualMessage((VisualMessage) message);
            } else if (message instanceof VisualDeletedMessage) {
                queue.queueVisualDeletedMessage((VisualDeletedMessage) message);
            } else if (message instanceof UnloadSceneMessage) {
                queue.queueUnloadSceneMessage((UnloadSceneMessage) message);
            } else {
                logger.severe("Unrecognized message queued: " + message);
            }

            queue.notifyAll();
        }
    }

    /**
     * Close this connection after sending the given message
     * to clients, and stop execution of its thread.
     * @param closingMessage Final message to send to clients
     * @throws java.net.SocketException If the unload message can't be
     * sent due to socket issues
     */
    public void close(CMUClientMessage closingMessage) throws SocketException {
        synchronized (closedLock) {
            closed = true;
        }
        synchronized(queue) {
            // Notify the queue so that the thread doesn't just get stuck
            // waiting on it (i.e. ensure the tread is destroyed).
            queue.notifyAll();
        }
        synchronized (socket) {
            try {
                this.writeToOutputStream(closingMessage);
                this.outputStream.close();
                this.socket.close();
            } catch (SocketException ex) {
                throw ex;
            } catch (IOException ex) {
                // No handling necessary, we're already closing
            }
        }
    }

    /**
     * Find out whether this connection is closed.
     * @return Whether this connection is closed
     */
    public boolean isClosed() {
        synchronized (closedLock) {
            return closed;
        }
    }

    /**
     * Send the given message to the client on the other end of this connection's
     * socket.
     * @param message The message to send
     * @throws java.net.SocketException If the message can't be sent due to
     * socket issues
     */
    protected void writeToOutputStream(Serializable message) throws SocketException {
        synchronized (socket) {
            try {
                this.outputStream.writeUnshared(message);
                this.outputStream.flush();
                this.outputStream.reset();
            } catch (SocketException ex) {
                throw ex;
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get a String representation of the connection, with debug info.
     * @return String representation of the connection
     */
    @Override
    public String toString() {
        return getClass() + "[socket=" + socket + "]";
    }

    /**
     * A queue which orders and coalesces CMUClientMessage's.  "Large-scale"
     * messages (SceneMessage, VisualMessage, etc.) are given priority and are
     * not coalesced.  NodeUpdateMessage's are treated differently and
     * with lower priority; at any time in the queue, we allow only one
     * message per node per NodeUpdateMessage subclass, and we coalesce messages
     * by storing only the most recently queued message of a particular class.
     */
    protected static class MessageQueue {

        private final LinkedList<CMUClientMessage> queue = new LinkedList<CMUClientMessage>();

        /**
         * Get the number of messages in the queue.
         * @return Number of messages in the queue
         */
        public synchronized int size() {
            synchronized (queue) {
                return queue.size();
            }
        }

        /**
         * Convenience method to find out whether the queue is empty.
         * @return Whether the queue is empty
         */
        public synchronized boolean empty() {
            return size() == 0;
        }

        /**
         * Add a NodeUpdateMessage to the queue, overwriting any other
         * NodeUpdateMessage of this type for the same node which might
         * be present.
         * @param message The message to add
         */
        public synchronized void queueNodeUpdateMessage(NodeUpdateMessage message) {

            // Check to see if an update of this type for this node is
            // already in the queue; if so, overwrite it
            boolean alreadyInQueue = false;
            ListIterator<CMUClientMessage> li = queue.listIterator();
            while (li.hasNext()) {
                CMUClientMessage nextMessage = li.next();
                if (nextMessage.getClass().isAssignableFrom(message.getClass()) && ((NodeUpdateMessage) nextMessage).getNodeID().equals(message.getNodeID())) {
                    li.set(message);
                    alreadyInQueue = true;
                    break;
                }
            }
            
            // Add to queue if necessary
            if (!alreadyInQueue) {
                queue.offerLast(message);
            }
        }

        /**
         * Add a scene message to the queue; give it priority.
         * @param message Message to add
         */
        public synchronized void queueSceneMessage(SceneMessage message) {
            queue.offerFirst(message);
        }

        /**
         * Add a visual message to the queue; put it just below any
         * scene messages in the queue.
         * @param message Message to add
         */
        public synchronized void queueVisualMessage(VisualMessage message) {
            ListIterator<CMUClientMessage> li = queue.listIterator();
            while (li.hasNext()) {
                CMUClientMessage nextMessage = li.next();
                if (nextMessage instanceof SceneMessage) {
                    li.previous();
                    break;
                }
            }
            li.add(message);
        }

        /**
         * Add a visual deleted message to the queue; do not give it priority.
         * @param message Message to add
         */
        public synchronized void queueVisualDeletedMessage(VisualDeletedMessage message) {
            queue.offerLast(message);
        }

        /**
         * Add an unload scene message to the queue; give it priority.
         * @param message Message to add
         */
        public synchronized void queueUnloadSceneMessage(UnloadSceneMessage message) {
            queue.offerFirst(message);
        }

        /**
         * Pop the top message from the queue.
         * @return The top message in the queue
         */
        public synchronized CMUClientMessage getNext() {
            return queue.pollFirst();
        }
    }
}
