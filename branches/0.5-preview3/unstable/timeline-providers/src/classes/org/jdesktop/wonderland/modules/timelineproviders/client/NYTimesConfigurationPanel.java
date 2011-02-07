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
package org.jdesktop.wonderland.modules.timelineproviders.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Configuration panel for the NY Times news provider
 * @author jkaplan
 */
public class NYTimesConfigurationPanel extends javax.swing.JPanel {

    private boolean ok = false;
    private PropertyChangeSupport listeners;

    public NYTimesConfigurationPanel() {
        initComponents();

        if (System.getProperty("nytimes.api.key") != null) {
            remove(apiKeyField);
            remove(apiKeyLabel);
        }
    }

    public void setAPIKey(String apiKey) {
        apiKeyField.setText(apiKey);
    }

    public String getAPIKey() {
        return apiKeyField.getText();
    }

    public int getResultLimit() {
        int limit = Integer.MAX_VALUE;

        if (return1RadioButton.isSelected()) {
            limit = 1;
        } else if (return2RadioButton.isSelected()) {
            limit = 2;
        } else if (return4RadioButton.isSelected()) {
            limit = 4;
        } else if (return6RadioButton.isSelected()) {
            limit = 6;
        } else if (return8RadioButton.isSelected()) {
            limit = 8;
        }
        return limit;
    }

    public void setResultLimit(int limit) {
        if (limit == 1) {
            return1RadioButton.setSelected(true);
        } else if (limit == 2) {
            return2RadioButton.setSelected(true);
        } else if (limit == 4) {
            return4RadioButton.setSelected(true);
        } else if (limit == 6) {
            return6RadioButton.setSelected(true);
        } else if (limit == 8) {
            return8RadioButton.setSelected(true);
        } else if (limit == Integer.MAX_VALUE) {
            returnAllRadioButton.setSelected(true);
        }
    }

    public boolean isOK() {
        return ok;
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tagsOrTextButtonGroup = new javax.swing.ButtonGroup();
        resultsButtonGroup = new javax.swing.ButtonGroup();
        searchByButtonGroup = new javax.swing.ButtonGroup();
        returnButtonGroup = new javax.swing.ButtonGroup();
        configurationLabel = new javax.swing.JLabel();
        returnLabel = new javax.swing.JLabel();
        return1RadioButton = new javax.swing.JRadioButton();
        return2RadioButton = new javax.swing.JRadioButton();
        return4RadioButton = new javax.swing.JRadioButton();
        return6RadioButton = new javax.swing.JRadioButton();
        return8RadioButton = new javax.swing.JRadioButton();
        returnAllRadioButton = new javax.swing.JRadioButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        apiKeyLabel = new javax.swing.JLabel();
        apiKeyField = new javax.swing.JTextField();

        configurationLabel.setFont(configurationLabel.getFont().deriveFont(configurationLabel.getFont().getStyle() | java.awt.Font.BOLD));
        configurationLabel.setText("New York Times Query Configuration");

        returnLabel.setText("Return:");

        returnButtonGroup.add(return1RadioButton);
        return1RadioButton.setText("1");

        returnButtonGroup.add(return2RadioButton);
        return2RadioButton.setText("2");

        returnButtonGroup.add(return4RadioButton);
        return4RadioButton.setText("4");

        returnButtonGroup.add(return6RadioButton);
        return6RadioButton.setText("6");

        returnButtonGroup.add(return8RadioButton);
        return8RadioButton.setSelected(true);
        return8RadioButton.setText("8");

        returnButtonGroup.add(returnAllRadioButton);
        returnAllRadioButton.setText("All items");

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        apiKeyLabel.setText("API Key:");

        apiKeyField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apiKeyFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configurationLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(apiKeyLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(apiKeyField, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(returnLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(return1RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(return2RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(return4RadioButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(return6RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(return8RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(returnAllRadioButton)))
                        .addGap(49, 49, 49))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configurationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(apiKeyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(apiKeyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(return2RadioButton)
                            .addComponent(return1RadioButton)
                            .addComponent(return4RadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(returnAllRadioButton)
                            .addComponent(return6RadioButton)
                            .addComponent(return8RadioButton)))
                    .addComponent(returnLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        ok = true;
        listeners.firePropertyChange("ok", new String(""), null);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        ok = false;
        listeners.firePropertyChange("cancel", new String(""), null);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void apiKeyFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apiKeyFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_apiKeyFieldActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField apiKeyField;
    private javax.swing.JLabel apiKeyLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JButton okButton;
    private javax.swing.ButtonGroup resultsButtonGroup;
    private javax.swing.JRadioButton return1RadioButton;
    private javax.swing.JRadioButton return2RadioButton;
    private javax.swing.JRadioButton return4RadioButton;
    private javax.swing.JRadioButton return6RadioButton;
    private javax.swing.JRadioButton return8RadioButton;
    private javax.swing.JRadioButton returnAllRadioButton;
    private javax.swing.ButtonGroup returnButtonGroup;
    private javax.swing.JLabel returnLabel;
    private javax.swing.ButtonGroup searchByButtonGroup;
    private javax.swing.ButtonGroup tagsOrTextButtonGroup;
    // End of variables declaration//GEN-END:variables
}