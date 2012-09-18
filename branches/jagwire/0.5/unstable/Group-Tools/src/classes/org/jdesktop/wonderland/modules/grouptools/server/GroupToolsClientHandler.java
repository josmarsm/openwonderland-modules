/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.grouptools.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;
import com.sun.sgs.kernel.ComponentRegistry;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.grouptools.common.GroupChatMessage;
import org.jdesktop.wonderland.modules.grouptools.common.GroupToolsConnectionMessage;
import org.jdesktop.wonderland.modules.grouptools.common.GroupToolsConnectionType;
import org.jdesktop.wonderland.modules.grouptools.common.RequestUsersFromGroupMessage;
import org.jdesktop.wonderland.modules.grouptools.common.UsersInGroupMessage;
import org.jdesktop.wonderland.modules.security.common.Principal;
import org.jdesktop.wonderland.modules.security.server.service.UserPrincipals;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.comms.annotation.ClientHandler;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.Resource;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecureTask;
import org.jdesktop.wonderland.server.security.SecurityManager;

/**
 *
 * @author Ryan Babiuch
 */
@ClientHandler
public class GroupToolsClientHandler implements ClientConnectionHandler,
        ManagedObject, Serializable
{
    private static final Logger LOGGER =
            Logger.getLogger(GroupToolsClientHandler.class.getName());

    private final ManagedReference<Map<String, Channel>> groupsRef;
    private final ManagedReference<Map<String, List<GroupChatMessage>>> chatLogsRef;

    public GroupToolsClientHandler() {

        Map<String, Channel> groups =
                new ScalableHashMap<String, Channel>();
        groupsRef = AppContext.getDataManager().createReference(groups);

        Map<String, List<GroupChatMessage>> chatLogs =
                new ScalableHashMap<String, List<GroupChatMessage>>();
        chatLogsRef = AppContext.getDataManager().createReference(chatLogs);
       
        

    }

    public ConnectionType getConnectionType() {
        return GroupToolsConnectionType.GROUP_TYPE;
    }

    public void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Message message) {
                               
        if(message instanceof RequestUsersFromGroupMessage) {
            RequestUsersFromGroupMessage m = (RequestUsersFromGroupMessage)message;

            //does the group name exist within our groups?
            if(getUsersInGroup(m.getGroupName()) != null) {
                //send okay message to stop blocking and then immediately send
                //UsersInGroupMessage
               // sender.send(clientID, new OKMessage(m.getMessageID()));
            sender.send(clientID,
                    new UsersInGroupMessage(m.getMessageID(),
                                        getUsersInGroup(m.getGroupName())));
            }
            else {
                //send error message
                System.out.println(m.getGroupName() +
                        " does not exist as a group.");
                
                sender.send(clientID, new ErrorMessage(m.getMessageID(),
                        "Group does not exist"));
            }            
        }
        /**
         * This block solves the issue of one client seeing duplicate messages
         * in a single chat window. There is still the issue of multiple
         * broadcast alerts being shown. This scenario is because if a client is
         * a member of multiple groups. Each of their groups is going to get a
         * personal broadcast message to them.
         *
         * 
         */
        else if(message instanceof GroupChatMessage) {
            GroupChatMessage gcm = (GroupChatMessage)message;

            //the "all" case
            if(gcm.getToGroup() == null) {
                LOGGER.warning("NULL toGroup(), sending message to everybody");
                //sender.send(message);
                sender.send(new GroupChatMessage(
                        gcm.getToGroup(),
                        gcm.getFrom(),
                        gcm.getMessageBody(),
                        gcm.isBroadcast(),
                        gcm.getMessageID()
                        ));
                return;
            }
            logMessage(gcm.getToGroup(), gcm);
            sendAdminMessage(sender, gcm);
            //for each group the message is addressed to
            for(String group: gcm.getToGroup()) {
                //grab the group's channel


                //if admin is a listed group, ignore it and continue
                //because admins automatically see everything anyway.
                if(group.equals("Admin")) {
                    continue;
                }
                Channel channel = groupsRef.get().get(group);
                //sanity check
                if (channel != null) {
                    Set<String> s = new HashSet();
                    s.add(group);
                    //we change the old toGroup value with a singleton Set
                    //and send down the channel
                    sender.send(channel,
                            new GroupChatMessage(s,
                                                gcm.getFrom(),
                                                gcm.getMessageBody(),
                                                gcm.isBroadcast(),
                                                gcm.getMessageID()));

                } else {
                    
                    LOGGER.warning("Message to empty group " +
                               gcm.getToGroup() + ". Message: " + gcm);
                }
            }

        }
    }
    /**
     * Send a message to users on the admin channel
     * 
     * @param message GroupChatMessage to be sent over channel
     */
    public void sendAdminMessage(WonderlandClientSender sender, GroupChatMessage message) {
        
        // Important to note the distinction to avoid confusion
        // of "admin" versus "Admin" below:
        // "admin" is in the server side HashMap as the name of a group,
        // "Admin" is on the client side jList
        Channel adminChannel = groupsRef.get().get("admin");
        if(adminChannel != null) {
            if(!message.isBroadcast())
            {
                for(String group : message.getToGroup()) {

                    Set<String> s = new HashSet();
                    s.add(group);
                    sender.send(adminChannel,
                            new GroupChatMessage(s,
                                                message.getFrom(),
                                                message.getMessageBody(),
                                                message.isBroadcast(),
                                                message.getMessageID()));
                }
            }
            else {
                //broadcast message
                sender.send(adminChannel,
                        new GroupChatMessage(message.getToGroup(),
                                             message.getFrom(),
                                             message.getMessageBody(),
                                             message.isBroadcast(),
                                             message.getMessageID()));
            }
        } else {
            LOGGER.warning("Message to admin failed.");
        }
    }
    public void logMessage(Set<String> groups, GroupChatMessage message) {

        for(String group : groups) {
            if(chatLogsRef.get().containsKey(group)) {
                chatLogsRef.get().get(group).add(message);
            }
        }
    }


    public void registered(WonderlandClientSender sender) {
    }

    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties) 
    {
        /**
         * When a client joins, add the client to the channel for each
         * group the client is a member of.  We do this using a secure
         * task that first checks for the user's groups, and adds the user to
         * all appropriate groups.
         */
        ResourceMap rm = new ResourceMap();
        Resource groupsResource = new GroupListResource();
        rm.put(groupsResource.getId(), new ActionMap(groupsResource,
                                                     new ViewAction()));

        SecurityManager sm = AppContext.getManager(SecurityManager.class);
        sm.doSecure(rm, new UserGroupsTask(this, sender, clientID));
    }

    public void clientDisconnected(WonderlandClientSender sender,
                                    WonderlandClientID clientID) 
    {
        // if the client's session is null, it means the client is disconnecting
        // as part of a logout. In that case, there is no need to remove the
        // client from any channels, because it is done automatically by
        // Darkstar
        if (clientID.getSession() == null) {
            return;
        }

        // remove client from all channels (removing from an extra channel
        // is a noop, so we just try them all
        for (Channel channel : groupsRef.get().values()) {
            channel.leave(clientID.getSession());
        }
    }
    

    /**
     * Add the given user to the given group
     * @param groupID the ID of the group to add the user to
     * @param clientID the ID of the client to add
     */
    public void addUserToGroup(String groupID, WonderlandClientID clientID) {
        LOGGER.warning("Add user " + clientID + " to group " + groupID);

        // find the channel for this group
        Channel channel = groupsRef.get().get(groupID);
        if (channel == null) {
            // if no channel exists, go ahead and create one
            channel = createChannel(groupID);
            groupsRef.get().put(groupID, channel);
        }

        // add the client to the channel
        channel.join(clientID.getSession());
    }

    /**
     * Remove the given user from the given group
     * @param groupID the ID of the group to remove the user from
     * @param clientID the ID of the client to remove
     */
    public void removeUserFromGroup(String groupID, WonderlandClientID clientID) {
        LOGGER.warning("Remove user " + clientID + " from group " + groupID);

        Channel channel = groupsRef.get().get(groupID);
        if (channel != null) {
            channel.leave(clientID.getSession());
        }
    }

    /**
     * Get all users in the given group
     * @param groupID the id of the group to get users for
     */
    public Set<BigInteger> getUsersInGroup(String groupID) {
        Channel channel = groupsRef.get().get(groupID);
        if (channel == null) {
            return Collections.EMPTY_SET;
        }

        DataManager dm = AppContext.getDataManager();
        Set<BigInteger> out = new LinkedHashSet<BigInteger>();
        for (Iterator<ClientSession> sessions = channel.getSessions();
             sessions.hasNext();)
        {
            ManagedReference sessionRef = dm.createReference(sessions.next());
            out.add(sessionRef.getId());
        }

        return out;
    }

    /**
     * Get all known groups
     * @return the set of known groups
     */
    public Set<String> getAllGroups() {
        return groupsRef.get().keySet();
    }

    /**
     * Send initial connection message to a client
     * @param groups the set of groups the client is a member of
     * @param sender the WonderlandClientSender to send messages
     * @param clientID the id of the client to send messages to
     */
    private void sendConnectionMessage(Set<String> groups,
                                       WonderlandClientSender sender,
                                       WonderlandClientID clientID)
    {
        LOGGER.warning("Send connect message for " + clientID +
                       " groups " + groups);

        /**
         * TODO send all messages logs for each group to the client
         */
        //HashMap m = new HashMap(chatLogsRef.get());
        sender.send(clientID,
                new GroupToolsConnectionMessage(new HashMap(chatLogsRef.get())));
       // sender.send(clientID, new GroupToolsConnectionMessage(null));
    }

    /**
     * Create a channel for the group with the given ID
     * @param id the id of the group to create a channel for
     */
    private Channel createChannel(String groupID) {
        String channelName = GroupToolsClientHandler.class.getName() + ".groupId";
        return AppContext.getChannelManager().createChannel(channelName, null,
                                                            Delivery.RELIABLE);
    }

    /**
     * A task to execute with the set of groups for the given user
     */
    private static class UserGroupsTask implements SecureTask, Serializable {
        private final ManagedReference<GroupToolsClientHandler> handlerRef;
        private final WonderlandClientSender sender;
        private final WonderlandClientID client;

        public UserGroupsTask(GroupToolsClientHandler handler,
                              WonderlandClientSender sender,
                              WonderlandClientID client)
        {
            this.handlerRef = AppContext.getDataManager().createReference(handler);
            this.sender = sender;
            this.client = client;
        }
        
        public void run(ResourceMap granted) {
            // find the resource we added earlier, which is now populated
            // with this user's groups
            ActionMap am = granted.get(GroupListResource.ID);
            GroupListResource list = (GroupListResource) am.getResource();

            // now process each group
            for (String groupName : list.getGroups()) {
                handlerRef.get().addUserToGroup(groupName, client);
            }

            // send the user their initial connection message
            handlerRef.get().sendConnectionMessage(list.getGroups(), sender,
                                                   client);
        }
    }

    /**
     * A security resource that stores the list of groups associated with
     * a user.
     */
    private static class GroupListResource implements Resource, Serializable {
        private static final String ID = GroupListResource.class.getName();

        /**
         * This set will be populated with the groups this user is a member
         * of.
         */
        private final Set<String> groups = new HashSet<String>();

        public String getId() {
            return ID;
        }

        public Set<String> getGroups() {
            return groups;
        }

        public Result request(WonderlandIdentity identity, Action action) {
            Set<Principal> principals =
                UserPrincipals.getUserPrincipals(identity.getUsername(), false);

            // if there was no result from the resolver, force the task to be
            // rescheduled to a time when we can block
            if (principals == null) {
                return Result.SCHEDULE;
            }

            // if we got here, the principals are loaded, so we can immediately
            // populate the list of groups
            addGroups(principals);
            return Result.GRANT;

        }

        public boolean request(WonderlandIdentity identity, Action action,
                               ComponentRegistry registry)
        {
            Set<Principal> principals =
                    UserPrincipals.getUserPrincipals(identity.getUsername(), true);

            addGroups(principals);
            return true;
        }

        public void addGroups(Set<Principal> principals) {
            for (Principal p : principals) {
                if (p.getType() == Principal.Type.GROUP) {
                    groups.add(p.getId());
                }
            }
        }
    }
}
