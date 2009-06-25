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
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventrecorder.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.spatial.UniverseManagerFactory;

/**
 *
 * @author Bernard Horan
 */
public class EventRecorderCellCacheMO extends ViewCellCacheMO {
    private final static Logger logger = Logger.getLogger(EventRecorderCellCacheMO.class.getName());
    private ManagedReference<EventRecorderImpl> recorderRef;
    /**
     * Creates a new instance of EventRecorderCellCacheMO
     * @param view
     * @param recorderRef
     */
    public EventRecorderCellCacheMO(ViewCellMO view, ManagedReference<EventRecorderImpl> recorderRef) {
        super(view);
        this.recorderRef = recorderRef;
    }

    /**
     * Notify CellCache that user has logged in<br>
     * More or less the same as my superclass, but without actually adding the cell to the world,
     * as that's already happened.
     * @param sender
     * @param clientID
     */
    @Override
    public void login(WonderlandClientSender sender, WonderlandClientID clientID) {
        this.sender = sender;
        this.clientID = clientID;

        ViewCellMO view = viewRef.get();
        UniverseManagerFactory.getUniverseManager().viewLogin(view);

//        logger.info("EventRecorderCellCacheMO.login() CELL CACHE LOGIN FOR USER "
//                    + clientID.getSession().getName() + " AS " + identity.getUsername());

        // set up the revalidate scheduler
        scheduler = new ImmediateRevalidateScheduler(sender, clientID);
    }

    /**
     * Copy of superclass method with the addition of recording. See comment in method.
     * @param cells
     */
    @Override
    protected void sendLoadMessages(Collection<CellDescription> cells) {


        ManagedReference<EventRecorderCellCacheMO> viewCellCacheRef =
                AppContext.getDataManager().createReference(this);

        scheduler.startRevalidate();
        for(CellDescription cellDescription : cells) {
            // if we haven't already loaded the cell, send a message
            if (loaded.add(cellDescription.getCellID())) {

                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Entering cell " + cellDescription.getCellID() +
                                 " cellcache for user " + identity.getUsername());
                }

                CellLoadOp op = new CellLoadOp(cellDescription, clientID,
                                               viewCellCacheRef, capabilities);
                scheduler.schedule(op);
                //Record the loaded cell
                recorderRef.get().recordLoadedCell(cellDescription.getCellID());
            }
        }
        scheduler.endRevalidate();
    }

    /**
     * More or less the same as my superclass with the addition of recording.
     * See comment in method.
     * @param removeCells
     */
    @Override
    public void generateUnloadMessagesService(Collection<CellDescription> removeCells) {
        ManagedReference<? extends ViewCellCacheMO> viewCellCacheRef =
                AppContext.getDataManager().createReference(this);


        scheduler.startRevalidate();
        // oldCells contains the set of cells to be removed from client memory
        for(CellDescription ref : removeCells) {
            if (loaded.remove(ref.getCellID())) {
//                logger.info("Leaving cell " + ref.getCellID() +
//                                " cellcache for user "+identity.getUsername());
                

                // schedule the unload operation
                CellUnloadOp op = new CellUnloadOp(ref, clientID,
                                                   viewCellCacheRef,
                                                   capabilities);
                scheduler.schedule(op);
                //Record the unloaded cell
                recorderRef.get().recordUnloadedCell(ref.getCellID());
            }
        }
        scheduler.endRevalidate();
    }

    /**
     * Notify CellCache that user has logged out<br>
     * More or less the same as my superclass, but doesn't remove the
     * cell from the world.
     * @param clientID
     */
    @Override
    protected void logout(WonderlandClientID clientID) {
        ViewCellMO view = viewRef.get();
        UniverseManagerFactory.getUniverseManager().viewLogout(view);
        recorderRef = null;
    }

    Set<CellID> getLoadedCells() {
        return loaded;
    }

}
