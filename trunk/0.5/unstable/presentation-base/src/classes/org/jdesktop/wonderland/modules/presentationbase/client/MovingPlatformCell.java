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

import com.jme.bounding.BoundingVolume;
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
public class MovingPlatformCell extends Cell implements ProximityListener {
    
    MovingPlatformCellRenderer renderer = null;

    private static final Logger logger =
            Logger.getLogger(MovingPlatformCell.class.getName());

    @UsesCellComponent
    private ProximityComponent prox;

    public MovingPlatformCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

        // Register yourself with the presentation manager.
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // Register the platform with the PresentationManager
        // on status changes.
        if(status==CellStatus.ACTIVE && increasing) {
            PresentationManager.getManager().addPlatform(this);

            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds()};
            prox.addProximityListener(this, bounds);


        } else if (status==CellStatus.DISK && !increasing) {
            PresentationManager.getManager().removePlatform(this);
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new MovingPlatformCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

        // Check to see if the avatar entering/exiting is the local one.
        if (cell.getCellCache().getViewCell().getCellID() == viewCellID) {
            if (entered) {
                logger.warning("Local user on platform.");

                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);
                
                MovingPlatformAvatarComponent mpac = avatar.getComponent(MovingPlatformAvatarComponent.class);
                mpac.addMotionListener(this);

            } else {
                logger.warning("Local user off platform.");

                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);

                MovingPlatformAvatarComponent mpac = avatar.getComponent(MovingPlatformAvatarComponent.class);
                mpac.removeMotionListener(this);
            }
        }


    }

}
