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

    private final String server;
    private final int port;
    private final CMUCell parentCell;

    /**
     * Standard constructor.
     * @param server The server on which the CMU instance is running.
     * @param port The port on which the CMU instance is running.
     */
    public VisualChangeReceiverThread(CMUCell parentCell, String server, int port) {
        super();
        this.parentCell = parentCell;
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
            // Get incoming stream from server
            Socket connection = new Socket(server, port);
            ObjectInputStream fromServer = new ObjectInputStream(connection.getInputStream());

            // Notify connection successful.
            parentCell.markConnected(this);

            System.out.println("Connected on port " + port);
            while (parentCell.allowsUpdatesFrom(this)) {
                // Read messages as long as they're being sent
                Object received = fromServer.readObject();
                parentCell.applyMessage(received, this);
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (SocketException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (UnknownHostException ex) {
            System.out.println("Disconnecting: " + ex);
        } catch (IOException ex) {
            Logger.getLogger(VisualChangeReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        //TODO: Notify the CMUCell about the reason for the disconnect?
        System.out.println("Disconnecting from port " + port);
        parentCell.markDisconnected(this);
    }
}



