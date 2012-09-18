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
package org.jdesktop.wonderland.modules.mediaboard.client.cell;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.modules.mediaboard.client.*;
import java.awt.Point;
import java.math.BigInteger;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardSVGCellClientState;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage.RequestStatus;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedDouble;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * Client Cell for SVG Whiteboard application.
 *
 * @author nsimpson
 * @author jbarratt
 */
public class WhiteboardCell extends App2DCell {

    private static final Logger LOGGER =
            Logger.getLogger(WhiteboardCell.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/mediaboard/client/resources/Bundle");
    /** The (singleton) window created by the whiteboard app */
    private WhiteboardWindow whiteboardWin;
    /** The cell client state message received from the server cell */
    private WhiteboardSVGCellClientState clientState;
    /** The communications component used to communicate with the server */
    private WhiteboardComponent commComponent;
    private String myUID;
    private boolean synced = false;
    protected final Object actionLock = new Object();

    /** executor to put messages onto a separate queue */
    private Executor msgExecutor = Executors.newSingleThreadExecutor();

    @UsesCellComponent
    private SharedStateComponent sharedStateComponent;
    private SharedMapCli stateMap;
    private SharedMapListener listener;
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
                    WhiteboardApp whiteboardApp = new WhiteboardApp("Mediaboard", clientState.getPixelScale());
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

                    stateMap = sharedStateComponent.get("state");
                    handleStateMap(stateMap);
                    listener = new SharedMapListener();
                    stateMap.addSharedMapListener(listener);

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
                    if(stateMap != null) {
                        stateMap.removeSharedMapListener(listener);
                        stateMap = null;
                        listener = null;
                    }
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
                            String docURI = msg.getURI();
                            
//                            if (docURI != null) {
//                                // load an SVG document
//                                ((WhiteboardApp) this.getApp()).openDocument(docURI, false);
////                                WhiteboardUtils.documentToXMLString(null)
//                                Document d = WhiteboardClientUtils.openDocument(docURI);
//                                
//                                
//                                System.out.println("\nFrom URI: \n"+removeSVGTags(WhiteboardUtils.documentToXMLString(d))+" \n");
//                                
//                            } else {
//                                // load state from SVG XML string
//                                System.out.println("\nFrom String: "+removeSVGTags(msg.getXMLString())+" \n");
//                                SVGDocument svgDocument = (SVGDocument) WhiteboardUtils.xmlStringToDocument(msg.getXMLString());
//                                ((WhiteboardApp) this.getApp()).setDocument(svgDocument, false);
//                            }
                            
                            
                            //if docURI exists...
                            if(docURI != null) {
                                
                                //if we don't have any svg string, just open the URI.
                                if(msg.getXMLString() == null || msg.getXMLString() == "") {
                                    
                                    
                                    ((WhiteboardApp) this.getApp()).openDocument(docURI, synced);
                                    
                                } else { //otherwise...
                                    String tagsRemoved = "";                 
                                    //...open it so we can get the xml form...
                                    Document d = WhiteboardUtils.openDocument(docURI);
                                    
                                    String textDocument = WhiteboardUtils.documentToXMLString(d);
                                    //...get the xml form and remove the <svg></svg> tags
                                    tagsRemoved = removeSVGTags(msg.getXMLString());
                                                                        
                                    //append the altered xml form to the current svg xml...
                                    String combinedMarkup = appendStringToSVGDocument(textDocument, tagsRemoved);
                                    System.out.println("From URI AND String:\n"+combinedMarkup+"\n\n");
                                    SVGDocument svgDocument = (SVGDocument)WhiteboardUtils.xmlStringToDocument(combinedMarkup);
                                    ((WhiteboardApp) this.getApp()).setDocument(svgDocument, false);
                                }
                                
                                
                            } else {//if the docURI doesn't exist, open from XML
                                System.out.println("FROM STRING: "+msg.getXMLString());
                                SVGDocument svgDocument = (SVGDocument) WhiteboardUtils.xmlStringToDocument(msg.getXMLString());
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

    /**
     * Given "<svg>Hello World!</svg>", should return "Hello World!"
     * @param xmlDocument string wrapped in <svg></svg>
     * @return unwrapped string
     */
    private String removeSVGTags(String xmlDocument) {
        
        //we first want to get the first index of '>' and replace everything
        //from 0 to that with ""
        int first = xmlDocument.indexOf('>');
                 
        //the second pass we want to replace </svg> with ""
       int last = xmlDocument.lastIndexOf("</svg>");
       if(last < 0) {
           last = xmlDocument.length();
       }
        return xmlDocument.substring(first+1, last);
    }
    
    /**
     * Given a string with "<svg></svg>", and markup: "hello world!", should 
     * return "<svg>hello world!</svg>
     * 
     * @param document svg document xml in string form
     * @param markup svg markup fragment
     * @return expanded svg document 
     */
    private String appendStringToSVGDocument(String document, String markup) {
        int indexToInsertAt = document.lastIndexOf("</svg>");
        
        StringBuilder sb = new StringBuilder(document);
        sb.insert(indexToInsertAt, markup);
        
        
        return sb.toString();
        
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

    public SharedMapCli getStateMap() {
        return stateMap;
    }

    private void handleStateMap(SharedMapCli map) {
        if(!map.containsKey("margin-width"))
            return;
        
        logger.warning("HANDLING MEDIABOARD STATE!");
        double d = ((SharedDouble)map.get("margin-width")).getValue();
        int nextMargin = new Double(d).intValue();
        whiteboardWin.getWhiteboardDocument().setTextMargin(new Integer(nextMargin).doubleValue());
    }
    
    
    class SharedMapListener implements SharedMapListenerCli {
        public void propertyChanged(SharedMapEventCli event) {
            //ensure we're operating on the state map
            if(!event.getMap().getName().equals("state"))
                return;

            //next, make sure we're operating on the correct property.
            if(!event.getPropertyName().equals("margin-width"))
                return;


            double d = ((SharedDouble)event.getNewValue()).getValue();
            int nextMargin = (new Double(d).intValue());
            whiteboardWin.getWhiteboardDocument().setTextMargin(new Integer(nextMargin).doubleValue());
            
        }
    }

}
