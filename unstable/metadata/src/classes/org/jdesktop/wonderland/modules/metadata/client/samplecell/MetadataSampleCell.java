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
package org.jdesktop.wonderland.modules.metadata.client.samplecell;

import org.jdesktop.wonderland.modules.metadata.client.*;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;


/**
 * Client-side cell for rendering JME content
 * adjusted to test Metadata module
 * 
 * @author jkaplan
 * @author mabonner
 */
public class MetadataSampleCell extends Cell {
    /* The type of shape: BOX or SPHERE */
    private String shapeType = null;

    private MetadataSampleRenderer cellRenderer = null;

    @UsesCellComponent MetadataComponent metadataComponent;

    public MetadataSampleCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param clientState
     */
    // @Override
    // public void setClientState(CellClientState clientState) {
    //     super.setClientState(clientState);
    //     shapeType = ((MetadataSampleCellClientState)clientState).getShapeType();
    //     if (cellRenderer != null) {
    //         cellRenderer.updateShape();
    //     }
    // }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            cellRenderer = new MetadataSampleRenderer(this);
            return cellRenderer;
        }
        return super.createCellRenderer(rendererType);
    }

    public String getShapeType() {
        // return shapeType;
        return "SPHERE";
    }


    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        // if (status == CellStatus.ACTIVE) {
        // //    menuComponent.setShowStandardMenuItems(false);
        //     menuComponent.addContextMenuFactory(new SampleContextMenuFactory());
        // }
        // else if (status == CellStatus.DISK) {
        //     // XXX remove menu item, but really don't have to....
        // }
    }

    
}
