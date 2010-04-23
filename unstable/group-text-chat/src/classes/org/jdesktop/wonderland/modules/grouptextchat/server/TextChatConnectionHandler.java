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
package org.jdesktop.wonderland.modules.grouptextchat.server;

import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupChatMessage;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupChatMessage.GroupAction;
import org.jdesktop.wonderland.modules.grouptextchat.common.TextChatMessage;
import org.jdesktop.wonderland.modules.grouptextchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Handles text chat messages from the client.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Drew Harry <drew_harryu@dev.java.net>
 */
public class TextChatConnectionHandler implements ClientConnectionHandler, Serializable {

    private static final LoggerWrapper logger = new LoggerWrapper(Logger.getLogger(TextChatConnectionHandler.class.getName()));

    private static String NEXT_GROUP_ID_BINDING = "NEXT_GROUP_ID";

    public ConnectionType getConnectionType() {
        return TextChatConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        GroupChatsSet gcs = new GroupChatsSet();
        AppContext.getDataManager().setBinding(GroupChatsSet.ID, gcs);

        ListenersSet ls = new ListenersSet();
        AppContext.getDataManager().setBinding(ListenersSet.ID, ls);
    }

    public void clientConnected(WonderlandClientSender sender,
            WonderlandClientID clientID, Properties properties) {
        // ignore
    }

    public void clientDisconnected(WonderlandClientSender sender,
            WonderlandClientID clientID) {
        // ignore
    }

    public void messageReceived(WonderlandClientSender sender,
            WonderlandClientID clientID, Message message) {

        TextChatMessage tcm = (TextChatMessage)message;

        // This exists for testing purposes only. Turning it off now that
        // chat-zones can properly add/remove people from groups.
        //tcm = this.processMessage(tcm, clientID);

        // Sending to specific users is turned off for now. That will get
        // folded into the group system later.
//        if(toUser != null && toUser.equals("") == false) {
//
//        for (WonderlandClientID id : sender.getClients()) {
//            String name = id.getSession().getName();
//            logger.warning("Looking at " + name + " for " + toUser);
//            if (name.equals(toUser) == true) {
//                sender.send(id, message);
//                return;
//            }
//        }
//        }

        // If the message isn't for a specific person, check to see which
        // group it's for. If it's for group 0, set the recipient list to all
        // users. If it's for a specific group, set the recipients to that group.
        Set<WonderlandClientID> recipients = null;
        GroupID toGroup = tcm.getGroup();

        if(toGroup.equals(new GroupID(GroupID.GLOBAL_GROUP_ID))) {
            recipients = sender.getClients();

            // now notify listeners of a new message. For now, listeners only
            // get global messages.

            // First, notify listeners of a new message. On the server side,
            // all listeners get all messages, even if they're sent to
            // specific people. It's up to listeners to decide what to do
            // with them.

            ListenersSet ls = (ListenersSet) AppContext.getDataManager().getBinding(ListenersSet.ID);
            AppContext.getDataManager().markForUpdate(ls);


            for(ManagedReference listenerRef : ls.listeners) {
                TextChatMessageListener listener = (TextChatMessageListener)listenerRef.get();
                logger.log(Level.FINER, "Sending to listener: " + listener);
                listener.handleMessage(tcm);
            }
        } else {
            // If we're not on channel 0, then we should be on one of the other
            // channels. Check the groups map to figure out who the recipients
            // of a non-zero message should be.

            GroupChatsSet gcs = (GroupChatsSet) AppContext.getDataManager().getBinding(GroupChatsSet.ID);


            AppContext.getDataManager().markForUpdate(gcs);
            
            if(gcs.groups.containsKey(toGroup)) {
                // Clone it first, so when we remove the client later we don't remove it from the actual group.
                recipients = new HashSet<WonderlandClientID>(gcs.groups.get(toGroup));
                logger.log(Level.FINER, "Received a message for GroupID: " + toGroup + " group recipients: " + recipients);
            } else {
                logger.log(Level.WARNING, "Received a message for GroupID " + toGroup + " but that group isn't a known group. Known Groups: " + gcs.groups.keySet());

                // Just make an empty set so the rest of the method works fine
                recipients = new HashSet<WonderlandClientID>();
            }
        }

        // Now send to everyone on our recipients list, minus the person who sent the message.
        recipients.remove(clientID);

        recipients = checkForUserExistence(recipients);

        if(recipients.size() > 0)
            sender.send(recipients, message);
        
        return;
    }

