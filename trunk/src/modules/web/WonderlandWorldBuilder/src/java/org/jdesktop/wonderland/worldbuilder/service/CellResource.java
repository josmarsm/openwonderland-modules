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
import org.jdesktop.wonderland.worldbuilder.persistence.CellPersistence;
import org.jdesktop.wonderland.worldbuilder.wrapper.CellWrapper;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.UriParam;
import org.jdesktop.wonderland.worldbuilder.Cell;

/**
 * REST Web Service
 *
 * @author jkaplan
 */

public class CellResource {
    private static final Logger logger =
            Logger.getLogger(CellResource.class.getName());
    
    /** context */
    private UriInfo context;
    
    /** the underlying cell */
    private Cell cell;
    
    /** Creates a new instance of CellResource */
    public CellResource(String cellId, UriInfo context) {
        this (CellPersistence.get().get(cellId), context);
    }
    
    /** Creates a new instance of CellResource */
    public CellResource(Cell cell, UriInfo context) {
        this.cell = cell;
        this.context = context;
    }

    /**
     * Retrieves representation of an instance of org.jdesktop.wonderland.worldbuilder.service.CellResource
     * @param id resource URI parameter
     * @return an instance of java.lang.String
     */
    @GET
    @ProduceMime({"application/xml", "application/json"})
    public CellWrapper get() {
        return new CellWrapper(cell, getURI(cell.getCellID()));
    }
    
    @PUT
    @POST
    @ConsumeMime({"application/xml", "application/json"})
    public void put(CellWrapper data) {
        logger.info("Update " + cell.getCellID());
        
        // make sure the ids match
        if (!data.getCellID().equals(cell.getCellID())) {
            throw new IllegalArgumentException("Wrong cell ID.  ID was: " + 
                         data.getCellID() + " expected " + cell.getCellID());
        }
        
        // make sure the cell's version hasn't changed
        if (data.getVersion() != cell.getVersion()) {
            throw new IllegalArgumentException("Bad cell version. Version " +
                    "was " + data.getVersion() + " current is " + 
                    cell.getVersion());
        }
               
        CellPersistence.get().update(data.getCell());
    }
    
    /**
     * Sub-resource locator method for parent
     */
    @Path("parent")
    public CellResource getCellResource() {
        return createCellResource(getCell().getParent());
    }
    
    /**
     * Sub-resource locator method for specific child.
     */
    @Path("child/{childIndex}")
    public CellResource getCellResource(@UriParam("childIndex") int childIndex) 
    {
        return createCellResource(getCell().getChildren().get(childIndex));
    }
    
    protected Cell getCell() {
        return cell;
    }
    
    protected CellResource createCellResource(Cell cell) {
        return new CellResource(cell.getCellID(), getContext());
    }
    
    protected UriInfo getContext() {
        return context;
    }
    
    protected URI getURI(String cellID) {
        URI baseURI = context.getBaseUri();
        return baseURI.resolve("cells/" + cellID);
    }
}
