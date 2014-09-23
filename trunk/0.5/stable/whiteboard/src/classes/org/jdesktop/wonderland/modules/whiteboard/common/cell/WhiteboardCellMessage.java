/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.whiteboard.common.cell;

import java.awt.Point;
import java.math.BigInteger;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * A Cell Message that carries whiteboard actions
 *
 * @author nsimpson
 * @author jbarratt
 * @author Abhishek Upadhyay
 */
public class WhiteboardCellMessage extends CellMessage {

    private static final Logger logger =
            Logger.getLogger(WhiteboardCellMessage.class.getName());

    public enum Action {

        UNKNOWN,
        NEW_DOCUMENT, OPEN_DOCUMENT,
        ADD_ELEMENT, REMOVE_ELEMENT, UPDATE_ELEMENT,
        SET_VIEW_POSITION,
        SET_ZOOM,
        GET_STATE, SET_STATE,
        UPDATE_BACKGROUND_IMAGE,
        ADD_BACKGROUND_IMAGE,
        ADD_TEXT_ELEMENT,
        UPDATE_TEXT_ELEMENT
    };

    public enum RequestStatus {

        REQUEST_OK,
        REQUEST_DENIED
    };
    protected BigInteger clientID;
    private Action action = Action.UNKNOWN;
    private RequestStatus status = RequestStatus.REQUEST_OK;
    private String xmlString = null;
    private String docURI = null;
    private Point position = new Point(0, 0);
    private Float zoom = 1.0f;
    private String uid = null;

    public WhiteboardCellMessage() {
    }

    public WhiteboardCellMessage(BigInteger clientID, CellID cellID, String uid, Action action, String xmlString,
            String docURI, Point position, Float zoom) {
        super(cellID);
        this.clientID = clientID;
        this.uid = uid;
        this.action = action;
        this.xmlString = xmlString;
        this.docURI = docURI;
        if (position != null) {
            this.position = position;
        }
        if (zoom != null) {
            this.zoom = zoom;
        }
    }

    public WhiteboardCellMessage(WhiteboardCellMessage pcm) {
        setClientID(pcm.getClientID());
        setUID(pcm.getUID());
        setCellID(pcm.getCellID());
        setAction(pcm.getAction());
        setXMLString(pcm.getXMLString());
        setURI(pcm.getURI());
        if (pcm.getPosition() != null) {
            setPosition(pcm.getPosition());
        }
        if (pcm.getZoom() != null) {
            setZoom(pcm.getZoom());
        }
    }

    public void setClientID(BigInteger clientID) {
        this.clientID = clientID;
    }

    public BigInteger getClientID() {
        return clientID;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public String getUID() {
        return uid;
    }

    /**
     * Set the action
     * @param action the action
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Get the action
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /** 
     * Set the xml string of the message
     * @param xmlString the SVG document or element
     */
    public void setXMLString(String xmlString) {
        this.xmlString = xmlString;
    }

    /**
     * Get the string of the message
     * @return the string of the message
     */
    public String getXMLString() {
        return xmlString;
    }

    public void setURI(String docURI) {
        this.docURI = docURI;
    }

    public String getURI() {
        return docURI;
    }

    /**
     * Set the (x, y) position of the image
     * @param position the (x, y) position of the image
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Get the (x, y) position of the image
     * @return the (x, y) position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Set the zoom factor
     * @param zoom the zoom factor
     */
    public void setZoom(Float zoom) {
        this.zoom = zoom;
    }

    /**
     * Get the zoom factor
     * @return the zoom factor
     */
    public Float getZoom() {
        return zoom;
    }

    /**
     * Set the status of this request
     * @param status the status of the request
     */
    public void setRequestStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Get the status of this request
     * @return the request status
     */
    public RequestStatus getRequestStatus() {
        return status;
    }

    /** 
     * Get a string representation of the SVG cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return "uid: " + getUID() + ", " +
                "action: " + getAction() + ", " +
                "status: " + getRequestStatus() + ", " +
                "document: " + getXMLString() + ", " +
                "uri: " + getURI() + ", " +
                "position: " + getPosition() + ", " +
                "zoom: " + getZoom();
    }
}
