/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.whiteboard.client.cell;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.modules.whiteboard.client.*;
import java.awt.Point;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardSVGCellClientState;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.RequestStatus;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import static org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action.ADD_BACKGROUND_IMAGE;
import static org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action.ADD_ELEMENT;
import static org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action.UPDATE_ELEMENT;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * Client Cell for SVG Whiteboard application.
 *
 * @author nsimpson
 * @author jbarratt
 * @author Abhishek Upadhyay
 */
public class WhiteboardCell extends App2DCell {

    private static final Logger LOGGER =
            Logger.getLogger(WhiteboardCell.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/whiteboard/client/resources/Bundle");
    /**
     * The (singleton) window created by the whiteboard app
     */
    private WhiteboardWindow whiteboardWin;
    /**
     * The cell client state message received from the server cell
     */
    private WhiteboardSVGCellClientState clientState;
    /**
     * The communications component used to communicate with the server
     */
    private WhiteboardComponent commComponent;
    private String myUID;
    private boolean synced = false;
    protected final Object actionLock = new Object();
    /**
     * executor to put messages onto a separate queue
     */
    private Executor msgExecutor = Executors.newSingleThreadExecutor();

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
     * Initialize the whiteboard with parameters from the server.
     *
     * @param clientState the client state to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (WhiteboardSVGCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
            case ACTIVE:
                if (increasing) {
                    // The cell is now visible
                    commComponent = getComponent(WhiteboardComponent.class);
                    WhiteboardApp whiteboardApp = new WhiteboardApp("Whiteboard", clientState.getPixelScale());
                    setApp(whiteboardApp);

                    // this cell displays the app
                    whiteboardApp.addDisplayer(this);

                    float placementHeight = clientState.getPreferredHeight() + 200/*TODO*/;
                    placementHeight *= clientState.getPixelScale().y;
                    setInitialPlacementSize(new Vector2f(0f, placementHeight));

                    // This app only has one WhiteboardWindow, so it is always top-level
                    try {
                        whiteboardWin = new WhiteboardWindow(this, whiteboardApp,
                                clientState.getPreferredWidth(), clientState.getPreferredHeight(),
                                true, clientState.getPixelScale(), commComponent);
                        whiteboardApp.setWindow(whiteboardWin);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Make the app window visible
                    whiteboardWin.setVisibleApp(true);
                    whiteboardWin.setVisibleUser(this, true);

                    // Sync
                    sync();
                }
                break;
            case DISK:
                if (!increasing) {
                    // The cell is no longer visible
                    whiteboardWin.setVisibleApp(false);
                    removeComponent(WhiteboardComponent.class);
                    App2D.invokeLater(new Runnable() {
                        public void run() {
                            whiteboardWin.cleanup();
                            commComponent = null;
                            whiteboardWin = null;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    public String getUID() {
        return myUID;
    }

    /**
     * Process the actions in a whiteboard message
     *
     * @param msg a whiteboard message
     */
    public void processMessage(final WhiteboardCellMessage msg) {
        // issue 1017: since message processing can be quite involved, do
        // it off of the Darkstar message handling thread.  The single
        // threaded executor guarantees that messages will be processed
        // in the order they are received.
        msgExecutor.execute(new Runnable() {
            public void run() {
                processQueuedMessage(msg);
            }
        });
    }

    protected void processQueuedMessage(WhiteboardCellMessage msg) {
        String msgUID = msg.getCellID().toString();

        if (isSynced()) {
            LOGGER.fine("whiteboard: " + msgUID + " received message: " + msg);
            if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED) {
                // this request was denied, create a retry thread
                try {
                    LOGGER.info("whiteboard: scheduling retry of request: " + msg);
                    retryRequest(msg.getAction(), msg.getXMLString(),
                            msg.getURI(), msg.getPosition(), msg.getZoom());
                } catch (Exception e) {
                    LOGGER.warning("whiteboard: failed to create retry request for: " + msg);
                }
            } else {
                // All messages from the server act as a trigger for retrying waiting requests
                switch (msg.getAction()) {
                    case OPEN_DOCUMENT:
                        ((WhiteboardApp) this.getApp()).openDocument(msg.getURI(), false);
                        break;
                    case NEW_DOCUMENT:
                        if(!getClientID().equals(msg.getClientID())) {
                            ((WhiteboardApp) this.getApp()).newDocument(false);
                        }
                        break;
                    case ADD_ELEMENT:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toAdd = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).addElement(toAdd, false);
                        }
                        break;
                    case REMOVE_ELEMENT:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toRemove = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).removeElement(toRemove, false);
                        }
                        break;
                    case UPDATE_ELEMENT:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toUpdate = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).updateElement(toUpdate, false);
                        }
                        break;
                    case ADD_TEXT_ELEMENT:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toAdd1 = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            Element existEle = whiteboardWin.getDocument().getElementById(toAdd1.getAttribute("id"));
                            if(existEle!=null) {
                                ((WhiteboardApp) this.getApp()).updateElement(toAdd1, false);
                            } else {
                                ((WhiteboardApp) this.getApp()).addElement(toAdd1, false);
                            }
                        }
                        break;    
                    case UPDATE_TEXT_ELEMENT:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toUpdate1 = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).updateElement(toUpdate1, false);
                        }
                        break;
                    case SET_VIEW_POSITION:
                        ((WhiteboardApp) this.getApp()).setViewPosition(msg.getPosition());
                        break;
                    case GET_STATE:
                        break;
                    case SET_STATE:
                        if (isSynced()) {
                            String docURI = msg.getURI();
                            if (docURI != null) {
                                // load an SVG document
                                ((WhiteboardApp) this.getApp()).openDocument(docURI, false);
                            } else {
                                // load state from SVG XML string
                                SVGDocument svgDocument = (SVGDocument) WhiteboardUtils.xmlStringToDocument(msg.getXMLString());
                                //remove extra bImage elemet
                                List<Element> bImgEles = new ArrayList<Element>();
                                NodeList nodes = svgDocument.getDocumentElement().getChildNodes();
                                if (nodes != null) {
                                    for (int i = 0; i < nodes.getLength(); i++) {
                                        Node node = nodes.item(i);
                                        if (node instanceof Element && ((Element) node).getAttribute("id").equals("bImage")) {
                                            bImgEles.add((Element) node);
                                        }
                                    }
                                }
                                if (bImgEles.size() > 1) {
                                    for (int i = 1; i < bImgEles.size(); i++) {
                                        svgDocument.getDocumentElement().removeChild(bImgEles.get(i));
                                    }
                                }
                                
                                Element bEle = svgDocument.getElementById("bImage");
                                if (bEle != null) {
                                    whiteboardWin.setSizeOfWindow(Integer.parseInt(bEle.getAttribute("width")), Integer.parseInt(bEle.getAttribute("height")));
                                }
                                ((WhiteboardApp) this.getApp()).setDocument(svgDocument, false);
                            }
                                
                            //setViewPosition(msg.getPosition());
                            //setZoom(msg.getZoom(), false);
                            LOGGER.info("whiteboard: synced");
                            whiteboardWin.showHUDMessage(BUNDLE.getString("Synced"), 3000);
                        }
                        break;
                    case SET_ZOOM:
                        ((WhiteboardApp) this.getApp()).setZoom(msg.getZoom(), false);
                        break;
                    case ADD_BACKGROUND_IMAGE:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toAddBI = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).addBackgroundImage(toAddBI, false);
                        }
                        break;
                    case UPDATE_BACKGROUND_IMAGE:
                        if(!getClientID().equals(msg.getClientID())) {
                            Element toUpdateBI = WhiteboardUtils.xmlStringToElement(msg.getXMLString());
                            ((WhiteboardApp) this.getApp()).updateBackgroundImage(toUpdateBI, false);
                        }
                        break;
                    default:
                        LOGGER.warning("whiteboard: unhandled message type: " + msg.getAction());
                        break;
                }
                // retry queued requests
                synchronized (actionLock) {
                    try {
                        LOGGER.fine("whiteboard: waking retry threads");
                        actionLock.notify();
                    } catch (Exception e) {
                        LOGGER.warning("whiteboard: exception notifying retry threads: " + e);
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
     * A resync is necessary when the cell transitions from INACTIVE to ACTIVE
     * cell state, where the cell may have missed state synchronization messages
     * while in the INACTIVE state.
     *
     * Resynchronization is only performed if the cell is currently synced. To
     * sync an unsynced cell, call sync(true) instead.
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
            LOGGER.info("whiteboard: unsynced");
            whiteboardWin.showHUDMessage(BUNDLE.getString("Unsynced"), 3000);
            //whiteboardWindow.updateMenu();
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            LOGGER.info("whiteboard: requesting sync with shared state");
            whiteboardWin.showHUDMessage(BUNDLE.getString("Syncing..."), 3000);
            //whiteboardWindow.updateMenu();
            sendRequest(Action.GET_STATE, null, null, null, null);
        }
    }

    protected void sendRequest(Action action, String xmlString, String docURI,
            Point position, Float zoom) {

        WhiteboardCellMessage msg = new WhiteboardCellMessage(getClientID(), getCellID(),
                getUID(), action, xmlString, docURI, position, zoom);
        // send request to server
        LOGGER.fine("whiteboard: sending request: " + msg);
        if (commComponent == null) {
            commComponent = getComponent(WhiteboardComponent.class);
        }
        commComponent.sendMessage(msg);
    }

    /**
     * Retries a whiteboard action request
     *
     * @param action the action to retry
     * @param document the search parameters
     * @param position the image scroll position
     */
    protected void retryRequest(Action action, String xmlString, String docURI, Point position, Float zoom) {
        LOGGER.fine("whiteboard: creating retry thread for: " + action + ", " + xmlString + ", " + position);
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
                    LOGGER.fine("whiteboard: waiting for retry window");
                    actionLock.wait();
                } catch (Exception e) {
                    LOGGER.fine("whiteboard: exception waiting for retry: " + e);
                }
            }
            // retry this request
            LOGGER.info("whiteboard: now retrying: " + action + ", " + xmlString + ", " + position + ", " + zoom);
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
