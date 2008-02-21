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
        OPEN_DOCUMENT, CLOSE_DOCUMENT, DOCUMENT_OPENED, 
        SHOW_PAGE, NEXT_PAGE, 
        PLAY, PAUSE, STOP, 
        SET_VIEW_POSITION
    };
    
    private Action action;
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
        this(cellID, action, null, 0, null);
    }

    public PDFCellMessage(CellID cellID, Action action, String doc, int page, Point position) {
        super(cellID);
        this.action = action;
        this.doc = doc;
        this.page = page;
        this.position = position;
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
     * Get a string representation of the PDF cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return getAction() + ", " + getDocument() + ", " + getPage() + ", " +
                getPageCount() + ", " + getPosition();
    }

    /**
     * Extract the message from binary data
     */
    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);

        action = Action.values()[DataInt.value(data)];
        doc = DataString.value(data);
        page = DataInt.value(data);
        pageCount = DataInt.value(data);
        position = new Point((int) DataDouble.value(data), (int) DataDouble.value(data));

    }

    /**
     * Create a binry version of the message
     */
    @Override
    protected void populateDataElements() {
        super.populateDataElements();

        dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataString(doc));
        dataElements.add(new DataInt(page));
        dataElements.add(new DataInt(pageCount));
        dataElements.add(new DataDouble(position.getX()));
        dataElements.add(new DataDouble(position.getY()));
    }
}
