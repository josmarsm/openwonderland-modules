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
package org.jdesktop.wonderland.modules.pdfviewer.client;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.pdfviewer.common.PDFViewerState;

/**
 * HUD control panel for PDF viewer.
 *
 * @author nsimpson
 */
public class PDFViewerControlPanel extends javax.swing.JPanel {

    private static final Logger logger = Logger.getLogger(PDFViewerControlPanel.class.getName());
    protected boolean fillMode = false;
    protected ArrayList<PDFViewerToolListener> cellMenuListeners = new ArrayList();
    protected Map toolMappings;
    protected Map colorMappings;
    protected PDFViewerWindow window;
    protected PDFViewerDragGestureListener gestureListener;
    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon syncIcon;
    private ImageIcon unsyncIcon;
    private ImageIcon dockedIcon;
    private ImageIcon undockedIcon;

    public PDFViewerControlPanel(PDFViewerWindow window) {
        this.window = window;
        initComponents();
        initIcons();
        initListeners();
    }

    public void addCellMenuListener(PDFViewerToolListener listener) {
        cellMenuListeners.add(listener);
    }

    public void removeCellMenuListener(PDFViewerToolListener listener) {
        cellMenuListeners.remove(listener);
    }

    private void initIcons() {
        playIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPlay32x32.png"));
        pauseIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPause32x32.png"));
        syncIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerSyncedYes32x32.png"));
        unsyncIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerSyncedNo32x32.png"));
        dockedIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerDock32x32.png"));
        undockedIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerUndock32x32.png"));
    }

    private void initListeners() {
        DragSource ds = DragSource.getDefaultDragSource();
        gestureListener = new PDFViewerDragGestureListener(window);
        ds.createDefaultDragGestureRecognizer(dragHUDButton,
                DnDConstants.ACTION_COPY_OR_MOVE, gestureListener);
    }

