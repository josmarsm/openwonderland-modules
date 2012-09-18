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
package org.jdesktop.wonderland.modules.grouptools.client;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.audiomanager.client.PresenceControls;
import org.jdesktop.wonderland.modules.grouptools.common.GroupChatMessage;
import org.jdesktop.wonderland.modules.grouptools.common.GroupToolsConnectionMessage;
import org.jdesktop.wonderland.modules.grouptools.common.GroupToolsConnectionType;
import org.jdesktop.wonderland.modules.grouptools.common.RequestUsersFromGroupMessage;
import org.jdesktop.wonderland.modules.grouptools.common.UsersInGroupMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupUtils;
import org.jdesktop.wonderland.modules.textchat.client.ChatManager;
import org.jdesktop.wonderland.modules.userlist.client.HUDTabbedPanel;

/**
 *
 * @author Ryan Babiuch
 */
public class GroupToolsConnection extends BaseConnection {
    private static List<GroupChatConnectionListener> listeners = null;
    private static GroupToolsConnection instance;
    private GroupChatManager chatManager;
    private Queue<GroupChatMessage> backBuffer = new LinkedList();
   

    private GroupToolsConnection() {
        listeners = new ArrayList<GroupChatConnectionListener>();
    }
    public static GroupToolsConnection getInstance() {
        if(instance == null) {
            instance = new GroupToolsConnection();
        }
        return instance;
    }
    public ConnectionType getConnectionType() {
        return GroupToolsConnectionType.GROUP_TYPE;
    }

    public void send(String s) {
        
    }

    public void setChatManager(GroupChatManager manager) {
        this.chatManager = manager;
    }

    public GroupChatManager getChatManager() {
        return chatManager;
    }

    public void handleMessage(Message message) {

        if(message instanceof GroupToolsConnectionMessage) {
            //populate the groups list

            System.out.println("Resolving groups... ");
            Set<GroupDTO> allGroups = new LinkedHashSet<GroupDTO>();
            allGroups.add(new GroupDTO("users"));

            Set<GroupDTO> userGroups = new LinkedHashSet<GroupDTO>();
            userGroups.add(new GroupDTO("users"));
            ServerSessionManager session = getSession().getSessionManager();

            try {
                allGroups.addAll(GroupUtils.getGroups(session.getServerURL(),
                                 null, false, session.getCredentialManager()));
                userGroups.addAll(GroupUtils.getGroupsForUser(session.getServerURL(),
                                 session.getUsername(), false, session.getCredentialManager()));
            } catch (Exception e) {
                System.out.println("Could not populate list.");
                e.printStackTrace();
            }
            System.out.println("Notifying listeners for group resolution");
            notifyListeners(allGroups, userGroups);

            notifyListeners((GroupToolsConnectionMessage)message);

        }
        else if(message instanceof UsersInGroupMessage) {
            //UsersInGroupMessage uigm = (UsersInGroupMessage)message;
            //initiateVoiceChat(uigm.getUsersInGroupMessage());
        }
        else if (message instanceof GroupChatMessage) {
            GroupChatMessage gcm = (GroupChatMessage)message;
            if(gcm.isBroadcast()) {
                showBroadcastMessage(gcm);
            } else {
                showChatMessage(gcm);
            }
        }

    }

    /**
     * Sends a non broadcasted chat message to clients in group. For practical
     * cases, this client should always be a member of the given group.
     *
     * @param group group to send message to
     * @param message message to be sent
     */
    public void sendChatMessage(String group, String message) {
        Set<String> chatGroup = new HashSet<String>();
        chatGroup.add(group);
        this.send(new GroupChatMessage(chatGroup,
                getSession().getUserID().getUsername(),
                message,
                false, //not broadcast
                null)); 

        System.out.println("TO: " + group +
                            "FROM: " + getSession().getUserID().getUsername() +
                            "MESSAGE: " +message);
    }


    /**
     * Sends a broadcasted chat message to clients in group. 
     * @param group
     * @param message
     */
    public void sendBroadcastMessage(Set<String> group, String message) {
        String userName = this.getSession().getUserID().getUsername();
        //String from = "Broadcast message from " + name;
        if(group.contains("All")) {
            this.send(new GroupChatMessage(null, userName, message, true, null));
            return;
        }
        this.send(new GroupChatMessage(group, userName, message, true, null));
    }

