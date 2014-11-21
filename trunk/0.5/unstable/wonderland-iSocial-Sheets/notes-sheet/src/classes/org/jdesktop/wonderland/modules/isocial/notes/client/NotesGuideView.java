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
package org.jdesktop.wonderland.modules.isocial.notes.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultMetadata;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.notes.common.NotesResult;
import org.jdesktop.wonderland.modules.isocial.notes.common.NotesSheet;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * Sample guide view.
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 */
@View(value=NotesSheet.class, roles={Role.GUIDE, Role.ADMIN})
public class NotesGuideView extends javax.swing.JPanel
        implements SheetView, ResultListener, PresenceManagerListener
{
    private static final Logger LOGGER =
            Logger.getLogger(NotesGuideView.class.getName());

    private final DefaultComboBoxModel resultsModel;

    private ISocialManager manager;
    private Sheet sheet;
    private Role role;

    /** Creates new form SampleGuideView1 */
    public NotesGuideView() {
        initComponents();

        resultsModel = new DefaultComboBoxModel();
        studentsCB.setModel(resultsModel);

        notesArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                checkButtons();
            }

            public void changedUpdate(DocumentEvent e) {
                checkButtons();
            }
        });
    }

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;

        // listen for presence changes
        PresenceManager pm = PresenceManagerFactory.getPresenceManager(
                manager.getSession().getPrimarySession());
        pm.addPresenceManagerListener(this);

        // listen for submitted results
        manager.addResultListener(sheet.getId(), this);

        // add records for all existing users
        for (PresenceInfo pi : pm.getAllUsers()) {
            UserRecord record = new UserRecord(pi.getUserID().getUsername());
            int index = resultsModel.getIndexOf(record);
            if (index == -1) {
                resultsModel.addElement(record);
            }
        }

        // add any existing notes for existing users
        try {
            for (Result r : manager.getResults(sheet.getId())) {
                this.resultAdded(r);
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Errror getting results", ioe);
        }
    }

    public String getMenuName() {
        return "Guide Notes";
    }

    public boolean isAutoOpen() {
        return ((NotesSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        return hud.createComponent(this);
    }

    public void close() {
        PresenceManager pm = PresenceManagerFactory.getPresenceManager(
                manager.getSession().getPrimarySession());
        pm.removePresenceManagerListener(this);

        manager.removeResultListener(sheet.getId(), this);
    }

    public void resultAdded(final Result result) {
        UserRecord record = new UserRecord(result.getCreator());
        int index = resultsModel.getIndexOf(record);
        if (index == -1) {
            resultsModel.addElement(record);
        } else {
            record = (UserRecord) resultsModel.getElementAt(index);
        }

        record.setResult(result);

        if (studentsCB.getSelectedItem() == record) {
            setCurrentResult(result);
        }
    }

    public void resultUpdated(final Result result) {
        resultAdded(result);
    }

    public void presenceInfoChanged(PresenceInfo pi, ChangeType ct) {
        UserRecord record = new UserRecord(pi.getUserID().getUsername());
        if (resultsModel.getIndexOf(record) == -1) {
            resultsModel.addElement(record);
        }
    }

    public void setCurrentResult(Result result) {
        if (result == null) {
            notesArea.setText("");
            return;
        }

        NotesResult notes = (NotesResult) result.getDetails();
        notesArea.setText(notes.getNotes());

        checkButtons();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        notesArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/isocial/notes/client/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("NotesGuideView.jLabel1.text")); // NOI18N

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
                .addGap(18, 18, 18)
                .addComponent(studentsCB, 0, 344, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(studentsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);

        notesArea.setColumns(20);
        notesArea.setRows(5);
        jScrollPane1.setViewportView(notesArea);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(445, 29));

        saveButton.setText(bundle.getString("NotesGuideView.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(370, Short.MAX_VALUE)
                .addComponent(saveButton))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(saveButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void studentsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentsCBActionPerformed
        UserRecord record = (UserRecord) studentsCB.getSelectedItem();
        setCurrentResult(record.getResult());
    }//GEN-LAST:event_studentsCBActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        NotesResult details = new NotesResult();
        details.setNotes(notesArea.getText());

        UserRecord record = (UserRecord) studentsCB.getSelectedItem();
        Result result;

        try {
            if (record.getResult() == null) {
                // submit a new result with visibility set to hidden
                ResultMetadata metadata = new ResultMetadata();
                metadata.setVisibility(ResultMetadata.Visibility.HIDDEN);
                result = manager.submitResultAs(record.getName(), sheet.getId(), 
                                                details, metadata);
            } else {
                // update the existing result
                result = manager.updateResult(record.getResult().getId(), details);
            }

            record.setResult(result);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error setting result", ioe);
        }

        checkButtons();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void checkButtons() {
        // if there is no item selected, disable the save button
        UserRecord record = (UserRecord) studentsCB.getSelectedItem();
        if (record == null) {
            saveButton.setEnabled(false);
            return;
        }
        
        String text = notesArea.getText();
        
        // if the result has not been submitted, enable the save button
        // if there is any text typed
        if (record.getResult() == null) {
            saveButton.setEnabled(text.trim().length() > 0);
            return;
        }
        
        // otherwise, enable the save button if the text is different than
        // the current result notes
        NotesResult result = (NotesResult) record.getResult().getDetails();
        saveButton.setEnabled(!text.equals(result.getNotes()));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea notesArea;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox studentsCB;
    // End of variables declaration//GEN-END:variables

    // End of variables declaration

    class UserRecord {
        private final String name;
        private Result result;

        public UserRecord(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final UserRecord other = (UserRecord) obj;
            if ((this.name == null) ?
                (other.name != null) : !this.name.equals(other.name))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}
