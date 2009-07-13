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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.modules.grouptextchat.client.TextChatConnection.TextChatListener;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

/**
 * Manages all of the Text Chat windows for the client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class ChatManager implements TextChatListener {

    private static Logger logger = Logger.getLogger(ChatManager.class.getName());
    private WeakReference<ChatUserListJFrame> userListFrameRef = null;
    private JFrame chatFrame = null;
    private JTabbedPane tabbedChatPane = null;
    private Map<GroupID, TextChatJPanel> textChatPanelMap = null;
    private JMenu menu = null;
    private JMenuItem textChatMenuItem = null;
    private JMenuItem userListMenuItem = null;
    private TextChatConnection textChatConnection = null;
    private String localUserName = null;
    private ServerSessionManager loginInfo = null;
    private SessionLifecycleListener sessionListener = null;

    private Map<Integer, Timer> flashTimers = new HashMap<Integer, Timer>();

    private static Color TAB_BACKGROUND = Color.GRAY;
    private static Color TAB_FOREGROUND = Color.GRAY;

    private static final Color TAB_FLASH_BACKGROUND = Color.RED;
    private static final Color TAB_FLASH_FOREGROUND = Color.RED;

    private int currentChatPanelIndex = 0;

    /** Constructor */
    public ChatManager(final ServerSessionManager loginInfo) {
//        logger.warning("CONSTRUCTING CHAT MANAGER");

        this.loginInfo = loginInfo;
        textChatPanelMap = new HashMap();

        // Create a new Chat menu underneath the "Tools" menu
        menu = new JMenu("Chat");

        // First create the text chat frame and keep a weak reference to it so
        // that it gets garbage collected

        tabbedChatPane = new JTabbedPane();
        tabbedChatPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedChatPane.setPreferredSize(new Dimension(400, 300));
        tabbedChatPane.setMinimumSize(new Dimension(400, 300));

        // Add a change listener so we know when different tabs are selected.
        tabbedChatPane.addChangeListener(new ChangeListener () {

            public void stateChanged(ChangeEvent arg0) {
                tabSelected(tabbedChatPane.getSelectedIndex());
            }
           

        });

        chatFrame = new JFrame();
        chatFrame.setTitle("Chat");
        chatFrame.getContentPane().add(tabbedChatPane);
        chatFrame.setPreferredSize(new Dimension(400, 300));
        chatFrame.setMinimumSize(new Dimension(400, 300));
        
        // Make the global chat panel.
        final TextChatJPanel textChatJPanel = new TextChatJPanel();
        textChatJPanel.setSelected(true);

        // add it to the tab pane
        tabbedChatPane.addTab("Global Chat", textChatJPanel);
        tabbedChatPane.setTabComponentAt(0, new TextChatTab(tabbedChatPane));

        // Grab the starting colors, which might be L&F specific, so we
        // don't want to hardcode them. But we will assume that they're
        // the same for every tab.
        TAB_BACKGROUND = tabbedChatPane.getBackgroundAt(0);
        TAB_FOREGROUND = tabbedChatPane.getForegroundAt(0);

        textChatPanelMap.put(new GroupID(GroupID.GLOBAL_GROUP_ID), textChatJPanel);

        // Add the global text chat frame to the menu item. Listen for when it
        // is selected or de-selected and show/hide the frame as appropriate.
        textChatMenuItem = new JMenuItem("Text Chat All");
        textChatMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chatFrame.isVisible() == false) {
                    chatFrame.setVisible(true);
                }
            }
        });
        
        textChatMenuItem.setEnabled(false);
        menu.add(textChatMenuItem);

        // Add the user list frame to the menu item. Listen for when it is
        // selected or de-selected and show/hide the frame as appropriate.
        userListMenuItem = new JMenuItem("Private Text Chat");
        userListMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame userListJFrame = userListFrameRef.get();
                if (userListJFrame.isVisible() == false) {
                    userListJFrame.setVisible(true);
                }
            }
        });

        userListMenuItem.setEnabled(false);
        menu.add(userListMenuItem);

        // Add the Chat menu item to the "Window" menu
        JmeClientMain.getFrame().addToWindowMenu(menu, 2);

        // Wait for a primary session to become active. When it does, then
        // we enable the menu items and set the primary sessions on their
        // objects.
        sessionListener = new SessionLifecycleListener() {
            public void sessionCreated(WonderlandSession session) {
                // Do nothing for now
            }

            public void primarySession(WonderlandSession session) {
                setPrimarySession(session);
            }
        };
        loginInfo.addLifecycleListener(sessionListener);

        // XXX Check if we already have primary session, should be handled
        // by addLifecycleListener
        if (loginInfo.getPrimarySession() != null) {
            setPrimarySession(loginInfo.getPrimarySession());
        }
    }


    /**
     * Unregister any menus we have created, etc.
     */
    public void unregister() {
        // Close down and remove any existing windows, start with the user list
        // window

        // Commented out for now because I'm not actually using the userListJFrame
        // right now. I think we'll probably need to just rely on the main one
        // user list at some point, not the text-chat-provided one. 
//        JFrame userListJFrame = userListFrameRef.get();
//        userListJFrame.setVisible(false);
//        userListJFrame.dispose();

        // Close down all of the individual text chat windows
        JTabbedPane tabbedPane = this.tabbedChatPane;
        for (Map.Entry<GroupID, TextChatJPanel> entry :
            textChatPanelMap.entrySet()) {
                int tabLocation = tabbedPane.indexOfTabComponent(entry.getValue());

                // Remove them from the tabbed pane.
                if(tabLocation != -1)
                    tabbedPane.removeTabAt(tabLocation);
        }
            
        textChatPanelMap.clear();

        // remove the session listener
        loginInfo.removeLifecycleListener(sessionListener);

        // Remove the menu item
        JmeClientMain.getFrame().removeFromWindowMenu(menu);
    }

    /**
     * Sets the primary session, when it is made the primary session. This
     * turns on everything: enables the menu items, displays the global chat
     * dialog.
     */
    private void setPrimarySession(WonderlandSession session) {
        logger.finer("setting Primary Session: " + session);
        // Capture the local user name for later use
        localUserName = session.getUserID().getUsername();
        
        // Create a new custom connection to receive text chats. Register a
        // listener that handles new text messages. Will display them in the
        // window.
        textChatConnection = new TextChatConnection();
        textChatConnection.addTextChatListener(this);

        // Open the text chat connection. If unsuccessful, then log an error
        // and return.
        try {
            textChatConnection.connect(session);
        } catch (ConnectionFailureException excp) {
            logger.log(Level.WARNING, "Unable to establish a connection to " +
                    "the chat connection.", excp);
            return;
        }

        // Create the user list frame and keep a weak reference to it so that it
        // gets garbage collected
        final ChatUserListJFrame userListJFrame =
                new ChatUserListJFrame(session.getUserID(), this);
//        userListFrame = new WeakReference(userListJFrame);

        // Otherwise, enable all of the GUI elements. First enable the user list
        // frame by setting its session
        userListJFrame.setPrimarySession(session);
        userListMenuItem.setEnabled(true);

        // Next, for the global chat, set its information and make it visible
        // initially.
        TextChatJPanel globalChatPanel = textChatPanelMap.get(new GroupID(GroupID.GLOBAL_GROUP_ID));

        globalChatPanel.setActive(textChatConnection, localUserName, new GroupID(GroupID.GLOBAL_GROUP_ID));
        this.tabbedChatPane.setTitleAt(0, globalChatPanel.getTitle());

        textChatMenuItem.setEnabled(true);
        textChatMenuItem.setSelected(true);

        chatFrame.setVisible(true);
    }

    /**
     * Creates a new text chat window, given the remote participants user name
     * and displays it.
     *
     * @param remoteUser The remote participants user name
     */
    public void startChat(GroupID group) {
        // Do all of this synchronized. This makes sure that multiple text chat
        // window aren't create if a local user clicks to create a new text
        // chat and a message comes in for that remote user.
        synchronized (textChatPanelMap) {
            // Check to see if the text chat window already exists. If so, then
            // we do nothing and return.
            TextChatJPanel chatPanel = textChatPanelMap.get(group);
            
            if (chatPanel != null) {
                // activate the frame if it already exists.
                reactivateChat(group);
                return;
            }

            // Otherwise, create the frame, add it to the map, and display
            TextChatJPanel newPanel = new TextChatJPanel();
            final GroupID key = group;

            // Add this back in later. Though do we really want people to be able to close this window?
            // just going to cause troubles. 
//            chatFrame.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    // Remove from the map which will let it garbage collect
//                    synchronized (textChatFrameRefMap) {
//                        e.getWindow().dispose();
//                        textChatFrameRefMap.remove(key);
//                    }
//                }
//            });
            
            textChatPanelMap.put(group, newPanel);
            newPanel.setActive(textChatConnection, localUserName, group);

            this.tabbedChatPane.addTab("Group Chat " + group, newPanel);
            tabbedChatPane.setTabComponentAt(tabbedChatPane.indexOfComponent(newPanel), new TextChatTab(tabbedChatPane));
        }
    }

    /**
     * Deactivates the text chat given the GroupID of the chat, if such a frame
     * exists. Displays a message in the window and turns off its GUI.
     *
     * @param GroupID The groupID for the chat that should be deactivated.
     */
    public void deactivateChat(GroupID group) {
        // Do all of this synchronized, so that we do not interfere with the
        // code to create chats
        synchronized (textChatPanelMap) {
            // Check to see if the text chat window exists. If not, then do
            // nothing.
            TextChatJPanel chatPanel = textChatPanelMap.get(group);
            if (chatPanel == null) {
                return;
            }
            chatPanel.deactivate();

            ((TextChatTab)this.tabbedChatPane.getTabComponentAt(tabbedChatPane.indexOfComponent(chatPanel))).setCloseButtonEnabled(true);
        }
    }

    /**
     * Re-activates the text chat given the remote user's name, if such a frame
     * exists. Displays a message in the window and turns on its GUI.
     */
    public void reactivateChat(GroupID group) {
        // Do all of this synchronized, so that we do not interfere with the
        // code to create chats
        synchronized (textChatPanelMap) {
            // Check to see if the text chat window exists. If not, then do
            // nothing.
            TextChatJPanel chatPanel = textChatPanelMap.get(group);
            if (chatPanel == null) {
                return;
            }
            chatPanel.reactivate();

            ((TextChatTab)this.tabbedChatPane.getTabComponentAt(tabbedChatPane.indexOfComponent(chatPanel))).setCloseButtonEnabled(false);
        }
    }

    /**
     * @inheritDoc()
     */
    public void textMessage(String message, String fromUser, GroupID group) {
        // Fetch the frame associated with the user. If the "to" user is an
        // empty string, then this is a "global" or "group" message and we fetch its
        // frame. It should exist. We always add the message, no matter whether
        // the frame is visible or not.


        // Otherwise, the "toUser" is for this specific user. We fetch the
        // frame associated with the "from" user. If it exists (which also
        // means it is visible, then add the message.

        synchronized (textChatPanelMap) {
            
            TextChatJPanel chatPanel = textChatPanelMap.get(group);

            logger.finer("Message received, panel index: " + this.tabbedChatPane.indexOfComponent(chatPanel));
            int tabIndex = this.tabbedChatPane.indexOfComponent(chatPanel);
            
            if(chatPanel==null) logger.warning("Received a chat message for a group which doesn't have a frame. The client should only receive messages from a group after getting a WELCOME message from that group.");

            if (chatPanel != null) {
                chatPanel.appendTextMessage(message, fromUser);

                logger.finest("Got message, checking for selection: " + this.tabbedChatPane.getSelectedIndex() + " appended Text to index: " + this.tabbedChatPane.indexOfTabComponent(chatPanel));

                    // Now check to see if we need to flash the tab. We do this only
                    // if the tab is not currently selected.
                if(chatPanel != this.tabbedChatPane.getSelectedComponent()) {
                    this.tabbedChatPane.setTitleAt(tabIndex, chatPanel.getTitle());
                    this.tabbedChatPane.setIconAt(tabIndex, chatPanel.getIcon());

//                    this.tabbedChatPane.getTabComponentAt(tabIndex).validate();
//                    this.tabbedChatPane.getTabComponentAt(tabIndex).repaint();

                    this.tabbedChatPane.validate();
                    this.tabbedChatPane.repaint();
                    this.tabbedChatPane.revalidate();
                }
            }
        }
    }

    /**
     * An event that fires when a tab is selected. We use this for managing
     * the unread-messages counting, primarily.
     * 
     * @param tabIndex The tab index of the tab that is selected.
     */
    private void tabSelected(int tabIndex) {        
        TextChatJPanel oldPane = (TextChatJPanel) this.tabbedChatPane.getComponentAt(currentChatPanelIndex);
        oldPane.setSelected(false);

        TextChatJPanel newPane = (TextChatJPanel) this.tabbedChatPane.getComponentAt(tabIndex);
        newPane.setSelected(true);

        this.tabbedChatPane.setTitleAt(tabIndex, newPane.getTitle());
        this.tabbedChatPane.setIconAt(tabIndex, newPane.getIcon());

        if(this.tabbedChatPane.getTabComponentAt(tabIndex) != null)
        {
//           this.tabbedChatPane.getTabComponentAt(tabIndex).validate();
//           this.tabbedChatPane.getTabComponentAt(tabIndex).repaint();

           this.tabbedChatPane.validate();
           this.tabbedChatPane.repaint();
           this.tabbedChatPane.revalidate();
        }
        
        currentChatPanelIndex = tabIndex;
    }

    public void userJoinedChat(GroupID group, String userName) {
        // Figure out which pane to send the message to and append away.
        TextChatJPanel chatPanel = this.textChatPanelMap.get(group);
        chatPanel.appendTextMessage("--- " + userName + " has joined ---", null);
    }

    public void userLeftChat(GroupID group, String userName) {
        TextChatJPanel chatPanel = this.textChatPanelMap.get(group);
        chatPanel.appendTextMessage("--- " + userName + " has left ---", null);
    }

    public void groupLabelChanged(GroupID groupID) {

        logger.warning("GROUP LABEL CHANGING, TESTING EXISTENCE: " + groupID + "; " + groupID.getLabel());
        if(this.textChatPanelMap.containsKey(groupID)) {
            // This little dance works because the newGroupID has a different label, but
            // having a different label but the same groupID will still evaluate to
            // equals.
            TextChatJPanel existingChatPanel = this.textChatPanelMap.get(groupID);
            this.textChatPanelMap.put(groupID, existingChatPanel);

            existingChatPanel.setGroup(groupID);

            // We need to force a redraw here, because otherwise the label won't
            // change until we click on it.
            logger.warning("NAME CHANGING, GROUP ID NOW: " + groupID.getLabel());
            logger.warning("name of existing chat panel: " + existingChatPanel.getTitle());

            this.tabbedChatPane.setTitleAt(tabbedChatPane.indexOfComponent(existingChatPanel), existingChatPanel.getTitle());
        }
    }

}
