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
package org.jdesktop.wonderland.modules.mediaboard.server.cell;

import org.jdesktop.wonderland.modules.mediaboard.server.*;
import com.jme.math.Vector2f;
import com.sun.sgs.app.ManagedReference;
import java.awt.Point;
import java.util.Calendar;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage.RequestStatus;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardSVGCellClientState;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardSVGCellServerState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A server cell associated with a whiteboard
 * @author nsimpson
 * @author jbarratt
 */
public class WhiteboardCellMO extends App2DCellMO {
    // The communications component used to broadcast to all clients

    @UsesCellComponentMO(WhiteboardComponentMO.class)
    private ManagedReference<WhiteboardComponentMO> commComponentRef;
    private WhiteboardSVGCellClientState stateHolder = new WhiteboardSVGCellClientState();
    private static final Logger logger =
            Logger.getLogger(WhiteboardCellMO.class.getName());
    // how long a client can retain control (ms)
    private static long controlTimeout = 90 * 1000;
    // SVG Document
    private transient Document svgDocument = null;
    // SVG Document URI
    private String svgDocumentURI = null;
    // SVG Document as XML string
    private String svgXML = null;
    // The preferred width (from the WFS file)
    private int preferredWidth;
    // The preferred height (from the WFS file)
    private int preferredHeight;
    private Point position;
    private Float zoom = new Float(1.0f);
    private boolean decorated = true;
    private String id;
    private Calendar controlOwnedDate = null;

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sharedStateComponentRef;


