/*
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
package org.jdesktop.wonderland.modules.timeline.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineProviderUtils;

/**
 * A panel for selecting providers to add to a Timeline
 * @author nsimpson
 */
public class TimelineAddProviderHUDPanel extends javax.swing.JPanel {

    private static final Logger logger =
            Logger.getLogger(TimelineAddProviderHUDPanel.class.getName());
    private PropertyChangeSupport listeners;
    private Map<String, String> queryBuilders;
    private Map<String, String> providerMap;

    public TimelineAddProviderHUDPanel() {
        providerMap = new LinkedHashMap<String, String>();
        initComponents();
        providerList.setModel(new DefaultListModel());
        populateProviderList();
    }

    private void populateProviderList() {
        DefaultListModel model = (DefaultListModel) providerList.getModel();
        model.clear();
        providerMap.clear();

        queryBuilders = TimelineProviderUtils.getQueryBuilders();
        if (queryBuilders != null) {
            Iterator<String> iter = queryBuilders.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                logger.info("--- found query builder: " + queryBuilders.get(key));
                model.addElement(queryBuilders.get(key));
                providerMap.put(queryBuilders.get(key), key);
            }
        }
    }

    /**
     * Get an array of selected providers
     * @return an array of provider name strings
     */
    public String[] getProviders() {
        // TODO: handle multiple selection
        String providerName = (String) providerList.getSelectedValue();
        String providerClass = providerMap.get(providerName);

        return new String[]{providerClass};
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

        providerScrollPane = new javax.swing.JScrollPane();
        providerList = new javax.swing.JList();
        addProviderLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        providerScrollPane.setViewportView(providerList);

        addProviderLabel.setFont(addProviderLabel.getFont().deriveFont(addProviderLabel.getFont().getStyle() | java.awt.Font.BOLD));
        addProviderLabel.setText("Select a Timeline provider:");

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addProviderLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(providerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, cancelButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addProviderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        listeners.firePropertyChange("add", new String(""), null);
    }//GEN-LAST:event_addButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        listeners.firePropertyChange("cancel", new String(""), null);
    }//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel addProviderLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList providerList;
    private javax.swing.JScrollPane providerScrollPane;
    // End of variables declaration//GEN-END:variables
}