    /**
     * Used to provide a text-based interface to adding/removing users from chat groups.
     *
     * Shouldn't ever be used in production, but it's the only way I had to easily
     * see if the group system was working.
     *
     * Syntax: /join - create a new group and join it
     *         /join [id] - join the group with [id]
     *         /leave [id] - leave the group with [id]
     *
     * An older version of this would also route messages to channels,
     * which is why it returns a TCM. This is purely for testing this module,
     * and isn't intended to be used by end-users.
     *
     * @param tcm
     * @param clientID
     * @return
     */
    private TextChatMessage processMessage(TextChatMessage tcm, WonderlandClientID clientID) {
        // Provides a sort of lame text protocol for asking to be added/removed from groups for testing purposes.
        String msgText = tcm.getTextMessage();

        if(msgText.startsWith("/")) {
            // Chop off the '/'
            msgText = msgText.substring(1);

            //explode on spaces.
            String[] pieces = msgText.split(" ");


            String command = pieces[0];

            if(pieces[0].equals("join")) {

                GroupID newGroup;
                if(pieces.length==1) {
                    newGroup = this.createChatGroup();
                }
                else if(pieces.length==2) {
                    newGroup = new GroupID(Integer.parseInt(pieces[1]));
                }
                else {
                    return tcm;
                }

                this.addUserToChatGroup(newGroup, clientID);

                return tcm;
            } else if(pieces[0].equals("leave")) {

                if(pieces.length != 2)
                    return tcm;

                GroupID group = new GroupID(Integer.parseInt(pieces[1]));

                this.removeUserFromChatGroup(group, clientID);
                return tcm;
            }
            else {

                return tcm;
                }

        } else {
            return tcm;
        }
    }

    /**
     * Convenience method for the two parameter version that sends the message from
     * a fake "Server" user.
     *
     * @param msg The body of the text chat message.
     */
    public void sendGlobalMessage(String msg) {
        this.sendGlobalMessage("Server", msg);
    }

    /**
     * Sends a global text message to all users. You can decide who the message should
     * appear to be from; it doesn't need to map to a known user.
     *
     * @param from The name the message should be displayed as being from.
     * @param msg The body of the text chat message.
     */
    public void sendGlobalMessage(String from, String msg) {
        logger.log(Level.FINER, "Sending global message from " + from + ": " + msg);
        // Originally included for the XMPP plugin, so people chatting with the XMPP bot
        // can have their messages replicated in-world with appropriate names.
        //
        // Of course, there are some obvious dangerous with this: it's not that hard
        // to fake an xmpp name to look like someone it's not. In an otherwise
        // authenticated world, this might be a way to make it look like
        // people are saying things they're not.

        CommsManager cm = WonderlandContext.getCommsManager();
        WonderlandClientSender sender = cm.getSender(TextChatConnectionType.CLIENT_TYPE);

        // Send to all clients, because the message is originating from a non-client source.
        Set<WonderlandClientID> clientIDs = sender.getClients();

        // Construct a new message with appropriate fields.
        TextChatMessage textMessage = new TextChatMessage(msg, from, new GroupID(GroupID.GLOBAL_GROUP_ID));
        sender.send(clientIDs, textMessage);
    }

    /**
     * Adds a listener object that will be called whenever a text chat message is sent.
     * Global messages sent from sendGlobalMessage are not included in these notifications.
     *
     * @param listener The listener object.
     */
    public void addTextChatMessageListener(TextChatMessageListener listener) {
        ListenersSet ls = (ListenersSet) AppContext.getDataManager().getBinding(ListenersSet.ID);
        AppContext.getDataManager().markForUpdate(ls);

        ls.listeners.add(AppContext.getDataManager().createReference(listener));
    }

