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
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
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

    private static Logger logger = Logger.getLogger(TextChatConnectionHandler.class.getName());

    /**
     * Stores the classes that have registered as listening for new chat messages.
     */
    private Set<ManagedReference> listeners = new HashSet<ManagedReference>();

    /**
     * Stores the mapping between groupIDs and users in that group. Users are allowed
     * to be in an arbitrary number of groups. S
     */
    private Map<GroupID, Set<WonderlandClientID>> groups = new HashMap<GroupID, Set<WonderlandClientID>>();

    private long nextGroupID = GroupID.getFirstGroupID();

    public ConnectionType getConnectionType() {
        return TextChatConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
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

        tcm = this.processMessage(tcm, clientID);

        // Check to see if the message is for a specific person.
        // If it is, send to that person and return. Ignore the
        // group mechanics.
//        String toUser = tcm.getToUserName();

        // Otherwise, we need to send the message to a specific client, based
        // upon the "to" field. Loop through the list of clients and find the
        // one with the matching user name

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

        if(toGroup.equals(GroupID.getGlobalGroupID())) {
            recipients = sender.getClients();

            // now notify listeners of a new message. For now, listeners only
            // get global messages.

            // First, notify listeners of a new message. On the server side,
            // all listeners get all messages, even if they're sent to
            // specific people. It's up to listeners to decide what to do
            // with them.
            for(ManagedReference listenerRef : this.listeners) {
                TextChatMessageListener listener = (TextChatMessageListener)listenerRef.get();
                logger.info("Sending to listener: " + listener);
                listener.handleMessage(tcm);
            }
        } else {
            // If we're not on channel 0, then we should be on one of the other
            // channels. Check the groups map to figure out who the recipients
            // of a non-zero message should be.
            if(groups.containsKey(toGroup)) {
                recipients = groups.get(toGroup);
            } else {
                logger.warning("Received a message for GroupID " + toGroup + " but that group isn't a known group. Known Groups: " + groups.keySet());


                // Just make an empty set so the rest of the method works fine
                recipients = new HashSet<WonderlandClientID>();
            }
        }

        // Now send to everyone on our recipients list, minus the person who sent the message.
        recipients.remove(clientID);
        sender.send(recipients, message);
        return;
    }

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

                this.sendGlobalMessage("User: " + clientID + " joined GroupID: " + newGroup);
                return tcm;
            } else if(pieces[0].equals("leave")) {

                if(pieces.length != 2)
                    return tcm;

                GroupID group = new GroupID(Integer.parseInt(pieces[1]));

                this.removeUserFromChatGroup(group, clientID);
                this.sendGlobalMessage("User: " + clientID + " has left GroupID: " + group);
                return tcm;
            } else {

                GroupID groupID = new GroupID(Integer.parseInt(pieces[0]));

                logger.info("Setting groupID on message and passing it on: " + groupID);
                tcm.setGroup(groupID);
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
        logger.finer("Sending global message from " + from + ": " + msg);
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
        TextChatMessage textMessage = new TextChatMessage(msg, from, GroupID.getGlobalGroupID());
        sender.send(clientIDs, textMessage);
    }

    /**
     * Adds a listener object that will be called whenever a text chat message is sent.
     * Global messages sent from sendGlobalMessage are not included in these notifications.
     *
     * @param listener The listener object.
     */
    public void addTextChatMessageListener(TextChatMessageListener listener) {
        
        this.listeners.add(AppContext.getDataManager().createReference(listener));
    }

    /**
     * Removes the listener object from the list of listeners.
     * 
     * @param listener The listener object.
     */
    public void removeTextChatMessageListener(TextChatMessageListener listener) {
        this.listeners.remove(AppContext.getDataManager().createReference(listener));
    }

    /**
     * Adds the user with ClientID wcid to the ChatGroup with gid. If gid isn't a valid group
     * we log a warning but otherwise ignore it.
     *
     * @param gid The id of the chat group the client is joining.
     * @param wcid The id of the client.
     */
    public void addUserToChatGroup(GroupID gid, WonderlandClientID wcid) {
        if(groups.containsKey(gid)) {
            Set<WonderlandClientID> s = groups.get(gid);
            s.add(wcid);
            logger.info("Added user: " + wcid + " to group: " + gid + " userlist now: " + s);
            
            // I don't think I need to do this, but having weird issues.
            groups.put(gid, s);

            // Send a message to the client telling it to display a new tab on the client UI. 
        } else {
            logger.warning("Attempted to add user " + wcid + " to unknown text chat group " + gid + " (known groups: " + groups.keySet() + ")");
        }
    }

    /**
     * Removes the user with ClientID wcid from the ChatGroup with gid.
     *
     * @param gid The id of the chat group the client is leaving.
     * @param wcid The id of the client.
     */
    public void removeUserFromChatGroup(GroupID gid, WonderlandClientID wcid) {
        if(groups.containsKey(gid)) {
            Set<WonderlandClientID> s = groups.get(gid);
            s.remove(wcid);
            logger.info("Removed user: " + wcid + " from group: " + gid + " userlist now: " + s);

            // Send a message to the client telling it to remove the right tab on the client UI.


        } else {
            logger.warning("Attempted to remove user " + wcid + " to unknown text chat group " + gid);
        }
    }

    /**
     * Create a new chat group. This must be done before adding people to the group.
     *
     * @return The GroupID of the new group.
     */
    public GroupID createChatGroup() {
        GroupID gid = new GroupID(nextGroupID++);

        groups.put(gid, new HashSet<WonderlandClientID>());

        logger.info("Created group: " + gid + " (known groups: " + groups.keySet() + ") nextGroupID=" + nextGroupID + "(Handler: " + this + ")");
        return gid;
    }

    /**
     * Convenience method for checking if a group exists. Probably not necessary
     * for the final system, but useful for testing purposes.
     *
     * @param gid
     * @return
     */
    public boolean chatGroupExists(GroupID gid) {
        return groups.containsKey(gid);
    }

    /**
     * Convenience method that removes the specified user from all text chat groups.
     * Useful when a user logs off and we want to clean out the groups they were
     *
     * @param wcid
     */
    public void removeUserFromAllGroups(WonderlandClientID wcid) {
        for(GroupID gid : groups.keySet()) {
            Set<WonderlandClientID> s = groups.get(gid);

            // Not sure on the etiquette here - is it cheaper to check to see if
            // the set contains the user before trying to remove it?
            s.remove(wcid);
        }
    }


}
