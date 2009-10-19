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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class TextChatJPanel extends javax.swing.JPanel {

    private Logger logger = Logger.getLogger(TextChatJPanel.class.getName());
    private TextChatConnection textChatConnection = null;
    private String localUser = null;
    private GroupID group = null;

    private int unreadMessages = 0;

    private boolean selected = false;

    /** Creates new form TextChatJPanel */
    public TextChatJPanel() {
        initComponents();

        // Listen for a Return on the text entry field, and click the Send
        // button
        messageTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });

        // Listen for the click of the Send button, and send the message to
        // the server. We immediately display the message locally since it is
        // not mirrored from the server for the sending client.
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = messageTextField.getText();
                textChatConnection.sendTextMessage(text, localUser, group);
                messageTextField.setText("");
                appendTextMessage(text, localUser);
            }
        });

        // Pick a good initial size for the frame
        setSize(300, 450);
    }

    /**
     * Adds a text message, given the user name and message to the chat window.
     */
    public void appendTextMessage(String message, String userName) {

        // This distinction is so we can send notification messages
        // like when people join/leave a chat session without
        // having to prepend a username in front of it. 
        String msg;
        if(userName!=null)
            msg = userName + ": " + message + "\n";
        else
            msg = message + "\n";

        messageTextArea.append(msg);
        messageTextArea.setCaretPosition(messageTextArea.getText().length());

        if(!selected)
            unreadMessages++;
    }

    /**
     * Makes the text frame active, giving the text chat connection, the name
     * of the local user and the name of the remote user (empty string if for
     * all users).
     *
     * @param connection The text chat communications connection
     * @param localUser The user name of this user
     * @param remoteUser The user name of the other user
     */
    public void setActive(TextChatConnection connection, String localUser,
            GroupID recipient) {

        this.textChatConnection = connection;
        this.localUser = localUser;
        this.group = recipient;
        messageTextField.setEnabled(true);
        sendButton.setEnabled(true);

        // Set the title based upon the remote user. If the remote user is "",
        // then insert "Everyone"
//
//        String title = "Text Chat Error";
//
//        if(this.group.equals(new GroupID(GroupID.GLOBAL_GROUP_ID)))
//            title = "Global Chat";
//        else
//            title = "Group Chat " + group;
//
//        setTitle(title);
    }

    /**
     * Deactivates the chat by displaying a message and turning off the GUI.
     */
    public void deactivate() {

        //TODO Fix this to be recipient aware.
//        String date = new SimpleDateFormat("h:mm a").format(new Date());
//
//        String msg = "--- User " + recipient.getUser() + " has left the world at " +
//                date + " ---\n";
//        messageTextArea.append(msg);


        messageTextField.setEnabled(false);
        sendButton.setEnabled(false);
    }

    /**
     * Re-activates the chat by displaying a message and turning on the GUI.
     */
    public void reactivate() {

        //TODO Fix this to be recipient aware.
//        String date = new SimpleDateFormat("h:mm a").format(new Date());
//        String msg = "--- User " + recipient.getUser() + " has joined the world at " +
//                date + " ---\n";
//        messageTextArea.append(msg);

        messageTextField.setEnabled(true);
        sendButton.setEnabled(true);
    }

    public String getTitle() {
        String messages = "";

        if(unreadMessages > 0) {
            messages = " (" + unreadMessages + ")";
        }

        if(group==null)
            return "";
        
        if(group.equals(new GroupID(GroupID.GLOBAL_GROUP_ID)))
            return "Global Chat" + messages;
        else if(group.getLabel() != null && !group.getLabel().equals("")) {
            logger.warning("getting title, and have non-null group label: " + group.getLabel());
            return group.getLabel();
        }
        else
            return "Group Chat " + group.toString() + messages;
    }

    public Icon getIcon() {
        // Just a stub for now. Add in an icon later
        // for tabs that have unread messages.
        if(unreadMessages > 0) return null;
        else return null;
    }

    void setSelected(boolean selected) {

        // If we're not currently selected and we're being set to selected
        // then set the unread messages to 0.
        
        if(!this.selected && selected)
            this.unreadMessages = 0;
        this.selected = selected;
    }

    public void setGroup(GroupID group) {
        this.group = group;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageTextArea = new javax.swing.JTextArea();
        textEntryPanel = new javax.swing.JPanel();
        messageTextField = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(4000, 3000));
        setMinimumSize(new java.awt.Dimension(400, 300));
        setPreferredSize(new java.awt.Dimension(400, 300));

        mainPanel.setMinimumSize(new java.awt.Dimension(400, 300));
        mainPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 50));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 300));

        messageTextArea.setColumns(20);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(5);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setPreferredSize(null);
        jScrollPane1.setViewportView(messageTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        mainPanel.add(jScrollPane1, gridBagConstraints);

        textEntryPanel.setLayout(new java.awt.GridBagLayout());

        messageTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        messageTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        textEntryPanel.add(messageTextField, gridBagConstraints);

        sendButton.setText("Send");
        sendButton.setEnabled(false);
        textEntryPanel.add(sendButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        mainPanel.add(textEntryPanel, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextArea messageTextArea;
    private javax.swing.JTextField messageTextField;
    private javax.swing.JButton sendButton;
    private javax.swing.JPanel textEntryPanel;
    // End of variables declaration//GEN-END:variables

}
