/**
  * iSocial Project
  * http://isocial.missouri.edu
  *
  * Copyright (c) 2011, University of Missouri iSocial Project, All 
  * Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * The iSocial project designates this particular file as
  * subject to the "Classpath" exception as provided by the iSocial
  * project in the License file that accompanied this code.
  */

/*
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

/*
 * WhiteboardControlPanel.java
 *
 * Created on Jan 29, 2009, 4:55:50 PM
 */
package org.jdesktop.wonderland.modules.mediaboard.client;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.jdesktop.wonderland.modules.mediaboard.client.WhiteboardToolManager.WhiteboardColor;
import org.jdesktop.wonderland.modules.mediaboard.client.WhiteboardToolManager.WhiteboardTool;

/**
 *
 * @author nsimpson
 * @author Ryan Babiuch
 */
public class WhiteboardControlPanel extends javax.swing.JPanel implements CellMenu {

    private static final Logger logger = Logger.getLogger(WhiteboardControlPanel.class.getName());
    protected boolean fillMode = false;
    protected ArrayList<WhiteboardCellMenuListener> cellMenuListeners = new ArrayList();
    protected Map toolMappings;
    protected Map colorMappings;
    protected WhiteboardDragGestureListener gestureListener;
    protected WhiteboardWindow window;

    public WhiteboardControlPanel(WhiteboardWindow window) {
        this.window = window;
        initComponents();
        initButtonMaps();
        initListeners();
    }

    public void addCellMenuListener(WhiteboardCellMenuListener listener) {
        cellMenuListeners.add(listener);
    }

    public void removeCellMenuListener(WhiteboardCellMenuListener listener) {
        cellMenuListeners.remove(listener);
    }

    private void initButtonMaps() {
        toolMappings = Collections.synchronizedMap(new HashMap());
        toolMappings.put(WhiteboardTool.SELECTOR, selectButton);
        toolMappings.put(WhiteboardTool.LINE, lineButton);
        toolMappings.put(WhiteboardTool.TEXT, textButton);
        toolMappings.put(WhiteboardTool.OPEN, openButton);
        toolMappings.put(WhiteboardTool.PICTURE, pictureButton);
        toolMappings.put(WhiteboardTool.SAVE, saveButton);

        colorMappings = Collections.synchronizedMap(new HashMap());
    }
    private void initListeners() {
        DragSource ds = DragSource.getDefaultDragSource();
        gestureListener = new WhiteboardDragGestureListener(window);
    }

    public void setSelectedColor(WhiteboardColor color) {
        JButton colorButton = (JButton) colorMappings.get(color);
        depressButton(colorButton, true);
    }

    public WhiteboardColor getSelectedColor() {
        return WhiteboardColor.BLACK;
    }

    /**
     * Gets whether a button is depressed
     * @param button the button to check
     * @return true if the button is depressed, false otherwise
     */
    public boolean isButtonDepressed(JButton button) {
        return button.isBorderPainted();
    }

    /**
     * Depress/undepress a button
     * @param button the button to depress
     * @param depress true to depress a button, false to undepress
     */
    public void depressButton(JButton button, boolean depress) {
        button.setBorderPainted(depress);
    }

    public void selectTool(WhiteboardTool tool) {
        JButton button = (JButton) toolMappings.get(tool);
        if (button != null) {
            depressButton(button, true);
        }
    }

    public void deselectTool(WhiteboardTool tool) {
        JButton button = (JButton) toolMappings.get(tool);
        if (button != null) {
            depressButton(button, false);
        }
    }

    public void selectColor(WhiteboardColor color) {
        JButton button = (JButton) colorMappings.get(color);
        if (button != null) {
            depressButton(button, true);
        }
    }

    public void deselectColor(WhiteboardColor color) {
        JButton button = (JButton) colorMappings.get(color);
        if (button != null) {
            depressButton(button, false);
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

        selectButton = new javax.swing.JButton();
        lineButton = new javax.swing.JButton();
        textButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        pictureButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(231, 230, 230));

        selectButton.setBackground(new java.awt.Color(231, 230, 230));
        selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/mediaboard/client/resources/select.png"))); // NOI18N
        selectButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        selectButton.setBorderPainted(false);
        selectButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        selectButton.setOpaque(true);
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        lineButton.setBackground(new java.awt.Color(231, 230, 230));
        lineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/mediaboard/client/resources/draw.png"))); // NOI18N
        lineButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        lineButton.setBorderPainted(false);
        lineButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        lineButton.setOpaque(true);
        lineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineButtonActionPerformed(evt);
            }
        });

        textButton.setBackground(new java.awt.Color(231, 230, 230));
        textButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/mediaboard/client/resources/text.png"))); // NOI18N
        textButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        textButton.setBorderPainted(false);
        textButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        textButton.setOpaque(true);
        textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textButtonActionPerformed(evt);
            }
        });

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/mediaboard/client/resources/save.png"))); // NOI18N
        saveButton.setBorderPainted(false);
        saveButton.setPreferredSize(new java.awt.Dimension(38, 38));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/mediaboard/client/resources/open.png"))); // NOI18N
        openButton.setBorderPainted(false);
        openButton.setPreferredSize(new java.awt.Dimension(38, 38));
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        pictureButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/mediaboard/client/resources/takePicture.png"))); // NOI18N
        pictureButton.setBorderPainted(false);
        pictureButton.setPreferredSize(new java.awt.Dimension(38, 38));
        pictureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pictureButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(selectButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lineButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(openButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pictureButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(selectButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lineButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(textButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(openButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(pictureButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        if (!isButtonDepressed(selectButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.selector();
            }
        }
}//GEN-LAST:event_selectButtonActionPerformed

    private void lineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineButtonActionPerformed
        if (!isButtonDepressed(lineButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.line();
            }
        }
}//GEN-LAST:event_lineButtonActionPerformed

    private void textButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textButtonActionPerformed
        if (!isButtonDepressed(textButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.text();
            }
        }
}//GEN-LAST:event_textButtonActionPerformed

    private void pictureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pictureButtonActionPerformed
        // TODO add your handling code here:
        if(!isButtonDepressed(pictureButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while(iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.picture();
            }
        }
    }//GEN-LAST:event_pictureButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
        if(!isButtonDepressed(saveButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while(iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.save();
            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        // TODO add your handling code here:
        if(!isButtonDepressed(openButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while(iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.open();
            }
        }
    }//GEN-LAST:event_openButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton lineButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton pictureButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton textButton;
    // End of variables declaration//GEN-END:variables
}
