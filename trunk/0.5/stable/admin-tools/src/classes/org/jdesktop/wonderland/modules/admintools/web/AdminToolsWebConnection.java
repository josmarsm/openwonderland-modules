/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.web;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.admintools.common.AdminToolsWebConnectionType;
import org.jdesktop.wonderland.modules.admintools.common.BroadcastMessage;
import org.jdesktop.wonderland.modules.admintools.common.DisconnectMessage;
import org.jdesktop.wonderland.modules.admintools.common.ErrorReportMessage;
import org.jdesktop.wonderland.modules.admintools.common.MuteMessage;
import org.jdesktop.wonderland.modules.admintools.common.UserList;
import org.jdesktop.wonderland.modules.admintools.common.UserListRequestMessage;
import org.jdesktop.wonderland.modules.admintools.common.UserListResponseMessage;

/**
 * Web connection for admin tools
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class AdminToolsWebConnection extends BaseConnection {
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsWebConnection.class.getName());
    
    public ConnectionType getConnectionType() {
        return AdminToolsWebConnectionType.CONNECTION_TYPE;
    }

    public UserList getUsers() throws InterruptedException {
        UserList out = null;

        ResponseMessage rm = sendAndWait(new UserListRequestMessage());
        if (rm instanceof UserListResponseMessage) {
            out = ((UserListResponseMessage) rm).getUsers();
        } else if (rm instanceof ErrorMessage) {
            LOGGER.log(Level.WARNING, "Error getting users: " +
                    ((ErrorMessage) rm).getErrorMessage(),
                    ((ErrorMessage) rm).getErrorCause());
        } else {
            LOGGER.warning("Unexpected message: " + rm);
        }

        return out;
    }

    public void forceMute(BigInteger sessionID) {
        send(new MuteMessage(null, sessionID));
    }

    public void forceErrorReport(BigInteger sessionID, String reportID) {
        send(new ErrorReportMessage(null, sessionID, reportID));
    }
    
    public void forceDisconnect(BigInteger sessionID) {
        send(new DisconnectMessage(null, sessionID));
    }

    public void broadcast(String text) {
        send(new BroadcastMessage(text));
    }

    @Override
    public void handleMessage(Message message) {
        LOGGER.warning("Unexpected message: " + message);
    }
}
