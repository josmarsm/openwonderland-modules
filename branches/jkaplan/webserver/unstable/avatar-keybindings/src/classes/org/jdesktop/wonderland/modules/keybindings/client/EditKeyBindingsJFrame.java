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
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 * A JFrame to allow editing of the avatar key bindings.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class EditKeyBindingsJFrame extends javax.swing.JFrame {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/keybindings/client/resources/Bundle");

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(EditKeyBindingsJFrame.class.getName());

    // A Map of avatar trigger name ordinal values and their display name. This
    // Map is necessary because the key bindings Map inside the avatar system
    // stores the ordinal value of the enum (which is wrong and should be
    // fixed in a future version).
    private Map<Integer, TriggerNames> triggerNameMap = null;

    // The model for the JTable to display the current key bindings map
    private KeyBindingsTableModel keyBindingsTableModel = null;

    // The JTable to display the current key bindings
    private JTable keyBindingsTable = null;

    /** Creates new form EditPlacemarksJFrame */
    public EditKeyBindingsJFrame() {
        initComponents();

        // First, create a Map that maps the key binding ordinal value to the
        // enum. This is because the avatar key bindings store the integer
        // ordinal value and we'll need to get a display string from it.
        triggerNameMap = new HashMap<Integer, TriggerNames>();
        for (TriggerNames triggerName : TriggerNames.values()) {
            triggerNameMap.put(triggerName.ordinal(), triggerName);
        }

        // Create the table to display the key bindings using an initially
        // empty key bindings map.
        keyBindingsTableModel = new KeyBindingsTableModel();
        keyBindingsTable = new JTable(keyBindingsTableModel);
        keyBindingsTable.setColumnSelectionAllowed(false);
        keyBindingsTable.setRowSelectionAllowed(true);
        keyBindingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userScrollPane.setViewportView(keyBindingsTable);

        // Listen for changes to the selection on the key bindings table and
        // enable/disable the Remove button as a result.
        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                boolean isRowSelected = keyBindingsTable.getSelectedRow() != -1;
                removeButton.setEnabled(isRowSelected);
            }
        };
        keyBindingsTable.getSelectionModel().addListSelectionListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // Only update the UI if we are making the frame visible
        if (visible == false) {
            return;
        }
        
        // Fetch the current key bindings from the avatar and update the table
        // model. This assumes there is a primary view Cell.
        Map<Integer, Integer> keyBindingsMap = getAvatarKeyBindings();
        keyBindingsTableModel.putKeyBindings(keyBindingsMap);
    }

    /**
     * Returns a Map of the current key bindings from the avatar. If there is
     * no avatar present, this method returns an empty Map.
     */
    private Map<Integer, Integer> getAvatarKeyBindings() {
        // Fetch the current key bindings from the avatar and update the table
        // model. This assumes there is a primary view Cell.
        Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        if (cell == null) {
            LOGGER.warning("Unable to find primary view cell, is null.");
            return new HashMap<Integer, Integer>();
        }
        CellRenderer renderer = cell.getCellRenderer(RendererType.RENDERER_JME);
        if (!(renderer instanceof AvatarImiJME)) {
            LOGGER.warning("View cell renderer is not of the property type: " +
                    renderer.getClass().getName());
            return new HashMap<Integer, Integer>();
        }
        WlAvatarCharacter avatar = ((AvatarImiJME)renderer).getAvatarCharacter();
        return avatar.getKeyBindings();
    }

    /**
     * Sets the Map of key bindings on the avatar. If there is no avatar present
     * this method does nothing.
     */
    private void setAvatarKeyBindings(Map<Integer, Integer> keyBindingsMap) {
        // Set the current key bindings on the avatar. This assumes there is a
        // primary view Cell.
        Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        if (cell == null) {
            LOGGER.warning("Unable to find primary view cell, is null.");
            return;
        }
        CellRenderer renderer = cell.getCellRenderer(RendererType.RENDERER_JME);
        if (!(renderer instanceof AvatarImiJME)) {
            LOGGER.warning("View cell renderer is not of the property type: " +
                    renderer.getClass().getName());
            return;
        }
        WlAvatarCharacter avatar = ((AvatarImiJME)renderer).getAvatarCharacter();
        avatar.setKeyBindings(keyBindingsMap);
    }

    /**
     * A table model that displays a list of key bindings.
     */
    private class KeyBindingsTableModel extends AbstractTableModel {

        // A Map of key event integers and the avatar trigger integers. This
        // is the master Map of the current set of key bindings.
        private Map<Integer, Integer> keyBindingsMap = null;

        // A list of key event Integer and a list the corresponding avatar
        // Trigger names. We keep these in two coordinated lists so that we
        // may have random, predictable access to them in this table mode.
        private List<Integer> keyEventList = new LinkedList<Integer>();
        private List<Integer> triggerNameList = new LinkedList<Integer>();

        /** Default constructor */
        public KeyBindingsTableModel() {
            this.keyBindingsMap = new HashMap<Integer, Integer>();
        }

        /**
         * Updates the linked lists 'keyEventList' and 'triggerNameList' based
         * upon the master 'keyEventMap' of the current key bindings. This
         * orders the list in alphabetical order of the name of the key event.
         */
        private void refreshBindingsList() {
            // First form a Map of the current key bindings using the String
            // names of the key events and the integer key event values.
            Map<String, Integer> keyEventMap = new HashMap<String, Integer>();
            for (Integer keyEvent : keyBindingsMap.keySet()) {
                String keyName = KeyEvent.getKeyText(keyEvent);
                keyEventMap.put(keyName, keyEvent);
            }

            // Next, form a list from the key event names and sort it.
            List<String> nameList = new LinkedList(keyEventMap.keySet());
            Collections.sort(nameList);

            // Finally, using the sorted list of names, fill the 'keyEventList'
            // and 'triggerNameList' lists so that we may access them in a
            // predictable order.
            keyEventList.clear();
            triggerNameList.clear();
            for (String name : nameList) {
                Integer keyEvent = keyEventMap.get(name);
                keyEventList.add(keyEvent);
                triggerNameList.add(keyBindingsMap.get(keyEvent));
            }
        }

        /**
         * @inheritDoc()
         */
        public int getRowCount() {
            return keyEventList.size();
        }

        /**
         * @inheritDoc()
         */
        public int getColumnCount() {
            return 2;
        }

        /**
         * @inheritDoc()
         */
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return BUNDLE.getString("Key_Event");

                case 1:
                    return BUNDLE.getString("Trigger_Name");
                    
                default:
                    return "";
            }
        }

        /**
         * @inheritDoc()
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    Integer keyEvent = keyEventList.get(rowIndex);
                    return KeyEvent.getKeyText(keyEvent);

                case 1:
                    Integer trigger = triggerNameList.get(rowIndex);
                    return triggerNameMap.get(trigger).toString();

                default:
                    return "";
            }
        }

        /**
         * Returns the Integer key event value of the Nth key binding in the
         * table.
         *
         * @param n The index into the list
         * @return Returns the nth key event integer value
         */
        public Integer getKeyEvent(int n) {
            return keyEventList.get(n);
        }

        /**
         * Returns the Map of key bindings, from the Integer value of the key
         * event to the Integer ordinal value of the avatar trigger.
         *
         * @return A Map of Integer key events to Integer trigger values
         */
        public Map<Integer, Integer> getKeyBindings() {
            return keyBindingsMap;
        }

        /**
         * Sets the Map of key bindings, from the Integer value of the key
         * event to the Integer ordinal value of the avatar trigger.
         *
         * @param A Map of Integer key events to Integer trigger values
         */
        public void putKeyBindings(Map<Integer, Integer> keyBindingsMap) {
            this.keyBindingsMap.putAll(keyBindingsMap);
            refreshBindingsList();
            fireTableDataChanged();
        }

        /**
         * Add a key binding to the Map of key bindings, given the Integer key
         * event value and the Integer ordinal value of the avatar trigger. If
         * the key event is already present, this method replaces the existing
         * mapping.
         * 
         * @param keyEvent The integer key event value
         * @param trigger The integer ordinal value of the trigger
         */
        public void putKeyBinding(Integer keyEvent, Integer trigger) {
            keyBindingsMap.put(keyEvent, trigger);
            refreshBindingsList();
            fireTableDataChanged();
        }

        /**
         * Removes a key binding from the Map of key bindings, given the Integer
         * key event value. If the key event is not present in the key binding
         * Map, this method does nothing.
         *
         * @param keyEvent The integer key event value
         */
        public void removeKeyBinding(Integer keyEvent) {
            keyBindingsMap.remove(keyEvent);
            refreshBindingsList();
            fireTableDataChanged();
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
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        userMainPanel = new javax.swing.JPanel();
        userScrollPane = new javax.swing.JScrollPane();
        userButtonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/keybindings/client/resources/Bundle"); // NOI18N
        setTitle(bundle.getString("EditKeyBindingsJFrame.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        mainPanel.setLayout(new java.awt.GridLayout(1, 0));

        userMainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userMainPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        userMainPanel.add(userScrollPane, gridBagConstraints);

        userButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        addButton.setText(bundle.getString("EditKeyBindingsJFrame.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        userButtonPanel.add(addButton);

        removeButton.setText(bundle.getString("EditKeyBindingsJFrame.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        userButtonPanel.add(removeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        userMainPanel.add(userButtonPanel, gridBagConstraints);

        mainPanel.add(userMainPanel);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed

        // When the Add... button is pressed popup a dialog asking for all of
        // the information.
        AddKeyBindingJDialog dialog = new AddKeyBindingJDialog(this, true);
        dialog.setTitle(BUNDLE.getString("Add_KeyBinding"));
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);

        // If the user hit the OK button in the dialog, then fetch the new
        // key binding and set. If the key event matches an existing key binding
        // then it will be overwritten. Update the table too.
        if (dialog.getReturnStatus() == AddKeyBindingJDialog.RET_OK) {
            Integer keyEvent = dialog.getKeyEvent();
            TriggerNames trigger = dialog.getTriggerName();
            keyBindingsTableModel.putKeyBinding(keyEvent, trigger.ordinal());
            setAvatarKeyBindings(keyBindingsTableModel.getKeyBindings());
            LOGGER.warning("PUTTING " + keyEvent + " " + trigger);
        }
}//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // When the Remove..... button is pressed find out the Integer value
        // of the key event and remove it from the key bindings and the refresh
        // the table.
        int row = keyBindingsTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        Integer keyEvent = keyBindingsTableModel.getKeyEvent(row);
        keyBindingsTableModel.removeKeyBinding(keyEvent);
        setAvatarKeyBindings(keyBindingsTableModel.getKeyBindings());
}//GEN-LAST:event_removeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel userButtonPanel;
    private javax.swing.JPanel userMainPanel;
    private javax.swing.JScrollPane userScrollPane;
    // End of variables declaration//GEN-END:variables
}
