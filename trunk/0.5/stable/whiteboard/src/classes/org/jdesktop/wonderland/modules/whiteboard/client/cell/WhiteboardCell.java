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
package org.jdesktop.wonderland.modules.whiteboard.client.cell;

import org.jdesktop.wonderland.modules.whiteboard.client.*;
import java.awt.Point;
import java.math.BigInteger;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardSVGCellClientState;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.RequestStatus;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * Client Cell for SVG Whiteboard application.
 *
 * @author nsimpson
 * @author jbarratt
 */
public class WhiteboardCell extends App2DCell {

    private static final Logger logger =
            Logger.getLogger(WhiteboardCell.class.getName());
    /** The (singleton) window created by the whiteboard app */
    private WhiteboardWindow whiteboardWin;
    /** The cell client state message received from the server cell */
    private WhiteboardSVGCellClientState clientState;
    /** The communications component used to communicate with the server */
    private WhiteboardComponent commComponent;
    private String myUID;
    private boolean synced = false;
    protected final Object actionLock = new Object();

    /**
     * Create an instance of WhiteboardCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public WhiteboardCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        myUID = cellID.toString();
    }

    /**
     * {@inheritDoc}
     */
    public AppType getAppType() {
        return new WhiteboardAppType();
    }

    /**
     * Initialize the whiteboard with parameters from the server.
     *
     * @param clientState the client state to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (WhiteboardSVGCellClientState) state;
//        // TODO:
//        // Handle Decorated, Showing application properties
//        ((WhiteboardApp) app).setPreferredWidth(clientState.getPreferredWidth());
//        ((WhiteboardApp) app).setPreferredHeight(clientState.getPreferredHeight());
//        ((WhiteboardApp) app).setPixelScale(new Point2f(clientState.getPixelScale(), clientState.getPixelScale()));
//        ((WhiteboardApp) app).setSize(clientState.getPreferredWidth(), clientState.getPreferredHeight());
//        ((WhiteboardApp) app).setDecorated(clientState.getDecorated());
//        ((WhiteboardApp) app).setShowing(true);
//
//        // Associate the app with this cell (must be done before making it visible)
//        app.setCell(this);
//
//        // Make the app window visible
//        ((WhiteboardApp) app).setVisible(true);

    // Note: we used to force a sync here. But in the new implementation we will
    // perform the sync when the cell status becomes BOUNDS.
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    public boolean setStatus(CellStatus status) {
        boolean ret = super.setStatus(status);

        switch (status) {
            case ACTIVE:
                // The cell is now visible
                commComponent = getComponent(WhiteboardComponent.class);
                WhiteboardApp whiteboardApp = new WhiteboardApp(getAppType(), clientState.getPreferredWidth(),
                        clientState.getPreferredHeight(), clientState.getPixelScale(),
                        commComponent);
                setApp(whiteboardApp);

                // Associate the app with this cell (must be done before making it visible)
                whiteboardApp.setDisplayer(this);

                // This app has only one whiteboardWindow, so it is always top-level
                try {
                    whiteboardWin = new WhiteboardWindow(whiteboardApp,
                            clientState.getPreferredWidth(), clientState.getPreferredHeight(),
                            true, clientState.getPixelScale(), commComponent);
                    whiteboardApp.setWindow(whiteboardWin);
                } catch (InstantiationException ex) {
                    throw new RuntimeException(ex);
                }
                
                // Make the app window visible
                whiteboardWin.setVisible(true);

                // Sync
                sync();
                break;
            case DISK:
                // The cell is no longer visible
                ((WhiteboardApp) app).setVisible(false);
                removeComponent(WhiteboardComponent.class);
                commComponent = null;
                whiteboardWin = null;
                break;
        }

        return ret;
    }

    public String getUID() {
        return myUID;
    }

    /**
     * Process the actions in a whiteboard message
     *
     * @param msg a whiteboard message
     */
    public void processMessage(WhiteboardCellMessage msg) {
        String msgUID = msg.getCellID().toString();
        WhiteboardCellMessage fscm = null;

        if (isSynced()) {
            logger.fine("whiteboard: " + msgUID + " received message: " + msg);
            if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED) {
                // this request was denied, create a retry thread
                try {
                    logger.info("whiteboard: scheduling retry of request: " + msg);
                    retryRequest(msg.getAction(), msg.getXMLString(),
                            msg.getURI(), msg.getPosition(), msg.getZoom());
                } catch (Exception e) {
                    logger.warning("whiteboard: failed to create retry request for: " + msg);
                }
            } else {
                // All messages from the server act as a trigger for retrying waiting requests
                switch (msg.getAction()) {
                    case OPEN_DOCUMENT:
                        ((WhiteboardApp) this.getApp()).openDocument(msg.getURI(), false);
                        break;
                    case NEW_DOCUMENT:
                        ((WhiteboardApp) this.getApp()).newDocument(false);
                        break;
                    case ADD_ELEMENT:
                        Element toAdd = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                        ((WhiteboardApp) this.getApp()).addElement(toAdd, false);
                        break;
                    case REMOVE_ELEMENT:
                        Element toRemove = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                        ((WhiteboardApp) this.getApp()).removeElement(toRemove, false);
                        break;
                    case UPDATE_ELEMENT:
                        Element toUpdate = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                        ((WhiteboardApp) this.getApp()).updateElement(toUpdate, false);
                        break;
                    case SET_VIEW_POSITION:
                        ((WhiteboardApp) this.getApp()).setViewPosition(msg.getPosition());
                        break;
                    case GET_STATE:
                        break;
                    case SET_STATE:
                        if (isSynced()) {
                            SVGDocument svgDocument = (SVGDocument) WhiteboardUtils.xmlStringToDocument(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).setDocument(svgDocument, false);
                            //setViewPosition(msg.getPosition());
                            //setZoom(msg.getZoom(), false);
                            logger.info("whiteboard: synced");
                        // TODO: who shows HUD messages?
                        // whiteboardWindow.showHUDMessage("synced", 3000);
                        }
                        break;
                    case SET_ZOOM:
                        ((WhiteboardApp) this.getApp()).setZoom(msg.getZoom(), false);
                        break;
                    default:
                        logger.warning("whiteboard: unhandled message type: " + msg.getAction());
                        break;
                }
                // retry queued requests
                synchronized (actionLock) {
                    try {
                        logger.fine("whiteboard: waking retry threads");
                        actionLock.notify();
                    } catch (Exception e) {
                        logger.warning("whiteboard: exception notifying retry threads: " + e);
                    }
                }
            }
        }
    }

    public void sync() {
        sync(!isSynced());
    }

    public void unsync() {
        sync(!isSynced());
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
        //whiteboardWindow.showHUDMessage("unsynced", 3000);
        //whiteboardWindow.updateMenu();
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("whiteboard: requesting sync with shared state");
            //whiteboardWindow.showHUDMessage("syncing...", 3000);
            //whiteboardWindow.updateMenu();
            sendRequest(Action.GET_STATE, null, null, null, null);
        }
    }

    protected void sendRequest(Action action, String xmlString, String docURI,
            Point position, Float zoom) {

        WhiteboardCellMessage msg = new WhiteboardCellMessage(getClientID(), getCellID(),
                getUID(), action, xmlString, docURI, position, zoom);
        // send request to server
        logger.fine("whiteboard: sending request: " + msg);
        if (commComponent == null) {
            commComponent = getComponent(WhiteboardComponent.class);
        }
        commComponent.sendMessage(msg);
    }

    /**
     * Retries a whiteboard action request
     * @param action the action to retry
     * @param document the search parameters
     * @param position the image scroll position
     */
    protected void retryRequest(Action action, String xmlString, String docURI, Point position, Float zoom) {
        logger.fine("whiteboard: creating retry thread for: " + action + ", " + xmlString + ", " + position);
        new ActionScheduler(action, xmlString, docURI, position, zoom).start();
    }

    protected class ActionScheduler extends Thread {

        private Action action;
        private String xmlString;
        private String docURI;
        private Point position;
        private Float zoom;

        public ActionScheduler(Action action, String xmlString, String docURI, Point position, Float zoom) {
            this.action = action;
            this.xmlString = xmlString;
            this.docURI = docURI;
            this.position = position;
            this.zoom = zoom;
        }

        @Override
        public void run() {
            // wait for a retry window
            synchronized (actionLock) {
                try {
                    logger.fine("whiteboard: waiting for retry window");
                    actionLock.wait();
                } catch (Exception e) {
                    logger.fine("whiteboard: exception waiting for retry: " + e);
                }
            }
            // retry this request
            logger.info("whiteboard: now retrying: " + action + ", " + xmlString + ", " + position + ", " + zoom);
            sendRequest(action, xmlString, docURI, position, zoom);
        }
    }

    /**
     * Returns the client ID of this cell's session.
     */
    public BigInteger getClientID() {
        return getCellCache().getSession().getID();
    }
}
