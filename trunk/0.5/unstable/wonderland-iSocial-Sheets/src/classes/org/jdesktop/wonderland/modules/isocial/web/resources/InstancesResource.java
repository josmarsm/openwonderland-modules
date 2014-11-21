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
import java.util.Collection;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.InstanceRequest;

/**
 * Resource for managing instances
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/instances")
public class InstancesResource extends ISocialResourceBase {
    private static final Logger LOGGER =
            Logger.getLogger(InstancesResource.class.getName());

    @Context private UriInfo uriInfo;

    @GET
    @Produces({"application/xml", "application/json"})
    public Response getInstances(@QueryParam("cohortId") String cohortId) {
        Collection<Instance> instances;
        if (cohortId == null || cohortId.isEmpty()) {
            instances = dao().getInstances();
        } else {
            instances = dao().getInstancesForCohort(cohortId);
        }

        return Response.ok(new ISocialModelCollection<Instance>(instances)).
                cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("new")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response newInstance(InstanceRequest req) {
        Instance created = dao().createInstance(req.getCohortId(),
                                                req.getUnitId(),
                                                req.getLessonId());
        URI uri = uriInfo.getBaseUriBuilder().path(InstancesResource.class).
                                              path(created.getId()).build();
        return Response.created(uri).entity(created).build();
    }

    @GET
    @Path("{instanceId}")
    @Produces({"application/xml", "application/json"})
    public Response getInstance(@PathParam("instanceId") String id) {
        Instance res = dao().getInstance(id);
        return Response.ok(res).cacheControl(NO_CACHE).build();
    }

    @GET
    @Path("current")
    @Produces({"application/xml", "application/json"})
    public Response getCurrentInstance() {
        Instance res = dao().getCurrentInstance();
        return Response.ok(res).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("setCurrent")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response setCurrentInstance(@QueryParam("instanceId") String instanceId) {
        Instance res = dao().setCurrentInstance(instanceId);
        return Response.ok(res).cacheControl(NO_CACHE).build();
    }

    @DELETE
    @Path("{instanceId}")
    public Response deleteInstance(@PathParam("instanceId") String id) {
        dao().removeInstance(id);
        return Response.ok().build();
    }
}
