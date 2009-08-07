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

package org.jdesktop.wonderland.modules.npc.client.cell;

import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellProperties;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.spi.CellPropertiesSPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.npc.common.NpcCellServerState;

/**
 * A property sheet for the sample cell type
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellProperties
public class NpcCellProperties extends javax.swing.JPanel implements CellPropertiesSPI {
    CellPropertiesEditor editor = null;

    /** Creates new form SampleCellProperties */
    public NpcCellProperties() {
        initComponents();

        // Listen for when the Browse... button is selected and display a
        // GUI to browser the content repository. Wait until OK has been
        // selected and fill in the text field with the URI
//        browseButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                // Fetch the browser for the webdav protocol and display it.
//                // Add a listener for the result and update the value of the
//                // text field for the URI
//                ContentBrowserManager manager = ContentBrowserManager.getContentBrowserManager();
//                final ContentBrowserSPI browser = manager.getDefaultContentBrowser();
//                browser.addContentBrowserListener(new ContentBrowserListener() {
//                    public void okAction(String uri) {
//                        uriTextField.setText(uri);
//                        browser.removeContentBrowserListener(this);
//                    }
//
//                    public void cancelAction() {
//                        browser.removeContentBrowserListener(this);
//                    }
//                });
//                browser.setVisible(true);
//            }
//        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        avatarComboBox = new javax.swing.JComboBox();

        jLabel1.setText("Choose Avatar:");

        avatarComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "assets/configurations/MaleD_CA_00_bin.xml", "assets/configurations/MaleD_CA_01_bin.xml", "assets/configurations/FemaleD_AZ_00_bin.xml", "assets/configurations/FemaleD_CA_00_bin.xml", "assets/configurations/FemaleFG_AA_01_bin.xml", "assets/configurations/FemaleFG_AA_02_bin.xml", "assets/configurations/FemaleFG_AA_03_bin.xml", "assets/configurations/FemaleFG_CA_00_bin.xml", "assets/configurations/FemaleFG_CA_01_bin.xml", "assets/configurations/FemaleFG_CA_02_bin.xml", "assets/configurations/FemaleFG_CA_03_bin.xml", "assets/configurations/FemaleFG_CA_04_bin.xml", "assets/configurations/MaleD_CA_00_bin.xml", "assets/configurations/MaleD_CA_01_bin.xml", "assets/configurations/MaleFG_AA_00_bin.xml", "assets/configurations/MaleFG_AA_01_bin.xml", "assets/configurations/MaleFG_CA_01_bin.xml", "assets/configurations/MaleFG_CA_03_bin.xml", "assets/configurations/MaleFG_CA_04_bin.xml", "assets/configurations/MaleMeso_00.xml", "assets/configurations/MaleMeso_01.xml" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(avatarComboBox, 0, 344, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(avatarComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(141, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox avatarComboBox;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    public Class getServerCellStateClass() {
       return NpcCellServerState.class;
    }

    public String getDisplayName() {
        return "NPC Cell";
    }

    public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
        this.editor = editor;
        editor.setPanelDirty(getServerCellStateClass(), true);
        return this;
    }

    public <T extends CellServerState> void updateGUI(T cellServerState) {
    }

    public <T extends CellServerState> void getCellServerState(T state) {
        if (state == null) {
            state = (T) new NpcCellServerState();
        }

        AvatarConfigComponentServerState acc = 
                (AvatarConfigComponentServerState) state.getComponentServerState(AvatarConfigComponentServerState.class);
        if (acc == null) {
            acc = new AvatarConfigComponentServerState();
            state.addComponentServerState(acc);
        }
  
        acc.setAvatarConfigURL((String) avatarComboBox.getSelectedItem());
    }
}