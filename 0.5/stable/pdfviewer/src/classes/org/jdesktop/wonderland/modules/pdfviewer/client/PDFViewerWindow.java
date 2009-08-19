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
package org.jdesktop.wonderland.modules.pdfviewer.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDDialog;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.pdfviewer.client.cell.PDFViewerCell;

/**
 *
 * The window for the Swing test.
 *
 * @author deronj
 */
@ExperimentalAPI
public class PDFViewerWindow extends WindowSwing {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(PDFViewerWindow.class.getName());
    /** The cell in which this window is displayed. */
    private PDFViewerCell cell;
    private PDFViewerPanel pdfPanel;
    private PDFViewerToolManager toolManager;
    private PDFViewerControlPanel controls;
    private HUDComponent controlComponent;
    private HUDDialog openDialogComponent;
    private SharedStateComponent ssc;
    private boolean synced = true;
    private DisplayMode displayMode;

    /**
     * Create a new instance of a PDFViewerWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public PDFViewerWindow(PDFViewerCell cell, App2D app, int width, int height, boolean topLevel,
            Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        this.cell = cell;
        setTitle("PDF Viewer");

        pdfPanel = new PDFViewerPanel(this);
        // Parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(pdfPanel);

        setComponent(pdfPanel);

        setDisplayMode(DisplayMode.HUD);
        showControls(false);
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        pdfPanel.setSSC(ssc);
    }

    public void openDocument() {
        if (openDialogComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            openDialogComponent = mainHUD.createDialog("Open PDF:");
            openDialogComponent.setPreferredLocation(Layout.CENTER);
            mainHUD.addComponent(openDialogComponent);
            openDialogComponent.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("text")) {
                        String url = openDialogComponent.getValueText();
                        if ((url != null) && (url.length() > 0)) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    openDialogComponent.setVisible(false);
                                    openDocument(openDialogComponent.getValueText());
                                }
                            });
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                openDialogComponent.setVisible(false);
                            }
                        });
                    }
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                openDialogComponent.setVisible(true);
            }
        });
    }

    public void openDocument(String documentURI) {
        pdfPanel.openDocument(documentURI);
    }

    public void firstPage() {
        pdfPanel.firstPage();
    }

    public void previousPage() {
        pdfPanel.previousPage();
    }

    public void nextPage() {
        pdfPanel.nextPage();
    }

    public void lastPage() {
        pdfPanel.lastPage();
    }

    public void gotoPage(int page, boolean notify) {
        pdfPanel.gotoPage(page, notify);
    }

    public void gotoPage(int page) {
        pdfPanel.gotoPage(page);
    }

    public void play() {
        pdfPanel.play();
    }

    public void pause() {
        pdfPanel.pause();
    }

    public void zoomIn() {
        pdfPanel.zoomIn();
    }

    public void zoomOut() {
        pdfPanel.zoomOut();
    }

    public void sync() {
        pdfPanel.sync();
    }

    public void unsync() {
        pdfPanel.unsync();
    }

    public boolean isSynced() {
        return synced;
    }

    /**
     * Resynchronize the state of the cell.
     *
     * A resync is necessary when the cell transitions from INACTIVE to
     * ACTIVE cell state, where the cell may have missed state synchronization
     * messages while in the INACTIVE state.
     *
     * Resynchronization is only performed if the cell is currently synced.
     * To sync an unsynced cell, call sync(true) instead.
     */
    public void resync() {
        if (isSynced()) {
            synced = false;
            sync(true);
        }
    }

    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("whiteboard: unsynced");
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("whiteboard: synced");
        }
    }

    /**
     * Sets the display mode for the control panel to in-world or on-HUD
     * @param mode the control panel display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Gets the control panel display mode
     * @return the display mode of the control panel: in-world or on HUD
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Shows or hides the HUD controls.
     * The controls are shown in-world or on-HUD depending on the selected
     * DisplayMode.
     *
     * @param visible true to show the controls, hide to hide them
     */
    public void showControls(final boolean visible) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                if (controlComponent == null) {
                    // create control panel
                    controls = new PDFViewerControlPanel(PDFViewerWindow.this);

                    // add event listeners
                    toolManager = new PDFViewerToolManager(PDFViewerWindow.this);
                    controls.addCellMenuListener(toolManager);

                    // create HUD control panel
                    controlComponent = mainHUD.createComponent(controls, cell);
                    controlComponent.setPreferredLocation(Layout.SOUTH);

                    // add HUD control panel to HUD
                    mainHUD.addComponent(controlComponent);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        // change visibility of controls
                        if (getDisplayMode() == DisplayMode.HUD) {
                            if (controlComponent.isWorldVisible()) {
                                controlComponent.setWorldVisible(false);
                            }
                            controlComponent.setVisible(visible);
                        } else {
                            controlComponent.setWorldLocation(new Vector3f(0.0f, 2.0f, 0.1f));
                            if (controlComponent.isVisible()) {
                                controlComponent.setVisible(false);
                            }
                            controlComponent.setWorldVisible(visible); // show world view
                        }
                    }
                });
            }
        });
    }

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }
}