    /**
     * Checks to see if the message has already been received once on this client.
     * If it has already been received, return true. Otherwise, check to see if
     * there are already 10 items in the queue, if so remove the head of the
     * queue and add the message to the end of the list.
     * 
     * @param message the message to be checked
     * @return true if the message already exists in the backBuffer
     */
    public synchronized boolean inBackBuffer(GroupChatMessage message) {
        //for each message in the buffer
        
            for(GroupChatMessage m : backBuffer) {
                //if the keys match
                if(m.getKey() == null) {
                    System.out.println("NULL key in message buffer!");
                    return true;
                }
                else if(message.getKey() == null) {
                    System.out.println("NULL key in message!");
                    return true;
                    //we should really catch this
                }
                if(m.getKey().equals(message.getKey())) {
                    System.out.println("Comparing keys A: " + m.getKey()
                            + " and B: " +message.getKey());

                    System.out.println(" For message: " + message.getMessageBody());
                    return true;
                }
                //if they don't match...
            }

            // is there more than 10 items?
            if(backBuffer.size() >= 10) {
                //if so, remove the head..
                backBuffer.remove();
            }
            //...add the message to the queue
            backBuffer.add(message);

            return false;        
    }
    public void printSet(Set<String> set) {
        for(String s: set) {
            System.out.print(s + " ");
        }
        System.out.println("");
    }
    public synchronized Set<String> getCollisions(GroupChatMessage message) {

        //if the argument key is null, this is an error.
        if(message.getKey() == null) {
            return null;
        }
        //set to hold a union of all potential target groups
        Set<String> union = new HashSet();
        
        System.out.println("Message to groups");
        printSet(message.getToGroup());


        //for each message in buffer
        for (GroupChatMessage m : backBuffer) {
            //this is an error, continue loop
            if(m.getKey() == null) {
                //return new HashSet();
                continue;
            } //if this is a collision
            else if(m.getKey().equals(message.getKey())) {

                //get the groups from the current message in buffer
                System.out.println("Back buffer message groups: ");
                printSet(m.getToGroup());
                Set<String> difference = new HashSet<String>(message.getToGroup());

                //grab all the groups that are in message, but not in m.
                difference.removeAll(m.getToGroup());
                System.out.println("Difference of argument message and buffer message");
                printSet(difference);
                //add the groups to the union of all groups
                union.addAll(difference);
                

            }
        }

        System.out.println("Union: ");
                printSet(union);
        if(backBuffer.size() >= 10) {
            backBuffer.remove();
        }

        backBuffer.add(message);
        return union;
    }

    public void showChatMessage(final GroupChatMessage message) {

        if(inBackBuffer(message)) {
            return;
        }
        if(message.getToGroup() == null) {
            //everybody case
            ChatManager.getChatManager().textMessage(message.getMessageBody(),
                                                    message.getFrom(),
                                                    null);
            System.out.println("Text chat all, not implemented yet");
            //singular case
        } else {
            chatManager.textMessage(message.getMessageBody(), message.getFrom(),
                message.getToGroup().iterator().next());
        }
    }
    public void showBroadcastMessage(final GroupChatMessage message) {

        if(!inBackBuffer(message)) {
            //return;
            String name = getSession().getUserID().getUsername();
            if(!message.getFrom().equals(name)) {

                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                        BroadcastMessagePanel panel = new BroadcastMessagePanel(message);
                        HUDComponent component = mainHUD.createComponent(panel);
                        panel.setHUDComponent(component);
                        component.setDecoratable(true);

                        String to = new String();
                        if(message.getToGroup() == null) {
                            //don't do the below:
                            to = "all";
                        }
                        else {
                            for(String s: message.getToGroup()) {
                                to = to + s + " ";
                            }
                        }

                        component.setName("Broadcast From " + message.getFrom()
                        + " to " + to );
                        component.setPreferredLocation(Layout.CENTER);

                        mainHUD.addComponent(component);
                        component.setVisible(true);
                    }
                });
            }
 
            if(message.getToGroup() == null) {
                System.out.println("Broadcast to all!");
                ChatManager.getChatManager().textMessage(message.getMessageBody(),
                                            message.getFrom() + " (broadcast)",
                                            "");
                return;
            }
       
           for(String group : message.getToGroup()) {
               chatManager.textMessage(message.getMessageBody(),
                       message.getFrom() + " (broadcast)",
                       group);
           }
        }
        else if(!getCollisions(message).isEmpty() && getCollisions(message) != null) {
            for(String group : getCollisions(message)) {
                chatManager.textMessage(message.getMessageBody(),
                                        message.getFrom() + " (broadcast)",
                                        group);
            }
        }
    }

    public void initiateVoiceChat(String groupName, PresenceControls pControls) {

        try {
            UsersInGroupMessage msg = (UsersInGroupMessage)this.sendAndWait(
                    new RequestUsersFromGroupMessage(groupName));

            if(!msg.getUsersInGroupMessage().isEmpty()) {
                Set<BigInteger> clients = msg.getUsersInGroupMessage();
                ArrayList<PresenceInfo> infos = new ArrayList();

                for(BigInteger id : clients) {
                    infos.add(pControls.getPresenceManager().getPresenceInfo(id));
                }
        
                pControls.startVoiceChat(infos, HUDTabbedPanel.getInstance().getHUDComponent());
            }
            else {
                System.out.println("Error occured");
            }

        } catch(Exception e) {
            System.out.println("Connection Interrupted during initiateVoiceChat");
            e.printStackTrace();

        }
    }
    public static void addGroupChatConnectionListener(GroupChatConnectionListener l) {
        synchronized(listeners) {
                listeners.add(l);
        }
    }

    public static void removeGroupChatConnectionListener(GroupChatConnectionListener l)
    {
        synchronized(listeners) {
            if(listeners.contains(l)) {
                listeners.remove(l);
            }
        }
    }

    private void notifyListeners(Set<GroupDTO> allGroups, Set<GroupDTO> myGroups) {
        for(GroupChatConnectionListener listener : listeners) {
            listener.groupsReceived(allGroups, myGroups);
        }
    }
    private void notifyListeners(GroupToolsConnectionMessage message) {
        if(message.getGroupLogs() == null) {
            return;
        }
        for(GroupChatConnectionListener listener : listeners) {
            listener.connected(message);
        }
    }

    public interface GroupChatConnectionListener {
        public void connected(GroupToolsConnectionMessage message);
        public void groupsReceived(Set<GroupDTO> allGroups, Set<GroupDTO> myGroups);
    }
}
