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
package org.jdesktop.wonderland.modules.sitting.client;

import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sitting.common.SittingCellComponentServerState;

/**
 *
 * @author Morris Ford
 */
@PropertiesFactory(SittingCellComponentServerState.class)
public class SittingComponentProperties extends JPanel implements PropertiesFactorySPI
    {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/sitting/client/resources/Bundle");
    private CellPropertiesEditor editor = null;
    private String originalInfo = null;
    private float originalOffset = 0.1f;
    private float originalHeading = 0.1f;
    private String originalMouse = "Left Mouse";

    /** Creates new form SampleComponentProperties */
    public SittingComponentProperties()
        {
        // Initialize the GUI
        initComponents();

        // Listen for changes to the info text field
        infoTextField.getDocument().addDocumentListener(new InfoTextFieldListener());
        jTextHeading.getDocument().addDocumentListener(new jTextHeadingListener());
        jTextOffset.getDocument().addDocumentListener(new jTextOffsetListener());
        }

    /**
     * @inheritDoc()
     */
    public String getDisplayName()
        {
        return BUNDLE.getString("Sitting_Component");
        }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel()
        {
        return this;
        }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor)
        {
        this.editor = editor;
        }

    /**
     * @inheritDoc()
     */
    public void open()
        {
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(SittingCellComponentServerState.class);
        if (state != null)
            {
            SittingCellComponentServerState sittingCellComponentServerState = (SittingCellComponentServerState) compState;
            originalInfo = sittingCellComponentServerState.getInfo();
            infoTextField.setText(originalInfo);
            originalHeading = sittingCellComponentServerState.getHeading();
            jTextHeading.setText(Float.toString(originalHeading));
            originalOffset = sittingCellComponentServerState.getOffset();
            jTextOffset.setText(Float.toString(originalOffset));
            originalMouse = sittingCellComponentServerState.getMouse();
            }
        }

    /**
     * @inheritDoc()
     */
    public void close()
        {
        // Do nothing for now.
        }

    /**
     * @inheritDoc()
     */
    public void apply()
        {
        // Fetch the latest from the info text field and set it.
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(SittingCellComponentServerState.class);
        ((SittingCellComponentServerState) compState).setInfo(infoTextField.getText());
        ((SittingCellComponentServerState) compState).setHeading(Float.valueOf(jTextHeading.getText()));
        ((SittingCellComponentServerState) compState).setOffset(Float.valueOf(jTextOffset.getText()));
        ((SittingCellComponentServerState) compState).setMouse((String) jComboMouse.getSelectedItem());
        editor.addToUpdateList(compState);
        }

    /**
     * @inheritDoc()
     */
    public void restore()
        {
        // Restore from the original state stored.
        infoTextField.setText(originalInfo);
        jTextHeading.setText(Float.toString(originalHeading));
        jTextOffset.setText(Float.toString(originalOffset));
        }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class InfoTextFieldListener implements DocumentListener
        {

        public void insertUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        public void removeUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        public void changedUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        private void checkDirty()
            {
            String name = infoTextField.getText();
            if (editor != null && name.equals(originalInfo) == false)
                {
                editor.setPanelDirty(SittingComponentProperties.class, true);
                }
            else if (editor != null)
                {
                editor.setPanelDirty(SittingComponentProperties.class, false);
                }
            }
        }

    class jTextOffsetListener implements DocumentListener
        {

        public void insertUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        public void removeUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        public void changedUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        private void checkDirty()
            {
            String look = jTextOffset.getText();
            if (editor != null && look.equals(Float.toString(originalOffset)) == false)
                {
                editor.setPanelDirty(SittingComponentProperties.class, true);
                }
            else if (editor != null)
                {
                editor.setPanelDirty(SittingComponentProperties.class, false);
                }
            }
        }


    class jTextHeadingListener implements DocumentListener
        {

        public void insertUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        public void removeUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        public void changedUpdate(DocumentEvent e)
            {
            checkDirty();
            }

        private void checkDirty()
            {
            String look = jTextHeading.getText();
            if (editor != null && look.equals(Float.toString(originalHeading)) == false)
                {
                editor.setPanelDirty(SittingComponentProperties.class, true);
                }
            else if (editor != null)
                {
                editor.setPanelDirty(SittingComponentProperties.class, false);
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

        jLabel1 = new javax.swing.JLabel();
        infoTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextHeading = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextOffset = new javax.swing.JTextField();
        jComboMouse = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();

        jLabel1.setText("Info:");

        jLabel2.setText("Heading");

        jTextHeading.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextHeadingActionPerformed(evt);
            }
        });

        jLabel5.setText("Sit Offset");

        jComboMouse.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Left Mouse", "Middle Mouse", "Right Mouse", "Shift Left Mouse", "Shift Middle Mouse", "Shift Right Mouse", "Control Left Mouse", "Control Middle Mouse", "Control Right Mouse" }));
        jComboMouse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboMouseActionPerformed(evt);
            }
        });

        jLabel6.setText("Mouse Event");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(infoTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel5))
                        .add(53, 53, 53)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jTextOffset)
                            .add(jTextHeading, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
                        .add(39, 39, 39)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jComboMouse, 0, 0, Short.MAX_VALUE)
                            .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                        .add(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(infoTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel6)
                    .add(jTextHeading, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel5)
                        .add(jTextOffset, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jComboMouse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(160, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextHeadingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextHeadingActionPerformed
}//GEN-LAST:event_jTextHeadingActionPerformed

    private void jComboMouseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboMouseActionPerformed
        editor.setPanelDirty(SittingComponentProperties.class, true);
    }//GEN-LAST:event_jComboMouseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField infoTextField;
    private javax.swing.JComboBox jComboMouse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField jTextHeading;
    private javax.swing.JTextField jTextOffset;
    // End of variables declaration//GEN-END:variables
}