    /**
     * Removes the listener object from the list of listeners.
     * 
     * @param listener The listener object.
     */
    public void removeTextChatMessageListener(TextChatMessageListener listener) {
        ListenersSet ls = (ListenersSet) AppContext.getDataManager().getBinding(ListenersSet.ID);
        AppContext.getDataManager().markForUpdate(ls);

        ls.listeners.remove(AppContext.getDataManager().createReference(listener));
    }

    /**
     * Adds the user with ClientID wcid to the ChatGroup with gid. If gid isn't a valid group
     * we log a warning but otherwise ignore it.
     *
     * @param gid The id of the chat group the client is joining.
     * @param wcid The id of the client.
     */
    public void addUserToChatGroup(GroupID gid, WonderlandClientID wcid) {

        GroupChatsSet gcs = (GroupChatsSet) AppContext.getDataManager().getBinding(GroupChatsSet.ID);

        if(gcs.groups.containsKey(gid)) {
            Set<WonderlandClientID> s = gcs.groups.get(gid);
            AppContext.getDataManager().markForUpdate(gcs);



            s.add(wcid);
            logger.log(Level.WARNING, "Added user: " + wcid + " to group: " + gid + " userlist now: " + s);
            
            // Send a message to the client telling it to display a new tab on the client UI.
            GroupChatMessage msg = new GroupChatMessage(gid, GroupAction.WELCOME);

            CommsManager cm = WonderlandContext.getCommsManager();
            WonderlandClientSender sender = cm.getSender(TextChatConnectionType.CLIENT_TYPE);
            sender.send(wcid, msg);

            // Send a message to everyone in the group that the person has joined.
            // We'll send it to the new user too, since it's helpful to see a "you
            // joined" message even if there's no chat recently, to show that
            // it's working.

            // Make sure the users are all there before we try to send to them.
            s = checkForUserExistence(s);
            if(s.size() > 0) {
                msg = new GroupChatMessage(gid, GroupAction.JOINED, WonderlandContext.getUserManager().getUser(wcid).getUsername());
                sender.send(s, msg);
            }

            // Push it back in so that the user is actually removed from the set permently.
            gcs.groups.put(gid, s);

        } else {
            logger.log(Level.WARNING, "Attempted to add user " + wcid + " to unknown text chat group " + gid + " (known groups: " + gcs.groups.keySet() + ")");
        }
    }


    private Set<WonderlandClientID> checkForUserExistence(Set<WonderlandClientID> users) {
        Set<WonderlandClientID> out = new HashSet<WonderlandClientID>();
        out = new HashSet<WonderlandClientID>(users);

        for(WonderlandClientID wcid : users) {

            try {
                wcid.getSession();
            } catch (ObjectNotFoundException e) {
                // This should trigger if the session is already gone.
                out.remove(wcid);
                logger.log(Level.INFO, "removing a user from list because they're dead: " + wcid);
            }
        }

        return out;
    }
    /**
     * Removes the user with ClientID wcid from the ChatGroup with gid.
     *
     * @param gid The id of the chat group the client is leaving.
     * @param wcid The id of the client.
     */
    public void removeUserFromChatGroup(GroupID gid, WonderlandClientID wcid) {

        GroupChatsSet gcs = (GroupChatsSet) AppContext.getDataManager().getBinding(GroupChatsSet.ID);

        if(gcs.groups.containsKey(gid)) {
            Set<WonderlandClientID> s = gcs.groups.get(gid);

            // Send a message to the client telling it to remove the right tab on the client UI.
            GroupChatMessage msg = new GroupChatMessage(gid, GroupAction.GOODBYE);

            CommsManager cm = WonderlandContext.getCommsManager();
            WonderlandClientSender sender = cm.getSender(TextChatConnectionType.CLIENT_TYPE);
            sender.send(wcid, msg);

            // Send a message to everyone in the group that the person has left.
            if(s.size() > 0) {
                msg = new GroupChatMessage(gid, GroupAction.LEFT, WonderlandContext.getUserManager().getUser(wcid).getUsername());
                sender.send(s, msg);
            }

            s.remove(wcid);
            logger.log(Level.FINE, "Removed user: " + wcid + " from group: " + gid + " userlist now: " + s);

        } else {
            logger.log(Level.WARNING, "Attempted to remove user " + wcid + " to unknown text chat group " + gid);
        }
    }

