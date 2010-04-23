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
package org.jdesktop.wonderland.modules.tooltip.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.tooltip.common.TooltipCellComponentServerState;

/**
 * The property sheet for the Tooltip Cell Component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@PropertiesFactory(TooltipCellComponentServerState.class)
public class TooltipCellComponentProperties extends JPanel
        implements PropertiesFactorySPI {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/tooltip/client/resources/Bundle");

    // The editor window
    private CellPropertiesEditor editor = null;

    // The original value for the tooltip text, before any editing
    private String originalText = null;

    // The original value for the tooltip timeout, -1 if there is no timeout
    private int originalTimeout = -1;

    // The initial default timeout to use
    private static final int DEFAULT_TIMEOUT = 2000;
    
    /** Creates new form TooltipComponentProperties */
    public TooltipCellComponentProperties() {
        // Initialize the GUI
        initComponents();

        // Listen for changes to the info text field, and update the "dirty"
        // state if necessary
        tooltipTextArea.getDocument().addDocumentListener(
                new InfoTextFieldListener());

        // Initially the timeout checkbox is not selected and the spinner is
        // not enabled
        timeoutCheckbox.setSelected(false);
        timeoutSpinner.setEnabled(false);
        
        // Set valid values for timeout spinner as integers between 1 and
        // max integer.
        Integer value = new Integer(DEFAULT_TIMEOUT);
        Integer min = new Integer(0);
        Integer max = Integer.MAX_VALUE;
        Integer step = new Integer(1);
        timeoutSpinner.setModel(new SpinnerNumberModel(value, min, max, step));

        // Listen for changes to the tooltip spinner and update the "dirty"
        // state if necessary
        timeoutSpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                // Figure out what the current timeout is, based upon whether
                // the checkbox is selected or not.
                int timeout = (timeoutCheckbox.isSelected() == true) ?
                    (Integer) timeoutSpinner.getValue() : -1;

                // Update the "dirty" state of the property sheet depending
                // upon whether the current timeout matches the original
                // timeout.
                if (editor != null && timeout != originalTimeout) {
                    editor.setPanelDirty(TooltipCellComponentProperties.class,
                            true);
                }
                else if (editor != null) {
                    editor.setPanelDirty(TooltipCellComponentProperties.class,
                            false);
                }
            }
        });

        // Listen for changes to the tooltip checkbox and update the "dirty"
        // state if necessary
        timeoutCheckbox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                // Figure out what the current timeout is, based upon whether
                // the checkbox is selected or not.
                int timeout = (timeoutCheckbox.isSelected() == true) ?
                    (Integer) timeoutSpinner.getValue() : -1;

                // Enable or disable the spinner depending upon whether the
                // checkbox is selected or not
                timeoutSpinner.setEnabled(timeoutCheckbox.isSelected());
                
                // Update the "dirty" state of the property sheet depending
                // upon whether the current timeout matches the original
                // timeout.
                if (editor != null && timeout != originalTimeout) {
                    editor.setPanelDirty(TooltipCellComponentProperties.class,
                            true);
                }
                else if (editor != null) {
                    editor.setPanelDirty(TooltipCellComponentProperties.class,
                            false);
                }
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Tooltip_Cell_Component");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @inheritDoc()
     */
    public void open() {

        // Fetch the tooltip component state from the Cell server state.
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                TooltipCellComponentServerState.class);

        // If there is a tooltip component server state (there should be), then
        // populate its values in the GUI.
        if (state != null) {
            TooltipCellComponentServerState tss =
                    (TooltipCellComponentServerState) compState;

            // Store away the tooltip text and update the GUI
            originalText = tss.getText();
            tooltipTextArea.setText(originalText);

            // Store away the tooltip timeout and update the GUI
            originalTimeout = tss.getTimeout();
            if (originalTimeout == -1) {
                timeoutCheckbox.setSelected(false);
                timeoutSpinner.setEnabled(false);
                timeoutSpinner.setValue((Integer) DEFAULT_TIMEOUT);
            }
            else {
                timeoutCheckbox.setSelected(true);
                timeoutSpinner.setEnabled(true);
                timeoutSpinner.setValue((Integer) originalTimeout);
            }
        }
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing for now.
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Fetch the latest tooltip component server state from the Cell server
        // state.
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                TooltipCellComponentServerState.class);

        // Update the tooltip text in the component server state
        ((TooltipCellComponentServerState) compState).setText(
                tooltipTextArea.getText());
        
        // Update the tooltip timeout in the component server state
        int timeout = -1;
        if (timeoutCheckbox.isSelected() == true) {
            timeout = (Integer) timeoutSpinner.getValue();
        }
        ((TooltipCellComponentServerState) compState).setTimeout(timeout);

        // Tell the Cell editor that this property sheet is "dirty"
        editor.addToUpdateList(compState);
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore from the original state stored.
        tooltipTextArea.setText(originalText);
        if (originalTimeout == -1) {
            timeoutCheckbox.setSelected(false);
            timeoutSpinner.setEnabled(false);
            timeoutSpinner.setValue((Integer) DEFAULT_TIMEOUT);
        }
        else {
            timeoutCheckbox.setSelected(true);
            timeoutSpinner.setEnabled(true);
            timeoutSpinner.setValue((Integer) originalTimeout);
        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class InfoTextFieldListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }

        private void checkDirty() {
            String name = tooltipTextArea.getText();
            if (editor != null && name.equals(originalText) == false) {
                editor.setPanelDirty(TooltipCellComponentProperties.class,
                        true);
            }
            else if (editor != null) {
                editor.setPanelDirty(TooltipCellComponentProperties.class,
                        false);
            }
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

        timeoutPanel = new javax.swing.JPanel();
        timeoutCheckbox = new javax.swing.JCheckBox();
        timeoutSpinner = new javax.swing.JSpinner();
        tooltipPanel = new javax.swing.JPanel();
        tooltipScrollPane = new javax.swing.JScrollPane();
        tooltipTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        timeoutPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/tooltip/client/resources/Bundle"); // NOI18N
        timeoutCheckbox.setText(bundle.getString("Timeout_Text_Label")); // NOI18N
        timeoutPanel.add(timeoutCheckbox);

        timeoutSpinner.setEnabled(false);
        timeoutPanel.add(timeoutSpinner);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(timeoutPanel, gridBagConstraints);

        tooltipPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tooltip_Text_Label"))); // NOI18N
        tooltipPanel.setLayout(new java.awt.GridLayout());

        tooltipTextArea.setColumns(20);
        tooltipTextArea.setRows(5);
        tooltipScrollPane.setViewportView(tooltipTextArea);

        tooltipPanel.add(tooltipScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(tooltipPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox timeoutCheckbox;
    private javax.swing.JPanel timeoutPanel;
    private javax.swing.JSpinner timeoutSpinner;
    private javax.swing.JPanel tooltipPanel;
    private javax.swing.JScrollPane tooltipScrollPane;
    private javax.swing.JTextArea tooltipTextArea;
    // End of variables declaration//GEN-END:variables
}
