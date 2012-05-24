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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.audiomanager.client.PresenceControls;
import org.jdesktop.wonderland.modules.grouptools.client.GroupToolsConnection.GroupChatConnectionListener;
import org.jdesktop.wonderland.modules.grouptools.common.GroupToolsConnectionMessage;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupUtils;

/**
 *
 * @author Ryan Babiuch
 */
public class GroupListHUDPanel extends javax.swing.JPanel implements
GroupChatConnectionListener {

    /** Creates new form GroupListHUDPanel */
    private PresenceControls controls = null;
    private Cell cell = null;
    private DefaultListModel listModel = null; // = new DefaultListModel();
//    private HUDComponent hudComponent = null;
    private GroupToolsConnection connection = null;
    private Set<GroupDTO> myGroups = new HashSet();
    public GroupListHUDPanel() {
        initComponents();
        listModel = new DefaultListModel();
        jList1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jList1.setModel(listModel);
        textButton.setEnabled(false);
        broadcastButton.setEnabled(false);

        jList1.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //textButton.setEnabled(true);
                //broadcastButton.setEnabled(true);
                boolean textVisible = true;
                boolean broadcastVisible = true;
                if(e.getValueIsAdjusting() == false) {
                    if(jList1.getSelectedIndex() == -1) {
                        textButton.setEnabled(false);
                        broadcastButton.setEnabled(false);
                        return;
                    }

                    if(groupsToStrings().contains("admin")) {
                        textButton.setEnabled(true);
                        broadcastButton.setEnabled(true);
                        return;
                    }

                    
                    List tmpList = Arrays.asList(jList1.getSelectedValues());
                    Set<String> selection = new HashSet(tmpList);
                    if(selection.contains("All") && selection.size() == 1) {
                        textButton.setEnabled(true);
                        broadcastButton.setEnabled(true);
                        return;
                    }
                    /*System.out.println("** My Groups **");
                    for(String s : groupsToStrings()) {
                        System.out.println("** " +s+ " **");
                    }*/
                   // System.out.println("* Selection(s) *");
                    for(String s : selection) {
                     //   System.out.println("* " + s + " *");
                        if(!groupsToStrings().contains(s)) {
                            //textButton.setEnabled(false);
                            textVisible = false;
                            break;
                        }
                    }
                    textButton.setEnabled(textVisible);
                    broadcastButton.setEnabled(broadcastVisible);
                }
            }
        });


    }

    public Set<String> getMyGroupNames() {
        Set<String> names = new HashSet();
        for(GroupDTO group: myGroups) {
            names.add(group.getId());
        }
        return names;
    }

    public Set<String> groupsToStrings() {
        Set<String> ss = new HashSet();
        for(GroupDTO group : myGroups) {
            ss.add(group.getId());
        }
        return ss;
    }

    public void setControls(PresenceControls controls, Cell cell) {
        this.controls = controls;
        this.cell = cell;
        
        
      //  populateList();
        
    }

    public void setConnection(GroupToolsConnection connection) {
        this.connection = connection;
        connection.addGroupChatConnectionListener(this);
    }

    public void populateList() {
        ServerSessionManager session = cell.getCellCache().getSession().getSessionManager();

        // retrieve the set of all groups and the set of this user's groups
        Set<GroupDTO> allGroups = new LinkedHashSet<GroupDTO>();
        allGroups.add(new GroupDTO("users"));

        Set<GroupDTO> userGroups = new LinkedHashSet<GroupDTO>();
        userGroups.add(new GroupDTO("users"));

        try {
            allGroups.addAll(GroupUtils.getGroups(session.getServerURL(),
                             null, false, session.getCredentialManager()));
            userGroups.addAll(GroupUtils.getGroupsForUser(session.getServerURL(),
                             session.getUsername(), false, session.getCredentialManager()));
        } catch (Exception e) {
            System.out.println("Could not populate list.");
            e.printStackTrace();
        }
        
        for( GroupDTO gto : allGroups) {
            listModel.addElement(gto.getId());
            //add strings to jList1
        }
    }

    /*
     * populates logs, not needed here
     */
    public void connected(GroupToolsConnectionMessage message) {

    }

    /*
     * Webserver tells us what groups we are a part of
     */
    public void groupsReceived(Set<GroupDTO> allGroups, Set<GroupDTO> myGroups) {
        for(GroupDTO group : allGroups) {
            if(group.getId().equals("users")) {
                //listModel.addElement("everybody");
                listModel.add(0, "All");
                continue;
            }
            else if(group.getId().equals("admin")) {
                listModel.add(1, "Admin");
                continue;
            }
            listModel.addElement(group.getId());
        }
        jList1.setSelectedIndex(0);
        this.myGroups = myGroups;
        System.out.println("groups received and added!");
        //not needed
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        textButton = new javax.swing.JButton();
        voiceChatButton = new javax.swing.JButton();
        broadcastButton = new javax.swing.JButton();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(43, 300));

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        textButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/grouptools/client/resources/UserListChatText24x24.png"))); // NOI18N
        textButton.setBorder(null);
        textButton.setMaximumSize(new java.awt.Dimension(24, 24));
        textButton.setMinimumSize(new java.awt.Dimension(24, 24));
        textButton.setPreferredSize(new java.awt.Dimension(24, 24));
        textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textButtonActionPerformed(evt);
            }
        });

        voiceChatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/grouptools/client/resources/UserListChatVoice24x24.png"))); // NOI18N
        voiceChatButton.setBorder(null);
        voiceChatButton.setEnabled(false);
        voiceChatButton.setMaximumSize(new java.awt.Dimension(24, 24));
        voiceChatButton.setMinimumSize(new java.awt.Dimension(24, 24));
        voiceChatButton.setPreferredSize(new java.awt.Dimension(24, 24));
        voiceChatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voiceChatButtonActionPerformed(evt);
            }
        });

        broadcastButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/grouptools/client/resources/BroadcastIcon24x24-1.png"))); // NOI18N
        broadcastButton.setBorder(null);
        broadcastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                broadcastButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(textButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(voiceChatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(broadcastButton)
                .addContainerGap(80, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(textButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(voiceChatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(broadcastButton))
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void voiceChatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voiceChatButtonActionPerformed

        String group = (String)listModel.getElementAt(jList1.getSelectedIndex());
        connection.initiateVoiceChat(group, controls);
    }//GEN-LAST:event_voiceChatButtonActionPerformed

    private void textButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textButtonActionPerformed
        // TODO add your handling code here:
        GroupToolsConnection.getInstance().getChatManager().startChat((String)jList1.getSelectedValue());
        
    }//GEN-LAST:event_textButtonActionPerformed

    private void broadcastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_broadcastButtonActionPerformed
        // TODO add your handling code here:
        Set<String> groups = new HashSet<String>();

        for(int i : jList1.getSelectedIndices()) {
            groups.add((String)listModel.getElementAt(i));
        }

         String title = new String();
         for(String s : groups) {
            title = title + " " + s;
         }

        // Changesd invokeAndWait() to invokeLater() to test functionality.
        try {
           // SwingUtilities.invokeAndWait(new Runnable() {
            //    public void run() {
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");                    
                    SendBroadcastPanel panel = new SendBroadcastPanel(groups);
                    HUDComponent component = mainHUD.createComponent(panel);
                    panel.setHUDComponent(component);
                    component.setDecoratable(true);
                    component.setName("Broadcast to" + title);
                    component.setPreferredLocation(Layout.CENTER);
                    mainHUD.addComponent(component);
                    //component.setEnabled(true);
                    component.setVisible(true);
         //       }
            //});
        } catch (Exception e) {
          System.out.println("Exception in broadcast button push");
          e.printStackTrace();
        }

    }//GEN-LAST:event_broadcastButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton broadcastButton;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton textButton;
    private javax.swing.JButton voiceChatButton;
    // End of variables declaration//GEN-END:variables

}
