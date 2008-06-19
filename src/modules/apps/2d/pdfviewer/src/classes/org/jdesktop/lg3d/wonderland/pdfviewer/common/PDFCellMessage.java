/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.pdfviewer.common;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataDouble;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 * A Cell Message that carries PDF Viewer actions
 *
 * @author nsimpson
 */
public class PDFCellMessage extends CellMessage {

    private static final Logger logger =
            Logger.getLogger(PDFCellMessage.class.getName());

    public enum Action {

        UNKNOWN,
        OPEN_DOCUMENT,
        SHOW_PAGE,
        PLAY, PAUSE, STOP,
        SET_VIEW_POSITION,
        GET_STATE, SET_STATE,
        REQUEST_COMPLETE
    };

    public enum RequestStatus {

        REQUEST_OK,
        REQUEST_DENIED
    };
    
    private String uid;
    private Action action = Action.UNKNOWN;
    private RequestStatus status = RequestStatus.REQUEST_OK;
    private String doc;
    private int page;
    private Point position;
    private int pageCount = 0;

    public PDFCellMessage() {
        super();
    }

    public PDFCellMessage(Action action) {
        this(action, null, 0, null);
    }

    public PDFCellMessage(Action action, String doc, int page, Point position) {
        super();
        this.action = action;
        this.doc = doc;
        this.page = page;
        this.position = position;
    }

    public PDFCellMessage(CellID cellID, Action action) {
        super(cellID);
        setAction(action);
    }

    public PDFCellMessage(CellID cellID, String uid, Action action, String doc, int page, Point position) {
        super(cellID);
        setUID(uid);
        this.action = action;
        this.doc = doc;
        this.page = page;
        this.position = position;
    }

    public PDFCellMessage(PDFCellMessage pcm) {
        setUID(pcm.getUID());
        setCellID(pcm.getCellID());
        setAction(pcm.getAction());
        setDocument(pcm.getDocument());
        setPage(pcm.getPage());
        setPageCount(pcm.getPageCount());
        setPosition(pcm.getPosition());
        setRequestStatus(pcm.getRequestStatus());
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
     * Set the URL of the document
     * @param doc the URL of the document
     */
    public void setDocument(String doc) {
        this.doc = doc;
    }

    /**
     * Get the document URL
     * @return the URL of the document
     */
    public String getDocument() {
        return doc;
    }

    /**
     * Set the currently selected page
     * @param page the page to go to
     */
    public void setPage(int page) {
        this.page = page;
    }

    /** 
     * Get the currently selected page
     * @return the current page number
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the number of pages in the document
     * @param pageCount the number of pages
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * Gets the number of pages in the document
     * @return the number of pages
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Set the (x, y) position of the page
     * @param position the (x, y) position of the page
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Get the (x, y) position of the page
     * @return the (x, y) position
     */
    public Point getPosition() {
        return position;
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
     * Get a string representation of the PDF cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return getAction() + ", " + getRequestStatus() + ", " + getDocument() +
                ", " + getPage() + ", " + getPageCount() + ", " + getPosition();
    }

    /**
     * Extract the message from binary data
     */
    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);

        uid = DataString.value(data);
        action = Action.values()[DataInt.value(data)];
        doc = DataString.value(data);
        page = DataInt.value(data);
        pageCount = DataInt.value(data);
        position = new Point((int) DataDouble.value(data), (int) DataDouble.value(data));
        status = RequestStatus.values()[DataInt.value(data)];
    }

    /**
     * Create a binary version of the message
     */
    @Override
    protected void populateDataElements() {
        super.populateDataElements();

        dataElements.add(new DataString(uid));
        dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataString(doc));
        dataElements.add(new DataInt(page));
        dataElements.add(new DataInt(pageCount));
        dataElements.add(new DataDouble(position.getX()));
        dataElements.add(new DataDouble(position.getY()));
        dataElements.add(new DataInt(status.ordinal()));
    }
}
