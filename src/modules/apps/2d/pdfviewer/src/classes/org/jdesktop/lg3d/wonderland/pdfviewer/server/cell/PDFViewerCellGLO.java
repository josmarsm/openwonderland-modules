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
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage.Action;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFViewerCellSetup;

/**
 * A server cell associated with a PDF viewer
 * @author nsimpson
 */
public class PDFViewerCellGLO extends SharedApp2DImageCellGLO
        implements ManagedObject, BeanSetupGLO, CellMessageListener {

    private static final Logger logger =
            Logger.getLogger(PDFViewerCellGLO.class.getName());
    // The setup object contains the current state of the PDF Viewer,
    // including document URL, current page and current scroll position
    // within the page. It's updated every time a client makes a change
    // to the document so that when new clients join, they receive the
    // current state.
    private ManagedReference setupRef = null;
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
        return setupRef.get(PDFViewerCellSetup.class);
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
        PDFViewerCellSetup setup = setupData.getCellSetup();

        DataManager dataMgr = AppContext.getDataManager();
        setupRef = dataMgr.createReference(setup);

        AxisAngle4d aa = new AxisAngle4d(setupData.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setupData.getOrigin());

        Matrix4d o = new Matrix4d(rot, origin, setupData.getScale());
        setOrigin(o);

        if (setupData.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float) setupData.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
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
    public void receivedMessage(ClientSession client, CellMessage message) {
        PDFCellMessage pdfmsg = (PDFCellMessage) message;
        logger.fine("receivedMessage: " + pdfmsg);

        // update setup data with the latest shared state
        PDFViewerCellSetup setup = setupRef.get(PDFViewerCellSetup.class);

        setup.setDocument(pdfmsg.getDocument());
        setup.setPage(pdfmsg.getPage());
        setup.setPosition(pdfmsg.getPosition());

        if (pdfmsg.getAction() == Action.PAUSE) {
            if (isSlideShowActive()) {
                stopSlideShow();
            } else {
                startSlideShow();
            }
        } else if (pdfmsg.getAction() == Action.DOCUMENT_OPENED) {
            // record the number of pages in the just opened document
            setup.setPageCount(pdfmsg.getPageCount());
            if (setup.getEndPage() == 0) {
                // initialize the end page, if not set
                setup.setEndPage(pdfmsg.getPageCount());
            }
            // if this is the first client to join, start the slide show if 
            // in slide show mode
            if (haveClients == false) {
                haveClients = true;
                if (setup.getSlideShow() == true) {
                    startSlideShow();
                }
            }
        }

        if (!isSlideShowActive()) {
            // only share state changes, if a slide show is not running, 
            // otherwise all viewers' states are managed by the slide show task
            PDFCellMessage msg = new PDFCellMessage(pdfmsg.getAction(),
                    pdfmsg.getDocument(), pdfmsg.getPage(), pdfmsg.getPosition());
            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
            // broadcast changes in sending client to all other clients
            sessions.remove(client);
            getCellChannel().send(sessions, msg.getBytes());
        }
    }

    public void startSlideShow() {
        if (slideShowTask == null) {
            logger.fine("starting slide show");

            PDFViewerCellSetup setup = setupRef.get(PDFViewerCellSetup.class);

            // create a task to run the slide show
            SlideShowTask slideTask = new SlideShowTask(this);
            // start/resume the slide show at the current page
            slideTask.setCurrentPage(setup.getPage());
            
            // start a task to change pages
            slideShowTask = AppContext.getTaskManager().schedulePeriodicTask(
                    slideTask,
                    setup.getShowDuration(),
                    setup.getShowDuration());
        }
    }

    public void stopSlideShow() {
        if (slideShowTask != null) {
            logger.fine("stopping slide show");
            slideShowTask.cancel();
            slideShowTask = null;
        }
    }

    public boolean isSlideShowActive() {
        return (slideShowTask != null);
    }
}
