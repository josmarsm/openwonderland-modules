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
package org.jdesktop.wonderland.modules.metadata.server;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

import org.jdesktop.wonderland.modules.metadata.common.MetadataSampleCellServerState;



/**
 * A sample cell, modified (removed some functionality, add MetadataComponent
 * by default) to test Metadata.
 * @author jkaplan
 * @author mabonner
 */
@ExperimentalAPI
public class MetadataSampleCellMO extends CellMO { 

    /* The shape of the cell: BOX or SPHERE */
    // private String shapeType = null;
    
    @UsesCellComponentMO(MetadataComponentMO.class) private ManagedReference<MetadataComponentMO> componentRef;

    /** Default constructor, used when cell is created via WFS */
    public MetadataSampleCellMO() {
    }

    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.metadata.client.samplecell.MetadataSampleCell";
    }

    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        if (cellServerState == null) {
            cellServerState = new MetadataSampleCellServerState();
        }
        // ((MetadataSampleCellServerState)cellServerState).setShapeType(shapeType);
        return super.getServerState(cellServerState);
    }
}
