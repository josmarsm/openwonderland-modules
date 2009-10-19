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
package org.jdesktop.wonderland.modules.presentationbase.server;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.presentationbase.common.MovingPlatformCellServerState;
import org.jdesktop.wonderland.modules.presentationbase.common.PresentationCellChangeMessage;
import org.jdesktop.wonderland.modules.presentationbase.common.PresentationCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationCellMO extends CellMO {

    protected int curSlide = 1;
    
    private ManagedReference<MovingPlatformCellMO> platformCellMORef;
    private ManagedReference<SlidesCell> slidesCellRef;

    // Figure out the right way to persist a reference to the
    // platform cell. 

    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.presentationbase.client.PresentationCell";
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state==null) {
            state = new PresentationCellServerState();
        }


        // This is hardcoded to true. The only way it can be false is
        // if this server state was generated on the client, and does
        // indeed need to be initialized on the server before use. 
        ((PresentationCellServerState)state).setInitialized(true);


        return super.getServerState(state);
    }

    /**
     * This is pulled from the PDF spreader's layout algorithm verbatim. It would be nice
     * for them to both call the same thing, but it feels kind of wrong to move the layout
     * function to a presentation-base util class.
     *
     * @param i
     * @return
     */
    protected Vector3f getPositionForIndex(SlidesCell cell, int i) {
//        Vector3f newPosition = new Vector3f(0, -1.0f, (cell.getCenterSpacing() * (i-1) + (cell.getCenterSpacing()*((cell.getNumSlides()-1)/2.0f)*-1)));
        Vector3f newPosition = new Vector3f((cell.getCenterSpacing() * (i-1)*1) + (cell.getCenterSpacing()*((cell.getNumSlides()-1)))/(-2.0f), -1.5f, 0.0f);
        logger.info("scaling by " + cell.getScale());
        
        newPosition = newPosition.mult(cell.getScale());

        // make this larger to push the platform further back from the slides.
        // this isn't a function of the scale
        newPosition.z = 9.0f;

        logger.info("returning position for platform: " + newPosition);
        return newPosition;
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        
        PresentationCellServerState pcsState = (PresentationCellServerState) state;

        // Going to be a little tricksy here. There's a bunch of setup work
        // we need to do if this cell was just created. To disambiguate
        // cell creation from a normal unload from the disk, or some other
        // setServerState situation, we're going to rely on the initialized bit
        // in the state.

        if(!pcsState.isInitialized()) {

            //////////////////////////////////////////////
            // Setup process as continued from PresentationCell.createPresentationSpace
            ////////////////////
            CellMO pdfCell = CellManagerMO.getCell(pcsState.getSlidesCellID());

            CellTransform pdfCellTransform = pdfCell.getLocalTransform(null);

            
            CellMO slideParent = pdfCell.getParent();
            if(slideParent==null) {
                CellManagerMO.getCellManager().removeCellFromWorld(pdfCell);
            } else {
                slideParent.removeChild(pdfCell);
            }

            SlidesCell slidesCell = (SlidesCell)pdfCell;

            // 0. Setup this cell so it's got the same transform that the PDF
            //    cell used to have, but bigger.
            BoundingVolume pdfBounds = slidesCell.getPDFBounds();
            this.setLocalBounds(pdfBounds);

            logger.warning("pdf bounds are: " + pdfBounds);

            this.setLocalTransform(pdfCellTransform);
            

            

            // 1. Reparent the PDF cell to be a child of this cell instead.
            //     (this chunk of code is very similar to
            //       CellEditConnectionHandler:304 where the REPARENT_CELL
            //       cell message is implemented. They should probably
            //       be refactored to be the same common utility method.)

            try {
                
                PositionComponentServerState posState = new PositionComponentServerState();
                
                posState.setTranslation(Vector3f.ZERO);
                posState.setBounds(pdfBounds);

                CellServerState pdfCellState = pdfCell.getServerState(null);
                pdfCellState.addComponentServerState(posState);

                pdfCell.setServerState(pdfCellState);

                this.addChild(pdfCell);
                
            } catch (MultipleParentException ex) {
                logger.info("MultipleParentException while reparenting the slidesCell: " + ex.getLocalizedMessage());
            }

            // 2. Create a presentation platform in front of the first slide, sized
            //    so it is as wide as the slide + the inter-slide space. Parent to
            //    the new PresentationCell.

            
            logger.info("numpages: " + slidesCell.getNumSlides() + " created by: " + slidesCell.getCreatorName());
            
            // The width of the presentation platform is the width of the slide + one spacing distance.
            float actualSlideSpacing = slidesCell.getInterslideSpacing();
            if(actualSlideSpacing < 0) actualSlideSpacing = 0.0f;

            float platformWidth = slidesCell.getMaxSlideWidth() + slidesCell.getInterslideSpacing();

            MovingPlatformCellMO platform = new MovingPlatformCellMO();

            MovingPlatformCellServerState platformState = new MovingPlatformCellServerState();
            platformState.setPlatformWidth(platformWidth);
            platformState.setPlatformDepth(8.0f);

            PositionComponentServerState posState = new PositionComponentServerState();
            posState.setTranslation(getPositionForIndex(slidesCell, curSlide));
            posState.setScaling(Vector3f.UNIT_XYZ);
            posState.setRotation(new Quaternion());
            
            platformState.addComponentServerState(posState);
            platform.setServerState(platformState);

            try {
                this.addChild(platform);
                logger.warning("Just added the platform to the cell.");
            } catch (MultipleParentException ex) {
                logger.warning("ERROR ADDING MOVING PLATFORM");
            }

            // 4. Attach a thought bubbles component to the parent cell.

            // 5. Add buttons to the main presentation toolbar for setting camera
            //    positions (back / top)

        } else {

        }

    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

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

        // Update the position of the MovingPlatformCell.
        if (this.platformCellMORef != null) {
            logger.info("Updating platform position.");
            MovableComponentMO mc = this.platformCellMORef.get().getComponent(MovableComponentMO.class);
            mc.moveRequest(null, new CellTransform(new Quaternion(), this.getPositionForIndex(this.slidesCellRef.get(), curSlide)));
        }
    }

    private static class PresentationCellChangeMessageReceiver extends AbstractComponentMessageReceiver {
        public PresentationCellChangeMessageReceiver(PresentationCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            PresentationCellMO cellMO = (PresentationCellMO) getCell();
            PresentationCellChangeMessage msg = (PresentationCellChangeMessage) message;

            if (msg.getSlideIncrement() == 1)
                cellMO.setCurSlide(cellMO.getCurSlide() + 1);
            else if (msg.getSlideIncrement() == -1)
                cellMO.setCurSlide(cellMO.getCurSlide() - 1);

        }
    }
}
