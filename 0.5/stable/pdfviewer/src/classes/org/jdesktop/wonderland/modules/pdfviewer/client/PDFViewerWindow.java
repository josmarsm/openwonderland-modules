/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
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
import org.jdesktop.wonderland.modules.pdfviewer.common.PDFViewerConstants;
import org.jdesktop.wonderland.modules.pdfviewer.common.PDFViewerState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * The window for the PDF viewer.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class PDFViewerWindow extends WindowSwing {

    /** The logger used by this class. */
    private static final Logger LOGGER = Logger.getLogger(
            PDFViewerWindow.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/pdfviewer/client/resources/Bundle");
    /** The cell in which this window is displayed. */
    private PDFViewerCell cell;
    private PDFViewerPanel pdfPanel;
    private PDFViewerToolManager toolManager;
    private PDFViewerControlPanel controls;
    private HUDComponent controlComponent;
    private HUDDialog openDialogComponent;
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
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
        super(app, Type.PRIMARY, width, height, topLevel, pixelScale);
        this.cell = cell;
        setTitle(BUNDLE.getString("PDF_Viewer"));

        pdfPanel = new PDFViewerPanel(this);
        // Parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(pdfPanel);

        setComponent(pdfPanel);

        setDisplayMode(DisplayMode.HUD);
        showControls(false);
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        statusMap = ssc.get(PDFViewerConstants.STATUS_MAP);
    }

    public void openDocument() {
        if (openDialogComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            openDialogComponent = mainHUD.createDialog(
                    BUNDLE.getString("Open_PDF"));
            openDialogComponent.setPreferredLocation(Layout.CENTER);
            openDialogComponent.setType(HUDDialog.MESSAGE_TYPE.QUERY);
            mainHUD.addComponent(openDialogComponent);
            openDialogComponent.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("ok")) {
                        String url = openDialogComponent.getValue();
                        if ((url != null) && (url.length() > 0)) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    openDialogComponent.setVisible(false);
                                    if (isSynced()) {
                                        // update the shared state
                                        statusMap.put(PDFViewerConstants.DOCUMENT_URI, SharedString.valueOf(openDialogComponent.getValue()));
                                    } else {
                                        // just open the document privately
                                        pdfPanel.openDocument(openDialogComponent.getValue());
                                    }
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
        openDocument(documentURI, 1);
    }

    public void openDocument(String documentURI, int pageNumber) {
        if (isSynced()) {
            try {
                // resolve the server-independent URI into a server-specific
                // URI for loading. Be careful not to share this URI across
                // servers
                URL documentURL = AssetUtils.getAssetURL(documentURI, cell);

                // convert to a string
                documentURI = documentURL.toExternalForm();

                // issue #807 -- make sure to get rid of spaces
                documentURI = documentURI.replace(" ", "%20");

                // open the resolved URI
                pdfPanel.openDocument(documentURI, pageNumber);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, "Error opening " + documentURI, ex);
            }
        }
    }

    public String getDocumentURI() {
        return pdfPanel.getDocumentURI();
    }

    /**
     * Display the first page in the document
     */
    public void firstPage() {
        gotoPage(1);
    }

    /**
     * Display the last page in the document
     */
    public void lastPage() {
        gotoPage(pdfPanel.getPageCount());
    }

    /**
     * Display the next page after the currently selected page
     */
    public void nextPage() {
        gotoPage(pdfPanel.getNextPage());
    }

    /**
     * Display the previous page to the currently selected page
     */
    public void previousPage() {
        gotoPage(pdfPanel.getPreviousPage());
    }

    /**
     * Go to the specified page
     * @param page the number of the page to go to
     */
    public void gotoPage(int page) {
        if (isSynced()) {
            // notify all clients
            statusMap.put(PDFViewerConstants.PAGE_NUMBER, SharedInteger.valueOf(page));
        } else {
            // show the page privately in this client
            pdfPanel.showPage(page);
        }
    }

    /**
     * Display a page
     * @param page the page to display
     */
    public void showPage(int page) {
        if (isSynced()) {
            pdfPanel.showPage(page);
        }
    }

    public void togglePlay() {
        if (isSynced()) {
            statusMap.put(PDFViewerConstants.SLIDE_SHOW_MODE, SharedBoolean.valueOf(!isPlaying()));
        }
    }

    public void play() {
        if (isSynced()) {
            LOGGER.info("play");
            updateControls();
        }
    }

    public void pause() {
        if (isSynced()) {
            LOGGER.info("pause");
            updateControls();
        }
    }

    public boolean isPlaying() {
        boolean playing = false;

        if (statusMap != null) {
            playing = ((SharedBoolean) statusMap.get(PDFViewerConstants.SLIDE_SHOW_MODE)).getValue();
        }

        return playing;
    }

    public void zoomIn() {
        pdfPanel.zoomIn();
    }

    public void zoomOut() {
        pdfPanel.zoomOut();
    }

    public void toggleSync() {
        sync(!synced);
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
            // unsyncing
            synced = false;
            LOGGER.info("unsynced");
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            // syncing with shared state
            int page = ((SharedInteger) statusMap.get(PDFViewerConstants.PAGE_NUMBER)).getValue();

            // open the document
            String currentDocument = getDocumentURI();
            String newDocument = ((SharedString) statusMap.get(PDFViewerConstants.DOCUMENT_URI)).getValue();
            if (newDocument != null) {
                if ((currentDocument == null) || !newDocument.equals(currentDocument)) {
                    // opening a new document
                    openDocument(newDocument, page);
                } else {
                    // re-opening the current document, just show the correct
                    // page
                    showPage(page);
                }
            }

            boolean playing = ((SharedBoolean) statusMap.get(PDFViewerConstants.SLIDE_SHOW_MODE)).getValue();
            if (playing) {
                play();
            } else {
                pause();
            }

            LOGGER.info("synced");
        }
        updateControls();
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
                LOGGER.info("show controls: " + visible);
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
                            controlComponent.setWorldLocation(new Vector3f(0.0f, -3.2f, 0.1f));
                            if (controlComponent.isVisible()) {
                                controlComponent.setVisible(false);
                            }
                            controlComponent.setWorldVisible(visible); // show world view
                        }

                        updateControls();
                    }
                });
            }
        });
    }

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }

    protected void updateControls() {
        if (controls != null) {
            controls.setSynced(isSynced());
            controls.setOnHUD(!toolManager.isOnHUD());
            controls.setMode(isPlaying() ? PDFViewerState.PLAYING : PDFViewerState.PAUSED);
        }
    }
}