    // Default constructor, used when the cell is created via WFS
    public WhiteboardCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.mediaboard.client.cell.WhiteboardCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new WhiteboardSVGCellClientState(pixelScale);
        }
        ((WhiteboardSVGCellClientState) cellClientState).copyLocal(stateHolder);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        WhiteboardSVGCellServerState serverState = (WhiteboardSVGCellServerState) state;
        stateHolder.setPreferredWidth(serverState.getPreferredWidth());
        stateHolder.setPreferredHeight(serverState.getPreferredHeight());
        stateHolder.setPixelScale(new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY()));

        svgDocument = WhiteboardUtils.newDocument();
        persistDocument();

        // get the document URI from the server state
        String uri = serverState.getSVGDocumentURI();
        if (uri != null && uri.equals("") == false) {
            setDocumentURI(uri);
        }

        // load the SVG XML
        String xml = serverState.getSVGDocumentXML();
        if ((xml != null) && (xml.length() > 0)) {
            setDocument(xml);
            constructDocument();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState stateToFill) {
        if (stateToFill == null) {
            stateToFill = new WhiteboardSVGCellServerState();
        }
        
        super.getServerState(stateToFill);

        WhiteboardSVGCellServerState state = (WhiteboardSVGCellServerState) stateToFill;
        state.setPreferredWidth(stateHolder.getPreferredWidth());
        state.setPreferredHeight(stateHolder.getPreferredHeight());
        state.setPixelScaleX(stateHolder.getPixelScale().x);
        state.setPixelScaleY(stateHolder.getPixelScale().y);

        state.setSVGDocumentXML(getDocument());
        state.setSVGDocumentURI(getDocumentURI());

        return stateToFill;
    }

    private void constructDocument() {
        svgDocument = WhiteboardUtils.xmlStringToDocument(getDocument());
    }

    private void persistDocument() {
        setDocument(WhiteboardUtils.documentToXMLString(svgDocument));
    }

    /**
     * Add element to the Document object
     * @param element
     */
    public void addElement(Element element) {
        svgDocument.getDocumentElement().appendChild(element);
    }

    public void removeElement(Element element) {
        // Can't just remove element given as its parentNode field is null
        // and is not removed as a result
        Element rem = svgDocument.getElementById(element.getAttributeNS(null, "id"));
        svgDocument.getDocumentElement().removeChild(rem);
    }

    public void updateElement(Element element) {
        Element upd = svgDocument.getElementById(element.getAttributeNS(null, "id"));
        svgDocument.getDocumentElement().replaceChild(element, upd);
    }

    public void setControllingCell(String id) {
        this.id = id;
        if (id != null) {
            controlOwnedDate = Calendar.getInstance();
        } else {
            controlOwnedDate = null;
        }
    }

    public String getControllingCell() {
        return id;
    }

    /**
     * Set the SVG document
     * @param document the SVG document
     */
    public void setDocument(String document) {
        svgXML = document;
    }

    /**
     * Get the SVG document
     * @return the document
     */
    public String getDocument() {
        return svgXML;
    }

    /**
     * Set the SVG document URI
     * @param svgDocumentURI the URI of the document
     */
    public void setDocumentURI(String svgDocumentURI) {
        this.svgDocumentURI = svgDocumentURI;
    }

    /**
     * Get the SVG document URI
     * @return the URI of the document
     */
    public String getDocumentURI() {
        return svgDocumentURI;
    }

    /*
     * Set the image position
     * @param position the scroll position in x and y coordinates
     */
    public void setPosition(Point position) {
       stateHolder.setPosition(position);
    }

    /*
     * Get the image position
     * @return the scroll position of the image
     */
    public Point getPosition() {
        return stateHolder.getPosition();
    }

    /**
     * Set the zoom factor
     * @param zoom the zoom factor
     */
    public void setZoom(float zoom) {
        stateHolder.setZoom(zoom);
    }

    /**
     * Get the zoom factor
     * @return the zoom factor
     */
    public float getZoom() {
        return stateHolder.getZoom();
    }

    /*
     * Set the preferred width
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        stateHolder.setPreferredWidth(preferredWidth);
    }

    /*
     * Get the preferred width
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return stateHolder.getPreferredWidth();
    }

    /*
     * Set the preferred height
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        stateHolder.setPreferredHeight(preferredHeight);
    }

    /*
     * Get the preferred height
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return stateHolder.getPreferredHeight();
    }

    /**
     * Set the window decoration status
     * @param decorated whether to show or hide the window decorations
     */
    public void setDecorated(boolean decorated) {
        stateHolder.setDecorated(decorated);
    }

    /**
     * Get the window decoration status
     * @return true if the window decorations are enabled, false otherwise
     */
    public boolean getDecorated() {
        return stateHolder.getDecorated();
    }

    public long getControlOwnedDuration() {
        long ownedDuration = 0;

        if (controlOwnedDate != null) {
            Calendar now = Calendar.getInstance();
            ownedDuration = now.getTimeInMillis() - controlOwnedDate.getTimeInMillis();
        }

        return ownedDuration;
    }

    /*
     * Handle message
     * @param client the client that sent the message
     * @param message the message
     */
    public void receivedMessage(WonderlandClientSender clientSender, WonderlandClientID clientID, CellMessage message) {
        if (message instanceof WhiteboardCellMessage) {
            WhiteboardCellMessage messageReceived = (WhiteboardCellMessage) message;
            logger.fine("whiteboard cell MO: received msg: " + messageReceived + " from client: " + clientID);

            WhiteboardComponentMO commComponent = commComponentRef.getForUpdate();

            WhiteboardCellMessage response = null;

            // client making the request
            String requester = messageReceived.getUID();
            if (requester == null) {
                logger.warning("requesting cell should not be null");
            }

            // Prevent multiple threads retrieving the state,
            // which may lead to more than one believing the GLO is free
            synchronized (this) {
                // time out requests from non-responsive clients
                if (getControllingCell() != null) {
                    // clients may lose connectivity to the server while processing
                    // requests. 
                    // if this happens, release the controlling client lock so that
                    // other clients can process their requests
                    long controlDuration = getControlOwnedDuration();

                    if (controlDuration >= controlTimeout) {
                        logger.warning("whiteboard cell MO: forcing control release of controlling cell: " + getControllingCell());
                        setControllingCell(null);
                    }
                }

                if (getControllingCell() == null) {
                    // no cell has control, grant control to the requesting cell
                    setControllingCell(requester);
                }
            }

            if (getControllingCell().equals(requester)) {

                // reflect the command to other clients
                // respond to a client that is (now) in control
                switch (messageReceived.getAction()) {
                    case GET_STATE:
                        // return current state of whiteboard
                        String xml = getDocument();
                        String uri = getDocumentURI();
                        response = new WhiteboardCellMessage(messageReceived.getClientID(),
                                messageReceived.getCellID(), messageReceived.getUID(),
                                Action.SET_STATE, xml, uri,
                                getPosition(), getZoom());
                        logger.fine("whiteboard Cell MO: sending set state msg: " + messageReceived.getClientID() + ", " + response);
                        commComponent.sendAllClients(clientID, response);
                        messageReceived = null;
                        break;
                    case OPEN_DOCUMENT:
                        if (messageReceived.getURI() != null) {
                            // Create new Document object from URI target
                            String nextUri = messageReceived.getURI();
                            
                            nextUri = nextUri.substring(2, nextUri.length());
                            System.out.println("[iSocial:Mediaboard] Opening URI: " +nextUri);
                            //just in case there are any spaces in the name, change spaces to underscores
                            setDocumentURI(messageReceived.getURI().replace(" ", "_"));
                            svgDocument = WhiteboardUtils.openDocument(nextUri);
                            persistDocument();
                        }
                        break;
                    case NEW_DOCUMENT:
                        svgDocument = WhiteboardUtils.newDocument();
                        persistDocument();
                        break;
                    case ADD_ELEMENT:
                        Element toAdd = WhiteboardUtils.xmlStringToElement(messageReceived.getXMLString());
                        constructDocument();
                        Element addingElement = (Element) svgDocument.importNode(toAdd, true);
                        addElement(addingElement);
                        persistDocument();
                        break;
                    case REMOVE_ELEMENT:
                        Element toRemove = WhiteboardUtils.xmlStringToElement(messageReceived.getXMLString());
                        constructDocument();
                        //Element removingElement = (Element) svgDocument.importNode(toRemove, false);
                        removeElement(toRemove);
                        persistDocument();
                        break;
                    case UPDATE_ELEMENT:
                        Element toUpdate = WhiteboardUtils.xmlStringToElement(messageReceived.getXMLString());
                        constructDocument();
                        Element updatingElement = (Element) svgDocument.importNode(toUpdate, true);
                        updateElement(updatingElement);
                        persistDocument();
                        break;
                    case SET_VIEW_POSITION:
                        setPosition(messageReceived.getPosition());
                        break;
                    case SET_ZOOM:
                        setZoom(messageReceived.getZoom());
                        break;
                    default:
                        break;
                }

                setControllingCell(null);

                if (messageReceived != null) { // forward request to other clients
                    logger.fine("whiteboard cell MO: broadcasting msg: " + messageReceived);
                    commComponent.sendAllClients(clientID, messageReceived);
                }

            } else {
                // another cell has control
                // send a denial to the requesting client
                messageReceived.setRequestStatus(RequestStatus.REQUEST_DENIED);
                logger.fine("whiteboard cell MO: sending denial to client: " + messageReceived);
                commComponent.sendAllClients(clientID, messageReceived);
            }
        }
    }
}
