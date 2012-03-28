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
package org.jdesktop.wonderland.modules.admintools.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.admintools.common.AdminToolsWebConnectionType;
import org.jdesktop.wonderland.modules.admintools.common.BroadcastMessage;
import org.jdesktop.wonderland.modules.admintools.common.DisconnectMessage;
import org.jdesktop.wonderland.modules.admintools.common.ErrorReportMessage;
import org.jdesktop.wonderland.modules.admintools.common.MuteMessage;
import org.jdesktop.wonderland.modules.admintools.common.UserList;
import org.jdesktop.wonderland.modules.admintools.common.UserList.User;
import org.jdesktop.wonderland.modules.admintools.common.UserListRequestMessage;
import org.jdesktop.wonderland.modules.admintools.common.UserListResponseMessage;
import org.jdesktop.wonderland.modules.security.server.service.GroupMemberResource;
import org.jdesktop.wonderland.server.UserListener;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.comms.SecureClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.comms.annotation.ClientHandler;
import org.jdesktop.wonderland.server.security.Resource;

/**
 * Connection handler for admin tools connection.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ClientHandler
public class AdminToolsWebConnectionHandler 
        implements SecureClientConnectionHandler, ManagedObject, Serializable,
                   UserListener
{
    private final Map<WonderlandClientID, User> users =
            new LinkedHashMap<WonderlandClientID, User>();

    public ConnectionType getConnectionType() {
        return AdminToolsWebConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        UserManager.getUserManager().addUserListener(this);
    }

    public Resource checkConnect(WonderlandClientID clientID, Properties properties)
    {
        return new GroupMemberResource("admin");
    }

    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties)
    {
    }

    public void connectionRejected(WonderlandClientID clientID) {
    }

    public Resource checkMessage(WonderlandClientID clientID, Message message) {
        return null;
    }

    public void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Message message)
    {
        if (message instanceof MuteMessage) {
            AdminToolsUtils.handleMute(sender, (MuteMessage) message);
        } else if (message instanceof ErrorReportMessage) {
            AdminToolsUtils.handleErrorReport(sender, (ErrorReportMessage) message);
        } else if (message instanceof DisconnectMessage) {
            AdminToolsUtils.handleDisconnect(sender, (DisconnectMessage) message);
        } else if (message instanceof BroadcastMessage) {
            AdminToolsUtils.handleBroadcast(sender, (BroadcastMessage) message);
        } else if (message instanceof UserListRequestMessage) {
            handleUserListRequest(sender, clientID, (UserListRequestMessage) message);
        }
    }

    public boolean messageRejected(WonderlandClientSender sender,
                                   WonderlandClientID clientID, Message message,
                                   Set<Action> requested, Set<Action> granted)
    {
        return true;
    }

    public void clientDisconnected(WonderlandClientSender sender,
                                  WonderlandClientID clientID)
    {
    }

    public void userLoggedIn(WonderlandClientID clientID, 
                             ManagedReference<UserMO> userRef) 
    {
        users.put(clientID, new User(clientID.getID().toString(),
                                     userRef.get().getUsername(),
                                     new Date()));
    }

    public void userLoggedOut(WonderlandClientID clientID, 
                              ManagedReference<UserMO> userRef, 
                              ManagedReference<Queue<Task>> logoutTasksRef) 
    {
        users.remove(clientID);
    }

    private void handleUserListRequest(WonderlandClientSender sender,
                                       WonderlandClientID clientID,
                                       UserListRequestMessage message)
    {
        UserList out = new UserList();
        for (User user : users.values()) {
            out.getUsers().add(user);
        }

        Collections.sort(out.getUsers(), new Comparator<User>() {
            public int compare(User t, User t1) {
                // first compare names
                int res = t.name.compareTo(t1.name);
                if (res == 0) {
                    // compare dates if the names are equal
                    res = t.when.compareTo(t1.when);
                }

                return res;
            }
        });

        sender.send(clientID,
                    new UserListResponseMessage(message.getMessageID(), out));
    }
}
