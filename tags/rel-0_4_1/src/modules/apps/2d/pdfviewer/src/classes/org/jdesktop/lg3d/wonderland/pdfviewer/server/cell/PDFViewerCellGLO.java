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
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.PeriodicTaskHandle;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage.Action;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage.RequestStatus;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFViewerCellSetup;

/**
 * A server cell associated with a PDF viewer
 * @author nsimpson
 */
public class PDFViewerCellGLO extends SharedApp2DImageCellGLO
        implements ManagedObject, BeanSetupGLO, CellMessageListener {

    private static final Logger logger =
            Logger.getLogger(PDFViewerCellGLO.class.getName());
    private static long controlTimeout = 90 * 1000; // how long a client can retain control (ms)

    // The setup object contains the current state of the PDF Viewer,
    // including document URL, current page and current scroll position
    // within the page. It's updated every time a client makes a change
    // to the document so that when new clients join, they receive the
    // current state.
    private ManagedReference stateRef = null;
    private PeriodicTaskHandle slideShowTask;
    private boolean haveClients = false;

    public PDFViewerCellGLO() {
        this(null, null, null, null);
    }

    public PDFViewerCellGLO(Bounds bounds, String appName, Matrix4d cellOrigin,
            Matrix4f viewRectMat) {
        super(bounds, appName, cellOrigin, viewRectMat, PDFViewerCellGLO.class.getName());
    }

    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     * @return the class name of the corresponding client cell
     */
    @Override
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.pdfviewer.client.cell.PDFViewerCell";
    }

    /**
     * Hack to get the Cell Channel from the private method
     * @return the Cell Channel
     */
    public Channel getCellChannel2() {
        return getCellChannel();
    }

    /**
     * Get the setup data for this cell
     * @return the cell setup data
     */
    @Override
    public PDFViewerCellSetup getSetupData() {
        return stateRef.get(PDFViewerStateMO.class).getCellSetup();
    }

    public PDFViewerStateMO getStateMO() {
        PDFViewerStateMO stateMO = null;
        if (stateRef != null) {
            stateMO = stateRef.get(PDFViewerStateMO.class);
        }

        return stateMO;
    }

    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param data the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup data) {
        BasicCellGLOSetup<PDFViewerCellSetup> setupData = (BasicCellGLOSetup<PDFViewerCellSetup>) data;
        super.setupCell(setupData);

        PDFViewerCellSetup pcs = setupData.getCellSetup();
        controlTimeout = pcs.getControlTimeout();

        if (getStateMO() == null) {
            // create a new managed object containing the setup data
            PDFViewerStateMO stateMO = new PDFViewerStateMO(setupData.getCellSetup());

            // create a managed reference to the new state managed object
            DataManager dataMgr = AppContext.getDataManager();
            stateRef = dataMgr.createReference(stateMO);
        }
    }

    /**
     * Called when the properties of a cell have changed.
     *
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellGLOSetup data) {
        setupCell(data);
    }

    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellGLOSetup getCellGLOSetup() {
        return new BasicCellGLOSetup<PDFViewerCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    /**
     * Open the cell channel
     */
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }

    /*
     * Handle message
     * @param client the client that sent the message
     * @param message the message
     */
    @Override
    public void receivedMessage(ClientSession client, CellMessage message) {
        if (message instanceof PDFCellMessage) {
            PDFCellMessage pdfcm = (PDFCellMessage) message;
            logger.fine("PDF GLO received msg: " + pdfcm);

            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());

            // clone the message
            PDFCellMessage msg = new PDFCellMessage(pdfcm);

            // the current state of the application
            PDFViewerStateMO stateMO = getStateMO();

            // client currently in control
            String controlling = stateMO.getControllingCell();
            // client making the request
            String requester = pdfcm.getUID();

            // time out requests from non-responsive clients
            if (controlling != null) {
                // clients may lose connectivity to the server while processing
                // requests. 
                // if this happens, release the controlling client lock so that
                // other clients can process their requests
                long controlDuration = stateMO.getControlOwnedDuration();

                if (controlDuration >= controlTimeout) {
                    logger.warning("forcing control release of controlling cell: " + stateMO.getControllingCell());
                    stateMO.setControllingCell(null);
                    controlling = null;
                }
            }

            if (controlling == null) {
                // no cell has control, grant control to the requesting cell
                stateMO.setControllingCell(requester);

                // reflect the command to all clients
                // respond to a client that is (now) in control
                switch (pdfcm.getAction()) {
                    case GET_STATE:
                        // return current state of PDF app
                        msg.setAction(Action.SET_STATE);
                        msg.setDocument(stateMO.getDocument());
                        msg.setPage(stateMO.getPage());
                        msg.setPosition(stateMO.getPosition());
                        msg.setPageCount(stateMO.getPageCount());
                        break;
                    case OPEN_DOCUMENT:
                        if (isSlideShowActive()) {
                            stopSlideShow();
                        }
                        stateMO.setDocument(msg.getDocument());
                        stateMO.setPage(msg.getPage());
                        stateMO.setPageCount(msg.getPageCount());
                        stateMO.setPosition(msg.getPosition());
                        break;
                    case SHOW_PAGE:
                        if (!isSlideShowActive()) {
                            stateMO.setPage(msg.getPage());
                        }
                        break;
                    case PLAY:
                        if (!isSlideShowActive()) {
                            startSlideShow();
                        }
                        break;
                    case PAUSE:
                    case STOP:
                        if (isSlideShowActive()) {
                            stopSlideShow();
                        }
                        break;
                    case SET_VIEW_POSITION:
                        stateMO.setPosition(msg.getPosition());
                        break;
                    case REQUEST_COMPLETE:
                        // release control of PDF document by this client
                        stateMO.setControllingCell(null);
                        stateMO.setPageCount(pdfcm.getPageCount());

                        // if this is the first client to join, start the slide show if 
                        // in slide show mode
                        if (haveClients == false) {
                            haveClients = true;
                            if (stateMO.getSlideShow() == true) {
                                startSlideShow();
                            }
                        }
                        break;
                }
                logger.fine("PDF GLO broadcasting msg: " + msg);
                getCellChannel().send(sessions, msg.getBytes());
            } else {
                // one cell has control
                switch (pdfcm.getAction()) {
                    case REQUEST_COMPLETE:
                        // release control of camera by this client
                        stateMO.setControllingCell(null);
                        stateMO.setPageCount(pdfcm.getPageCount());
                        // broadcast request complete to all clients
                        // broadcast the message to all clients, including the requester
                        logger.fine("PDF GLO broadcasting msg: " + msg);
                        getCellChannel().send(sessions, msg.getBytes());
                        break;
                    default:
                        // send a denial to the requesting client
                        msg.setRequestStatus(RequestStatus.REQUEST_DENIED);
                        logger.info("PDF GLO sending denial to client: " + msg);
                        getCellChannel().send(client, msg.getBytes());
                        break;
                }
            }
        } else {
            super.receivedMessage(client, message);
        }
    }

    public void startSlideShow() {
        if (slideShowTask == null) {
            logger.info("PDF starting slide show");

            // the current state of the application
            PDFViewerStateMO stateMO = getStateMO();

            // create a task to run the slide show
            SlideShowTask slideTask = new SlideShowTask(this);

            stateMO.setSlideShow(true);

            if (stateMO.getStartPage() != 0) {
                // a slide show range has been specified, start the slide 
                // show on the first page of the slide show
                slideTask.setCurrentPage(stateMO.getStartPage());
            } else {
                // no slide show range has been specified, so use the entire
                // document, but start/resume at the current page
                stateMO.setStartPage(1);
                stateMO.setEndPage(stateMO.getPageCount());
                slideTask.setCurrentPage(stateMO.getPage());
            }
            logger.info("PDF slide show from page: " + stateMO.getStartPage() +
                    " to " + stateMO.getEndPage() +
                    ", starting from : " + slideTask.getCurrentPage());
            // start a task to change pages
            slideShowTask = AppContext.getTaskManager().schedulePeriodicTask(
                    slideTask,
                    0,
                    stateMO.getShowDuration());
        }
    }

    public void stopSlideShow() {
        if (slideShowTask != null) {
            logger.info("PDF stopping slide show");

            PDFViewerStateMO stateMO = getStateMO();
            stateMO.setSlideShow(false);
            slideShowTask.cancel();
            slideShowTask = null;
        }
    }

    public boolean isSlideShowActive() {
        return (slideShowTask != null);
    }
}
