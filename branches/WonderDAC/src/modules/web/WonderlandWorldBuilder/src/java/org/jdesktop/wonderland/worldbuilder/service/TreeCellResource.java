/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.worldbuilder.service;

import java.net.URI;
import java.util.logging.Level;
import org.jdesktop.wonderland.worldbuilder.wrapper.CellWrapper;
import org.jdesktop.wonderland.worldbuilder.wrapper.TreeCellWrapper;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.worldbuilder.Cell;
import org.jdesktop.wonderland.worldbuilder.persistence.CellPersistence;
import org.jdesktop.wonderland.worldbuilder.persistence.UnmodifiedCell;
import org.jdesktop.wonderland.worldbuilder.wrapper.CellRefWrapper;
import org.jdesktop.wonderland.worldbuilder.wrapper.CellsWrapper;

/**
 * REST Web Service
 *
 * @author jkaplan
 */

public class TreeCellResource extends CellResource {
    private static final Logger logger =
            Logger.getLogger(TreeCellResource.class.getName());
    
    
    /** Creates a new instance of CellResource */
    public TreeCellResource(String cellId, UriInfo context) {
        super (cellId, context);
    }
    
    /** Creates a new instance of CellResource */
    public TreeCellResource(Cell cell, UriInfo context) {
        super (cell, context);
    }

    /**
     * Retrieves representation of an instance of org.jdesktop.wonderland.worldbuilder.service.CellResource
     * @param id resource URI parameter
     * @return an instance of java.lang.String
     */
    @GET
    @ProduceMime({"application/xml", "application/json"})
    public CellWrapper get(@QueryParam("depth") @DefaultValue("-1") int depth) {
        return new TreeCellWrapper(getCell(), getContext().getAbsolutePath(), 
                                   depth, true);
    }
    
    @PUT
    @ConsumeMime({"application/xml", "application/json"})
    @Override
    public Response put(CellWrapper data) {
        logger.info("Update tree " + getCell().getCellID());
        
        // make sure the ids match
        if (!data.getCellID().equals(getCell().getCellID())) {
            throw new IllegalArgumentException("Wrong cell ID.  ID was: " + 
                         data.getCellID() + " expected " + 
                         getCell().getCellID());
        }
        
        // make sure the cell's version hasn't changed
        if (data.getVersion() != getCell().getVersion()) {
            throw new IllegalArgumentException("Bad cell version. Version " +
                    "was " + data.getVersion() + " current is " + 
                    getCell().getVersion());
        }
              
        assembleTree(data);
        
        StringBuffer sb = new StringBuffer();
        printTree(data.getCell(), sb, 0);
        logger.info("Tree is:\n" + sb.toString());
        
        // write the changes
        try {
            CellPersistence.get().updateTree(data.getCell());
            return Response.ok().build();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error writing tree " + data.getCellID(),
                       ex);
            return Response.serverError().build();
        }
    }
   
    @Override
    protected CellResource createCellResource(Cell cell) {
        return new TreeCellResource(cell.getCellID(), getContext());
    }
    
    @Override
    protected URI getURI(String cellID) {
        URI baseURI = getContext().getBaseUri();
        return baseURI.resolve("tree/" + cellID);
    }
    
    /**
     * Rebuild the tree of cells from a tree of CellWrappers.  
     * @param cell the cell to assemble
     * @param wrapper the equivalent wrapper
     */
    private void assembleTree(CellWrapper wrapper) {
        CellsWrapper children = wrapper.getChildren();
        if (children != null) {
            
            // add full children
            if (children.getCells() != null) {
                for (CellWrapper child : children.getCells()) {
                    // assemble the child tree
                    assembleTree(child);
                
                    // attach the child
                    wrapper.getCell().addChild(child.getCell());
                }
            }
            
            // replace references to children with instances of
            // UnmodifiedCell
            if (children.getCellRefs() != null) {
                for (CellRefWrapper child : children.getCellRefs()) {
                    // attach child
                    UnmodifiedCell ref = new UnmodifiedCell(child.getCellID());
                    wrapper.getCell().addChild(ref);
                }
            }
        }
    }
    
    private void printTree(Cell cell, StringBuffer sb, int indent) {
        for (int i = 0 ; i < indent; i++) {
            sb.append(' ');
        }
        sb.append(cell.getCellID());
        if (cell instanceof UnmodifiedCell) {
            sb.append(" (unmodified)");
        }
        sb.append("\n");
    
        for (Cell child : cell.getChildren()) {
            printTree(child, sb, indent + 2);
        }
    }
}
