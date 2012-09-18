/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.pdfpresentation.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFLayoutHelper;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFLayoutHelper;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellChangeMessage.MessageType;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellClientState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellServerState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationLayout;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationCellMO extends CellMO {

    protected int curSlide = 0;
    
    private ManagedReference<MovingPlatformCellMO> platformCellMORef;
    private ManagedReference<SlidesCell> slidesCellRef;


    // ************************** //
    // FIELDS FROM PDF SPREADER   //
    // ************************** //
    private String pdfURI;

    // I'm tempted to switch these over to a generic hash, but that complicates
    // our UI situation substantially - we would need an API for providing
    // UI components to set the values so we have a reliable way to edit them.
    // That's enough overhead that I'm content with this setup for now.

    private PresentationLayout layout;

    private String creatorName;

    private float slideWidth;

    @UsesCellComponentMO(MovableComponentMO.class)
    private ManagedReference<MovableComponentMO> moveRef;

    private BoundingBox pdfBounds;

    // Figure out the right way to persist a reference to the
    // platform cell. 

    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.pdfpresentation.client.PresentationCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        this.pdfURI = ((PresentationCellServerState)state).getSourceURI();
        this.creatorName = ((PresentationCellServerState)state).getCreatorName();
        this.layout = ((PresentationCellServerState)state).getLayout();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new PresentationCellServerState();
        }

        ((PresentationCellServerState)state).setSourceURI(pdfURI);
        ((PresentationCellServerState)state).setCreatorName(creatorName);
        ((PresentationCellServerState)state).setLayout(layout);

        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new PresentationCellClientState();

        }

        logger.info("client state requested, sending spacing: " + this.layout.getSpacing() + "; scale: " + this.layout.getScale() + "; layout: " + layout);

        ((PresentationCellClientState)cellClientState).setPdfURI(pdfURI);
        ((PresentationCellClientState)cellClientState).setLayout(layout);

        return super.getClientState(cellClientState, clientID, capabilities);
    }


    /**
     * This is pulled from the PDF spreader's layout algorithm verbatim. It would be nice
     * for them to both call the same thing, but it feels kind of wrong to move the layout
     * function to a presentation-base util class.
     *
     * @param i
     * @return
     */
    protected CellTransform getPositionForIndex(int i) {
        return PDFLayoutHelper.getPlatformPosition(layout, i);
    }


    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        logger.info("Setting PresentationCellMO live: " + live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live) {
            channel.addMessageReceiver(PresentationCellChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver) new PresentationCellChangeMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(PresentationCellChangeMessage.class);
        }
    }
    
    public void setPlatformCellMO(MovingPlatformCellMO cellMO) {
        this.platformCellMORef = AppContext.getDataManager().createReference(cellMO);
    }

    public void setSlidesCell(SlidesCell slidesCell) {
        this.slidesCellRef = AppContext.getDataManager().createReference(slidesCell);
    }



    public int getCurSlide() {
        return curSlide;
    }

    public void setCurSlide(int curSlide) {

        // Clamp the range on possible slide
        // values.
        if(curSlide < 0)
            curSlide = 0;
        else if(curSlide == this.getNumSlides())
            curSlide = this.getNumSlides() -1;

        this.curSlide = curSlide;

        logger.info("CurrentSlide: " + curSlide);
        // Update the position of the MovingPlatformCell.
        if (this.platformCellMORef != null) {
            CellTransform newPlatformPosition = this.getPositionForIndex(curSlide);
            MovableComponentMO mc = this.platformCellMORef.get().getComponent(MovableComponentMO.class);
            mc.moveRequest(null, newPlatformPosition);
            
            logger.warning("Updating platform position: " + newPlatformPosition.getTranslation(null) + ", rot: " + newPlatformPosition.getRotation(null));
        }
    }

    //***********************************//
    // METHODS FROM PDF SPREADER CELL MO //
    //***********************************//

    private static class PresentationCellChangeMessageReceiver extends AbstractComponentMessageReceiver {
        public PresentationCellChangeMessageReceiver(PresentationCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // do something.
            logger.info("RECEIVED MESSAGE");
            PresentationCellMO cellMO = (PresentationCellMO)getCell();

            PresentationCellChangeMessage msg = (PresentationCellChangeMessage)message;

            logger.info("Received PDFSpreader message from client: " + msg.getLayout());
            // when we get a message, change our internal state and send it back to everyone else.
            
            if(msg.getType() == MessageType.LAYOUT) {

                cellMO.setPresentationLayout(msg.getLayout());
                // Pass on only LAYOUT messages, not document.
                // All clients already know document info, that message
                // is just to keep the server in sync.

                // Remove the user who sent it from the list of people to send to.
                Set<WonderlandClientID> clients = sender.getClients();
                clients.remove(clientID);

                sender.send(clients, message);
                logger.info("Sending new layout to clients: " + clients);

                // This is semantically kind of wrong, but triggers
                // a relayout of the platform, which is what we need
                // for now.
                cellMO.setCurSlide(cellMO.getCurSlide());
            } else if(msg.getType() == MessageType.SLIDE_CHANGE) {
                if (msg.getSlideIncrement() == 1)
                    cellMO.setCurSlide(cellMO.getCurSlide() + 1);
                else if (msg.getSlideIncrement() == -1)
                    cellMO.setCurSlide(cellMO.getCurSlide() - 1);
            }

            cellMO.updateBounds();
        }
    }

        public int getNumSlides() {
        return this.layout.getSlides().size();
    }

    public float getInterslideSpacing() {
        // I think we might need to do some math to get this value out. I'm
        // not sure it really corresponds with the spacing variable as written.
        // We need to figure out per-slide width first.

        float interslideSpacing = this.layout.getSpacing() - this.slideWidth;

        if(interslideSpacing < 0)
            interslideSpacing = 0.0f;

        logger.info("InterslideSpacing: " + interslideSpacing);

        return interslideSpacing;
    }

    public float getCenterSpacing() {
        logger.info("centerSpacing: " + this.layout.getSpacing());
        return this.layout.getSpacing();
    }

    public String getCreatorName() {
        return this.creatorName;
    }

    private boolean isDocumentSetup() {
        return this.layout.getSlides()!=null && this.slideWidth != 0;
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
            float width = this.layout.getSlides().size() * (this.getMaxSlideWidth() + this.getInterslideSpacing());
            width *= this.layout.getScale();

            float height = 2*this.getMaxSlideWidth(); // there's absolutely no reason to believe this is true, except that it's guaranteed to be bigger than pretty much any reasonable aspect ratio. Plus, height isn't that important here anyway, as long as the ground is included.
            height *= this.layout.getScale();

            float depth = 20;

            logger.warning("Setting bounds: w " + width + "; h " + height + "; d " + depth);
            this.pdfBounds = (new BoundingBox(new Vector3f(0f, 0f, 0f), width, height, depth));
        }
    }

    public float getScale() {
        return this.layout.getScale();
    }

    public void setPresentationLayout(PresentationLayout layout) {
        this.layout = layout;
    }

    public BoundingBox getPDFBounds() {
        return this.pdfBounds;
    }
}
