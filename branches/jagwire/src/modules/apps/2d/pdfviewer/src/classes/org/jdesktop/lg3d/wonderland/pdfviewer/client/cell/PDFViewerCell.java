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
package org.jdesktop.lg3d.wonderland.pdfviewer.client.cell;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.rmi.server.UID;
import java.util.logging.Logger;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.*;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFViewerCellSetup;

/**
 * Client Cell for a PDF Viewer Shared Application.
 *
 * @author nsimpson
 */
public class PDFViewerCell extends SharedApp2DImageCell
        implements ExtendedClientChannelListener {

    private static final Logger logger =
            Logger.getLogger(PDFViewerCell.class.getName());
    private PDFViewerCellSetup pdfSetup;
    private String myUID;

    public PDFViewerCell(final CellID cellID, String channelName, Matrix4d origin) {
        super(cellID, channelName, origin);
        myUID = new UID().toString();
    }

    /**
     * Initialize the PDF Viewer and load the document
     * @param setupData the setup data to initialize the cell with
     */
    @Override
    public void setup(CellSetup setupData) {
        super.setup(setupData);

        pdfSetup = (PDFViewerCellSetup) setupData;

        ((PDFViewerApp) app).setPreferredWidth((int) pdfSetup.getPreferredWidth());
        ((PDFViewerApp) app).setPreferredHeight((int) pdfSetup.getPreferredHeight());
        ((PDFViewerApp) app).setSize((int) pdfSetup.getPreferredWidth(), (int) pdfSetup.getPreferredHeight());
        ((PDFViewerApp) app).setDecorated(pdfSetup.getDecorated());
        ((PDFViewerApp) app).setInSlideShowMode(pdfSetup.getSlideShow());
        ((PDFViewerApp) app).setShowing(true);

        // request sync with shared whiteboard state
        logger.info("PDF viewer requesting initial sync");
        ((PDFViewerApp) app).sync(true);
    }

    /**
     * Reconfigure the cell, when the origin, bounds, or other setup information
     * has changed.
     */
    @Override
    public void reconfigure(Matrix4d origin, Bounds bounds, CellSetup setupData) {
        super.reconfigure(origin, bounds, setupData);
        ((PDFViewerApp) app).resync();
    }
    
    public String getUID() {
        if (myUID == null) {
        }
        return myUID;
    }

    /**
     * Set the channel associated with this cell
     * @param channel the channel to associate with this cell
     */
    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    protected void handleResponse(PDFCellMessage msg) {
        ((PDFViewerApp) app).handleResponse(msg);
    }

    /**
     * Process a cell message
     * @param channel the channel
     * @param session the session id
     * @param data the message data
     */
    @Override
    public void receivedMessage(ClientChannel channel, SessionId session,
            byte[] data) {
        CellMessage msg = Message.extractMessage(data, CellMessage.class);

        if (msg instanceof PDFCellMessage) {
            PDFCellMessage pdfmsg = Message.extractMessage(data, PDFCellMessage.class);
            logger.fine("PDF viewer received message: " + pdfmsg);
            handleResponse(pdfmsg);
        } else {
            super.receivedMessage(channel, session, data);
        }
    }

    /**
     * Process a channel leave event
     * @param channel the left channel
     */
    public void leftChannel(ClientChannel channel) {
        logger.fine("PDF viewer left channel: " + channel);
    }
}
