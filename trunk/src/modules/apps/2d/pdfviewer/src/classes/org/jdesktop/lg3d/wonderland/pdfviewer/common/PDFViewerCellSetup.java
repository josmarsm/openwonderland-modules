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
import java.io.Serializable;
import java.util.logging.Logger;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.SharedApp2DImageCellSetup;
import org.jdesktop.lg3d.wonderland.pdfviewer.client.cell.PDFViewerApp;

/**
 * Container for PDF Viewer cell data
 *
 * @author nsimpson
 */
public class PDFViewerCellSetup extends SharedApp2DImageCellSetup implements Serializable {

    private static final Logger logger =
            Logger.getLogger(PDFViewerCellSetup.class.getName());
    private static final int DEFAULT_WIDTH = 791;   // 8.5"x11" page format
    private static final int DEFAULT_HEIGHT = 1024; //
    private long controlTimeout = 90 * 1000;    // how long a client can retain control (ms)
    public static final int LOOP_FOREVER = -1;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private boolean decorated = true;           // show window decorations
    private String document;
    private int page = 1;                       // current page
    private int pageCount = 0;                  // number of pages in document
    private Point position = new Point(0, 0);   // page pan/scroll position
    private boolean slideShow = false;          // whether in slide show mode
    private int slideShowStartPage = 0;         // page to start slideshow at
    private int slideShowEndPage = 0;           // page to end slideshow at
    private long slideShowDuration = 5000;      // milliseconds
    private long slideShowLoopDelay = 10000;    // milliseconds
    private int slideShowCount = LOOP_FOREVER;  // continuous slide show
    private String checksum;

    public PDFViewerCellSetup() {
        this(null, null, null);
    }

    public PDFViewerCellSetup(String appName, Matrix4f viewRectMat, String clientClassName) {
        super(appName, viewRectMat, clientClassName);
    }

    /**
     * Return the classname of the AppWindowGraphics2DApp subclass
     */
    @Override
    public String getClientClassname() {
        return PDFViewerApp.class.getName();
    }

    /*
     * Set the URL of the PDF document associated with this cell
     * @param doc the URL of the PDF document
     */
    public void setDocument(String document) {
        this.document = document;
    }

    /*
     * Get the URL of the PDF document associated with this cell
     * @return the URL of the PDF document
     */
    public String getDocument() {
        return document;
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

    /*
     * Set the current page
     * @param page the current page in the PDF document
     */
    public void setPage(int page) {
        this.page = page;
    }

    /*
     * Get the current page
     * @return the current page
     */
    public int getPage() {
        return page;
    }

    /*
     * Set the page position
     * @param position the scroll position in x and y coordinates
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /*
     * Get the page position
     * @return the scroll position of the page
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Set slide show mode
     * @param slideShow if true, display document as a slide show
     */
    public void setSlideShow(boolean slideShow) {
        this.slideShow = slideShow;
    }

    /**
     * Returns whether the application is in slideshow mode
     * @return true if in slideshow mode, false otherwise
     */
    public boolean getSlideShow() {
        return slideShow;
    }

    /**
     * Sets the first page of the slide show
     * @param slideShowStartPage the first page in the slide show
     */
    public void setStartPage(int slideShowStartPage) {
        this.slideShowStartPage = slideShowStartPage;
    }

    /**
     * Gets the first page of the slide show
     * @return the first page of the slideshow
     */
    public int getStartPage() {
        return slideShowStartPage;
    }

    /**
     * Sets the last page of the slide show
     * @param slideShowEndPage the last page in the slide show
     */
    public void setEndPage(int slideShowEndPage) {
        this.slideShowEndPage = slideShowEndPage;
    }

    /**
     * Gets the last page of the slide show
     * @return the last page of the slide show (-1 == end of document)
     */
    public int getEndPage() {
        return slideShowEndPage;
    }

    /**
     * Sets the duration to show each slide for
     * @param setShowDuration the duration (in milliseconds) to display each
     * slide
     */
    public void setShowDuration(long slideShowDuration) {
        this.slideShowDuration = slideShowDuration;
    }

    /**
     * Gets the duration to show each slide for
     * @return the duration (in milliseconds) to display each slide
     */
    public long getShowDuration() {
        return slideShowDuration;
    }

    /**
     * Sets the duration to pause before restarting the slide show
     * @param slideShowLoopDelay the duration (in milliseconds) to pause
     * before restarting the slide show
     */
    public void setLoopDelay(long slideShowLoopDelay) {
        this.slideShowLoopDelay = slideShowLoopDelay;
    }

    /**
     * Gets the duration to pause before restarting the slide show
     * @return the duration (in milliseconds) to pause before restarting the
     * slide show
     */
    public long getLoopDelay() {
        return slideShowLoopDelay;
    }

    /**
     * Sets the number of times to show the slide show (1 == once)
     * @param slideShowCount the number of times to show the slide show
     */
    public void setShowCount(int slideShowCount) {
        this.slideShowCount = slideShowCount;
    }

    /**
     * Gets the number of times to show the slide show
     * @return the number of times to show the slide show
     */
    public int getShowCount() {
        return slideShowCount;
    }

    /*
     * Set the preferred width
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    /*
     * Get the preferred width
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    /*
     * Set the preferred height
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /*
     * Get the preferred height
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }

    /** 
     * Set the window decoration status
     * @param decorated whether to show or hide the window decorations
     */
    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    /**
     * Get the window decoration status
     * @return true if the window decorations are enabled, false otherwise
     */
    public boolean getDecorated() {
        return decorated;
    }

    /**
     * Set the timeout for client requests
     * Clients that take longer than this time to process a request
     * will lose control
     * @param controlTimeout maximum time in milliseconds that a client can
     * retain control
     */
    public void setControlTimeout(long controlTimeout) {
        this.controlTimeout = controlTimeout;
    }

    /**
     * Get the control timeout (the length of time a client can have 
     * control before the server takes control away from the client)
     * @return the control timeout in milliseconds
     */
    public long getControlTimeout() {
        return controlTimeout;
    }

    /*
     * Get the checksum for the PDF document
     * @return the checksum of the PDF document
     */
    public String getChecksum() {
        return checksum;
    }

    /*
     * Set the checksum of the PDF document
     * @param checksum the checksum of the PDF document
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
