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
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.presentationbase.client.jme.cell.MovingPlatformCellRenderer;
import org.jdesktop.wonderland.modules.presentationbase.common.MovingPlatformCellClientState;

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

    protected float platformWidth = 10.0f;
    protected float platformDepth = 10.0f;

    public MovingPlatformCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // Register the platform with the PresentationManager
        // on status changes.
        if(status==CellStatus.ACTIVE && increasing) {
//            PresentationToolbarManager.getManager().addPlatform(this);

            BoundingBox box = new BoundingBox(Vector3f.ZERO, this.platformWidth, 20.0f, this.platformDepth);

            // move it to the same place to trigger a world bounds cache update.
//            MovableComponent mc = this.getComponent(MovableComponent.class);
//            mc.localMoveRequest(this.getLocalTransform());

            // Transform these bounds listeners into world coordinates because proximity listeners seem to not
            // actually check against local coordinates.

//            CellTransform localToWorld = this.getLocalToWorldTransform();
//            Vector3f boundsCenter = localToWorld.transform(Vector3f.ZERO);

//            BoundingVolume worldBounds = this.getLocalBounds();
//
//            localToWorld.transform(worldBounds);
//


            BoundingVolume[] bounds = new BoundingVolume[]{box.clone(null)};

            prox.addProximityListener(this, bounds);

//            logger.warning("Added proximity listener, sending: " + bounds[0] + "; local: " + this.getLocalBounds() + " world: " + this.getWorldBounds());


            // check to see if we have a parent. If we do, phone home.
            if(this.getParent()!=null) {
                if(this.getParent() instanceof PresentationCell) {
                    PresentationCell presentationCell = (PresentationCell)this.getParent();

                    presentationCell.setPlatformCell(this);
                }
            }


        } else if (status==CellStatus.DISK && !increasing) {
//            PresentationToolbarManager.getManager().removePlatform(this);
        }
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        this.platformWidth = ((MovingPlatformCellClientState)state).getPlatformWidth();
        this.platformDepth = ((MovingPlatformCellClientState)state).getPlatformDepth();

        if(platformWidth==0.0f)
            platformWidth = 10.0f;

        if(platformDepth==0.0f)
            platformDepth = 10.0f;

        logger.warning("SETTING PLATFORM WIDTH/DEPTH: " + this.platformWidth + "x" + this.platformDepth);
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

        logger.warning("view enter/exit. entered: " + entered);
        
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

    public float getPlatformWidth() {

        return this.platformWidth;
    }

    public float getPlatformDepth() {
        return platformDepth;
    }
}
