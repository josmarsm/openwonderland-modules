///**
// * Project Wonderland
// *
// * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
// *
// * Redistributions in source code form must reproduce the above
// * copyright and this condition.
// *
// * The contents of this file are subject to the GNU General Public
// * License, Version 2 (the "License"); you may not use this file
// * except in compliance with the License. A copy of the License is
// * available at http://www.opensource.org/licenses/gpl-license.php.
// *
// * Sun designates this particular file as subject to the "Classpath"
// * exception as provided by Sun in the License file that accompanied
// * this code.
// */
//
//package org.jdesktop.wonderland.modules.thoughtbubbles.server;
//
//import com.jme.bounding.BoundingVolume;
//import java.io.Serializable;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.logging.Logger;
//import org.jdesktop.wonderland.common.cell.CellID;
//import org.jdesktop.wonderland.server.WonderlandContext;
//import org.jdesktop.wonderland.server.cell.CellMO;
//import org.jdesktop.wonderland.server.cell.CellManagerMO;
//import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
//import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;
//import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
//import org.jdesktop.wonderland.server.comms.WonderlandClientID;
//
//public class ThoughtBubblesProximityListener implements ProximityListenerSrv, Serializable {
//
//    private static final Logger logger = Logger.getLogger(ThoughtBubblesProximityListener.class.getName());
//
////    Set<WonderlandClientID> clientsInCell = new HashSet<WonderlandClientID>();
//
//    public void viewEnterExit(boolean entered, CellID cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
//
//        // We have to do a little dance here to get a WonderlandClientID out of the
//        // ViewCell system.
//        CellManagerMO cm = WonderlandContext.getCellManager();
//        CellMO cellMO = cm.getCell(viewCellID);
//
//        WonderlandClientID wcid = null;
//
//        if(cellMO instanceof ViewCellMO) {
//            ViewCellMO viewCell = (ViewCellMO)cellMO;
//            ViewCellCacheMO viewCellCache = viewCell.getCellCache();
//            wcid = viewCellCache.getClientID();
//        }
//        else {
//            logger.info("Got a cell that wasn't a ViewCellMO.");
//            return;
//        }
//
//        if(wcid==null) {
//            logger.warning("Couldn't get a wcid for the viewCellID: " + viewCellID);
//            return;
//        }
//
//        // Get our root cell.
//        ThoughtBubblesCellComponentMO pdfSpreaderCell = (ThoughtBubblesCellComponentMO) cm.getCell(cell);
//
//        // We need to filter these. Because we're adding/removing this listener
//        // every time the size of the cell changes, it's triggering new events
//        // every time because it forgets that the person was already there.
//        // So we need to track it here, because this object persists across those
//        // add/remove operations.
//
////        logger.finer("HELLO");
//        // Send messages back to the parent that we have a user entering the cell.
//        if(entered) {
////            if(!clientsInCell.contains(wcid)) {
////                clientsInCell.add(wcid);
//                pdfSpreaderCell.userEnteredCell(wcid);
////            }
//        } else {
////            if(clientsInCell.contains(wcid)) {
////                clientsInCell.remove(wcid);
//                pdfSpreaderCell.userLeftCell(wcid);
////            }
//        }
//
//    }
//
//}