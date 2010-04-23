/**
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

/*
 * SingleEventPanel.java
 *
 * Created on Jan 11, 2010, 11:03:11 AM
 */
package org.jdesktop.wonderland.modules.cmu.client.ui.events;

import java.awt.GridLayout;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponsePair;
import org.jdesktop.wonderland.modules.cmu.common.events.WonderlandEvent;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;

/**
 * Panel representing a single Wonderland event, along with the appropriate
 * response.
 * @author kevin
 */
public class SingleEventPanel extends javax.swing.JPanel {

    private final EventEditor parentFrame;
    private EventSettingsPanel settingsPanel = null;

    /** Creates new form SingleEventPanel */
    public SingleEventPanel(EventEditor parentFrame) {
        initComponents();
        this.parentFrame = parentFrame;
        this.populateEventList();
        this.populateResponseList();


        this.settingsContainer.setLayout(new GridLayout());
        this.eventTypeSelection.setSelectedIndex(0);
    }

    public CMUCell getParentCell() {
        if (this.getParentFrame() == null) {
            return null;
        }
        return this.getParentFrame().getParentCell();
    }

    public EventEditor getParentFrame() {
        return this.parentFrame;
    }

    public EventResponsePair getEventAndResponse() {
        if (this.settingsPanel == null) {
            return null;
        } else {
            WonderlandEvent event = settingsPanel.getEvent();
            CMUResponseFunction response = ((ResponseMenuItem) responseList.getSelectedItem()).getResponse();

            if (event == null || response == null) {
                return null;
            }

            return new EventResponsePair(event, response);
        }
    }

    @SuppressWarnings("unchecked")
    public void setEventAndResponse(EventResponsePair pair) {
        // Find the event menu item corresponding to this event type
        for (int i = 0; i < this.eventTypeSelection.getItemCount(); i++) {
            EventMenuItem item = (EventMenuItem) this.eventTypeSelection.getItemAt(i);


            if (pair.getEvent().getClass().isAssignableFrom(item.getEventPanel().getEventClass())) {
                this.eventTypeSelection.setSelectedIndex(i);
            }
        }

        // Set the settings fields appropriately
        this.settingsPanel.setEvent(pair.getEvent());

        // Find the response corresponding the the provided value, and select it
        for (int i = 0; i < this.responseList.getItemCount(); i++) {
            ResponseMenuItem item = (ResponseMenuItem) this.responseList.getItemAt(i);

            if (item.getResponse().equals(pair.getResponse())) {
                this.responseList.setSelectedIndex(i);
            }
        }
    }

    private void loadSettingsPanel() {

        if (this.settingsPanel != null) {
            this.settingsContainer.remove(this.settingsPanel);
        }

        this.settingsPanel = ((EventMenuItem) this.eventTypeSelection.getSelectedItem()).getEventPanel();
        this.settingsContainer.add(this.settingsPanel);

        this.populateResponseList();

        // Repack parent frame to update with new panel
        this.getParentFrame().setPreferredSize(this.getParentFrame().getSize());
        this.getParentFrame().pack();

        this.getParentFrame().repaint();
    }

    private void populateEventList() {
        this.eventTypeSelection.removeAllItems();

        this.eventTypeSelection.addItem(new EventMenuItem("Proximity", new ProximitySettingsPanel()));
        this.eventTypeSelection.addItem(new EventMenuItem("Menu", new ContextSettingsPanel()));
        this.eventTypeSelection.addItem(new EventMenuItem("Movement", new MovementSettingsPanel()));
    }

    private void populateResponseList() {
        this.responseList.removeAllItems();
        if (this.getParentCell() != null && this.getParentCell().getAllowedResponses() != null) {
            for (CMUResponseFunction response : this.getParentCell().getAllowedResponses()) {
                if (this.settingsPanel != null && this.settingsPanel.allowsResponse(response)) {
                    this.responseList.addItem(new ResponseMenuItem(response));
                }
            }
        }
    }

    private class EventMenuItem {

        private final String eventName;
        private final EventSettingsPanel eventPanel;

        public EventMenuItem(String eventName, EventSettingsPanel eventPanel) {
            this.eventName = eventName;
            this.eventPanel = eventPanel;
        }

        public String getEventName() {
            return eventName;
        }

        public EventSettingsPanel getEventPanel() {
            return eventPanel;
        }

        @Override
        public String toString() {
            return this.getEventName();
        }
    }

    private class ResponseMenuItem {

        private final CMUResponseFunction response;

        public ResponseMenuItem(CMUResponseFunction response) {
            this.response = response;
        }

        public CMUResponseFunction getResponse() {
            return this.response;
        }

        @Override
        public String toString() {
            return this.response.getFunctionName();
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

        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        eventTypeSelection = new javax.swing.JComboBox();
        removeButton = new javax.swing.JButton();
        responseList = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        settingsContainer = new javax.swing.JPanel();

        setBorder(null);
        setMinimumSize(new java.awt.Dimension(0, 119));

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setPreferredSize(new java.awt.Dimension(390, 150));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Event type:");

        eventTypeSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventTypeChanged(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(eventTypeSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 151, Short.MAX_VALUE)
                .addComponent(removeButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeButton)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(eventTypeSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel1.setText("call this method:");

        settingsContainer.setBorder(null);

        javax.swing.GroupLayout settingsContainerLayout = new javax.swing.GroupLayout(settingsContainer);
        settingsContainer.setLayout(settingsContainerLayout);
        settingsContainerLayout.setHorizontalGroup(
            settingsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 504, Short.MAX_VALUE)
        );
        settingsContainerLayout.setVerticalGroup(
            settingsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(responseList, 0, 368, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(settingsContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseList, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonPressed
        this.getParentFrame().removeEvent(this);
    }//GEN-LAST:event_removeButtonPressed

    private void eventTypeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventTypeChanged
        this.loadSettingsPanel();
    }//GEN-LAST:event_eventTypeChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox eventTypeSelection;
    private javax.swing.JButton removeButton;
    private javax.swing.JComboBox responseList;
    private javax.swing.JPanel settingsContainer;
    // End of variables declaration//GEN-END:variables
}
