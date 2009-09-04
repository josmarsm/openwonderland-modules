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

package org.jdesktop.wonderland.modules.proximitytest.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.awt.Color;
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
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.proximitytest.client.jme.cell.PlatformCellRenderer;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PlatformCell extends Cell implements ProximityListener {
    
    PlatformCellRenderer renderer = null;

    private static final Logger logger =
            Logger.getLogger(PlatformCell.class.getName());

    @UsesCellComponent
    private ProximityComponent prox;

    protected float platformWidth = 5.0f;
    protected float platformDepth = 5.0f;

    public PlatformCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if(status==CellStatus.ACTIVE && increasing) {

            this.setLocalBounds(new BoundingBox(Vector3f.ZERO, this.platformWidth, 20.0f, this.platformDepth));

            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds()};

            prox.addProximityListener(this, bounds);

            logger.warning("Added proximity listener, sending: " + bounds[0] + "; local: " + this.getLocalBounds() + " world: " + this.getWorldBounds());

        } else if (status==CellStatus.DISK && !increasing) {


        }
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
    }

    

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new PlatformCellRenderer(this);
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
                logger.warning("++++++++++++++++Local user on platform.+++++++++++++++++");
                renderer.setColor(Color.red);
            } else {
                logger.warning("-----------------Local user off platform.-----------------");
                renderer.setColor(Color.gray);
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
