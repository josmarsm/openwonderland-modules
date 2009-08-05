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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.presentationbase.common.MovingPlatformCellServerState;
import org.jdesktop.wonderland.modules.presentationbase.common.PresentationCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net
 */
public class PresentationCellMO extends CellMO {


    

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


        return state;
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


            // 0. Setup this cell so it's got the same transform that the PDF
            //    cell used to have, but bigger.
            this.setLocalTransform(pdfCell.getLocalTransform(null));


            // 1. Reparent the PDF cell to be a child of this cell instead.
            //     (this chunk of code is very similar to
            //       CellEditConnectionHandler:304 where the REPARENT_CELL
            //       cell message is implemented. They should probably
            //       be refactored to be the same common utility method.)

            CellMO slideParent = pdfCell.getParent();

            if(slideParent==null) {
                CellManagerMO.getCellManager().removeCellFromWorld(pdfCell);
            } else {
                slideParent.removeChild(pdfCell);
            }
            try {
                this.addChild(pdfCell);

                // Now move it to 0,0,0 within its new parent cell (which
                // already moved to the PDF cell's location.
                MovableComponentMO mc = pdfCell.getComponent(MovableComponentMO.class);

                if(mc!=null)
                    mc.moveRequest(null, new CellTransform(null, Vector3f.ZERO));
                else
                    logger.warning("NO MOVABLE COMPONENT ON THE PDF CELL");

                this.setLocalBounds(pdfCell.getLocalBounds());
                
            } catch (MultipleParentException ex) {
                logger.info("MultipleParentException while reparenting the slidesCell: " + ex.getLocalizedMessage());
            }

            // 2. Create a presentation platform in front of the first slide, sized
            //    so it is as wide as the slide + the inter-slide space. Parent to
            //    the new PresentationCell.

            SlidesCell slidesCell = (SlidesCell)pdfCell;


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
            posState.setTranslation(new Vector3f(0.0f, 0.0f, 0.0f));
            posState.setScaling(Vector3f.UNIT_XYZ);
            posState.setRotation(new Quaternion());
            
            platformState.addComponentServerState(posState);
            platform.setServerState(platformState);


            // Move the platform into the right position.
//            MovableComponentMO mc = platform.getComponent(MovableComponentMO.class);
//            if(mc!=null)
//                mc.moveRequest(null, new CellTransform(null, new Vector3f(0.0f,0.0f,0.0f)));
//            else
//                logger.warning("NO MOVABLE COMPONENT FOUND ON THE PLATFORM");


            try {
                this.addChild(platform);
            } catch (MultipleParentException ex) {
                Logger.getLogger(PresentationCellMO.class.getName()).log(Level.SEVERE, null, ex);
            }

            // 4. Attach a thought bubbles component to the parent cell.

            // 5. Add buttons to the main presentation toolbar for setting camera
            //    positions (back / top)


        } else {

        }


    }
}
