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

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.modules.grouptools.client.GroupToolsConnection.GroupChatConnectionListener;
import org.jdesktop.wonderland.modules.grouptools.common.GroupChatMessage;
import org.jdesktop.wonderland.modules.grouptools.common.GroupToolsConnectionMessage;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.textchat.client.ChatManager;

/**
 * Manages text chat windows for the client's groups
 *
 * @author Ryan Babiuch
 */
public class GroupChatManager implements GroupChatConnectionListener {
    //private JFrame chatFrame = null;
   // private JTabbedPane tabbedChatPane = null;
    private GroupListHUDPanel groupList = null;
    private HashMap<String, GroupChatJPanel> groupChatPanelMap = null;
    private HashMap<String, HUDComponent> groupChatHUDMap = null;
    //private GroupToolsConnection groupToolsConnection = null;
    private String userName = null;
    private ServerSessionManager loginInfo = null;
    private SessionLifecycleListener sessionListener = null;
    private HashSet<String> myGroups = null;
    private HashSet<String> allGroups = null;


    private Map<Integer, Timer> flashTimers = new HashMap<Integer, Timer>();

    private static Color TAB_BACKGROUND = Color.GRAY;
    private static Color TAB_FOREGROUND = Color.GRAY;

    private static final Color TAB_FLASH_BACKGROUND = Color.RED;
    private static final Color TAB_FLASH_FOREGROUND = Color.RED;

    private int currentChatPanelIndex = 0;

    private HUDComponent component;
    private static Queue<String> notificationBuffer;

    public static Queue<String> getNotificationBuffer() {
        if(notificationBuffer == null) {
            notificationBuffer = new LinkedList();
        }
        return notificationBuffer;
    }

    public GroupChatManager(final ServerSessionManager loginInfo, GroupListHUDPanel groupList) {
        
        this.loginInfo = loginInfo;
        this.groupList = groupList;
        groupChatPanelMap = new HashMap();
        groupChatHUDMap = new HashMap();
        myGroups = new HashSet();
        allGroups = new HashSet();

        sessionListener = new SessionLifecycleListener() {
            public void sessionCreated(WonderlandSession session) {

            }

            public void primarySession(WonderlandSession session) {
                setPrimarySession(session);
            }

        };
        loginInfo.addLifecycleListener(sessionListener);

        if(loginInfo.getPrimarySession() != null) {
            setPrimarySession(loginInfo.getPrimarySession());
        }
    }

    public void unregister() {

        groupChatPanelMap.clear();
        groupChatHUDMap.clear();
        loginInfo.removeLifecycleListener(sessionListener);

    }

    public void createChatPanels(Set<String> groups) {

        for(String group : groups) {
           final GroupChatJPanel chatPanel = new GroupChatJPanel();
           chatPanel.setGroup(group);

           groupChatPanelMap.put(group, chatPanel);
           createHUDComponent(group, chatPanel);           
        }

    }
    public void createHUDComponent(final String group, final GroupChatJPanel panel) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                HUDComponent component = mainHUD.createComponent(panel);
                component.setDecoratable(true);
                component.setPreferredLocation(Layout.SOUTH);
                component.setName("Group Chat: " +group);
                mainHUD.addComponent(component);

