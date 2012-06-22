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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.EZScriptComponentServerState;
import org.jdesktop.wonderland.modules.ezscript.common.SharedBounds;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

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
    private BoundsViewerEntity boundsEntity;
    private boolean originalShowBounds = false;
    private static final Logger logger = Logger.getLogger(EZScriptComponentProperties.class.getName());

    /** Creates new form SampleComponentProperties */
    public EZScriptComponentProperties() {
        // Initialize the GUI
        initComponents();

        // Listen for changes to the info text field
        infoTextField.getDocument().addDocumentListener(
                new InfoTextFieldListener());
        radiusSpinner.getModel().addChangeListener(new RadiusSpinnerChangeListener());
        ySpinner.getModel().addChangeListener(new YSpinnerChangeListener());
        zSpinner.getModel().addChangeListener(new ZSpinnerChangeListener());
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
            if(component == null)
                return;
            
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

            if(states.get("bounds") != null) {
                originalBounds = (SharedBounds)states.get("bounds");
            } else {
                logger.warning("Bounds not found in state. Defaulting to SPHERE.");
                originalBounds = SharedBounds.SPHERE;
            }

            
            //originalBounds = SharedBounds.valueOf(editor.getCell().getLocalBounds());
           
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
        if(boundsEntity != null) {
            boundsEntity.dispose();
            boundsEntity = null;
        }
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
            
            hideBounds();
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
        showBounds();


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
        if(boxButton.isSelected()) {
            value = "BOX";
        } else {
            value = "SPHERE";
        }

        xs[0] = ((SpinnerNumberModel)radiusSpinner.getModel()).getNumber().floatValue();
        xs[1] = ((SpinnerNumberModel)ySpinner.getModel()).getNumber().floatValue();
        xs[2] = ((SpinnerNumberModel)zSpinner.getModel()).getNumber().floatValue();

        map.put("bounds", SharedBounds.valueOf(value, xs));
        
    }
    private boolean areBoundsDirty() {
        if(originalBounds.getValue().equals("BOX") && boxButton.isSelected() == false)
            return true;

        if(originalBounds.getValue().equals("SPHERE") && sphereButton.isSelected() == false)
            return true;

        SpinnerNumberModel snm = (SpinnerNumberModel)radiusSpinner.getModel();
        if(originalBounds.getExtents()[0] != snm.getNumber().floatValue())
            return true;
        snm = (SpinnerNumberModel)ySpinner.getModel();
        if (originalBounds.getExtents().length > 1) {
            if (originalBounds.getExtents()[1] != snm.getNumber().floatValue()) {
                return true;

            }
        }

        snm = (SpinnerNumberModel)zSpinner.getModel();
        if (originalBounds.getExtents().length > 2) {
            if (originalBounds.getExtents()[2] != snm.getNumber().floatValue()) {
                return true;

            }
        }
        
        return false;
    }
    /**
     * Highly adapted from MicrophoneComponentProperties.
     * Thanks to author Joe Provino
     */
    private void showBounds() {
        if(boundsEntity != null) {
            boundsEntity.dispose();
            boundsEntity = null;
        }

        if(!boundsCheckbox.isSelected()) {
           return;
        }
        boundsEntity = new BoundsViewerEntity(editor.getCell());
        Vector3f origin = new Vector3f();//getCellTranslation();
        SpinnerNumberModel snm = (SpinnerNumberModel)radiusSpinner.getModel();
        if(boxButton.isSelected()) {
            SpinnerNumberModel ynm = (SpinnerNumberModel)ySpinner.getModel();
            SpinnerNumberModel znm = (SpinnerNumberModel)zSpinner.getModel();
            boundsEntity.showBounds(new BoundingBox(origin,
                                                    snm.getNumber().floatValue(),
                                                    ynm.getNumber().floatValue(),
                                                    znm.getNumber().floatValue()));
        }
        else {
            boundsEntity.showBounds(new BoundingSphere(snm.getNumber().floatValue(),
                                                        origin));

        }
    }

    private Vector3f getCellTranslation() {
        Cell cell = editor.getCell();
        CellTransform transform = cell.getLocalTransform();
        return transform.getTranslation(null);
    }
    private void hideBounds() {
        if(boundsEntity != null) {
            boundsEntity.dispose();
            boundsEntity = null;

            boundsCheckbox.setSelected(false);
        }
    }

    class RadiusSpinnerChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if(editor!= null) {
                editor.setPanelDirty(EZScriptComponentProperties.class, isDirty());
                showBounds();
            }
        }

    }

    class YSpinnerChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if(editor!= null) {
                editor.setPanelDirty(EZScriptComponentProperties.class, isDirty());
                showBounds();
            }
        }

    }

    class ZSpinnerChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if(editor!= null) {
                editor.setPanelDirty(EZScriptComponentProperties.class, isDirty());
                showBounds();
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
        clearButton = new javax.swing.JButton();
        openEditorButton = new javax.swing.JButton();
        boundsCheckbox = new javax.swing.JCheckBox();

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
        sphereButton.setSelected(true);
        sphereButton.setText("Sphere");
        sphereButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sphereButtonActionPerformed(evt);
            }
        });

        boundsButtonGroup.add(boxButton);
        boxButton.setText("Box");
        boxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxButtonActionPerformed(evt);
            }
        });

        radiusSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 100.0d, 1.0d));

        jLabel1.setText("Radius/X-extent");

        ySpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 100.0d, 1.0d));

        zSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 100.0d, 1.0d));

        jLabel2.setText("Y-extent");

        jLabel3.setText("Z-Extent");

        clearButton.setText("Clear Callbacks");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        openEditorButton.setText("Open Editor");
        openEditorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openEditorButtonActionPerformed(evt);
            }
        });

        boundsCheckbox.setText("Show Bounds");
        boundsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boundsCheckboxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(9, 9, 9)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(callbacksLabel))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(32, 32, 32)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cellsCheckbox)
                            .add(mouseCheckbox)
                            .add(proximityCheckbox)
                            .add(keyboardCheckbox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 89, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(clearButton)
                            .add(openEditorButton))
                        .add(48, 48, 48))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
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
                            .add(radiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(boundsCheckbox)
                .addContainerGap(335, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(callbacksLabel)
                .add(18, 18, 18)
                .add(proximityCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mouseCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyboardCheckbox)
                    .add(openEditorButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cellsCheckbox)
                    .add(clearButton))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 33, Short.MAX_VALUE)
                .add(boundsCheckbox)
                .addContainerGap())
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

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
        Cell cell = editor.getCell();
        EZScriptComponent component = cell.getComponent(EZScriptComponent.class);
        component.getCallbacksMap().put("clear", SharedString.valueOf("clear"));
    }//GEN-LAST:event_clearButtonActionPerformed

    private void openEditorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openEditorButtonActionPerformed
        // TODO add your handling code here:
        Cell cell = editor.getCell();
        EZScriptComponent component = cell.getComponent(EZScriptComponent.class);
        component.showEditorWindow();
    }//GEN-LAST:event_openEditorButtonActionPerformed

    private void boundsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundsCheckboxActionPerformed
        // TODO add your handling code here:
        if(editor == null) {
            return;
        }

        editor.setPanelDirty(EZScriptComponentProperties.class, isDirty());
        showBounds();        
    }//GEN-LAST:event_boundsCheckboxActionPerformed

    private void boxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxButtonActionPerformed
        // TODO add your handling code here:
        if(boxButton.isSelected() == false) {
            return;
        }

        if(editor != null) {
            editor.setPanelDirty(EZScriptComponentProperties.class, isDirty());
            showBounds();
        }
    }//GEN-LAST:event_boxButtonActionPerformed

    private void sphereButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sphereButtonActionPerformed
        // TODO add your handling code here:
        if(sphereButton.isSelected() == false) {
            return;
        }

        if(editor != null) {
            editor.setPanelDirty(EZScriptComponentProperties.class, isDirty());
            showBounds();
        }
    }//GEN-LAST:event_sphereButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup boundsButtonGroup;
    private javax.swing.JCheckBox boundsCheckbox;
    private javax.swing.JRadioButton boxButton;
    private javax.swing.JLabel callbacksLabel;
    private javax.swing.JCheckBox cellsCheckbox;
    private javax.swing.JButton clearButton;
    private javax.swing.JTextField infoTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox keyboardCheckbox;
    private javax.swing.JCheckBox mouseCheckbox;
    private javax.swing.JButton openEditorButton;
    private javax.swing.JCheckBox proximityCheckbox;
    private javax.swing.JSpinner radiusSpinner;
    private javax.swing.JRadioButton sphereButton;
    private javax.swing.JSpinner ySpinner;
    private javax.swing.JSpinner zSpinner;
    // End of variables declaration//GEN-END:variables
}
