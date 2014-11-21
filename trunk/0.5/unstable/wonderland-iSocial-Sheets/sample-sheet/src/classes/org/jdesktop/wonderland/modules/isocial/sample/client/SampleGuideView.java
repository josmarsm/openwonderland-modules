/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.sample.client;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.sample.common.SampleSheet;

/**
 * Sample guide view.
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 */
@View(value=SampleSheet.class, roles={Role.GUIDE, Role.ADMIN})
public class SampleGuideView extends javax.swing.JPanel
        implements SheetView, ResultListener
{
    private static final Logger LOGGER =
            Logger.getLogger(SampleGuideView.class.getName());

    private final DefaultComboBoxModel resultsModel;

    private ISocialManager manager;
    private Sheet sheet;
    private Role role;

    private SampleViewPanel panel;

    /** Creates new form SampleGuideView1 */
    public SampleGuideView() {
        initComponents();

        resultsModel = new DefaultComboBoxModel();
        studentsCB.setModel(resultsModel);
    }

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;
        
        manager.addResultListener(sheet.getId(), this);
        try {
            for (Result r : manager.getResults(sheet.getId())) {
                resultsModel.addElement(new NamedResult(r));
            } 
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Errror getting results", ioe);
        }

        panel = new SampleViewPanel(manager, sheet);
        panel.setEditable(false);

        // see if there is a result
        NamedResult nr = (NamedResult) studentsCB.getSelectedItem();
        if (nr != null) {
            panel.setResult(nr.getResult());
        }


        add(panel, BorderLayout.CENTER);
    }

    public String getMenuName() {
        return ((SampleSheet) sheet.getDetails()).getName();
    }

    public boolean isAutoOpen() {
        return true;
    }

    public HUDComponent open(HUD hud) {
        return hud.createComponent(this);
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }

    public void resultAdded(final Result result) {
        resultsModel.addElement(new NamedResult(result));
    }

    public void resultUpdated(final Result result) {

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        studentsCB = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/isocial/sample/client/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("SampleGuideView.jLabel1.text")); // NOI18N

        studentsCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        studentsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentsCBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentsCB, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(studentsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void studentsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentsCBActionPerformed
        NamedResult nr = (NamedResult) studentsCB.getSelectedItem();
        if (nr != null && panel != null) {
            panel.setResult(nr.getResult());
        }
    }//GEN-LAST:event_studentsCBActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox studentsCB;
    // End of variables declaration//GEN-END:variables

    class NamedResult {
        private final Result result;

        public NamedResult(Result result) {
            this.result = result;
        }

        public Result getResult() {
            return result;
        }

        @Override
        public String toString() {
            return result.getCreator();
        }
    }
}
