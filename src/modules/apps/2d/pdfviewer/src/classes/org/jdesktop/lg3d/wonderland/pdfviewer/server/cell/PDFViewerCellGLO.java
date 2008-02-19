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

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
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
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFViewerCellSetup;

/**
 * A server cell associated with a PDF viewer
 * @author nsimpson
 */
public class PDFViewerCellGLO extends SharedApp2DImageCellGLO
        implements BeanSetupGLO, CellMessageListener {

    private static final Logger logger =
            Logger.getLogger(PDFViewerCellGLO.class.getName());
    // The setup object contains the current state of the PDF Viewer,
    // including document URL, current page and current scroll position
    // within the page. It's updated every time a client makes a change
    // to the document so that when new clients join, they receive the
    // current state.
    private BasicCellGLOSetup<PDFViewerCellSetup> setup;

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
     * Get the setup data for this cell
     * @return the cell setup data
     */
    @Override
    public PDFViewerCellSetup getSetupData() {
        System.err.println("----get setup data");
        return setup.getCellSetup();
    }

    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup setupData) {
        System.err.println("----setup cell");

        setup = (BasicCellGLOSetup<PDFViewerCellSetup>) setupData;

        AxisAngle4d aa = new AxisAngle4d(setup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setup.getOrigin());

        Matrix4d o = new Matrix4d(rot, origin, setup.getScale());
        setOrigin(o);

        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float) setup.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
        }
    }

    /**
     * Add the specified cell as a childRef of this cell. Also adds this cell
     * as a part of the childRef
     */
    @Override
    public void addChildCell(ManagedReference childRef) {
        super.addChildCell(childRef);
        System.err.println("----child cell count: " + childCells.size());
    }

    /**
     * Remove the child from this cell
     */
    @Override
    public void removeChildCell(ManagedReference childRef) {
        super.removeChildCell(childRef);
        System.err.println("----child cell count: " + childCells.size());
    }

    /**
     * Called when the properties of a cell have changed.
     *
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellGLOSetup setupData) {
        System.err.println("reconfigure cell");
        setupCell(setupData);
    }

    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellGLOSetup getCellGLOSetup() {
        System.err.println("getCellGLOSetup");
        return new BasicCellGLOSetup<PDFViewerCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    /**
     * Open the cell channel
     */
    @Override
    public void openChannel() {
        System.err.println("----open channel");
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
        setup.getCellSetup().setDocument(pdfmsg.getDocument());
        setup.getCellSetup().setPage(pdfmsg.getPage());
        setup.getCellSetup().setPosition(pdfmsg.getPosition());

        // notify all clients except the client that sent the message
        PDFCellMessage msg = new PDFCellMessage(pdfmsg.getAction(),
                pdfmsg.getDocument(), pdfmsg.getPage(), pdfmsg.getPosition());
        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
        sessions.remove(client);
        getCellChannel().send(sessions, msg.getBytes());
    }

    public void startSlideShow() {

    }

    public void stopSlideShow() {

    }
}
