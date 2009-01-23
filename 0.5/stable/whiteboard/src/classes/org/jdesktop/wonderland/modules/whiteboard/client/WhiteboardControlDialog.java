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
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.Color;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.border.Border;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardColor;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool;

/**
 *
 * @author nsimpson
 */
public class WhiteboardControlDialog extends javax.swing.JDialog implements CellMenu {

    private static final Logger logger = Logger.getLogger(WhiteboardControlDialog.class.getName());
    protected boolean fillMode = false;
    protected ArrayList<WhiteboardCellMenuListener> cellMenuListeners = new ArrayList();
    protected Border border;
    protected Map toolMappings;
    protected Map colorMappings;

    public WhiteboardControlDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        //makeTransparent();
        initComponents();
        initButtonMaps();
        border = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED);
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
        toolMappings.put(WhiteboardTool.RECT, rectangleButton);
        toolMappings.put(WhiteboardTool.ELLIPSE, ellipseButton);
        toolMappings.put(WhiteboardTool.TEXT, textButton);

        colorMappings = Collections.synchronizedMap(new HashMap());
        colorMappings.put(WhiteboardColor.RED, colorRedButton);
        colorMappings.put(WhiteboardColor.GREEN, colorGreenButton);
        colorMappings.put(WhiteboardColor.BLUE, colorBlueButton);
        colorMappings.put(WhiteboardColor.BLACK, colorBlackButton);
        colorMappings.put(WhiteboardColor.WHITE, colorWhiteButton);
    }

    /**
     * Make the dialog transparent
     * 
     * This method from:
     * http://blog.keilly.com/2008/05/creating-swing-widget-part-3.html
     */
    private void makeTransparent() {
        setUndecorated(true); // remove the window controls
        setResizable(false);  // remove the resize control

        // OSX transparency
        setBackground(new Color(0f, 0f, 0f, 0f));

        // Non-reflection version for Java 6 SE u10:
        // AWTUtilities.setWindowOpaque(this, false);
        //
        // Reflection version (to compile on Java 1.5):
        try {
            Class clazz = Class.forName("com.sun.awt.AWTUtilities");
            Method method = clazz.getMethod("setWindowOpaque",
                    new Class[]{Window.class, Boolean.TYPE});
            method.invoke(clazz, new Object[]{this, false});
        } catch (ClassNotFoundException e) {
            // Oh well, not Java 6 u10
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
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

    public void setFillMode() {
        fillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/fill.png")));
    }

    public void setDrawMode() {
        fillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/draw.png")));
    }

    public void setSynced(boolean synced) {
        if (synced == true) {
            syncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/sync.png")));
        } else {
            syncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/unsync.png")));
        }
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

        controlPanel = new javax.swing.JPanel();
        dragButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        lineButton = new javax.swing.JButton();
        rectangleButton = new javax.swing.JButton();
        ellipseButton = new javax.swing.JButton();
        textButton = new javax.swing.JButton();
        fillButton = new javax.swing.JButton();
        colorRedButton = new javax.swing.JButton();
        colorGreenButton = new javax.swing.JButton();
        colorBlueButton = new javax.swing.JButton();
        colorBlackButton = new javax.swing.JButton();
        colorWhiteButton = new javax.swing.JButton();
        syncButton = new javax.swing.JButton();

        setTitle("Whiteboard");
        setName("controlDialog"); // NOI18N

        controlPanel.setBackground(new java.awt.Color(231, 230, 230));
        controlPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        controlPanel.setAlignmentY(0.0F);
        controlPanel.setName("controlPanel"); // NOI18N

        dragButton.setEnabled(false);
        dragButton.setFocusCycleRoot(true);
        dragButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dragButton.setName("dragButton"); // NOI18N

        newButton.setBackground(new java.awt.Color(231, 230, 230));
        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/new.png"))); // NOI18N
        newButton.setBorderPainted(false);
        newButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        newButton.setName("newButton"); // NOI18N
        newButton.setOpaque(true);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        selectButton.setBackground(new java.awt.Color(231, 230, 230));
        selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/select.png"))); // NOI18N
        selectButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        selectButton.setBorderPainted(false);
        selectButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        selectButton.setName("selectButton"); // NOI18N
        selectButton.setOpaque(true);
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        lineButton.setBackground(new java.awt.Color(231, 230, 230));
        lineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/line.png"))); // NOI18N
        lineButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        lineButton.setBorderPainted(false);
        lineButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        lineButton.setName("lineButton"); // NOI18N
        lineButton.setOpaque(true);
        lineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineButtonActionPerformed(evt);
            }
        });

        rectangleButton.setBackground(new java.awt.Color(231, 230, 230));
        rectangleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/rect.png"))); // NOI18N
        rectangleButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        rectangleButton.setBorderPainted(false);
        rectangleButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        rectangleButton.setName("rectangleButton"); // NOI18N
        rectangleButton.setOpaque(true);
        rectangleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rectangleButtonActionPerformed(evt);
            }
        });

        ellipseButton.setBackground(new java.awt.Color(231, 230, 230));
        ellipseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/ellipse.png"))); // NOI18N
        ellipseButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        ellipseButton.setBorderPainted(false);
        ellipseButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        ellipseButton.setName("ellipseButton"); // NOI18N
        ellipseButton.setOpaque(true);
        ellipseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipseButtonActionPerformed(evt);
            }
        });

        textButton.setBackground(new java.awt.Color(231, 230, 230));
        textButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/text.png"))); // NOI18N
        textButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        textButton.setBorderPainted(false);
        textButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        textButton.setName("textButton"); // NOI18N
        textButton.setOpaque(true);
        textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textButtonActionPerformed(evt);
            }
        });

        fillButton.setBackground(new java.awt.Color(231, 230, 230));
        fillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/draw.png"))); // NOI18N
        fillButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        fillButton.setBorderPainted(false);
        fillButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        fillButton.setName("fillButton"); // NOI18N
        fillButton.setOpaque(true);
        fillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fillButtonActionPerformed(evt);
            }
        });

        colorRedButton.setBackground(new java.awt.Color(231, 230, 230));
        colorRedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/red.png"))); // NOI18N
        colorRedButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorRedButton.setBorderPainted(false);
        colorRedButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        colorRedButton.setName("colorRedButton"); // NOI18N
        colorRedButton.setOpaque(true);
        colorRedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorRedButtonActionPerformed(evt);
            }
        });

        colorGreenButton.setBackground(new java.awt.Color(231, 230, 230));
        colorGreenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/green.png"))); // NOI18N
        colorGreenButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorGreenButton.setBorderPainted(false);
        colorGreenButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        colorGreenButton.setName("colorGreenButton"); // NOI18N
        colorGreenButton.setOpaque(true);
        colorGreenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorGreenButtonActionPerformed(evt);
            }
        });

        colorBlueButton.setBackground(new java.awt.Color(231, 230, 230));
        colorBlueButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/blue.png"))); // NOI18N
        colorBlueButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorBlueButton.setBorderPainted(false);
        colorBlueButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        colorBlueButton.setName("colorBlueButton"); // NOI18N
        colorBlueButton.setOpaque(true);
        colorBlueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorBlueButtonActionPerformed(evt);
            }
        });

        colorBlackButton.setBackground(new java.awt.Color(231, 230, 230));
        colorBlackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/black.png"))); // NOI18N
        colorBlackButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorBlackButton.setBorderPainted(false);
        colorBlackButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        colorBlackButton.setName("colorBlackButton"); // NOI18N
        colorBlackButton.setOpaque(true);
        colorBlackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorBlackButtonActionPerformed(evt);
            }
        });

        colorWhiteButton.setBackground(new java.awt.Color(231, 230, 230));
        colorWhiteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/white.png"))); // NOI18N
        colorWhiteButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorWhiteButton.setBorderPainted(false);
        colorWhiteButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        colorWhiteButton.setName("colorWhiteButton"); // NOI18N
        colorWhiteButton.setOpaque(true);
        colorWhiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorWhiteButtonActionPerformed(evt);
            }
        });

        syncButton.setBackground(new java.awt.Color(231, 230, 230));
        syncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/sync.png"))); // NOI18N
        syncButton.setBorderPainted(false);
        syncButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        syncButton.setName("syncButton"); // NOI18N
        syncButton.setOpaque(true);
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout controlPanelLayout = new org.jdesktop.layout.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlPanelLayout.createSequentialGroup()
                .add(dragButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lineButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rectangleButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ellipseButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fillButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(colorRedButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(colorGreenButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(colorBlueButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(colorBlackButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(colorWhiteButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(syncButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(dragButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(newButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(selectButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(lineButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(rectangleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(ellipseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(textButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(fillButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(colorRedButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(colorGreenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(colorBlueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(colorBlackButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(colorWhiteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(syncButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void colorBlueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorBlueButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.blue();
        }
}//GEN-LAST:event_colorBlueButtonActionPerformed

    private void colorBlackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorBlackButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.black();
        }
}//GEN-LAST:event_colorBlackButtonActionPerformed

    private void colorGreenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorGreenButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.green();
        }
}//GEN-LAST:event_colorGreenButtonActionPerformed

    private void colorRedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorRedButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.red();
        }
}//GEN-LAST:event_colorRedButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.newDoc();
        }
}//GEN-LAST:event_newButtonActionPerformed

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

    private void rectangleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rectangleButtonActionPerformed
        if (!isButtonDepressed(rectangleButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.rect();
            }
        }
}//GEN-LAST:event_rectangleButtonActionPerformed

    private void ellipseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellipseButtonActionPerformed
        if (!isButtonDepressed(ellipseButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.ellipse();
            }
        }
}//GEN-LAST:event_ellipseButtonActionPerformed

    private void textButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textButtonActionPerformed
        if (!isButtonDepressed(textButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.text();
            }
        }
}//GEN-LAST:event_textButtonActionPerformed

    private void fillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            fillMode = !fillMode;   // toggle between fill and draw modes
            if (fillMode == true) {
                listener.fill();
            } else {
                listener.draw();
            }
        }
}//GEN-LAST:event_fillButtonActionPerformed

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.sync();
        }
}//GEN-LAST:event_syncButtonActionPerformed

    private void colorWhiteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorWhiteButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.white();
        }
}//GEN-LAST:event_colorWhiteButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                WhiteboardControlDialog dialog = new WhiteboardControlDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colorBlackButton;
    private javax.swing.JButton colorBlueButton;
    private javax.swing.JButton colorGreenButton;
    private javax.swing.JButton colorRedButton;
    private javax.swing.JButton colorWhiteButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton dragButton;
    private javax.swing.JButton ellipseButton;
    private javax.swing.JButton fillButton;
    private javax.swing.JButton lineButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton rectangleButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton syncButton;
    private javax.swing.JButton textButton;
    // End of variables declaration//GEN-END:variables
}
