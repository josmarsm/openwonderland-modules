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
package org.jdesktop.wonderland.modules.keybindings.client;

import imi.character.avatar.AvatarContext.TriggerNames;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;

/**
 * A dialog box so that users can add a key binding.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AddKeyBindingJDialog extends javax.swing.JDialog {

    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;
    private int returnStatus = RET_CANCEL;

    // The Map of key event name to its integer value
    private Map<String, Integer> keyEventMap = null;

    // The Map of the trigger name to its enum value
    private Map<String, TriggerNames> triggerMap = null;

    // The value of the Integer key event when the OK button is pressed
    private Integer keyEvent = null;

    // The value of the Trigger Name when the OK button is pressed
    private TriggerNames trigger = null;

    // A comprehensive list of all VK_ key codes. There seems to be no way to
    // enumerate these directly from Java. I wish I knew a better way to do
    // this.
    private final Integer keyCodeArray[] = {
        KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
        KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7,
        KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_A, KeyEvent.VK_B,
        KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_F,
        KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J,
        KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N,
        KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R,
        KeyEvent.VK_S, KeyEvent.VK_T, KeyEvent.VK_U, KeyEvent.VK_V,
        KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z,

        // You can add more here!
    };

    /** Default constructor */
    public AddKeyBindingJDialog(JFrame frame, boolean modal) {
        super(frame, modal);
        initComponents();

        // Initialize the values of the key event combo box with all possible
        // key events. We put this in a list and sort it alphabetically. We
        // also must keep a Map so that we can translate back from the name
        // into the integer key code.
        keyEventMap = new HashMap<String, Integer>();
        for (int i : keyCodeArray) {
            keyEventMap.put(KeyEvent.getKeyText(i), i);
        }
        List<String> keyNameList = new LinkedList<String>(keyEventMap.keySet());
        Collections.sort(keyNameList);
        String[] nameArray = keyNameList.toArray(new String[] {});
        DefaultComboBoxModel keyNameModel = new DefaultComboBoxModel(nameArray);
        keyEventComboBox.setModel(keyNameModel);
        keyEventComboBox.setSelectedIndex(0);

        // Initialize the values of the trigger name combo box with all possible
        // key bindings. We put this in a list and sort it alphabetically. We
        // also must keep a Map so that we can translate back from the name
        // into the enum ordinal value.
        triggerMap = new HashMap<String, TriggerNames>();
        for (TriggerNames name : TriggerNames.values()) {
            triggerMap.put(name.toString(), name);
        }
        List<String> triggerList = new LinkedList<String>(triggerMap.keySet());
        Collections.sort(triggerList);
        String[] triggerArray = triggerList.toArray(new String[] {});
        DefaultComboBoxModel triggerModel = new DefaultComboBoxModel(triggerArray);
        triggerNameComboBox.setModel(triggerModel);
        triggerNameComboBox.setSelectedItem(0);
    }

    /**
     * Returns the value of the key event selected.
     *
     * @return The Integer key event
     */
    public Integer getKeyEvent() {
        return keyEvent;
    }

    /**
     * Returns the value of the trigger name selected.
     *
     * @return The Trigger Name
     */
    public TriggerNames getTriggerName() {
        return trigger;
    }

    /**
     * @return The return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
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
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        keyEventLabel = new javax.swing.JLabel();
        triggerNameLabel = new javax.swing.JLabel();
        keyEventComboBox = new javax.swing.JComboBox();
        triggerNameComboBox = new javax.swing.JComboBox();

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/placemarks/client/resources/Bundle"); // NOI18N
        setTitle(bundle.getString("AddEditPlacemarkJDialog.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/keybindings/client/resources/Bundle"); // NOI18N
        okButton.setText(bundle1.getString("AddKeyBindingJDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle1.getString("AddKeyBindingJDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        keyEventLabel.setText(bundle1.getString("AddKeyBindingJDialog.keyEventLabel.text")); // NOI18N

        triggerNameLabel.setText(bundle1.getString("AddKeyBindingJDialog.triggerNameLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(triggerNameLabel)
                    .add(keyEventLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(okButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(triggerNameComboBox, 0, 254, Short.MAX_VALUE)
                            .add(keyEventComboBox, 0, 254, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyEventComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keyEventLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(triggerNameLabel)
                    .add(triggerNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        // Find the currently selected item in the key event combo box and
        // convert into an integer key code
        String keyEventName = (String)keyEventComboBox.getSelectedItem();
        keyEvent = keyEventMap.get(keyEventName);

        // Find the currently selected item in the trigger name combo box and
        // convert into an enum
        String triggerName = (String)triggerNameComboBox.getSelectedItem();
        trigger = triggerMap.get(triggerName);
        
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox keyEventComboBox;
    private javax.swing.JLabel keyEventLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox triggerNameComboBox;
    private javax.swing.JLabel triggerNameLabel;
    // End of variables declaration//GEN-END:variables
}