                groupChatHUDMap.put(group, component);
            }
       });
    }

    private void setPrimarySession(WonderlandSession session) {
        userName = session.getUserID().getUsername();

         groupList.setConnection(GroupToolsConnection.getInstance());
         GroupToolsConnection.getInstance().addGroupChatConnectionListener(this);
       GroupToolsConnection.getInstance().setChatManager(this);
        try {
            GroupToolsConnection.getInstance().connect(session);
        } catch(ConnectionFailureException e) {
            System.out.println("Unable to establish connection to the GroupTools " +
                    "connection");
            return;
        }
    }

    public void startChat(final String group) {
        synchronized(groupChatPanelMap) {

            if(group.equals("All")) {
//                ChatManager.getChatManager().showTextChatAll();
                ChatManager.getChatManager().startChat("");
                return;
            }
           GroupChatJPanel panel = groupChatPanelMap.get(group);
            if(panel != null) {
                reactivateChat(group);
                return;
            }
            //This should never really get executed
            System.out.println("This should never get executed.");
            final GroupChatJPanel newPanel = new GroupChatJPanel();
            final String key = group;

            groupChatPanelMap.put(group, newPanel);
            
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                    HUDComponent component = mainHUD.createComponent(newPanel);
                    component.setDecoratable(true);
                    component.setPreferredLocation(Layout.SOUTH);
                    component.setName("Group Chat: " +group);
                    mainHUD.addComponent(component);
                    
                    newPanel.setActive(GroupToolsConnection.getInstance(), userName, group);
                    component.setVisible(true);
                }
            });            
        }        
    }

    public void deactivateChat(String group) {
        synchronized(groupChatPanelMap) {
           final GroupChatJPanel panel = groupChatPanelMap.get(group);
           final HUDComponent hudComponent = groupChatHUDMap.get(group);
            if(panel == null) {
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    hudComponent.setVisible(false);
                    panel.deactivate();
                }
            });
        }
    }

    public void reactivateChat(final String group) {
        synchronized(groupChatPanelMap) {
            final GroupChatJPanel panel = groupChatPanelMap.get(group);
            final HUDComponent hudComponent = groupChatHUDMap.get(group);
            if(panel == null) {
                return;
            }

            if(hudComponent != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.reactivate();
                        hudComponent.setVisible(true);
                    }
                });
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                        HUDComponent component = mainHUD.createComponent(panel);
                        component.setDecoratable(true);
                        component.setPreferredLocation(Layout.SOUTH);
                        component.setName("Group Chat: " + group);
                        mainHUD.addComponent(component);
                        component.setVisible(true);
                        panel.reactivate();
                    }
                });
            }
            
           // ((TextChatTab)this.tabbedChatPane.getTabComponentAt(tabbedChatPane.indexOfComponent(panel))).setCloseButtonEnabled(false);
        }
    }

    public void textMessage(final String message, final String from, String toGroup) {
        synchronized(groupChatPanelMap) {
            if(toGroup == null) {
                //send to Text Chat All
                ChatManager.getChatManager().textMessage(message, from, "");
                return;
            }
            final GroupChatJPanel panel = groupChatPanelMap.get(toGroup);
            //int tabIndex = this.tabbedChatPane.indexOfComponent(panel);
            if(panel == null) {
                System.out.println("Received a chat message for a group which" +
                        " doesn't have a frame.");
            }

            if(panel != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.appendTextMessage(message, from);
                    }
                });

                HUDComponent c = groupChatHUDMap.get(toGroup);
                if(c != null && c.isVisible()) {
                    reactivateChat(toGroup);
                } else {
                    //show notification
                    String notification = "Unread message(s) for " + toGroup;
                    //getNotificationBuffer().add(notification);
                    showNotification(notification);
                }
               // panel.appendTextMessage(message, from);

            }
        }
    }
    public void showNotification(final String message) {
        // be sure to update the buffer on the AWT event thread to avoid
        // synchronization issues
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // check if this is the first message before we add anything
                // the the buffer
                boolean firstMessage = getNotificationBuffer().isEmpty();

                // if this same message is already in the buffer, don't add
                // it again
                if (getNotificationBuffer().contains(message)) {
                    return;
                }

                // add our message to the buffer. The message will stay in
                // the buffer until the notification worker removes it
                // when the message is done displaying
                getNotificationBuffer().add(message);

                if (firstMessage) {
                    // if this is the first message in the queue, display it
                    // and schedule a worker to check the queue when the message
                    // disappears
                    HUDComponent component = doShowNotification(message);
                    new BackgroundNotificationWorker(component, 5 * 1000).execute();
                }
            }
        });
    }

    // must be called from AWT event thread
    static HUDComponent doShowNotification(String message) {
        // display in the HUD
        HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
        NotifyPanel panel = new NotifyPanel(message);

        HUDComponent component = hud.createComponent(panel);
        component.setDecoratable(false);
        component.setPreferredLocation(Layout.NORTHEAST);
        hud.addComponent(component);
        component.setVisible(true);

        return component;
    }

    /**
     * Populate my group's panels with logs
     * @param message
     */
    public void connected(GroupToolsConnectionMessage message) {
        //populate panels with group names and logs
        
        Map<String, List<GroupChatMessage>> logs = message.getGroupLogs();
        
        for(String group : logs.keySet()) {
            if(myGroups.contains(group)) {
                for(GroupChatMessage msg : logs.get(group)) {
                    textMessage(msg.getMessageBody(), msg.getFrom(), group);
                }
            }
        }
    }


    public void groupsReceived(Set<GroupDTO> allGroups, Set<GroupDTO> myGroups) {
        
        for(GroupDTO group : allGroups) {
            this.allGroups.add(group.getId());
        }

        for(GroupDTO group : myGroups) {
            this.myGroups.add(group.getId());
        }

        if(this.myGroups.contains("admin")) {
            createChatPanels(this.allGroups);
        }
        else {
            createChatPanels(this.myGroups);
        }

    }
}