    /**
     * Create a new chat group. This must be done before adding people to the group.
     *
     * @return The GroupID of the new group.
     */
    public GroupID createChatGroup() {
        
               NextGroupID nextGroupID;
        try {

            nextGroupID = (NextGroupID) AppContext.getDataManager().getBinding(NextGroupID.ID);

        } catch (NameNotBoundException e) {
            nextGroupID = new NextGroupID();
            nextGroupID.next = GroupID.FIRST_GROUP_ID;

            AppContext.getDataManager().setBinding(NextGroupID.ID, nextGroupID);
        }

        GroupID gid = new GroupID(nextGroupID.next);

        nextGroupID.next++;
        
        AppContext.getDataManager().markForUpdate(nextGroupID);

        GroupChatsSet gcs = (GroupChatsSet) AppContext.getDataManager().getBinding(GroupChatsSet.ID);
        AppContext.getDataManager().markForUpdate(gcs);

        gcs.groups.put(gid, new HashSet<WonderlandClientID>());

        logger.log(Level.INFO, "Created group: " + gid + " (known groups: " + gcs.groups.keySet() + ") nextGroupID=" + nextGroupID);
        return gid;
    }

    /**
     * Convenience method that removes the specified user from all text chat groups.
     * Useful when a user logs off and we want to clean out the groups they were
     *
     * @param wcid
     */
    public void removeUserFromAllGroups(WonderlandClientID wcid) {

        GroupChatsSet gcs = (GroupChatsSet) AppContext.getDataManager().getBinding(GroupChatsSet.ID);
        AppContext.getDataManager().markForUpdate(gcs);


        for(GroupID gid : gcs.groups.keySet()) {
//            gcs.groups.get(gid).remove(wcid);
            

            // Not sure on the etiquette here - is it cheaper to check to see if
            // the set contains the user before trying to remove it?
            if(gcs.groups.get(gid).contains(wcid)) {
                // route through the other method so leave notifcations get sent properly.
                this.removeUserFromChatGroup(gid, wcid);
            }

        }
    }

    public void setGroupLabel(GroupID group, String label) {
        GroupChatsSet groupsContainer = (GroupChatsSet) AppContext.getDataManager().getBinding(GroupChatsSet.ID);
        AppContext.getDataManager().markForUpdate(groupsContainer);

        if(groupsContainer.groups.containsKey(group)) {
            logger.log(Level.INFO, "Found the GroupID we're looking to update.");

            Set<WonderlandClientID> wcids = (Set<WonderlandClientID>) groupsContainer.groups.get(group);
            group.setLabel(label);
            groupsContainer.groups.put(group, wcids);

            // Now we need to update anyone on that list of their current group name.
            if(wcids != null && wcids.size() > 0) {
                WonderlandClientSender sender = WonderlandContext.getCommsManager().getSender(TextChatConnectionType.CLIENT_TYPE);
                GroupChatMessage msg = new GroupChatMessage(group, GroupAction.LABEL);
                sender.send(wcids, msg);
            }
        }
    }

    private static class ListenersSet implements ManagedObject, Serializable {
        public static final String ID="TEXT_CHAT_LISTENERS_SET";
        public Set<ManagedReference> listeners = new HashSet<ManagedReference>();
    }

    private static class GroupChatsSet implements ManagedObject, Serializable {
        public static final String ID="TEXT_CHAT_GROUP_CHATS_SET";
        public Map<GroupID, Set<WonderlandClientID>> groups = new HashMap<GroupID, Set<WonderlandClientID>>();
    }

    private static class NextGroupID implements ManagedObject, Serializable {
        public static final String ID="NEXT_GROUP_CHAT_ID";
        public long next;
    }

}
