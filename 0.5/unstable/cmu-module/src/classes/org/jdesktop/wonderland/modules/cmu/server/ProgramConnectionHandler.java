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
package org.jdesktop.wonderland.modules.cmu.server;

import org.jdesktop.wonderland.modules.cmu.common.ProgramConnectionType;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.CreateProgramResponseMessage;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author kevin
 */
public class ProgramConnectionHandler implements ClientConnectionHandler, Serializable {

    private WonderlandClientSender clientSender;
    private final List<Message> messageQueue = new Vector<Message>();   // Ordered message queue

    public ConnectionType getConnectionType() {
        return ProgramConnectionType.TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        this.clientSender = sender;
    }

    public void clientConnected(WonderlandClientSender sender, WonderlandClientID clientID, Properties properties) {
        // Send any messages that were in queue.
        synchronized(messageQueue) {
            Iterator<Message> iterator = messageQueue.iterator();
            while (iterator.hasNext()) {
                clientSender.send(iterator.next());
                iterator.remove();
            }
        }
    }

    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, Message message) {
        // Create new program instance
        if (CreateProgramResponseMessage.class.isAssignableFrom(message.getClass())) {
            handleCreatedResponseMessage((CreateProgramResponseMessage) message);
        }

        // Change playback speed
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        // No action.
    }

    protected void handleCreatedResponseMessage(CreateProgramResponseMessage message) {
        // Find relevant cell.
        CellMO cellMO = CellManagerMO.getCell(message.getCellID());
        assert (cellMO != null) && CMUCellMO.class.isAssignableFrom(cellMO.getClass());

        CMUCellMO cmuCellMO = (CMUCellMO) cellMO;
        cmuCellMO.setServerAndPort(message.getServer(), message.getPort());
    }

    public void sendMessage(Message message) {
        if (this.clientSender.getClients().isEmpty()) {
            // Queue messages until at least one client connects (we shouldn't ever have more than one)
            synchronized(messageQueue) {
                messageQueue.add(message);
            }
        }
        else {
            this.clientSender.send(message);
        }
    }
}
