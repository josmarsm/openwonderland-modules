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
    protected Vector3f getPositionForIndex(int i) {
//        Vector3f newPosition = new Vector3f(0, -1.0f, (cell.getCenterSpacing() * (i-1) + (cell.getCenterSpacing()*((cell.getNumSlides()-1)/2.0f)*-1)));

        Vector3f newPosition = this.layout.getSlides().get(i).getTransform().getTranslation(null);

//        Vector3f newPosition = new Vector3f((cell.getCenterSpacing() * (i-1)*1) + (cell.getCenterSpacing()*((cell.getNumSlides()-1)))/(-2.0f), -1.5f, 0.0f);
        logger.info("Slide position: " + newPosition);
        
        newPosition = newPosition.mult(this.layout.getScale());

        // make this larger to push the platform further back from the slides.
        // this isn't a function of the scale
//        newPosition.z = 9.0f;
        newPosition.z += this.layout.getMaxSlideWidth() / 2 + 1;

        newPosition.y -= (this.layout.getMaxSlideHeight()/2 + 1);

        logger.info("final position for platform: " + newPosition);
        return newPosition;
    }

//    @Override
//    public void setServerState(CellServerState state) {
//        super.setServerState(state);
//
//        PresentationCellServerState pcsState = (PresentationCellServerState) state;
//
////        // Going to be a little tricksy here. There's a bunch of setup work
////        // we need to do if this cell was just created. To disambiguate
////        // cell creation from a normal unload from the disk, or some other
////        // setServerState situation, we're going to rely on the initialized bit
////        // in the state.
////
////        if(!pcsState.isInitialized()) {
////
////            //////////////////////////////////////////////
////            // Setup process as continued from PresentationCell.createPresentationSpace
////            ////////////////////
////            CellMO pdfCell = CellManagerMO.getCell(pcsState.getSlidesCellID());
////
////            CellTransform pdfCellTransform = pdfCell.getLocalTransform(null);
////
////
////            CellMO slideParent = pdfCell.getParent();
////            if(slideParent==null) {
////                CellManagerMO.getCellManager().removeCellFromWorld(pdfCell);
////            } else {
////                slideParent.removeChild(pdfCell);
////            }
////
////            SlidesCell slidesCell = (SlidesCell)pdfCell;
////
////            // 0. Setup this cell so it's got the same transform that the PDF
////            //    cell used to have, but bigger.
////            BoundingVolume pdfBounds = slidesCell.getPDFBounds();
////            this.setLocalBounds(pdfBounds);
////
////            logger.warning("pdf bounds are: " + pdfBounds);
////
////            this.setLocalTransform(pdfCellTransform);
////
////
////
////
////            // 1. Reparent the PDF cell to be a child of this cell instead.
////            //     (this chunk of code is very similar to
////            //       CellEditConnectionHandler:304 where the REPARENT_CELL
////            //       cell message is implemented. They should probably
////            //       be refactored to be the same common utility method.)
////
////            try {
////
////                PositionComponentServerState posState = new PositionComponentServerState();
////
////                posState.setTranslation(Vector3f.ZERO);
////                posState.setBounds(pdfBounds);
////
////                CellServerState pdfCellState = pdfCell.getServerState(null);
////                pdfCellState.addComponentServerState(posState);
////
////                pdfCell.setServerState(pdfCellState);
////
////                this.addChild(pdfCell);
////
////            } catch (MultipleParentException ex) {
////                logger.info("MultipleParentException while reparenting the slidesCell: " + ex.getLocalizedMessage());
////            }
////
////            // 2. Create a presentation platform in front of the first slide, sized
////            //    so it is as wide as the slide + the inter-slide space. Parent to
////            //    the new PresentationCell.
////
////
////            logger.info("numpages: " + slidesCell.getNumSlides() + " created by: " + slidesCell.getCreatorName());
////
////            // The width of the presentation platform is the width of the slide + one spacing distance.
////            float actualSlideSpacing = slidesCell.getInterslideSpacing();
////            if(actualSlideSpacing < 0) actualSlideSpacing = 0.0f;
////
////            float platformWidth = slidesCell.getMaxSlideWidth() + slidesCell.getInterslideSpacing();
////
////            MovingPlatformCellMO platform = new MovingPlatformCellMO();
////
////            MovingPlatformCellServerState platformState = new MovingPlatformCellServerState();
////            platformState.setPlatformWidth(platformWidth);
////            platformState.setPlatformDepth(8.0f);
////
////            PositionComponentServerState posState = new PositionComponentServerState();
////            posState.setTranslation(getPositionForIndex(slidesCell, curSlide));
////            posState.setScaling(Vector3f.UNIT_XYZ);
////            posState.setRotation(new Quaternion());
////
////            platformState.addComponentServerState(posState);
////            platform.setServerState(platformState);
////
////            try {
////                this.addChild(platform);
////                logger.warning("Just added the platform to the cell.");
////            } catch (MultipleParentException ex) {
////                logger.warning("ERROR ADDING MOVING PLATFORM");
////            }
////
////            // 4. Attach a thought bubbles component to the parent cell.
////
////            // 5. Add buttons to the main presentation toolbar for setting camera
////            //    positions (back / top)
////
////        } else {
////
////        }
//
//    }

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
        this.curSlide = curSlide;

        logger.info("CurrentSlide: " + curSlide);
        // Update the position of the MovingPlatformCell.
        if (this.platformCellMORef != null) {
            logger.info("Updating platform position.");
            MovableComponentMO mc = this.platformCellMORef.get().getComponent(MovableComponentMO.class);

//            logger.info("movable component: " + mc + "; slidesCell: " + this.slidesCellRef + "; resolved: " + this.slidesCellRef.get());
            mc.moveRequest(null, new CellTransform(new Quaternion(), this.getPositionForIndex(curSlide)));
        }
    }

//    private static class PresentationCellChangeMessageReceiver extends AbstractComponentMessageReceiver {
//        public PresentationCellChangeMessageReceiver(PresentationCellMO cellMO) {
//            super(cellMO);
//        }
//
//        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
//            PresentationCellMO cellMO = (PresentationCellMO) getCell();
//            PresentationCellChangeMessage msg = (PresentationCellChangeMessage) message;
//
//
//        }
//    }

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

            // Either a message contains a numPages bit, or the other info.
            // Probably
//            if(msg.getType() == MessageType.DOCUMENT) {
//                if(!cellMO.isDocumentSetup()) {
//                cellMO.setNumPages(msg.getNumPages());
//                cellMO.setSlideWidth(msg.getSlideWidth());
//                logger.warning("Setting document data. numpages: " + msg.getNumPages() + "; slideWidth: " + msg.getSlideWidth());
//                }
            
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
