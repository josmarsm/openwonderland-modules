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
package org.jdesktop.wonderland.modules.grouptextchat.client;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupChatMessage;
import org.jdesktop.wonderland.modules.grouptextchat.common.TextChatMessage;
import org.jdesktop.wonderland.modules.grouptextchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

/**
 * Client-side base connection for text chat.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TextChatConnection extends BaseConnection {
    private static final Logger logger = Logger.getLogger(TextChatConnection.class.getName());

    /**
     * @inheritDoc()
     */
    public ConnectionType getConnectionType() {
        return TextChatConnectionType.CLIENT_TYPE;
    }

    /**
     * Sends a text chat message from a user to a user. If the "to" user name
     * is null or an empty string, the message is sent to all users.
     *
     * @param message The String text message to send
     * @param from The user name the message is from
     * @param to The user name the message is to
     */
    public void sendTextMessage(String message, String from, GroupID group) {
        super.send(new TextChatMessage(message, from, group));
    }

    /**
     * @inheritDoc()
     */
    public void handleMessage(Message message) {
        if (message instanceof TextChatMessage) {
            TextChatMessage msg = ((TextChatMessage)message);

            String text = msg.getTextMessage();
            String from = msg.getFromUserName();
            GroupID group = msg.getGroup();
            
            synchronized (listeners) {
                for (TextChatListener listener : listeners) {
                    listener.textMessage(text, from, group);
                }
            }
        }
        else if(message instanceof GroupChatMessage) {
            GroupChatMessage gcm = (GroupChatMessage)message;

            // Manage the UI appropriately. For now, just write a logger message.
            logger.info("Got GroupChatMessage: " + gcm.getAction() + " gid: " + gcm.getGroupID());


            switch(gcm.getAction()) {
                case WELCOME:
                    // Add the appropriate tab to the UI.
                    synchronized (listeners) {
                    for (TextChatListener listener : listeners) {

                        // We don't need to disambiguate between old chats that are restarting
                        // and new chats here - the ChatManager will do that for us (although
                        // we should also think about other potential listeners who might
                        // not track that information.)
                        listener.startChat(gcm.getGroupID());
                    }
                    }
                    break;
                case GOODBYE:
                    // Remove the appropriate tab from the UI.
                    synchronized (listeners) {
                    for (TextChatListener listener : listeners) {

                        // We don't need to disambiguate between old chats that are restarting
                        // and new chats here - the ChatManager will do that for us (although
                        // we should also think about other potential listeners who might
                        // not track that information.)
                        listener.deactivateChat(gcm.getGroupID());
                    }
                    }
                    break;
                case JOINED:
                    synchronized (listeners) {
                    for (TextChatListener listener : listeners) {
                        listener.userJoinedChat(gcm.getGroupID(), gcm.getName());
                    }
                    }
                    
                    break;
                case LEFT:
                    synchronized (listeners) {
                    for (TextChatListener listener : listeners) {
                        listener.userLeftChat(gcm.getGroupID(), gcm.getName());
                    }
                    }
                case LABEL:
                    synchronized (listeners) {
                    for (TextChatListener listener : listeners) {
                        listener.groupLabelChanged(gcm.getGroupID());
                    }
                    }
                    break;
                default:
                    logger.warning("Received GroupChatMessage with unknown action: " + gcm.getAction());
                    break;
            }

        }
    }

    private Set<TextChatListener> listeners = new HashSet();

    /**
     * Adds a new listener for text chat messages. If the listener is already
     * present, this method does nothing.
     *
     * @param listener The listener to add
     */
    public void addTextChatListener(TextChatListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for text chat messages. If the listener is not
     * present, this method does nothing.
     *
     * @param listener The listener to remove
     */
    public void removeTextChatListener(TextChatListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Listener for text chat messages
     */
    public interface TextChatListener {

        /**
         * The name of a group chat has changed.
         * 
         * @param groupID
         */
        public void groupLabelChanged(GroupID groupID);

        /**
         * A text message has been received by the client, given the user name
         * the message is from and the user name the message is to (empty string
         * if for everyone.
         *
         * @param message The String text message
         * @param from The String user name from which the message came
         * @param to The String user name to which the message is intended
         */
        public void textMessage(String message, String from, GroupID group);

        /**
         * A message indicating that a chat has been opened. Includes the
         * groupID of that chat.
         *
         * @param group The GroupID for the new chat group.
         */
        public void startChat(GroupID group);

        /**
         * A message indicating that this client has left the specified chat
         * group.
         * 
         * @param group The group chat that is ending.
         */
        public void deactivateChat(GroupID group);

        /**
         * A message indicating that a remote user has joined a chat group.
         * @param group
         * @param userName
         */
        public void userJoinedChat(GroupID group, String userName);

        /**
         * A message indicating that a remote user has left a chat group.
         *
         * @param group
         * @param userName
         */
        public void userLeftChat(GroupID group, String userName);

    }
}
