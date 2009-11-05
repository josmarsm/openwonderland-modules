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
package org.jdesktop.wonderland.modules.pdfviewer.client.cell;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.pdfviewer.client.PDFViewerApp;
import org.jdesktop.wonderland.modules.pdfviewer.client.PDFViewerWindow;
import org.jdesktop.wonderland.modules.pdfviewer.common.PDFViewerConstants;
import org.jdesktop.wonderland.modules.pdfviewer.common.cell.PDFViewerCellClientState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;

/**
 * Client cell for the PDF viewer.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class PDFViewerCell extends App2DCell implements SharedMapListenerCli {

    private static final Logger logger = Logger.getLogger(PDFViewerCell.class.getName());
    // The (singleton) window created by the PDF viewer app
    private PDFViewerWindow pdfViewerWindow;
    // the PDF viwer application
    private PDFViewerApp pdfViewerApp;
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
    private PDFViewerCellClientState clientState;

    /**
     * Create an instance of PDFViewerCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public PDFViewerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the PDF viewer with parameters from the server.
     *
     * @param clientState the client state to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (PDFViewerCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
            case ACTIVE:
                // the cell is now visible
                if (increasing) {
                    if (this.getApp() == null) {
                        pdfViewerApp = new PDFViewerApp("pdf", new Vector2f(0.01f, 0.01f)/*clientState.getPixelScale()*/);
                        setApp(pdfViewerApp);
                    }
                    // tell the app to be displayed in this cell.
                    pdfViewerApp.addDisplayer(this);

                    // set initial position above ground
                    float placementHeight = clientState.getPreferredHeight() + 200;
                    placementHeight *= clientState.getPixelScale().y;
                    setInitialPlacementSize(new Vector2f(0f, placementHeight));

                    // this app has only one window, so it is always top-level
                    try {
                        pdfViewerWindow = new PDFViewerWindow(this, pdfViewerApp,
                                clientState.getPreferredWidth(), clientState.getPreferredHeight(),
                                true, clientState.getPixelScale());
                        pdfViewerWindow.setDecorated(clientState.getDecorated());

                        pdfViewerApp.setWindow(pdfViewerWindow);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    // load the PDF viewer's status map
                    pdfViewerWindow.setSSC(ssc);
                    statusMap = ssc.get(PDFViewerConstants.STATUS_MAP);
                    statusMap.addSharedMapListener(this);

                    // get the currently loaded document
                    SharedString documentURI = statusMap.get(PDFViewerConstants.DOCUMENT_URI,
                            SharedString.class);
                    handleLoadDocument(null, null, documentURI);
//
//                    // get the scroll position
//                    SharedInteger scroll = statusMap.get(PDFViewerConstants.PAGE_POSITION,
//                            SharedInteger.class);
//                    handleScrolled(null, null, scroll);

                    // get the slide show mode
                    SharedBoolean slideshow = statusMap.get(PDFViewerConstants.SLIDE_SHOW_MODE,
                            SharedBoolean.class);
                    handleSlideShowMode(null, null, slideshow);


                    // both the app and the user want this window to be visible
                    pdfViewerWindow.setVisibleApp(true);
                    pdfViewerWindow.setVisibleUser(this, true);
                }
                break;
            case DISK:
                if (!increasing) {
                    // The cell is no longer visible
                    pdfViewerWindow.setVisibleApp(false);
                    App2D.invokeLater(new Runnable() {

                        public void run() {
                            pdfViewerWindow.cleanup();
                            pdfViewerWindow = null;
                        }
                    });
                }
                break;
        }
    }

    public void propertyChanged(SharedMapEventCli event) {
        SharedMapCli map = event.getMap();
        if (map.getName().equals(PDFViewerConstants.STATUS_MAP)) {
            // there's only one map, a map containing the state of the viewer,
            // its key determines what changed:
            //
            // DOCUMENT_URI: a new document has been loaded into this viewer
            // PAGE_NUMBER: the page has changed
            // PAGE_POSITION: the page has been scrolled
            // SLIDE_SHOW_MODE: the viewer has entered or exited slide show mode
            //
            // newData specifies the new value of the key
            // note that there's only one property change processed at a time

            handleStatusChange(event.getPropertyName(), event.getOldValue(),
                    event.getNewValue());
        } else {
            logger.warning("unrecognized shared map: " + map.getName());
        }
    }

    private void handleStatusChange(String key, SharedData oldData, SharedData newData) {
        if (key.equals(PDFViewerConstants.DOCUMENT_URI)) {
            // a new media file
            handleLoadDocument(key, oldData, newData);
        } else if (key.equals(PDFViewerConstants.PAGE_NUMBER)) {
            // page has changed
            handlePageChange(key, oldData, newData);
        } else if (key.equals(PDFViewerConstants.PAGE_POSITION)) {
            // page has scrolled
            handleScrolled(key, oldData, newData);
        } else if (key.equals(PDFViewerConstants.SLIDE_SHOW_MODE)) {
            // entered slide show mode
            handleSlideShowMode(key, oldData, newData);
        } else {
            logger.warning("unhandled status change event: " + key);
        }
    }

    private void handleLoadDocument(String media, SharedData oldData, SharedData newData) {
        if (newData != null) {
            // get the page number
            int page = ((SharedInteger) statusMap.get(PDFViewerConstants.PAGE_NUMBER)).getValue();

            String documentURI = ((SharedString) newData).getValue();
            pdfViewerWindow.openDocument(documentURI, page);
        }
    }

    private void handlePageChange(String media, SharedData oldData, SharedData newData) {
        if (newData != null) {
            Integer page = ((SharedInteger) newData).getValue();
            pdfViewerWindow.showPage(page);
        }
    }

    private void handleScrolled(String media, SharedData oldData, SharedData newData) {
        if (newData != null) {
            // TODO: handle scrolling in zoom mode?
        }
    }

    private void handleSlideShowMode(String media, SharedData oldData, SharedData newData) {
        if (newData != null) {
            Boolean playing = ((SharedBoolean) newData).getValue();
            if (playing) {
                pdfViewerWindow.play();
            } else {
                pdfViewerWindow.pause();
            }
        }
    }
}
