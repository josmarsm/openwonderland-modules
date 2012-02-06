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
package org.jdesktop.wonderland.modules.userlist.client;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.audiomanager.client.UserListHUDPanel;
import org.jdesktop.wonderland.modules.audiomanager.client.UserListPanel;
import org.jdesktop.wonderland.modules.userlist.client.views.WonderlandUserList;

/**
 *
 * @author Ryan Babiuch
 */
public class HUDTabbedPanel extends javax.swing.JPanel
    
{

    /** Creates new form HUDTabbedPanel */
    private Cell cell;
    private HUDComponent hudComponent;
    private WonderlandUserList userList;
    private static boolean configured = false;
    private static HUDTabbedPanel instance;



    private HUDTabbedPanel() {
        initComponents();
    }
    private HUDTabbedPanel(WonderlandUserList userList) {
        initComponents();

        this.userList = userList;
        
        addTab("users", userList);
        if(instance == null) {
            instance = this;
        }


    }
    public void uninitialize() {
        instance = null;
    }

    public static HUDTabbedPanel getInstance() {
        if(instance == null) {            
            instance = new HUDTabbedPanel();

        }
        return instance;
    }

    public WonderlandUserList getUserListHUDPanel() {
        return this.userList;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPanel = new javax.swing.JTabbedPane();

        setPreferredSize(new java.awt.Dimension(194, 300));

        tabbedPanel.setPreferredSize(new java.awt.Dimension(194, 300));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public JTabbedPane getTabbedPanel() {
        return tabbedPanel;
    }


    public void addTab(String caption, JPanel newTab) {
        int index = tabbedPanel.getSelectedIndex();
        tabbedPanel.addTab(caption, newTab);
        tabbedPanel.setSelectedIndex(index);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration//GEN-END:variables

}
