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
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.modules.presentationbase.client.jme.cell.MovingPlatformCellRenderer;
import org.jdesktop.wonderland.modules.presentationbase.common.PresentationCellServerState;

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
                logger.warning("Local user in presentation space.");

                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);

                // Do something with the entering avatar.

            } else {
                logger.warning("Local user out of presentation space.");

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
        // 0. Put a toolbar up for everyone that gives them next/previous controls.
        //     (eventually this should be just for the username that created
        //      the file, but it's not clear to me how to do that since this
        //      object contains only local state and isn't synced at all.)
        // ------------------ deferring this until the platform is in place ---

        // 1. Create a new PresentationCell, put it in the right place, and
        //    reparent the PDF cell into it. Size it so it contains the PDF cell
        //    plus tons of extra space in front of the slides for the platform.

        // Get a reference to the connection we'll use to send these messages.
        ClientConnection sender = LoginManager.getPrimary().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE);
        
        PresentationCellServerState state = new PresentationCellServerState();

        state.setSlidesCellID(slidesCell.getCellID());
        state.setInitialized(false);


        CellCreateMessage createPresentationCell = new CellCreateMessage(null, state);

        LoginManager.getPrimary().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE).getSession().send(sender, createPresentationCell);

        // This setup process continues on the server side, where the cell
        // figures out how big it is and where it should go in the
        // setServerState method on the just-created new cell.
    }
}
