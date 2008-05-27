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
package org.jdesktop.lg3d.wonderland.pdfviewer.server.cell;

import com.sun.sgs.app.ManagedObject;
import java.awt.Point;
import java.io.Serializable;
import java.util.Calendar;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFViewerCellSetup;

/**
 *
 * @author nsimpson
 */
public class PDFViewerStateMO implements Serializable, ManagedObject {

    private static final Logger logger =
            Logger.getLogger(PDFViewerStateMO.class.getName());
    private PDFViewerCellSetup setup;
    private String id;
    private Calendar controlOwnedDate = null;

    public PDFViewerStateMO(PDFViewerCellSetup setup) {
        this.setup = setup;
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

    public PDFViewerCellSetup getCellSetup() {
        return setup;
    }
    /*
     * Set the URL of the PDF document associated with this cell
     * @param doc the URL of the PDF document
     */

    public void setDocument(String document) {
        setup.setDocument(document);
    }

    /*
     * Get the URL of the PDF document associated with this cell
     * @return the URL of the PDF document
     */
    public String getDocument() {
        return setup.getDocument();
    }

    /**
     * Sets the number of pages in the document
     * @param pageCount the number of pages
     */
    public void setPageCount(int pageCount) {
        setup.setPageCount(pageCount);
    }

    /**
     * Gets the number of pages in the document
     * @return the number of pages
     */
    public int getPageCount() {
        return setup.getPageCount();
    }

    /*
     * Set the current page
     * @param page the current page in the PDF document
     */
    public void setPage(int page) {
        setup.setPage(page);
    }

    /*
     * Get the current page
     * @return the current page
     */
    public int getPage() {
        return setup.getPage();
    }

    /*
     * Set the page position
     * @param position the scroll position in x and y coordinates
     */
    public void setPosition(Point position) {
        setup.setPosition(position);
    }

    /*
     * Get the page position
     * @return the scroll position of the page
     */
    public Point getPosition() {
        return setup.getPosition();
    }

    /**
     * Set slide show mode
     * @param slideShow if true, display document as a slide show
     */
    public void setSlideShow(boolean slideShow) {
        setup.setSlideShow(slideShow);
    }

    /**
     * Returns whether the application is in slideshow mode
     * @return true if in slideshow mode, false otherwise
     */
    public boolean getSlideShow() {
        return setup.getSlideShow();
    }

    /**
     * Sets the first page of the slide show
     * @param slideShowStartPage the first page in the slide show
     */
    public void setStartPage(int slideShowStartPage) {
        setup.setStartPage(slideShowStartPage);
    }

    /**
     * Gets the first page of the slide show
     * @return the first page of the slideshow
     */
    public int getStartPage() {
        return setup.getStartPage();
    }

    /**
     * Sets the last page of the slide show
     * @param slideShowEndPage the last page in the slide show
     */
    public void setEndPage(int slideShowEndPage) {
        setup.setEndPage(slideShowEndPage);
    }

    /**
     * Gets the last page of the slide show
     * @return the last page of the slide show (-1 == end of document)
     */
    public int getEndPage() {
        return setup.getEndPage();
    }

    /**
     * Sets the duration to show each slide for
     * @param setShowDuration the duration (in milliseconds) to display each
     * slide
     */
    public void setShowDuration(long slideShowDuration) {
        setup.setShowDuration(slideShowDuration);
    }

    /**
     * Gets the duration to show each slide for
     * @return the duration (in milliseconds) to display each slide
     */
    public long getShowDuration() {
        return setup.getShowDuration();
    }

    /**
     * Sets the duration to pause before restarting the slide show
     * @param slideShowLoopDelay the duration (in milliseconds) to pause
     * before restarting the slide show
     */
    public void setLoopDelay(long slideShowLoopDelay) {
        setup.setLoopDelay(slideShowLoopDelay);
    }

    /**
     * Gets the duration to pause before restarting the slide show
     * @return the duration (in milliseconds) to pause before restarting the
     * slide show
     */
    public long getLoopDelay() {
        return setup.getLoopDelay();
    }

    /**
     * Sets the number of times to show the slide show (1 == once)
     * @param slideShowCount the number of times to show the slide show
     */
    public void setShowCount(int slideShowCount) {
        setup.setShowCount(slideShowCount);
    }

    /**
     * Gets the number of times to show the slide show
     * @return the number of times to show the slide show
     */
    public int getShowCount() {
        return setup.getShowCount();
    }

    /*
     * Set the preferred width
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        setup.setPreferredWidth(preferredWidth);
    }

    /*
     * Get the preferred width
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return setup.getPreferredWidth();
    }

    /*
     * Set the preferred height
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        setup.setPreferredHeight(preferredHeight);
    }

    /*
     * Get the preferred height
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return setup.getPreferredHeight();
    }

    public long getControlOwnedDuration() {
        long ownedDuration = 0;

        if (controlOwnedDate != null) {
            Calendar now = Calendar.getInstance();
            ownedDuration = now.getTimeInMillis() - controlOwnedDate.getTimeInMillis();
        }

        return ownedDuration;
    }
}
