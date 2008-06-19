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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.awt.Point;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFViewerCellSetup;

/**
 *
 * @author nsimpson
 */
public class SlideShowTask implements Serializable, Task, ManagedObject {

    private static final Logger logger =
            Logger.getLogger(SlideShowTask.class.getName());
    
    private ManagedReference pdfRef = null;
    private ManagedReference pdfStateRef = null;
    private int currentPage = 0;
    private int showIteration = 0;

    public SlideShowTask(PDFViewerCellGLO glo) {
        // cache references to the PDFViewerCellGLO and the PDFViewerCellSetup
        // instances
        DataManager dataMgr = AppContext.getDataManager();
        pdfRef = dataMgr.createReference(glo);

        PDFViewerStateMO pdfStateMO = glo.getStateMO();
        pdfStateRef = dataMgr.createReference(pdfStateMO);
        
        // start at the first page of the slide show
        currentPage = pdfStateMO.getStartPage();
    }

    /**
     * Sets the current page
     * @param currentPage the page to show
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    /**
     * Gets the current page
     * @return the current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Slide show task that sends page change commands to all connected clients.
     * Shows a new slide every PDFViewerCellSetup.getShowDuration() milliseconds.
     */
    public void run() {
        PDFViewerCellGLO glo = pdfRef.get(PDFViewerCellGLO.class);
        PDFViewerStateMO stateMO = pdfStateRef.get(PDFViewerStateMO.class);

        if ((stateMO.getShowCount() == PDFViewerCellSetup.LOOP_FOREVER) ||
                (showIteration < stateMO.getShowCount())) {
            logger.info("PDF slide show: showing page " + currentPage +
                    " in range " + stateMO.getStartPage() + "-" + stateMO.getEndPage());

            // send a message to all clients to show the next page in the 
            // slide show
            stateMO.setPage(currentPage);
            PDFCellMessage msg = new PDFCellMessage(PDFCellMessage.Action.SHOW_PAGE,
                    stateMO.getDocument(), currentPage, new Point());

            Set<ClientSession> sessions = new HashSet<ClientSession>(glo.getCellChannel2().getSessions());
            glo.getCellChannel2().send(sessions, msg.getBytes());

            if (currentPage >= stateMO.getEndPage()) {
                // end of a slide show iteration, there may be more
                showIteration++;
                logger.info("PDF slide show: end of iteration " + showIteration + " of " +
                        ((stateMO.getShowCount() == PDFViewerCellSetup.LOOP_FOREVER)
                        ? "an infinite "
                        : ("a " + stateMO.getShowCount() + " loop ")) + "slide show");
            }
            // determine next page to show
            currentPage = (currentPage >= stateMO.getEndPage()) ? stateMO.getStartPage() : currentPage + 1;
        }
    }
}
