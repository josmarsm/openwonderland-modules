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

package org.jdesktop.wonderland.modules.presentationbase.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.presentationbase.client.jme.cell.MovingPlatformCellRenderer;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net
 */
public class PresentationCell extends Cell implements ProximityListener {
    
    MovingPlatformCellRenderer renderer = null;

    private static final Logger logger =
            Logger.getLogger(PresentationCell.class.getName());

    @UsesCellComponent
    private ProximityComponent prox;

    public PresentationCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // Register the platform with the PresentationManager
        // on status changes.
        if(status==CellStatus.ACTIVE && increasing) {

            this.setLocalBounds(new BoundingBox(Vector3f.ZERO, 10.0f, 20.0f, 10.0f));

            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds()};
            prox.addProximityListener(this, bounds);
            logger.warning("Added proximity listener.");

        } else if (status==CellStatus.DISK && !increasing) {

        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        // Don't really do anything here.
            return super.createCellRenderer(rendererType);
    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

        logger.warning("view enter/exit. entered: " + entered);
        
        // Check to see if the avatar entering/exiting is the local one.
        if (cell.getCellCache().getViewCell().getCellID() == viewCellID) {
            if (entered) {
                logger.warning("Local user on platform.");

                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);

                // Do something with the entering avatar.

            } else {
                logger.warning("Local user off platform.");

                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);

                // Do something with the exiting avatar.
            }
        }


    }

    public static void createPresentationSpace(SlidesCell slidesCell) {

        // Do a bunch of exciting things now to do this setup, including
        // getting layout information from the slidesCell.

        logger.warning("Setting up a presentation space for slidesCell: " + slidesCell);

        // Overall steps:
        //
        // 1. Put a toolbar up for everyone that gives them next/previous controls.
        //     (eventually this should be just for the username that created
        //      the file, but it's not clear to me how to do that since this
        //      object contains only local state and isn't synced at all.)


        // 2. Create a presentation platform in front of the first slide, sized
        //    so it is as wide as the slide + the inter-slide space.
        //

        // 3. Tell the PDF spreader to grow itself to contain the whole space
        //    of the presentation.

        // 4. Attach a thought bubbles component to the parent cell.

        // 5. Add buttons to the main presentation toolbar for setting camera
        //    positions (back / top)

    }
}
