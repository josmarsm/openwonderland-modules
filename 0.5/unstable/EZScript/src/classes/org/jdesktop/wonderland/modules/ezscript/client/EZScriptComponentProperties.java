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
package org.jdesktop.wonderland.modules.ezscript.client;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.EZScriptComponentServerState;
import org.jdesktop.wonderland.modules.ezscript.common.SharedBounds;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * @author JagWire
 */
@PropertiesFactory(EZScriptComponentServerState.class)
public class EZScriptComponentProperties
        extends JPanel implements PropertiesFactorySPI {


    private CellPropertiesEditor editor = null;
    private String originalInfo = null;
    private SharedBoolean originalProximityEnabled = SharedBoolean.valueOf(false);
    private SharedBoolean originalMouseEnabled = SharedBoolean.valueOf(false);
    private SharedBoolean originalKeyboardEnabled = SharedBoolean.valueOf(false);
    private SharedBoolean originalFarCellEnabled = SharedBoolean.valueOf(false);
    private SharedBounds originalBounds = SharedBounds.BOX;

    /** Creates new form SampleComponentProperties */
    public EZScriptComponentProperties() {
        // Initialize the GUI
        initComponents();

        // Listen for changes to the info text field
        infoTextField.getDocument().addDocumentListener(
                new InfoTextFieldListener());
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return "EZScript";
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
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                EZScriptComponentServerState.class);
        if (state != null) {
            EZScriptComponentServerState EZScriptComponentServerState =
                    (EZScriptComponentServerState) compState;
            originalInfo = EZScriptComponentServerState.getInfo();
            infoTextField.setText(originalInfo);


            EZScriptComponent component = editor.getCell().getComponent(EZScriptComponent.class);
            SharedMapCli states = component.getStateMap();
            if(states == null)
                return;

            if(states.get("proximity") != null)
                originalProximityEnabled = (SharedBoolean)states.get("proximity");

            if(states.get("mouse") != null)
                originalMouseEnabled = (SharedBoolean)states.get("mouse");
            
            if(states.get("keyboard") != null)
                originalKeyboardEnabled = (SharedBoolean)states.get("keyboard");

            if(states.get("farcell") != null)
                originalFarCellEnabled = (SharedBoolean)states.get("farcell");

            if(states.get("bounds") != null)
                originalBounds = (SharedBounds)states.get("bounds");

            if(originalBounds.getValue().equals("BOX")) {
                boxButton.setSelected(true);
                radiusSpinner.getModel().setValue(originalBounds.getExtents()[0]);
                ySpinner.getModel().setValue(originalBounds.getExtents()[1]);
                zSpinner.getModel().setValue(originalBounds.getExtents()[2]);

            } else {
                sphereButton.setSelected(true);
                radiusSpinner.getModel().setValue(originalBounds.getExtents()[0]);

            }
            proximityCheckbox.setSelected(originalProximityEnabled.getValue());
            mouseCheckbox.setSelected(originalMouseEnabled.getValue());
            keyboardCheckbox.setSelected(originalKeyboardEnabled.getValue());
            cellsCheckbox.setSelected(originalFarCellEnabled.getValue());

            
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
        // Fetch the latest from the info text field and set it.
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                EZScriptComponentServerState.class);
        ((EZScriptComponentServerState) compState).setInfo(
                infoTextField.getText());
        editor.addToUpdateList(compState);
        
        if(isDirty()) {
            editor.setPanelDirty(EZScriptComponentProperties.class,
                                true);
            EZScriptComponent component = editor.getCell().getComponent(EZScriptComponent.class);
            if(component != null) {
                SharedMapCli states = component.getStateMap();
                states.put("proximity",
                        SharedBoolean.valueOf(proximityCheckbox.isSelected()));
                states.put("mouse",
                        SharedBoolean.valueOf(mouseCheckbox.isSelected()));
                states.put("keyboard",
                        SharedBoolean.valueOf(keyboardCheckbox.isSelected()));
                states.put("farcell",
                        SharedBoolean.valueOf(cellsCheckbox.isSelected()));
                putBounds(states);


            }
        } else {
            editor.setPanelDirty(EZScriptComponentProperties.class,
                                false);
        }


    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore from the original state stored.
        infoTextField.setText(originalInfo);
        proximityCheckbox.setSelected(originalProximityEnabled.getValue());
        mouseCheckbox.setSelected(originalMouseEnabled.getValue());
        keyboardCheckbox.setSelected(originalKeyboardEnabled.getValue());
        cellsCheckbox.setSelected(originalFarCellEnabled.getValue());
        restoreBounds();


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
            String name = infoTextField.getText();
            if (editor != null && name.equals(originalInfo) == false) {
                editor.setPanelDirty(EZScriptComponentProperties.class, true);
            } else if (editor != null) {
                editor.setPanelDirty(EZScriptComponentProperties.class, false);
            }
        }
    }

    private boolean isDirty() {
        return ( proximityCheckbox.isSelected() != originalProximityEnabled.getValue()
            || mouseCheckbox.isSelected() != originalMouseEnabled.getValue()
            || keyboardCheckbox.isSelected() != originalKeyboardEnabled.getValue()
            || cellsCheckbox.isSelected() != originalFarCellEnabled.getValue()
            || areBoundsDirty());
    }
    private void restoreBounds() {
        String value = originalBounds.getValue();
        float[] xs = originalBounds.getExtents();
        if(value.equals("BOX")) {
            boxButton.setEnabled(true);
        } else {
            sphereButton.setEnabled(true);
        }

        radiusSpinner.getModel().setValue((new Float(xs[0])).doubleValue());
        ySpinner.getModel().setValue((new Float(xs[1])).doubleValue());
        zSpinner.getModel().setValue((new Float(xs[2])).doubleValue());

    }
    private void putBounds(SharedMapCli map) {
        String value;
        float[] xs = { 1, 1, 1};
        if(boxButton.isEnabled()) {
            value = "BOX";
        } else {
            value = "SPHERE";
        }

        xs[0] = ((Double)radiusSpinner.getValue()).floatValue();
        xs[1] = ((Double)ySpinner.getValue()).floatValue();
        xs[2] = ((Double)zSpinner.getValue()).floatValue();

        map.put("bounds", SharedBounds.valueOf(value, xs));
        
    }
    private boolean areBoundsDirty() {
        if(originalBounds.equals("BOX") && boxButton.isSelected() == false)
            return true;

        if(originalBounds.equals("SPHERE") && sphereButton.isSelected() == false)
            return true;

        if(originalBounds.getExtents()[0] != ((Double)radiusSpinner.getValue()).floatValue())
            return true;

        if(originalBounds.getExtents()[1] != ((Double)ySpinner.getValue()).floatValue())
            return true;

        if(originalBounds.getExtents()[2] != ((Double)zSpinner.getValue()).floatValue())
            return true;
        
        return false;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoTextField = new javax.swing.JTextField();
        boundsButtonGroup = new javax.swing.ButtonGroup();
        callbacksLabel = new javax.swing.JLabel();
        proximityCheckbox = new javax.swing.JCheckBox();
        mouseCheckbox = new javax.swing.JCheckBox();
        keyboardCheckbox = new javax.swing.JCheckBox();
        cellsCheckbox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        sphereButton = new javax.swing.JRadioButton();
        boxButton = new javax.swing.JRadioButton();
        radiusSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        ySpinner = new javax.swing.JSpinner();
        zSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        callbacksLabel.setText("Callbacks:");

        proximityCheckbox.setText("Proximity Events");
        proximityCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proximityCheckboxActionPerformed(evt);
            }
        });

        mouseCheckbox.setText("Mouse Events");
        mouseCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mouseCheckboxActionPerformed(evt);
            }
        });

        keyboardCheckbox.setText("Keyboard Events");
        keyboardCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardCheckboxActionPerformed(evt);
            }
        });

        cellsCheckbox.setText("Far-Cell Events");
        cellsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cellsCheckboxActionPerformed(evt);
            }
        });

        boundsButtonGroup.add(sphereButton);
        sphereButton.setText("Sphere");

        boundsButtonGroup.add(boxButton);
        boxButton.setText("Box");

        radiusSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 100.0d, 1.0d));

        jLabel1.setText("Radius/X-extent");

        ySpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 100.0d, 1.0d));

        zSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 100.0d, 1.0d));

        jLabel2.setText("Y-extent");

        jLabel3.setText("Z-Extent");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(callbacksLabel))
                    .add(layout.createSequentialGroup()
                        .add(32, 32, 32)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mouseCheckbox)
                            .add(proximityCheckbox)
                            .add(keyboardCheckbox)
                            .add(cellsCheckbox))))
                .addContainerGap(344, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .add(20, 20, 20))
            .add(layout.createSequentialGroup()
                .add(125, 125, 125)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel1)
                        .add(jLabel2)
                        .add(jLabel3))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sphereButton))
                .add(45, 45, 45)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(boxButton)
                    .add(zSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(radiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(173, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(callbacksLabel)
                .add(18, 18, 18)
                .add(proximityCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mouseCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(keyboardCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cellsCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sphereButton)
                    .add(boxButton))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(45, 45, 45)
                        .add(radiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(ySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(zSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(53, 53, 53)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel2)
                        .add(18, 18, 18)
                        .add(jLabel3)))
                .addContainerGap(78, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void proximityCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proximityCheckboxActionPerformed
        if(editor != null) {
            if(isDirty()) {
                editor.setPanelDirty(EZScriptComponentProperties.class, true);
            } else {
                editor.setPanelDirty(EZScriptComponentProperties.class, false);
            }
        }
    }//GEN-LAST:event_proximityCheckboxActionPerformed

    private void mouseCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mouseCheckboxActionPerformed
        if(editor != null) {
            if(isDirty()) {
                editor.setPanelDirty(EZScriptComponentProperties.class, true);
            } else {
                editor.setPanelDirty(EZScriptComponentProperties.class, false);
            }
        }
    }//GEN-LAST:event_mouseCheckboxActionPerformed

    private void keyboardCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardCheckboxActionPerformed
        if(editor != null) {
            if(isDirty()) {
                editor.setPanelDirty(EZScriptComponentProperties.class, true);
            } else {
                editor.setPanelDirty(EZScriptComponentProperties.class, false);
            }
        }
    }//GEN-LAST:event_keyboardCheckboxActionPerformed

    private void cellsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cellsCheckboxActionPerformed
          if(editor != null) {
            if(isDirty()) {
                editor.setPanelDirty(EZScriptComponentProperties.class, true);
            } else {
                editor.setPanelDirty(EZScriptComponentProperties.class, false);
            }
        }
    }//GEN-LAST:event_cellsCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup boundsButtonGroup;
    private javax.swing.JRadioButton boxButton;
    private javax.swing.JLabel callbacksLabel;
    private javax.swing.JCheckBox cellsCheckbox;
    private javax.swing.JTextField infoTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox keyboardCheckbox;
    private javax.swing.JCheckBox mouseCheckbox;
    private javax.swing.JCheckBox proximityCheckbox;
    private javax.swing.JSpinner radiusSpinner;
    private javax.swing.JRadioButton sphereButton;
    private javax.swing.JSpinner ySpinner;
    private javax.swing.JSpinner zSpinner;
    // End of variables declaration//GEN-END:variables
}
