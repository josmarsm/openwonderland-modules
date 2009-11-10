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

package org.jdesktop.wonderland.modules.pdfpresentation.client;

import com.sun.pdfview.PDFFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.pdf.client.PDFDeployer;
import org.jdesktop.wonderland.modules.pdfpresentation.client.jme.cell.PDFSpreaderCellRenderer;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFSpreaderCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFSpreaderCellChangeMessage.LayoutType;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFSpreaderCellChangeMessage.MessageType;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFSpreaderCellClientState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationLayout;

/**
 * The client side component of the PDFSpreaderCell. When a user drops a PDF,
 * this is the client code that manages the process.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PDFSpreaderCell extends Cell {

    private PDFSpreaderCellRenderer renderer = null;

    private static final Logger logger =
            Logger.getLogger(PDFSpreaderCell.class.getName());

    private String pdfURI;

    private PDFFile pdfDocument;

    private PresentationLayout layout;

    private String creatorName;

    public PDFSpreaderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    protected void updateLayout() {

        try {
            // On updateLayout, trigger a refresh based on current settings.
            this.layout.setSlides(PDFLayoutHelper.generateLayoutMetadata(this.layout.getLayout(), PDFDeployer.loadDeployedPDF(pdfURI), this.layout.getSpacing()));
        } catch (MalformedURLException ex) {
            Logger.getLogger(PDFSpreaderCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PDFSpreaderCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(PDFSpreaderCell.class.getName()).log(Level.SEVERE, null, ex);
        }

        // WRITE THIS METHOD
        renderer.layoutUpdated();
    }

    protected void sendCurrentLayoutToServer() {
        // This ONLY gets called by the HUD panel, so when we get this call
        // we know that a local change has occured that we need to send
        // to the server.
        PDFSpreaderCellChangeMessage msg = new PDFSpreaderCellChangeMessage(MessageType.LAYOUT);
 
        msg.setLayout(layout);

        this.sendCellMessage(msg);

        logger.finer("just sent cell message to server: " + msg);
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        this.pdfURI = ((PDFSpreaderCellClientState)state).getPdfURI();
        this.layout = ((PDFSpreaderCellClientState)state).getLayout();
        this.creatorName = ((PDFSpreaderCellClientState)state).getCreatorName();
        
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

    public PresentationLayout getLayout() {
        return layout;
    }

    public void setLayout(PresentationLayout layout) {
        logger.finer("Setting layout to: " + layout);
        this.layout = layout;

        // This gets called only when messages come in from other clients
        // who have presumably caused a relayout operation.
        // This means we should update the positions (but not textures
        // or node properties) of all our slides.
    }

    public void setLayoutType(LayoutType layout) {
        this.layout.setLayout(layout);
    }

    public float getScale() {
        return this.layout.getScale();
    }

    public void setScale(float scale) {
        this.layout.setScale(scale);
        logger.finer("Setting scale to: " + scale);
    }

    public float getSpacing() {
        return this.layout.getSpacing();
    }

    public void setSpacing(float spacing) {
        logger.finer("Setting spacing to: " + spacing);
        this.layout.setSpacing(spacing);
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

    public String getCreatorName() {
        return this.creatorName;
    }

    class PDFSpreaderCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            PDFSpreaderCellChangeMessage msg = (PDFSpreaderCellChangeMessage)message;
            
            // if we got a message, grab the layout data and push it into the cell.
            
            setLayout(msg.getLayout());
        }
    }

//    public PDFFile getDocument() {
//        return this.pdfDocument;
//    }
//
//    private void setDocument(PDFFile document) {
//        this.pdfDocument = document;
//    }
}