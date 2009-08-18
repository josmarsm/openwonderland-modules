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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread to process incoming scene graph changes from a CMU instance.
 * Establishes a connection with the instance, and forwards messages
 * sent by the instance to the registered with the thread.
 * @author kevin
 */
public class VisualChangeReceiverThread extends Thread {

    private final String hostname;
    private final int port;
    private final CMUCell parentCell;
    private final Object statsLock = new Object();
    private long numReads = 0;

    /**
     * Standard constructor.
     * @param server The server on which the CMU instance is running.
     * @param port The port on which the CMU instance is running.
     */
    public VisualChangeReceiverThread(CMUCell parentCell, String hostname, int port) {
        super("CMU Message Receiver " + hostname + ":" + port);
        this.parentCell = parentCell;
        this.hostname = hostname;
        this.port = port;
    }

    private class MonitorThread extends Thread {

        public MonitorThread() {
            super("CMU Message Statistics Monitor " + hostname + ":" + port);
        }

        private static final long PAUSE_TIME = 5000;
        @Override
        public void run() {
            while (VisualChangeReceiverThread.this.isAlive()) {
                try {
                    Thread.sleep(PAUSE_TIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VisualChangeReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                long currNumReads;
                synchronized(statsLock) {
                   currNumReads = getNumReads();
                   resetStats();
                }
                System.out.println(currNumReads + "messages in last " + PAUSE_TIME / 1000 + "s");
            }
        }
    }

    /**
     * Create a connection to the CMU instance, and wait for incoming
     * messages; these can be either transformation updates, or information
     * about new nodes.
     */
    @Override
    public void run() {
        //new MonitorThread().start();
        try {
            // Get incoming stream from server
            Socket connection = new Socket(hostname, port);
            ObjectInputStream fromServer = new ObjectInputStream(connection.getInputStream());

            // Notify connection successful.
            parentCell.updateConnectedState(true, this);

            System.out.println("Connected on port " + port);
            while (parentCell.allowsUpdatesFrom(this)) {
                // Read messages as long as they're being sent
                Object received = fromServer.readUnshared();
                addObjectToStats(received);
                parentCell.applyMessage(received, this);
            }
            fromServer.close();
            connection.close();
        } catch (ClassNotFoundException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (SocketException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (UnknownHostException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (EOFException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (IOException ex) {
            Logger.getLogger(VisualChangeReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        //TODO: Notify the CMUCell about the reason for the disconnect?
        System.out.println("Disconnecting from port " + port);
        parentCell.updateConnectedState(false, this);
    }

    private void addObjectToStats(Object object) {
        synchronized(statsLock) {
            numReads++;
        }
    }

    public long getNumReads() {
        synchronized(statsLock) {
            return numReads;
        }
    }

    public void resetStats() {
        synchronized(statsLock) {
            numReads = 0;
        }
    }
}