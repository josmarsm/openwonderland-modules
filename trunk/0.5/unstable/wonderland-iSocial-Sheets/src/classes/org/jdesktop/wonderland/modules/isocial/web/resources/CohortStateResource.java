/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.web.resources;

import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceBase;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortState;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;

/**
 * Resource for cohort state
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/cohortState")
public class CohortStateResource extends ISocialResourceBase {
    @Context private UriInfo uriInfo;

    @GET
    @Path("{key}")
    @Produces({"application/xml", "application/json"})
    public Response getState(@PathParam("key") String key) {
        Instance cur = getCurrentInstance();
        CohortState state = dao().getCohortState(cur.getCohortId(), key);
        if (state == null) {
            return Response.status(Response.Status.NOT_FOUND).
                    entity("No state for " + key).build();
        }
        
        return Response.ok(state).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{key}")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response setState(@PathParam("key") String key, CohortState state) {
        // set the current cohort
        Instance cur = getCurrentInstance();
        state.setCohortId(cur.getCohortId());
        
        if (!state.getKey().equals(key)) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Key mismatch: " + state.getKey() + " != " + key).
                    build();
        }

        CohortState added = dao().setCohortState(state);
        URI uri = uriInfo.getBaseUriBuilder().path(CohortStateResource.class).
                                              path(added.getKey()).build();
        return Response.created(uri).entity(added).build();
    }
    
    @DELETE
    @Path("{key}")
    public Response deleteResult(@PathParam("key") String key) {
        Instance cur = getCurrentInstance();
        
        dao().removeResult(cur.getCohortId(), key);
        return Response.ok().build();
    }
    
    private Instance getCurrentInstance() {
        Instance out = dao().getCurrentInstance();
        if (out == null) {
            throw new WebApplicationException(Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("No current instance").build());
        }
        
        return out;
    }
}
