/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.server;

import com.sun.sgs.app.AppContext;
import java.io.Serializable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.isocial.common.CurrentInstanceMessage;
import org.jdesktop.wonderland.modules.isocial.common.ISocialConnectionType;
import org.jdesktop.wonderland.modules.isocial.common.ResultMessage;
import org.jdesktop.wonderland.modules.isocial.common.RoleRequestMessage;
import org.jdesktop.wonderland.modules.isocial.common.RoleResponseMessage;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.security.server.service.GroupMemberResource;
import org.jdesktop.wonderland.server.comms.SecureClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.comms.annotation.ClientHandler;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.Resource;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecureTask;
import org.jdesktop.wonderland.server.security.SecurityManager;

/**
 * Connection handler for iSocial connection
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ClientHandler
public class ISocialConnectionHandler
        implements SecureClientConnectionHandler, Serializable
{
    private static final Logger LOGGER =
            Logger.getLogger(ISocialConnectionHandler.class.getName());

    public ConnectionType getConnectionType() {
        return ISocialConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
    }

    public Resource checkConnect(WonderlandClientID clientID,
                                 Properties properties)
    {
        return null;
    }

    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties)
    {
    }

    public void connectionRejected(WonderlandClientID clientID) {
    }

    public Resource checkMessage(WonderlandClientID clientID, Message message) {
        // should only be sent by the web server
        if (message instanceof ResultMessage ||
            message instanceof CurrentInstanceMessage)
        {
            return new GroupMemberResource("admin");
        }

        return null;
    }

    public void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Message message)
    {
        if (message instanceof RoleRequestMessage) {
            handleRoleRequest(sender, clientID, message);
        } else if (message instanceof ResultMessage ||
                   message instanceof CurrentInstanceMessage)
        {
            // forward to everyone
            sender.send(message);
        }
    }

    public boolean messageRejected(WonderlandClientSender sender,
                                   WonderlandClientID clientID,
                                   Message message, Set<Action> requested,
                                   Set<Action> granted)
    {
        return true;
    }

    public void clientDisconnected(WonderlandClientSender sender,
                                  WonderlandClientID clientID)
    {
    }

    public void handleRoleRequest(WonderlandClientSender sender,
                                  WonderlandClientID clientID,
                                  Message message)
    {
        SecurityManager security = AppContext.getManager(SecurityManager.class);

        Resource adminResource = new GroupMemberResource("admin");
        Resource guideResource = new GroupMemberResource("guide");

        ActionMap adminMap = new ActionMap(adminResource);
        adminMap.put("view", new ViewAction());

        ActionMap guideMap = new ActionMap(guideResource);
        guideMap.put("view", new ViewAction());

        ResourceMap rm = new ResourceMap();
        rm.put(adminResource.getId(), adminMap);
        rm.put(guideResource.getId(), guideMap);

        security.doSecure(rm, new RoleTask(sender, clientID, message.getMessageID(),
                                           adminResource.getId(), guideResource.getId()));
    }

    private static class RoleTask implements SecureTask, Serializable {
        private final WonderlandClientSender sender;
        private final WonderlandClientID clientID;
        private final MessageID messageID;
        private final String adminResourceID;
        private final String guideResourceID;

        public RoleTask(WonderlandClientSender sender,
                        WonderlandClientID clientID,
                        MessageID messageID,
                        String adminResourceID,
                        String guideResourceID)
        {
            this.sender = sender;
            this.clientID = clientID;
            this.messageID = messageID;
            this.adminResourceID = adminResourceID;
            this.guideResourceID = guideResourceID;
        }

        public void run(ResourceMap granted) {
            Role role = Role.STUDENT;
            
            if (granted.containsKey(adminResourceID) &&
                    !granted.get(adminResourceID).isEmpty())
            {
                role = Role.ADMIN;
            } else if (granted.containsKey(guideResourceID) &&
                    !granted.get(guideResourceID).isEmpty())
            {
                role = Role.GUIDE;
            }

            sender.send(clientID, new RoleResponseMessage(messageID, role));
        }
    }
}
