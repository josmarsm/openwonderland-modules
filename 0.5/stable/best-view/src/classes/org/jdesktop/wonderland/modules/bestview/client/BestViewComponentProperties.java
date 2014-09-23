/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.bestview.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.bestview.common.BestViewServerState;

/**
 * Component properties for best view component
 * 
 * @author Abhishek Upadhyay
 */
@PropertiesFactory(BestViewServerState.class)
public class BestViewComponentProperties extends javax.swing.JPanel
        implements PropertiesFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.bestview.client.Bundle");
    /**
     * the properties editor
     */
    private CellPropertiesEditor editor;
    private float oldOffsetX;
    private float oldOffsetY;
    private float oldOffsetZ;
    private float oldLookDirX;
    private float oldLookDirY;
    private float oldLookDirZ;
    private float oldLookDirW;
    private float newOffsetX;
    private float newOffsetY;
    private float newOffsetZ;
    private float newLookDirX;
    private float newLookDirY;
    private float newLookDirZ;
    private float newLookDirW;
    private float newZoom;
    private float oldZoom;
    private int newTrigger;
    private int oldTrigger;
    boolean dirty;
    float objOldPosX;
    float objOldPosY;
    float objOldPosZ;
    private BestViewComponent parentCellComp = null;

    /**
     * Creates new form BestViewComponentProperties
     */
    public BestViewComponentProperties() {
        initComponents();
        jPanel3.setVisible(false);
        String text = "Use this offset to adjust the \"camera\" position so that the avatar sees the object in the optimal way.";
        String text1 = "To automatically fill in these values, set your avatar's camera to \"First Person\" and then position yourself so you see the object in the optimal view.Then press this button.";
        jLabel11.setText(String.format("<html>%s<html>", text));
        jLabel13.setText(String.format("<html>%s<html>", text1));

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void removeUpdate(DocumentEvent e) {

                checkDirty();
            }

            public void changedUpdate(DocumentEvent e) {
                checkDirty();
            }
        });
        if (editor != null) {
            parentCellComp = editor.getCell().getComponent(BestViewComponent.class);
        }
    }

    /**
     * Get the display name of this editor
     */
    public String getDisplayName() {
        return BUNDLE.getString("Best_View");
    }

    /**
     * Notification from the framework of our editor object. Called before the
     * panel is displayed.
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        // save the editor
        this.editor = editor;
    }

    /**
     * Get the panel to display
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * Called when the view is first opened.
     */
    public void open() {
        // read data from the editor to show the current state

        restore();
    }

    /**
     * Called when the view is closed.
     */
    public void close() {
        // clean up
    }

    /**
     * Called to request this form restore the default values
     */
    public void restore() {
        setPropertyValues();
    }

    public void setPropertyValues() {
        if (editor != null) {
            CellServerState state = editor.getCellServerState();
            if (state != null) {
                CellComponentServerState compState = state.getComponentServerState(BestViewServerState.class);

                BestViewServerState bestViewServerState = (BestViewServerState) compState;

                if (bestViewServerState.getOffsetX() == 0 && bestViewServerState.getOffsetY() == 0
                        && bestViewServerState.getOffsetZ() == 0 && bestViewServerState.getLookDirX() == 0
                        && bestViewServerState.getLookDirY() == 0 && bestViewServerState.getLookDirZ() == 0
                        && bestViewServerState.getLookDirW() == 0) {
                    CellTransform xform = BestViewUtils.getBestView(editor.getCell());
                    Vector3f cellPos = editor.getCell().getWorldTransform().getTranslation(null);

                    oldOffsetX = cellPos.x - xform.getTranslation(null).x;
                    oldOffsetY = cellPos.y - xform.getTranslation(null).y;
                    oldOffsetZ = cellPos.z - xform.getTranslation(null).z;

                    oldLookDirX = xform.getRotation(null).x;
                    oldLookDirY = xform.getRotation(null).y;
                    oldLookDirZ = xform.getRotation(null).z;
                    oldLookDirW = xform.getRotation(null).w;
                    oldZoom = bestViewServerState.getZoom();
                    oldTrigger = bestViewServerState.getTrigger();
                } else {

                    oldOffsetX = bestViewServerState.getOffsetX();
                    oldOffsetY = bestViewServerState.getOffsetY();
                    oldOffsetZ = bestViewServerState.getOffsetZ();
                    oldLookDirX = bestViewServerState.getLookDirX();
                    oldLookDirY = bestViewServerState.getLookDirY();
                    oldLookDirZ = bestViewServerState.getLookDirZ();
                    oldLookDirW = bestViewServerState.getLookDirW();
                    oldZoom = bestViewServerState.getZoom();
                    oldTrigger = bestViewServerState.getTrigger();
                }
                objOldPosX = bestViewServerState.getOldObjPosX();
                objOldPosY = bestViewServerState.getOldObjPosY();
                objOldPosZ = bestViewServerState.getOldObjPosZ();

                newOffsetX = oldOffsetX;
                newOffsetY = oldOffsetY;
                newOffsetZ = oldOffsetZ;
                newZoom = oldZoom;
                newTrigger = oldTrigger;
                if (newTrigger == 0) {
                    jRadioButton1.setSelected(true);
                    jRadioButton2.setSelected(false);
                } else {
                    jRadioButton2.setSelected(true);
                    jRadioButton1.setSelected(false);
                }

                newLookDirX = oldLookDirX;
                newLookDirY = oldLookDirY;
                newLookDirZ = oldLookDirZ;
                newLookDirW = oldLookDirW;


                if (editor != null && parentCellComp == null) {
                    parentCellComp = editor.getCell().getComponent(BestViewComponent.class);
                }

                jSpinner7.setValue(oldOffsetX);
                jSpinner8.setValue(oldOffsetY);
                jSpinner9.setValue(oldOffsetZ);
                jSpinner10.setValue(oldLookDirX);
                jSpinner11.setValue(oldLookDirY);
                jSpinner12.setValue(oldLookDirZ);
                jTextField1.setText(String.valueOf(oldZoom));
            }
        }
    }

    /**
     * Called to make the changes permanent
     */
    public void apply() {
        // update the ComponentServerState object in the editor
        if (checkDirty()) {
            if (editor != null && parentCellComp == null) {
                parentCellComp = editor.getCell().getComponent(BestViewComponent.class);
            }
            Quaternion rot = ViewManager.getViewManager().getCameraTransform().getRotation(null);

            //check if rotation is changed or not
            if ((oldLookDirX != newLookDirX) || (oldLookDirY != newLookDirY) || (oldLookDirZ != newLookDirZ)) {
                newLookDirW = rot.w;
            }
            CellServerState state = editor.getCellServerState();
            CellComponentServerState compState = state.getComponentServerState(BestViewServerState.class);
            ((BestViewServerState) compState).setOffsetX(newOffsetX);
            ((BestViewServerState) compState).setOffsetY(newOffsetY);
            ((BestViewServerState) compState).setOffsetZ(newOffsetZ);
            ((BestViewServerState) compState).setLookDirX(newLookDirX);
            ((BestViewServerState) compState).setLookDirY(newLookDirY);
            ((BestViewServerState) compState).setLookDirZ(newLookDirZ);
            ((BestViewServerState) compState).setLookDirW(newLookDirW);
            ((BestViewServerState) compState).setOldObjPosX(objOldPosX);
            ((BestViewServerState) compState).setOldObjPosY(objOldPosY);
            ((BestViewServerState) compState).setOldObjPosZ(objOldPosZ);
            ((BestViewServerState) compState).setZoom(newZoom);
            ((BestViewServerState) compState).setTrigger(newTrigger);
            editor.addToUpdateList(compState);

        }
    }

    public boolean checkDirty() {
        dirty = false;

        if (!(jTextField1.getText().equals(null) || jTextField1.getText().equals(""))) {
            newZoom = Float.parseFloat(jTextField1.getText());
        }

        dirty |= !(newOffsetX == oldOffsetX);
        dirty |= !(newOffsetY == oldOffsetY);
        dirty |= !(newOffsetZ == oldOffsetZ);

        dirty |= !(newLookDirX == oldLookDirX);
        dirty |= !(newLookDirY == oldLookDirY);
        dirty |= !(newLookDirZ == oldLookDirZ);
        dirty |= !(newLookDirW == oldLookDirW);
        dirty |= !(newZoom == oldZoom);
        dirty |= !(newTrigger == oldTrigger);

        if (editor != null) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }

        return dirty;
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
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jSpinner7 = new javax.swing.JSpinner();
        jSpinner8 = new javax.swing.JSpinner();
        jSpinner9 = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSpinner10 = new javax.swing.JSpinner();
        jSpinner11 = new javax.swing.JSpinner();
        jSpinner12 = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/bestview/client/Bundle"); // NOI18N
        jLabel8.setText(bundle.getString("BestViewComponentProperties.jLabel8.text")); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 0, 0));
        jLabel6.setText(bundle.getString("BestViewComponentProperties.jLabel6.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 153, 0));
        jLabel7.setText(bundle.getString("BestViewComponentProperties.jLabel7.text")); // NOI18N

        jLabel11.setText(bundle.getString("BestViewComponentProperties.jLabel11.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setText(bundle.getString("BestViewComponentProperties.jLabel5.text")); // NOI18N

        jButton1.setText(bundle.getString("BestViewComponentProperties.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jSpinner7.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        NumberEditor ne7 = (NumberEditor)(jSpinner7.getEditor());
        ne7.getFormat().setMinimumFractionDigits(2);
        jSpinner7.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner7StateChanged(evt);
            }
        });

        jSpinner8.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner8.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        NumberEditor ne8 = (NumberEditor)(jSpinner7.getEditor());
        ne8.getFormat().setMinimumFractionDigits(2);
        jSpinner8.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner8StateChanged(evt);
            }
        });

        jSpinner9.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner9.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        NumberEditor ne9 = (NumberEditor)(jSpinner7.getEditor());
        ne9.getFormat().setMinimumFractionDigits(2);
        jSpinner9.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner9StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6))
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinner7, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                    .addComponent(jSpinner8)
                    .addComponent(jSpinner9))
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jSpinner7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinner8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(7, 7, 7))
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jButton1)
                    .addComponent(jSpinner9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText(bundle.getString("BestViewComponentProperties.jLabel1.text")); // NOI18N

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 255));
        jLabel12.setText(bundle.getString("BestViewComponentProperties.jLabel12.text")); // NOI18N

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 0, 0));
        jLabel9.setText(bundle.getString("BestViewComponentProperties.jLabel9.text")); // NOI18N

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 153, 0));
        jLabel10.setText(bundle.getString("BestViewComponentProperties.jLabel10.text")); // NOI18N

        jLabel13.setText(bundle.getString("BestViewComponentProperties.jLabel13.text")); // NOI18N

        jSpinner10.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner10.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        NumberEditor ne10 = (NumberEditor)(jSpinner7.getEditor());
        ne10.getFormat().setMinimumFractionDigits(2);
        jSpinner10.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner10StateChanged(evt);
            }
        });

        jSpinner11.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner11.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        NumberEditor ne11 = (NumberEditor)(jSpinner7.getEditor());
        ne11.getFormat().setMinimumFractionDigits(2);
        jSpinner11.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner11StateChanged(evt);
            }
        });

        jSpinner12.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner12.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        NumberEditor ne12 = (NumberEditor)(jSpinner7.getEditor());
        ne12.getFormat().setMinimumFractionDigits(2);
        jSpinner12.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner12StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(122, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(29, 29, 29))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinner10, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                            .addComponent(jSpinner11)
                            .addComponent(jSpinner12))))
                .addGap(21, 21, 21)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jSpinner10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinner11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinner12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)))
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText(bundle.getString("BestViewComponentProperties.jLabel2.text")); // NOI18N

        jLabel3.setText(bundle.getString("BestViewComponentProperties.jLabel3.text")); // NOI18N

        jTextField1.setText(bundle.getString("BestViewComponentProperties.jTextField1.text")); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTextField1PropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jLabel2)
                .addGap(26, 26, 26)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setText(bundle.getString("BestViewComponentProperties.jLabel4.text")); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText(bundle.getString("BestViewComponentProperties.jRadioButton1.text")); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(bundle.getString("BestViewComponentProperties.jRadioButton2.text")); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(jLabel4)
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jRadioButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // fill up the current psition of camera
        Vector3f pos = ViewManager.getViewManager().getCameraPosition(null);

        Vector3f pos1 = editor.getCell().getWorldTransform().getTranslation(null);
        Quaternion quat = editor.getCell().getWorldTransform().getRotation(null);
        float angles[] = new float[3];
        quat.toAngles(angles);
        objOldPosX = (float) Math.toDegrees(angles[0]);
        objOldPosY = (float) Math.toDegrees(angles[1]);
        objOldPosZ = (float) Math.toDegrees(angles[2]);
        jSpinner7.setValue(pos1.x - pos.x);
        jSpinner8.setValue(pos1.y - pos.y);
        jSpinner9.setValue(pos1.z - pos.z);
        Quaternion rot = ViewManager.getViewManager().getCameraTransform().getRotation(null);
        //newLookDirW = rot.w;
        jSpinner10.setValue(rot.x);
        jSpinner11.setValue(rot.y);
        jSpinner12.setValue(rot.z);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jSpinner7StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner7StateChanged
        // TODO add your handling code here:
        newOffsetX = Float.parseFloat(String.valueOf(jSpinner7.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner7StateChanged

    private void jSpinner8StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner8StateChanged
        // TODO add your handling code here:
        newOffsetY = Float.parseFloat(String.valueOf(jSpinner8.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner8StateChanged

    private void jSpinner9StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner9StateChanged
        // TODO add your handling code here:
        newOffsetZ = Float.parseFloat(String.valueOf(jSpinner9.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner9StateChanged

    private void jSpinner10StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner10StateChanged
        // TODO add your handling code here:
        newLookDirX = Float.parseFloat(String.valueOf(jSpinner10.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner10StateChanged

    private void jSpinner11StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner11StateChanged
        // TODO add your handling code here:
        newLookDirY = Float.parseFloat(String.valueOf(jSpinner11.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner11StateChanged

    private void jSpinner12StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner12StateChanged
        // TODO add your handling code here:
        newLookDirZ = Float.parseFloat(String.valueOf(jSpinner12.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner12StateChanged

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTextField1PropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1PropertyChange

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
        newTrigger = 0;
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
        newTrigger = 1;
        if (checkDirty()) {
            editor.setPanelDirty(BestViewComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jRadioButton2ActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JSpinner jSpinner10;
    private javax.swing.JSpinner jSpinner11;
    private javax.swing.JSpinner jSpinner12;
    private javax.swing.JSpinner jSpinner7;
    private javax.swing.JSpinner jSpinner8;
    private javax.swing.JSpinner jSpinner9;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
