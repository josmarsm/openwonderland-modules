/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.client;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState;
import static com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.*;
import static com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger.IN_RANGE;
import static com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger.LEFT_CLICK;
import static com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger.RIGHT_CLICK;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureMessage;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * The property sheet for the gesture component component.
 * 
 * @author Abhishek Upadhyay.
 */
@PropertiesFactory(TriggerGestureComponentServerState.class)
public class TriggerGestureComponentProperties extends javax.swing.JPanel implements PropertiesFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/wonderbuilders/modules"
            + "/triggergesture/client/resources/Bundle");
    
    private CellPropertiesEditor editor = null;
    private String[] gestures = {
        BUNDLE.getString("AnswerCell"),
        BUNDLE.getString("Bow"),
        BUNDLE.getString("Cheer"),
        BUNDLE.getString("Clap"),
        BUNDLE.getString("Follow"),
        BUNDLE.getString("Laugh"),
        BUNDLE.getString("No"),
        BUNDLE.getString("PublicSpeaking"),
        BUNDLE.getString("RaiseHand"),
        BUNDLE.getString("ShakeHands"),
        BUNDLE.getString("Sit"),
        BUNDLE.getString("Wave"),
        BUNDLE.getString("Wink"),
        BUNDLE.getString("Yes")
    };
    
    private Trigger origTrigger = Trigger.LEFT_CLICK;
    private String origGesture = BUNDLE.getString("AnswerCell");
    private String origName = BUNDLE.getString("MenuItemName");
    private int origRadius = 3;
    
    private Trigger trigger = Trigger.LEFT_CLICK;
    private BoundsViewerEntity boundsViewerEntity;
    
    /**
     * Creates new form TriggerGestureComponentProperties
     */
    public TriggerGestureComponentProperties() {
        initComponents();
        initials();
    }
    
    private void initials() {
        lblRadius.setEnabled(false);
        txtMenuItem.setEnabled(false);
        spRadius.setEnabled(false);
        chbShowBounds.setEnabled(false);
        for(String g : gestures) {
            cbGestures.addItem(g);
        }
        txtMenuItem.getDocument().addDocumentListener(new TriggerGestureDocumentListener());
    }
    
    /*
     * enable/disable JComponents
     */
    private void changeComponentStatus() {
        if(rbLeftClick.isSelected()) {
            trigger = Trigger.LEFT_CLICK;
            txtMenuItem.setEnabled(false);
            spRadius.setEnabled(false);
            lblRadius.setEnabled(false);
            chbShowBounds.setEnabled(false);
        }
        if(rbRightClick.isSelected()) {
            trigger = Trigger.RIGHT_CLICK;
            txtMenuItem.setEnabled(true);
            spRadius.setEnabled(false);
            lblRadius.setEnabled(false);
            chbShowBounds.setEnabled(false);
        }
        if(rbRange.isSelected()) {
            trigger = Trigger.IN_RANGE;
            txtMenuItem.setEnabled(false);
            spRadius.setEnabled(true);
            lblRadius.setEnabled(true);
            chbShowBounds.setEnabled(true);
        }
    }
    
    /*
     * show/hide bounds
     */
    private void showBounds() {
	if (boundsViewerEntity != null) {
	    boundsViewerEntity.dispose();
	    boundsViewerEntity = null;
	}

	if (chbShowBounds.isSelected() == false) {
	    return;
	}

	boundsViewerEntity = new BoundsViewerEntity(editor.getCell());

	if (chbShowBounds.isSelected()) {
	    boundsViewerEntity.showBounds(
		new BoundingSphere((Integer) spRadius.getValue(), new Vector3f()));
	}
    }
    
    private void checkDirty() {
        boolean dirty = false;
        
        dirty |= !origTrigger.equals(trigger);
        dirty |= !origGesture.equals(cbGestures.getSelectedItem().toString());
        dirty |= !origName.equals(txtMenuItem.getText());
        dirty |= origRadius!=((Integer)spRadius.getModel().getValue()).intValue();
        
        if(editor!=null) {
            editor.setPanelDirty(TriggerGestureComponentProperties.class, dirty);
        }
    }
    
    /*
     * listen for the change value in text fields
     */
    private class TriggerGestureDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rbLeftClick = new javax.swing.JRadioButton();
        rbRightClick = new javax.swing.JRadioButton();
        rbRange = new javax.swing.JRadioButton();
        txtMenuItem = new javax.swing.JTextField();
        lblRadius = new javax.swing.JLabel();
        spRadius = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        cbGestures = new javax.swing.JComboBox();
        chbShowBounds = new javax.swing.JCheckBox();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Select Trigger : ");

        buttonGroup1.add(rbLeftClick);
        rbLeftClick.setText("On Left Click");
        rbLeftClick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbLeftClickActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbRightClick);
        rbRightClick.setText("On Right Click menu item");
        rbRightClick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRightClickActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbRange);
        rbRange.setText("When avatar is in range");
        rbRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRangeActionPerformed(evt);
            }
        });

        txtMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMenuItemActionPerformed(evt);
            }
        });

        lblRadius.setText("Radius of circle : ");

        spRadius.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(3), null, null, Integer.valueOf(1)));
        spRadius.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spRadiusStateChanged(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Select Gesture : ");

        cbGestures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGesturesActionPerformed(evt);
            }
        });

        chbShowBounds.setText("Show Bounds");
        chbShowBounds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbShowBoundsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(rbRightClick)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMenuItem, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(rbLeftClick)
                            .addComponent(rbRange)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(lblRadius)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spRadius, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cbGestures, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chbShowBounds)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(rbLeftClick))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbRightClick)
                    .addComponent(txtMenuItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbRange)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRadius)
                    .addComponent(spRadius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chbShowBounds))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbGestures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbRightClickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRightClickActionPerformed
        // TODO add your handling code here:
        changeComponentStatus();
        checkDirty();
    }//GEN-LAST:event_rbRightClickActionPerformed

    private void rbRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRangeActionPerformed
        // TODO add your handling code here:
        changeComponentStatus();
        checkDirty();
    }//GEN-LAST:event_rbRangeActionPerformed

    private void rbLeftClickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbLeftClickActionPerformed
        // TODO add your handling code here:
        changeComponentStatus();
        checkDirty();
    }//GEN-LAST:event_rbLeftClickActionPerformed

    private void txtMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMenuItemActionPerformed

    private void cbGesturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGesturesActionPerformed
        // TODO add your handling code here:
        checkDirty();
    }//GEN-LAST:event_cbGesturesActionPerformed

    private void spRadiusStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spRadiusStateChanged
        // TODO add your handling code here:
        checkDirty();
        showBounds();
    }//GEN-LAST:event_spRadiusStateChanged

    private void chbShowBoundsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbShowBoundsActionPerformed
        // TODO add your handling code here:
        showBounds();
    }//GEN-LAST:event_chbShowBoundsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbGestures;
    private javax.swing.JCheckBox chbShowBounds;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblRadius;
    private javax.swing.JRadioButton rbLeftClick;
    private javax.swing.JRadioButton rbRange;
    private javax.swing.JRadioButton rbRightClick;
    private javax.swing.JSpinner spRadius;
    private javax.swing.JTextField txtMenuItem;
    // End of variables declaration//GEN-END:variables

    public String getDisplayName() {
        return BUNDLE.getString("TriggerGestureProperties.DispayName");
    }

    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    public JPanel getPropertiesJPanel() {
        return this;
    }

    public void open() {
        CellServerState cellServerState = editor.getCellServerState();
        TriggerGestureComponentServerState compServerState = (TriggerGestureComponentServerState) cellServerState
                .getComponentServerState(TriggerGestureComponentServerState.class);
        
        if(compServerState!=null) {
            switch(compServerState.getTrigger()) {
                case LEFT_CLICK:
                    origTrigger = Trigger.LEFT_CLICK;
                    rbLeftClick.setSelected(true);
                    changeComponentStatus();
                    break;
                case RIGHT_CLICK:
                    origTrigger = Trigger.RIGHT_CLICK;
                    rbRightClick.setSelected(true);
                    changeComponentStatus();
                    break;
                case IN_RANGE:
                    origTrigger = Trigger.IN_RANGE;
                    rbRange.setSelected(true);
                    changeComponentStatus();
                    break;
            }
            
            origGesture = compServerState.getGesture();
            if(origGesture.equals("")) {
                origGesture = BUNDLE.getString("NoGesture");
            }
            cbGestures.setSelectedItem(compServerState.getGesture());
            origName = compServerState.getContextMenuName();
            txtMenuItem.setText(origName);
            
            origRadius = compServerState.getRadius();
            spRadius.setValue(origRadius);
            chbShowBounds.setSelected(false);
        }
        checkDirty();
    }

    public void close() {
        //remove the bounds
        if (boundsViewerEntity != null) {
	    boundsViewerEntity.dispose();
	    boundsViewerEntity = null;
	}
    }

    public void restore() {
        switch(origTrigger) {
            case LEFT_CLICK:
                rbLeftClick.setSelected(true);
                break;
            case RIGHT_CLICK:
                rbRightClick.setSelected(true);
                break;
            case IN_RANGE:
                rbRange.setSelected(true);
                break;
        }
        txtMenuItem.setText(origName);
        spRadius.setValue(origRadius);
        cbGestures.setSelectedItem(origGesture);
        changeComponentStatus();
    }

    public void apply() {
        
        CellServerState cellServerState = editor.getCellServerState();
        TriggerGestureComponentServerState compServerState = (TriggerGestureComponentServerState) cellServerState
                .getComponentServerState(TriggerGestureComponentServerState.class);
        TriggerGestureComponent tgc = editor.getCell().getComponent(TriggerGestureComponent.class);
        
        if(compServerState!=null) {
            switch(trigger) {
                case LEFT_CLICK:
                    compServerState.setTrigger(Trigger.LEFT_CLICK);
                    break;
                case RIGHT_CLICK:
                    compServerState.setTrigger(Trigger.RIGHT_CLICK);
                    tgc.addContenxtMenuItem(txtMenuItem.getText());
                    break;
                case IN_RANGE:
                    compServerState.setTrigger(Trigger.IN_RANGE);
                    tgc.addProximityListener((Integer)spRadius.getValue());
                    break;
            }
            
            if(!origName.equals(txtMenuItem.getText()) || origRadius!=((Integer)spRadius
                    .getValue()).intValue()) {
                TriggerGestureMessage msg = new TriggerGestureMessage();
                msg.setRadius(((Integer)spRadius.getValue()).intValue());
                msg.setContextMenuName(txtMenuItem.getText());
                editor.getCell().sendCellMessage(msg);
            }
            
            compServerState.setContextMenuName(txtMenuItem.getText());
            compServerState.setGesture(cbGestures.getSelectedItem().toString());
            compServerState.setRadius((Integer)spRadius.getValue());
            editor.addToUpdateList(compServerState);
        }
    }
}
