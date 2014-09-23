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
package org.jdesktop.wonderland.modules.navigateto.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Color;
import java.math.BigDecimal;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.navigateto.common.NavigateToServerState;

/**
 * Component properties for best view component
 *
 * @author nilang shah
 * @author Abhishek Upadhyay
 */
@PropertiesFactory(NavigateToServerState.class)
public class NavigateToComponentProperties extends javax.swing.JPanel
        implements PropertiesFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.navigateto.client.Bundle");
    /**
     * the properties editor
     */
    private CellPropertiesEditor editor;
    private NavigateToComponent parentCellComp = null;
    private int newTrigger;
    private int oldTrigger;
    private float newOffsetX;
    private float oldOffsetX;
    private float newOffsetY;
    private float oldOffsetY;
    private float newOffsetZ;
    private float oldOffsetZ;
    private float newLookDir;
    private float oldLookDir;
    private boolean newBestview;
    private boolean oldBestview;
    boolean dirty;
    private float oldObjPos = 999;

    /**
     * Creates new form BestViewComponentProperties
     */
    public NavigateToComponentProperties() {
        initComponents();

        //listen events for text field
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (checkDirty()) {
                    editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if (checkDirty()) {
                    editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
                }
            }

            public void changedUpdate(DocumentEvent e) {
                if (checkDirty()) {
                    editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
                }
            }
        });
        int width = 160;
        String text = "Use this offset to adjust the \"landing\" position of the avatar in front of the object.";
        String text1 = "To automatically fill in these values, move your avatar to the correct position in front of the object. Then press this button.";
        jLabel11.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, text));
        jLabel12.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, text1));
        if (editor != null) {
            parentCellComp = editor.getCell().getComponent(NavigateToComponent.class);
        }
        jLabel13.setVisible(false);
    }

    /**
     * Get the display name of this editor
     */
    public String getDisplayName() {
        return BUNDLE.getString("Navigate_To");
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
        // restore default values from the editor
        populateValues();
    }

    /**
     * populate property values
     */
    public void populateValues() {
        if (editor != null) {
            CellServerState state = editor.getCellServerState();
            if (state != null) {
                CellComponentServerState compState = state.getComponentServerState(NavigateToServerState.class);
                NavigateToServerState navigateToComponentServerState = (NavigateToServerState) compState;
                if (navigateToComponentServerState != null) {
                    oldTrigger = navigateToComponentServerState.getTrigger();
                    oldOffsetX = navigateToComponentServerState.getOffsetX();
                    oldOffsetY = navigateToComponentServerState.getOffsetY();
                    oldOffsetZ = navigateToComponentServerState.getOffsetZ();
                    oldBestview = navigateToComponentServerState.getBestView();
                    oldLookDir = navigateToComponentServerState.getLookDirY();
                    newTrigger = oldTrigger;
                    newOffsetX = oldOffsetX;
                    newOffsetY = oldOffsetY;
                    newOffsetZ = oldOffsetZ;
                    newBestview = oldBestview;
                    newLookDir = oldLookDir;
                    if (editor != null && parentCellComp == null) {
                        parentCellComp = editor.getCell().getComponent(NavigateToComponent.class);
                    }
                    if (oldTrigger == 0) {
                        jRadioButton1.setSelected(true);
                        jRadioButton2.setSelected(false);
                    } else {
                        jRadioButton2.setSelected(true);
                        jRadioButton1.setSelected(false);
                    }
                    jSpinner1.setValue(oldOffsetX);
                    jSpinner2.setValue(oldOffsetY);
                    jSpinner3.setValue(oldOffsetZ);
                    jCheckBox1.setSelected(newBestview);

                    //convert angle into degree and populate in textfield
                    Quaternion q1 = ((AvatarCell) ClientContextJME.getViewManager().getPrimaryViewCell())
                            .getWorldTransform().getRotation(null);
                    float angles[] = new float[3];
                    q1.toAngles(angles);
                    angles[0] = (float) Math.toDegrees(angles[0]);
                    angles[1] = (float) Math.toDegrees(angles[1]);
                    angles[2] = (float) Math.toDegrees(angles[2]);
                    BigDecimal bd = new BigDecimal(oldLookDir);
                    BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                    jTextField1.setText(String.valueOf(rounded.doubleValue()));

                }
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
                parentCellComp = editor.getCell().getComponent(NavigateToComponent.class);
            }
            //convert angle from degree to vector
            float angle = 0;
            if (isNumeric(jTextField1.getText())) {
                angle = Float.parseFloat(jTextField1.getText());
            } else {
                restore();
                return;
            }
            Quaternion cellQuat = editor.getCell().getWorldTransform().getRotation(null);
            float[] ff = new float[3];
            cellQuat.toAngles(ff);
            Quaternion q2 = new Quaternion(new float[]{0, (float) Math.toRadians(angle), 0});
            float[] f1 = new float[3];
            q2.toAngles(f1);
            Quaternion my1 = new Quaternion();
            my1.fromAngles(f1);
            //set values in server state
            CellServerState state = editor.getCellServerState();
            CellComponentServerState compState = state.getComponentServerState(NavigateToServerState.class);
            ((NavigateToServerState) compState).setTrigger(newTrigger);
            ((NavigateToServerState) compState).setOffsetX(newOffsetX);
            ((NavigateToServerState) compState).setOffsetY(newOffsetY);
            ((NavigateToServerState) compState).setOffsetZ(newOffsetZ);
            ((NavigateToServerState) compState).setBestView(newBestview);
            if (oldObjPos == 999) {
                //object rotation
                Quaternion objq = editor.getCell().getWorldTransform().getRotation(null);
                float oangles[] = new float[3];
                objq.toAngles(oangles);
                oangles[0] = (float) Math.toDegrees(oangles[0]);
                oangles[1] = (float) Math.toDegrees(oangles[1]);
                oangles[2] = (float) Math.toDegrees(oangles[2]);
                ((NavigateToServerState) compState).setLookDirX(oangles[1]);
            } else {
                ((NavigateToServerState) compState).setLookDirX(oldObjPos);
            }
            ((NavigateToServerState) compState).setLookDirY(angle);
            editor.addToUpdateList(compState);
        }

    }

    public boolean checkDirty() {
        dirty = false;
        if (!(jTextField1.getText().equals(null) || jTextField1.getText().equals(""))) {
            try {
                newLookDir = Float.parseFloat(jTextField1.getText());
                jLabel13.setVisible(false);
            } catch (Exception e) {
                jLabel13.setVisible(true);
                editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
                return false;
            }
        }
        dirty |= !(newTrigger == oldTrigger);
        dirty |= !(newOffsetX == oldOffsetX);
        dirty |= !(newOffsetY == oldOffsetY);
        dirty |= !(newOffsetZ == oldOffsetZ);
        dirty |= !(newBestview == oldBestview);
        dirty |= !(newLookDir == oldLookDir);
        return dirty;
    }

    public static boolean isNumeric(String str) {
        int dot = 0;
        int minus = 0;
        for (char c : str.toCharArray()) {
            if (c == '-') {
                if (minus == 1) {
                    return false;
                }
                minus++;
                continue;
            }
            if (c == '.') {
                if (dot == 1) {
                    return false;
                }
                dot++;
                continue;
            }
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
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
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jRadioButton2 = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jSpinner2 = new javax.swing.JSpinner();
        jSpinner3 = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();

        buttonGroup1.add(jRadioButton1);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/navigateto/client/Bundle"); // NOI18N
        jRadioButton1.setText(bundle.getString("NavigateToComponentProperties.jRadioButton1.text")); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText(bundle.getString("NavigateToComponentProperties.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText(bundle.getString("NavigateToComponentProperties.jLabel2.text")); // NOI18N

        jCheckBox1.setText(bundle.getString("NavigateToComponentProperties.jCheckBox1.text")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(bundle.getString("NavigateToComponentProperties.jRadioButton2.text")); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jCheckBox1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner2StateChanged(evt);
            }
        });
        jSpinner2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jSpinner2FocusLost(evt);
            }
        });

        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner3StateChanged(evt);
            }
        });
        jSpinner3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jSpinner3FocusLost(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 255));
        jLabel8.setText(bundle.getString("NavigateToComponentProperties.jLabel8.text")); // NOI18N

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.01f)));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });
        jSpinner1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jSpinner1FocusLost(evt);
            }
        });
        jSpinner1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinner1PropertyChange(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 0, 0));
        jLabel6.setText(bundle.getString("NavigateToComponentProperties.jLabel6.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 153, 0));
        jLabel7.setText(bundle.getString("NavigateToComponentProperties.jLabel7.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setText(bundle.getString("NavigateToComponentProperties.jLabel5.text")); // NOI18N

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText(bundle.getString("NavigateToComponentProperties.jLabel9.text")); // NOI18N

        jTextField1.setText(bundle.getString("NavigateToComponentProperties.jTextField1.text")); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 0, 51));
        jLabel13.setText(bundle.getString("NavigateToComponentProperties.jLabel13.text")); // NOI18N

        jLabel10.setText(bundle.getString("NavigateToComponentProperties.jLabel10.text")); // NOI18N

        jLabel11.setText(bundle.getString("NavigateToComponentProperties.jLabel11.text")); // NOI18N
        jLabel11.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jButton1.setText(bundle.getString("NavigateToComponentProperties.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel12.setText(bundle.getString("NavigateToComponentProperties.jLabel12.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel5))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8))))))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(180, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
        newTrigger = 0;
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }

    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
        newTrigger = 1;
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }

    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        //calculate offset values
        Vector3f avatarCellVector = ((AvatarCell) ClientContextJME.getViewManager().getPrimaryViewCell()).getWorldTransform().getTranslation(null);
        Vector3f cellVector = editor.getCell().getWorldTransform().getTranslation(null);
        newOffsetX = ((float) cellVector.getX() - (float) avatarCellVector.getX());
        newOffsetY = ((float) cellVector.getY() - (float) avatarCellVector.getY());
        newOffsetZ = ((float) cellVector.getZ() - (float) avatarCellVector.getZ());
        //Fill the offset values
        jSpinner1.setValue(newOffsetX);
        jSpinner2.setValue(newOffsetY);
        jSpinner3.setValue(newOffsetZ);
        //calculate look direction
        Quaternion q1 = ((AvatarCell) ClientContextJME.getViewManager().getPrimaryViewCell())
                .getWorldTransform().getRotation(null);
        float angles[] = new float[3];
        q1.toAngles(angles);
        angles[0] = (float) Math.toDegrees(angles[0]);
        angles[1] = (float) Math.toDegrees(angles[1]);
        angles[2] = (float) Math.toDegrees(angles[2]);

        //object rotation
        Quaternion objq = editor.getCell().getWorldTransform().getRotation(null);
        float oangles[] = new float[3];
        objq.toAngles(oangles);
        oangles[0] = (float) Math.toDegrees(oangles[0]);
        oangles[1] = (float) Math.toDegrees(oangles[1]);
        oangles[2] = (float) Math.toDegrees(oangles[2]);

        oldObjPos = oangles[1];
        BigDecimal bd = new BigDecimal(angles[1]);
        BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        jTextField1.setText(String.valueOf(rounded.doubleValue()));
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jSpinner1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinner1PropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinner1PropertyChange

    private void jSpinner1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jSpinner1FocusLost
        // TODO add your handling code here:
        newOffsetX = Float.parseFloat(String.valueOf(jSpinner1.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner1FocusLost

    private void jSpinner2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jSpinner2FocusLost
        // TODO add your handling code here:
        newOffsetY = Float.parseFloat(String.valueOf(jSpinner2.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner2FocusLost

    private void jSpinner3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jSpinner3FocusLost
        // TODO add your handling code here:
        newOffsetZ = Float.parseFloat(String.valueOf(jSpinner3.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner3FocusLost

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        // TODO add your handling code here:
        newOffsetX = Float.parseFloat(String.valueOf(jSpinner1.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner1StateChanged

    private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
        // TODO add your handling code here:
        newOffsetY = Float.parseFloat(String.valueOf(jSpinner2.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner2StateChanged

    private void jSpinner3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner3StateChanged
        // TODO add your handling code here:
        newOffsetZ = Float.parseFloat(String.valueOf(jSpinner3.getValue()));
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jSpinner3StateChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        newBestview = jCheckBox1.isSelected();
        if (checkDirty()) {
            editor.setPanelDirty(NavigateToComponentProperties.class, dirty);
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
    }//GEN-LAST:event_jTextField1ActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
