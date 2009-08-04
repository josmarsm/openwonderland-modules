/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.pdfspreader.client;

import com.sun.pdfview.PDFFile;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.pdfspreader.client.jme.cell.PDFSpreaderCellRenderer;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage.LayoutType;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellClientState;
import org.jdesktop.wonderland.modules.presentationbase.client.SlidesCell;

/**
 * The client side component of the PDFSpreaderCell. When a user drops a PDF,
 * this is the client code that manages the process.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PDFSpreaderCell extends Cell implements SlidesCell {

    private PDFSpreaderCellRenderer renderer = null;

    private static final Logger logger =
            Logger.getLogger(PDFSpreaderCell.class.getName());

    private String pdfURI;

    private PDFFile pdfDocument;

    private LayoutType layout = LayoutType.LINEAR;

    private float spacing = 4.0f;
    private float scale = 1.0f;


    public PDFSpreaderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

    }

    protected void updateLayout() {
        renderer.updateLayout();
    }

    protected void sendCurrentLayoutToServer() {
        // This ONLY gets called by the HUD panel, so when we get this call
        // we know that a local change has occured that we need to send
        // to the server.
        PDFSpreaderCellChangeMessage msg = new PDFSpreaderCellChangeMessage();
        msg.setLayout(layout);
        msg.setScale(scale);
        msg.setSpacing(spacing);
        this.sendCellMessage(msg);

        logger.finer("just sent cell message to server: " + msg);
    }




    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        this.pdfURI = ((PDFSpreaderCellClientState)state).getPdfURI();
        this.spacing = ((PDFSpreaderCellClientState)state).getSpacing();
        this.layout = ((PDFSpreaderCellClientState)state).getLayout();
        this.scale = ((PDFSpreaderCellClientState)state).getScale();
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);


        if(status==CellStatus.ACTIVE && increasing) {
            channel.addMessageReceiver(PDFSpreaderCellChangeMessage.class, new PDFSpreaderCellMessageReceiver());
        } else if (status==CellStatus.DISK && !increasing) {
            // Leaving here for potential future logic that needs to happen here.
        } else if (status==CellStatus.RENDERING&& !increasing) {
            // As we're falling down the status chain, try removing the listener
            // earlier. It seems to be gone by the time we get to DISK.
            channel.removeMessageReceiver(PDFSpreaderCellChangeMessage.class);
        }

    }

    public LayoutType getLayout() {
        return layout;
    }

    public void setLayout(LayoutType layout) {
        logger.finer("Setting layout to: " + layout);
        this.layout = layout;
        renderer.updateLayout();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        logger.finer("Setting scale to: " + scale);
        renderer.updateLayout();

    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        logger.finer("Setting spacing to: " + spacing);
        this.spacing = spacing;
        renderer.updateLayout();
    }



    public String getSourceURI() {
        return this.pdfURI;
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new PDFSpreaderCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    public void updateServerCell() {
        // package the current local settings up and send them to the server.
        // this should only trigger on local changes.

        PDFSpreaderCellChangeMessage msg = new PDFSpreaderCellChangeMessage();
        msg.setLayout(this.layout);
        msg.setScale(scale);
        msg.setSpacing(spacing);

        this.sendCellMessage(msg);
    }

    public CellTransform getTransform() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNumSlides() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getInterslideSpacing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class PDFSpreaderCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            PDFSpreaderCellChangeMessage msg = (PDFSpreaderCellChangeMessage)message;
            
            // if we got a message, unpack it and apply the settings as specified.
            setScale(msg.getScale());
            setSpacing(msg.getSpacing());
            setLayout(msg.getLayout());
        }
    }

    public PDFFile getDocument() {
        return this.pdfDocument;
    }

    private void setDocument(PDFFile document) {
        this.pdfDocument = document;
    }
}