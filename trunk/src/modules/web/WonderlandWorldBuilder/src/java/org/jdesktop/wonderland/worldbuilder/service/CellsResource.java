/*
 *  CellsResource
 *
 * Created on February 19, 2008, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.worldbuilder.service;

import org.jdesktop.wonderland.worldbuilder.persistence.CellPersistence;
import org.jdesktop.wonderland.worldbuilder.wrapper.CellWrapper;
import java.net.URI;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import org.jdesktop.wonderland.worldbuilder.Cell;

/**
 * REST Web Service
 *
 * @author jkaplan
 */

@Path("/cells")
public class CellsResource {
    private static final Logger logger =
            Logger.getLogger(CellsResource.class.getName());
    
    @HttpContext
    private UriInfo context;
    
    /** Creates a new instance of CellsResource */
    public CellsResource() {
        this (null);
    }
    
    public CellsResource(UriInfo context) {
        this.context = context;
    }
    
    @GET
    @ProduceMime({"application/xml", "application/json"})
    public CellWrapper get() {
        Cell root = CellPersistence.get().getRoot();
        URI baseURI = context.getAbsolutePath();
        URI rootURI = baseURI.resolve("cells/" + root.getCellID());
        
        return new CellWrapper(root, rootURI);
    }
    
    @PUT
    @ConsumeMime({"application/xml", "application/json"})
    public void put(CellWrapper data) {
        Cell root = CellPersistence.get().getRoot();
        new CellResource(root.getCellID(), context).put(data);
    }
    
    /**
     * Sub-resource locator method for  {id}
     */
    @Path("{cellId}")
    public CellResource getCellResource(@UriParam("cellId") String cellId) {
        return new CellResource(cellId, context);
    }
}
