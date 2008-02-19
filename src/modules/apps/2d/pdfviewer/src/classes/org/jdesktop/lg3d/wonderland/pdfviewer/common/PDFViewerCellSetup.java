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
import java.util.logging.Logger;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.SharedApp2DCellSetup;

/**
 * Container for PDF Viewer cell data
 *
 * @author nsimpson
 */
public class PDFViewerCellSetup extends SharedApp2DCellSetup {

    private static final Logger logger =
            Logger.getLogger(PDFViewerCellSetup.class.getName());
    private static final int DEFAULT_WIDTH = 791;   // 8.5"x11" page format
    private static final int DEFAULT_HEIGHT = 1024; //
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private String document;
    private int page = 1;
    private Point position = new Point(0, 0);
    private boolean slideShow = false;
    private int slideShowStartPage = 0;     // page to start slideshow at
    private int slideShowEndPage = 0;       // page to end slideshow at
    private int slideShowDuration = 5000;   // milliseconds
    private int slideShowLoopDelay = 10000; // milliseconds
    private int slideShowCount = 0;         // don't do a slide show
    private String checksum;

    public PDFViewerCellSetup() {
        this(null, null);
    }

    public PDFViewerCellSetup(String appName, Matrix4f viewRectMat) {
        super(appName, viewRectMat);
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
     * Gets the first page of the slide sh ow
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
    public void setShowDuration(int slideShowDuration) {
        this.slideShowDuration = slideShowDuration;
    }

    /**
     * Gets the duration to show each slide for
     * @return the duration (in milliseconds) to display each slide
     */
    public int getShowDuration() {
        return slideShowEndPage;
    }
    
    /**
     * Sets the duration to pause before restarting the slide show
     * @param slideShowLoopDelay the duration (in milliseconds) to pause
     * before restarting the slide show
     */
    public void setLoopDelay(int slideShowLoopDelay) {
        this.slideShowLoopDelay = slideShowLoopDelay;
    }

    /**
     * Gets the duration to pause before restarting the slide show
     * @return the duration (in milliseconds) to pause before restarting the
     * slide show
     */
    public int getLoopDelay() {
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
    public int getLoopCount() {
        return slideShowCount;
    }
    
    public void startSlideShow() {
        
    }
    
    public void stopSlideShow() {
        
    }
    
    public boolean isSlideShowActive() {
        return false;
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

    /*
     * Get the checksum for the PDF document
     * @return the checksm of the PDF document
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
