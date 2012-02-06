/*
 * Project Wonderland
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.userlist.client.views;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * A panel for changing the displayed alias for a user.
 *
 * @author jp
 * @author nsimpson
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * @author JagWire
 */
public class ChangeNamePanel extends javax.swing.JPanel implements ChangeNameView {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
    
    public ChangeNamePanel() {
        initComponents();
    }

    public ChangeNamePanel(String username) {

        this();

        String text = BUNDLE.getString("Change_Alias_For");
        text = MessageFormat.format(text, username);
        aliasLabel.setText(text);
        usernameAliasTextField.setText(username);
        setVisible(true);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        usernameAliasTextField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        aliasLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        nameLabel.setFont(nameLabel.getFont());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        nameLabel.setText(bundle.getString("ChangeNamePanel.nameLabel.text")); // NOI18N

        cancelButton.setText(bundle.getString("ChangeNamePanel.cancelButton.text")); // NOI18N

        okButton.setText(bundle.getString("ChangeNamePanel.okButton.text")); // NOI18N

        aliasLabel.setFont(aliasLabel.getFont().deriveFont(aliasLabel.getFont().getStyle() | java.awt.Font.BOLD));
        aliasLabel.setText(bundle.getString("ChangeNamePanel.aliasLabel.text")); // NOI18N

        statusLabel.setFont(statusLabel.getFont());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(aliasLabel)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(nameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(usernameAliasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(241, Short.MAX_VALUE)
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(okButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(aliasLabel)
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameAliasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aliasLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField usernameAliasTextField;
    // End of variables declaration//GEN-END:variables


    public void setStatusLabel(String text) {
//        throw new UnsupportedOperationException("Not supported yet.");
        statusLabel.setText(text);
    }

    public String getAliasFieldText() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return usernameAliasTextField.getText();
    }

    public void addOKButtonActionListener(ActionListener listener) {
//        throw new UnsupportedOperationException(
//        "Not supported yet.");
        okButton.addActionListener(listener);
    }

    public void addCancelButtonActionListener(ActionListener listener) {
//        throw new UnsupportedOperationException("Not supported yet.");
        cancelButton.addActionListener(listener);
    }

    public void addAliasTextFormActionListener(ActionListener listener) {
//        throw new UnsupportedOperationException("Not supported yet.");
        usernameAliasTextField.addActionListener(listener);
    }
}
