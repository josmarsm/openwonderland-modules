/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerState;

/**
 * Webcam Viewer control panel.
 *
 * @author nsimpson
 */
public class WebcamViewerControlPanel extends javax.swing.JPanel implements WebcamViewerControls {

    private static final Logger logger = Logger.getLogger(WebcamViewerControlPanel.class.getName());
    protected boolean fillMode = false;
    protected ArrayList<WebcamViewerToolListener> cellMenuListeners = new ArrayList();
    protected Map toolMappings;
    protected Map colorMappings;
    protected WebcamViewerWindow window;
    protected WebcamViewerDragGestureListener gestureListener;
    private ImageIcon playIcon;
    private ImageIcon pauseIcon;

    public WebcamViewerControlPanel(WebcamViewerWindow window) {
        this.window = window;
        initComponents();
        initListeners();
        playIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamPlay32x32.png"));
        pauseIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamPause32x32.png"));
    }

    /**
     * Get the component associated with these controls
     * @return the associated component
     */
    public JComponent getComponent() {
        return this;
    }

    /**
     * Add a listener for tool events
     * @param listener a listener to receive tool events
     */
    public void addCellMenuListener(WebcamViewerToolListener listener) {
        cellMenuListeners.add(listener);
    }

    /**
     * Remove a listener for tool events
     * @param listener the listener to remove
     */
    public void removeCellMenuListener(WebcamViewerToolListener listener) {
        cellMenuListeners.remove(listener);
    }

    /**
     *  Initialize drag and drop listener
     */
    private void initListeners() {
        DragSource ds = DragSource.getDefaultDragSource();
        gestureListener = new WebcamViewerDragGestureListener(window);
        ds.createDefaultDragGestureRecognizer(dragHUDButton,
                DnDConstants.ACTION_COPY_OR_MOVE, gestureListener);
    }

    /**
     * Update control panel mode to reflect state of player
     * @param state the state of the player
     */
    public void setMode(WebcamViewerState state) {
        switch (state) {
            case PLAYING:
                playHUDButton.setIcon(pauseIcon);
                break;
            case PAUSED:
                playHUDButton.setIcon(playIcon);
                break;
            case STOPPED:
                playHUDButton.setIcon(playIcon);
                break;
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
     * Depress/undepress a button (for displaying a selected mode)
     * @param button the button to depress
     * @param depress true to depress a button, false to undepress
     */
    public void depressButton(JButton button, boolean depress) {
        button.setBorderPainted(depress);
    }

    /**
     * Set the state of the on-HUD button
     * @param onHUD true if the control panel is displayed on-HUD, false
     * if in-world
     */
    public void setOnHUD(boolean onHUD) {
        if (onHUD) {
            toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamDock32x32.png")));
        } else {
            toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamUndock32x32.png")));
        }
    }

    /**
     * Set the state of the sync button
     * @param synced true if synced, false if not
     */
    public void setSynced(boolean synced) {
        if (synced) {
            syncHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamSyncedYes32x32.png")));
        } else {
            syncHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamSyncedNo32x32.png")));
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

        toggleHUDButton = new javax.swing.JButton();
        openHUDButton = new javax.swing.JButton();
        stopHUDButton = new javax.swing.JButton();
        playHUDButton = new javax.swing.JButton();
        syncHUDButton = new javax.swing.JButton();
        dragHUDButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(231, 230, 230));

        toggleHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamDock32x32.png"))); // NOI18N
        toggleHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        toggleHUDButton.setBorderPainted(false);
        toggleHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        toggleHUDButton.setOpaque(true);
        toggleHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleHUDButtonActionPerformed(evt);
            }
        });

        openHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        openHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamOpen32x32.png"))); // NOI18N
        openHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        openHUDButton.setBorderPainted(false);
        openHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        openHUDButton.setOpaque(true);
        openHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openHUDButtonActionPerformed(evt);
            }
        });

        stopHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        stopHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamStop32x32.png"))); // NOI18N
        stopHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        stopHUDButton.setBorderPainted(false);
        stopHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        stopHUDButton.setOpaque(true);
        stopHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopHUDButtonActionPerformed(evt);
            }
        });

        playHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        playHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamPlay32x32.png"))); // NOI18N
        playHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        playHUDButton.setBorderPainted(false);
        playHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        playHUDButton.setOpaque(true);
        playHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playHUDButtonActionPerformed(evt);
            }
        });

        syncHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        syncHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamSyncedYes32x32.png"))); // NOI18N
        syncHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        syncHUDButton.setBorderPainted(false);
        syncHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        syncHUDButton.setOpaque(true);
        syncHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncHUDButtonActionPerformed(evt);
            }
        });

        dragHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        dragHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcamviewer/client/resources/WebCamDrag32x32.png"))); // NOI18N
        dragHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        dragHUDButton.setBorderPainted(false);
        dragHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        dragHUDButton.setOpaque(true);
        dragHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dragHUDButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toggleHUDButton)
                .add(0, 0, 0)
                .add(openHUDButton)
                .add(0, 0, 0)
                .add(stopHUDButton)
                .add(0, 0, 0)
                .add(playHUDButton)
                .add(0, 0, 0)
                .add(syncHUDButton)
                .add(0, 0, 0)
                .add(dragHUDButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toggleHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(openHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(stopHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(playHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(syncHUDButton)
            .add(dragHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void toggleHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleHUDButtonActionPerformed
        Iterator<WebcamViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WebcamViewerToolListener listener = iter.next();
            listener.toggleHUD();
        }
}//GEN-LAST:event_toggleHUDButtonActionPerformed

    private void openHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openHUDButtonActionPerformed
        Iterator<WebcamViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WebcamViewerToolListener listener = iter.next();
            listener.connectCamera();
        }
    }//GEN-LAST:event_openHUDButtonActionPerformed

    private void stopHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopHUDButtonActionPerformed
        Iterator<WebcamViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WebcamViewerToolListener listener = iter.next();
            listener.stop();
        }
    }//GEN-LAST:event_stopHUDButtonActionPerformed

    private void playHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playHUDButtonActionPerformed
        Iterator<WebcamViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WebcamViewerToolListener listener = iter.next();
            if (window.isPlaying()) {
                listener.pause();
            } else {
                listener.play();
            }
        }
    }//GEN-LAST:event_playHUDButtonActionPerformed

    private void dragHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragHUDButtonActionPerformed
        Iterator<WebcamViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WebcamViewerToolListener listener = iter.next();
            // TODO: implement drag
        }
    }//GEN-LAST:event_dragHUDButtonActionPerformed

    private void syncHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncHUDButtonActionPerformed
        Iterator<WebcamViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WebcamViewerToolListener listener = iter.next();
            listener.sync();
        }
    }//GEN-LAST:event_syncHUDButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dragHUDButton;
    private javax.swing.JButton openHUDButton;
    private javax.swing.JButton playHUDButton;
    private javax.swing.JButton stopHUDButton;
    private javax.swing.JButton syncHUDButton;
    private javax.swing.JButton toggleHUDButton;
    // End of variables declaration//GEN-END:variables
}