    public void setMode(final PDFViewerState state) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
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
                validate();
            }
        });
    }

    public void setOnHUD(final boolean onHUD) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (onHUD) {
                    toggleHUDButton.setIcon(dockedIcon);
                } else {
                    toggleHUDButton.setIcon(undockedIcon);
                }
            }
        });
    }

    public void setSynced(final boolean synced) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (synced) {
                    syncHUDButton.setIcon(syncIcon);
                } else {
                    syncHUDButton.setIcon(unsyncIcon);
                }
            }
        });

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
        firstPageHUDButton = new javax.swing.JButton();
        previousPageHUDButton = new javax.swing.JButton();
        nextPageHUDButton = new javax.swing.JButton();
        lastPageHUDButton = new javax.swing.JButton();
        playHUDButton = new javax.swing.JButton();
        zoomInHUDButton = new javax.swing.JButton();
        zoomOutHUDButton = new javax.swing.JButton();
        syncHUDButton = new javax.swing.JButton();
        dragHUDButton = new javax.swing.JButton();
        gotoHUDButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(231, 230, 230));

        toggleHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerDock32x32.png"))); // NOI18N
        toggleHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        toggleHUDButton.setBorderPainted(false);
        toggleHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        toggleHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        toggleHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        toggleHUDButton.setOpaque(true);
        toggleHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        toggleHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleHUDButtonActionPerformed(evt);
            }
        });

        openHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        openHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerOpen32x32.png"))); // NOI18N
        openHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        openHUDButton.setBorderPainted(false);
        openHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        openHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        openHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        openHUDButton.setOpaque(true);
        openHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        openHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openHUDButtonActionPerformed(evt);
            }
        });

        firstPageHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        firstPageHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPageFirst32x32.png"))); // NOI18N
        firstPageHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        firstPageHUDButton.setBorderPainted(false);
        firstPageHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        firstPageHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        firstPageHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        firstPageHUDButton.setOpaque(true);
        firstPageHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        firstPageHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstPageHUDButtonActionPerformed(evt);
            }
        });

        previousPageHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        previousPageHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPagePrev32x32.png"))); // NOI18N
        previousPageHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        previousPageHUDButton.setBorderPainted(false);
        previousPageHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        previousPageHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        previousPageHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        previousPageHUDButton.setOpaque(true);
        previousPageHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        previousPageHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageHUDButtonActionPerformed(evt);
            }
        });

        nextPageHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        nextPageHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPageNext32x32.png"))); // NOI18N
        nextPageHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        nextPageHUDButton.setBorderPainted(false);
        nextPageHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        nextPageHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        nextPageHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        nextPageHUDButton.setOpaque(true);
        nextPageHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        nextPageHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageHUDButtonActionPerformed(evt);
            }
        });

        lastPageHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        lastPageHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPageLast32x32.png"))); // NOI18N
        lastPageHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        lastPageHUDButton.setBorderPainted(false);
        lastPageHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        lastPageHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        lastPageHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        lastPageHUDButton.setOpaque(true);
        lastPageHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        lastPageHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastPageHUDButtonActionPerformed(evt);
            }
        });

        playHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        playHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPlay32x32.png"))); // NOI18N
        playHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        playHUDButton.setBorderPainted(false);
        playHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        playHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        playHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        playHUDButton.setOpaque(true);
        playHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        playHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playHUDButtonActionPerformed(evt);
            }
        });

        zoomInHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        zoomInHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerZoomIn32x32.png"))); // NOI18N
        zoomInHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        zoomInHUDButton.setBorderPainted(false);
        zoomInHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        zoomInHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        zoomInHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        zoomInHUDButton.setOpaque(true);
        zoomInHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        zoomInHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInHUDButtonActionPerformed(evt);
            }
        });

        zoomOutHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        zoomOutHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerZoomOut32x32.png"))); // NOI18N
        zoomOutHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        zoomOutHUDButton.setBorderPainted(false);
        zoomOutHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        zoomOutHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        zoomOutHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        zoomOutHUDButton.setOpaque(true);
        zoomOutHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        zoomOutHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutHUDButtonActionPerformed(evt);
            }
        });

        syncHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        syncHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerSyncedYes32x32.png"))); // NOI18N
        syncHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        syncHUDButton.setBorderPainted(false);
        syncHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        syncHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        syncHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        syncHUDButton.setOpaque(true);
        syncHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        syncHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncHUDButtonActionPerformed(evt);
            }
        });

        dragHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        dragHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerDrag32x32.png"))); // NOI18N
        dragHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        dragHUDButton.setBorderPainted(false);
        dragHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        dragHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        dragHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        dragHUDButton.setOpaque(true);
        dragHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        dragHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dragHUDButtonActionPerformed(evt);
            }
        });

        gotoHUDButton.setBackground(new java.awt.Color(231, 230, 230));
        gotoHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/pdfviewer/client/resources/PDFviewerPageGoto32x32.png"))); // NOI18N
        gotoHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        gotoHUDButton.setBorderPainted(false);
        gotoHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        gotoHUDButton.setMaximumSize(new java.awt.Dimension(38, 38));
        gotoHUDButton.setMinimumSize(new java.awt.Dimension(38, 38));
        gotoHUDButton.setOpaque(true);
        gotoHUDButton.setPreferredSize(new java.awt.Dimension(38, 38));
        gotoHUDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoHUDButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toggleHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(openHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(firstPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(previousPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(nextPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(lastPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(gotoHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(playHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(zoomInHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(zoomOutHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(syncHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(dragHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(toggleHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previousPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(firstPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(openHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nextPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lastPageHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gotoHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dragHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(syncHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(zoomOutHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(zoomInHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(playHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void toggleHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.toggleHUD();
        }
}//GEN-LAST:event_toggleHUDButtonActionPerformed

    private void openHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.openDocument();
        }
    }//GEN-LAST:event_openHUDButtonActionPerformed

    private void firstPageHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstPageHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.firstPage();
        }
    }//GEN-LAST:event_firstPageHUDButtonActionPerformed

    private void previousPageHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousPageHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.previousPage();
        }
    }//GEN-LAST:event_previousPageHUDButtonActionPerformed

    private void nextPageHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.nextPage();
        }
    }//GEN-LAST:event_nextPageHUDButtonActionPerformed

    private void lastPageHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastPageHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.lastPage();
        }
    }//GEN-LAST:event_lastPageHUDButtonActionPerformed

    private void playHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.togglePlay();
        }
    }//GEN-LAST:event_playHUDButtonActionPerformed

    private void zoomInHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.zoomIn();
        }
    }//GEN-LAST:event_zoomInHUDButtonActionPerformed

    private void zoomOutHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.zoomOut();
        }
    }//GEN-LAST:event_zoomOutHUDButtonActionPerformed

    private void syncHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncHUDButtonActionPerformed
        Iterator<PDFViewerToolListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            PDFViewerToolListener listener = iter.next();
            listener.toggleSync();
        }
    }//GEN-LAST:event_syncHUDButtonActionPerformed

    private void dragHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragHUDButtonActionPerformed
        // TODO: implement drag
    }//GEN-LAST:event_dragHUDButtonActionPerformed

    private void gotoHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoHUDButtonActionPerformed
        // TODO: display a goto page dialog
    }//GEN-LAST:event_gotoHUDButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dragHUDButton;
    private javax.swing.JButton firstPageHUDButton;
    private javax.swing.JButton gotoHUDButton;
    private javax.swing.JButton lastPageHUDButton;
    private javax.swing.JButton nextPageHUDButton;
    private javax.swing.JButton openHUDButton;
    private javax.swing.JButton playHUDButton;
    private javax.swing.JButton previousPageHUDButton;
    private javax.swing.JButton syncHUDButton;
    private javax.swing.JButton toggleHUDButton;
    private javax.swing.JButton zoomInHUDButton;
    private javax.swing.JButton zoomOutHUDButton;
    // End of variables declaration//GEN-END:variables
}
