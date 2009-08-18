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
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.SceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.UnloadSceneMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;

/**
 * A Socket/ObjectOutputStream pair, used to store these two things
 * together.
 * @author kevin
 */
public class ClientConnection extends Thread {

    private final MessageQueue queue = new MessageQueue();
    private final SceneConnectionHandler parentHandler;
    private final Socket socket;
    private final long burstLength;    // The length of time (in ms) that should be taken to send a single "frame" of the scene
    private boolean closed = false;
    private final Object closedLock = new Object();
    private ObjectOutputStream outputStream = null;
    private Logger logger = Logger.getLogger(ClientConnection.class.getName());

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
            assert numMessagesInBurst > 0;

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

    public void queueMessage(CMUClientMessage message) {
        synchronized (queue) {
            if (message instanceof TransformationMessage) {
                queue.queueTransformationMessage((TransformationMessage) message);
            } else if (message instanceof SceneMessage) {
                queue.queueSceneMessage((SceneMessage) message);
            } else if (message instanceof VisualMessage) {
                queue.queueVisualMessage((VisualMessage) message);
            } else if (message instanceof VisualDeletedMessage) {
                queue.queueVisualDeletedMessage((VisualDeletedMessage) message);
            } else if (message instanceof UnloadSceneMessage) {
                queue.queueUnloadSceneMessage((UnloadSceneMessage) message);
            } else {
                logger.warning("Unrecognized message queued: " + message);
            }

            queue.notifyAll();
        }
    }

    public void close() throws SocketException {
        synchronized (closedLock) {
            closed = true;
        }
        synchronized (socket) {
            try {
                this.writeToOutputStream(new UnloadSceneMessage());
                this.outputStream.close();
                this.socket.close();
            } catch (SocketException ex) {
                throw ex;
            } catch (IOException ex) {
                // No handling necessary, we're already closing
            }
        }
    }

    public boolean isClosed() {
        synchronized (closedLock) {
            return closed;
        }
    }

//    private int numSent = 0;
    private void writeToOutputStream(Serializable message) throws SocketException {
        //TODO: add throws clause to avoid infinite loop
        synchronized (socket) {
            try {
                this.outputStream.writeUnshared(message);
                this.outputStream.flush();
                this.outputStream.reset();
//                if (numSent > 20000) {
//                    System.out.println("Still sending: " + message);
//                    numSent = 0;
//                }
//                numSent++;
            } catch (SocketException ex) {
                throw ex;
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String toString() {
        return "Connection: " + socket;
    }

    private static class MessageQueue {

        private final LinkedList<CMUClientMessage> queue = new LinkedList<CMUClientMessage>();

        public synchronized int size() {
            synchronized (queue) {
                return queue.size();
            }
        }

        public synchronized boolean empty() {
            return size() == 0;
        }

        public synchronized void queueTransformationMessage(TransformationMessage message) {

            // Check to see if the transformation for this node is
            // already in the queue; if so, overwrite it
            boolean alreadyInQueue = false;
            ListIterator<CMUClientMessage> li = queue.listIterator();
            while (li.hasNext()) {
                CMUClientMessage nextMessage = li.next();
                if (nextMessage instanceof TransformationMessage && ((TransformationMessage) nextMessage).getNodeID().equals(message.getNodeID())) {
                    li.set(message);
                    //System.out.println("Overwriting transformation");
                    alreadyInQueue = true;
                    break;
                }
            }

            // Add to queue if necessary
            if (!alreadyInQueue) {
                queue.offerLast(message);
            }
        }

        public synchronized void queueSceneMessage(SceneMessage message) {
            queue.offerFirst(message);
        }

        public synchronized void queueVisualMessage(VisualMessage message) {
            System.out.println("Queueing individual visual message");
            queue.offerFirst(message);
        }

        public synchronized void queueVisualDeletedMessage(VisualDeletedMessage message) {
            queue.offerLast(message);
        }

        public synchronized void queueUnloadSceneMessage(UnloadSceneMessage message) {
            queue.offerFirst(message);
        }

        public synchronized CMUClientMessage getNext() {
            return queue.pollFirst();
        }
    }
}
