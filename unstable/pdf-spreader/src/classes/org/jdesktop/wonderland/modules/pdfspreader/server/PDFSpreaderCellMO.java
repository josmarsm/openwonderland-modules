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

package org.jdesktop.wonderland.modules.pdfspreader.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ManagedReference;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage.LayoutType;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage.MessageType;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellClientState;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellServerState;
import org.jdesktop.wonderland.modules.presentationbase.server.PresentationCellMO;
import org.jdesktop.wonderland.modules.presentationbase.server.SlidesCell;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

public class PDFSpreaderCellMO extends CellMO implements SlidesCell {

    private static final Logger logger = Logger.getLogger(PDFSpreaderCellMO.class.getName());

    private String pdfURI;

    private float spacing = 4.0f;
    private float scale = 1.0f;
    private LayoutType layout = LayoutType.LINEAR;
    private String creatorName;

    protected int numPages = 0;
    private float slideWidth = 0;

    @UsesCellComponentMO(MovableComponentMO.class)
    private ManagedReference<MovableComponentMO> moveRef;

    private BoundingBox pdfBounds;

    public PDFSpreaderCellMO () {
        super();
    }

    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.pdfspreader.client.PDFSpreaderCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        this.pdfURI = ((PDFSpreaderCellServerState)state).getSourceURI();
        this.scale = ((PDFSpreaderCellServerState)state).getScale();
        this.spacing = ((PDFSpreaderCellServerState)state).getSpacing();
        this.layout = ((PDFSpreaderCellServerState)state).getLayout();
        this.creatorName = ((PDFSpreaderCellServerState)state).getCreatorName();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new PDFSpreaderCellServerState();
        }

        ((PDFSpreaderCellServerState)state).setSourceURI(pdfURI);
        ((PDFSpreaderCellServerState)state).setScale(scale);
        ((PDFSpreaderCellServerState)state).setSpacing(spacing);
        ((PDFSpreaderCellServerState)state).setLayout(layout);
        ((PDFSpreaderCellServerState)state).setCreatorName(creatorName);
        
        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new PDFSpreaderCellClientState();

        }

        logger.info("client state requested, sending spacing: " + spacing + "; scale: " + scale + "; layout: " + layout);

        ((PDFSpreaderCellClientState)cellClientState).setPdfURI(pdfURI);
        ((PDFSpreaderCellClientState)cellClientState).setSpacing(spacing);
        ((PDFSpreaderCellClientState)cellClientState).setScale(scale);
        ((PDFSpreaderCellClientState)cellClientState).setLayout(layout);

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    public void setLayout(LayoutType layout) {
        logger.warning("Setting layout to: " + layout);
        this.layout = layout;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return this.scale;
    }

    public BoundingBox getPDFBounds() {
        return this.pdfBounds;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);


        logger.info("Setting PDFSpreaderCellMO live: " + live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {
            channel.addMessageReceiver(PDFSpreaderCellChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver)new PDFSpreaderCellMessageReceiver(this));

            // Check to see if we should register with a potential PresentationCell parent.
            if(this.getParent() instanceof PresentationCellMO) {{
                ((PresentationCellMO)this.getParent()).setSlidesCell(this);
            }
            }
        }
        else {
            channel.removeMessageReceiver(PDFSpreaderCellChangeMessage.class);
        }
    }

    public int getNumSlides() {
        return this.numPages;
    }

    public float getInterslideSpacing() {
        // I think we might need to do some math to get this value out. I'm
        // not sure it really corresponds with the spacing variable as written.
        // We need to figure out per-slide width first.

        float interslideSpacing = this.spacing - this.slideWidth;

        if(interslideSpacing < 0)
            interslideSpacing = 0.0f;

        logger.info("InterslideSpacing: " + interslideSpacing);

        return interslideSpacing;
    }

    public float getCenterSpacing() {
        logger.info("centerSpacing: " + this.spacing);
        return this.spacing;
    }

    public String getCreatorName() {
        return this.creatorName;
    }

    private void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    private boolean isDocumentSetup() {
        return this.numPages>0 && this.slideWidth>0;
    }

    private void setSlideWidth(float slideWidth) {
        this.slideWidth = slideWidth;
    }

    public float getMaxSlideWidth() {
        return this.slideWidth;
    }

    private void updateBounds() {
    // Using the latest data, figure out what our bounds should be.
        if(isDocumentSetup()) {
            // This formula is of course different for different layouts, but we're only going to implement LINEAR now and use it for everything.
            // we're also going to aim way high on the bounds, because it's better
            // to be too high than too low.
            float width = this.numPages * (this.getMaxSlideWidth() + this.getInterslideSpacing());
            width *= this.scale;
            
            float height = 2*this.getMaxSlideWidth(); // there's absolutely no reason to believe this is true, except that it's guaranteed to be bigger than pretty much any reasonable aspect ratio. Plus, height isn't that important here anyway, as long as the ground is included.
            height *= this.scale;
            
            float depth = 20;

            logger.warning("Setting bounds: w " + width + "; h " + height + "; d " + depth);
            this.pdfBounds = (new BoundingBox(new Vector3f(0f, 0f, 0f), width, height, depth));
        }
    }

    private static class PDFSpreaderCellMessageReceiver extends AbstractComponentMessageReceiver {
        public PDFSpreaderCellMessageReceiver(PDFSpreaderCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // do something.
            PDFSpreaderCellMO cellMO = (PDFSpreaderCellMO)getCell();

            PDFSpreaderCellChangeMessage msg = (PDFSpreaderCellChangeMessage)message;

            logger.info("Received PDFSpreader message from client: " + msg.getLayout() + "; " + msg.getScale() + "; " + msg.getSpacing());
            // when we get a message, change our internal state and send it back to everyone else.

            // Either a message contains a numPages bit, or the other info.
            // Probably
            if(msg.getType() == MessageType.DOCUMENT) {
                if(!cellMO.isDocumentSetup()) {
                cellMO.setNumPages(msg.getNumPages());
                cellMO.setSlideWidth(msg.getSlideWidth());
                logger.warning("Setting document data. numpages: " + msg.getNumPages() + "; slideWidth: " + msg.getSlideWidth());
                }
            } else if(msg.getType() == MessageType.LAYOUT) {
                cellMO.setSpacing(msg.getSpacing());
                cellMO.setScale(msg.getScale());
                cellMO.setLayout(msg.getLayout());

                // Pass on only LAYOUT messages, not document.
                // All clients already know document info, that message
                // is just to keep the server in sync.

                // Remove the user who sent it from the list of people to send to.
                Set<WonderlandClientID> clients = sender.getClients();
                clients.remove(clientID);

                sender.send(clients, message);
            }

            cellMO.updateBounds();
        }
    }

    /**
     * This event is fired by the ProximityListener when an avatar enters this
     * cell.
     *
     * @param wcid The WonderlandClientID of the avatar that entered the cell.
     */
    public void userEnteredCell(WonderlandClientID wcid) {

    }

    /**
     * This event is fired by the ProximityListener when an avatar leaves this
     * cell.
     *
     * @param wcid The WonderlandClientID of the avatar that entered the cell.
     */
    public void userLeftCell(WonderlandClientID wcid) {

    }
}