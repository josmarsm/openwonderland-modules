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

import org.jdesktop.wonderland.worldbuilder.persistence.CellPersistence;
import org.jdesktop.wonderland.worldbuilder.wrapper.CellWrapper;
import org.jdesktop.wonderland.worldbuilder.wrapper.TreeCellWrapper;
import java.net.URI;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.worldbuilder.Cell;

/**
 * REST Web Service
 *
 * @author jkaplan
 */

@Path("/tree")
public class TreeResource {
    private static final Logger logger =
            Logger.getLogger(TreeResource.class.getName());
    
    @Context
    private UriInfo context;
    
    /** Creates a new instance of CellsResource */
    public TreeResource() {
        this (null);
    }
    
    public TreeResource(UriInfo context) {
        this.context = context;
    }
    
    @GET
    @ProduceMime({"application/xml", "application/json"})
    public CellWrapper get(@QueryParam("depth") @DefaultValue("-1") int depth) {
        Cell root = CellPersistence.get().getRoot();
        URI baseURI = context.getAbsolutePath();
        URI rootURI = baseURI.resolve("tree/" + root.getCellID());
        
        return new TreeCellWrapper(root, rootURI, depth, true);
    }
    
    @PUT
    @ConsumeMime({"application/xml", "application/json"})
    public Response put(CellWrapper data) {
        Cell root = CellPersistence.get().getRoot();
        return new TreeCellResource(root.getCellID(), context).put(data);
    }
    
    @POST
    @ConsumeMime({"application/xml", "application/json"})
    public Response post(CellWrapper data) {
        return put(data);
    }
    
    /**
     * Sub-resource locator method for  {id}
     */
    @Path("{cellId}")
    public CellResource getCellResource(@PathParam("cellId") String cellId) {
        return new TreeCellResource(cellId, context);
    }
}
