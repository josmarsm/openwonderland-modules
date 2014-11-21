/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

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

import java.net.URI;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.isocial.common.model.Cohort;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.query.ResultQuery;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceBase;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceUtils;

/**
 * Resource for managing cohorts
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/cohorts")
public class CohortsResource extends ISocialResourceBase {
    private static final Logger LOGGER =
            Logger.getLogger(CohortsResource.class.getName());

    @Context private UriInfo uriInfo;

    @GET
    @Produces({"application/xml", "application/json"})
    public Response getCohorts() {
        // return the cohorts for the given context
        return Response.ok(new ISocialModelCollection<Cohort>(dao().getCohorts())).
                        cacheControl(NO_CACHE).build();
    }

    @GET
    @Path("{cohortId}")
    @Produces({"application/xml", "application/json"})
    public Response getCohort(@PathParam("cohortId") String cohortId) {
        // find the cohort
        Cohort cohort = dao().getCohort(cohortId);
        if (cohort == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(cohort).build();
    }

    @GET
    @Path("{cohortId}/instances")
    @Produces({"application/xml", "application/json"})
    public Response getCohortInstances(@PathParam("cohortId") String cohortId) {
        // find the cohort
        Cohort cohort = dao().getCohort(cohortId);
        if (cohort == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ISocialModelCollection<Instance> instances =
                new ISocialModelCollection<Instance>(dao().getInstancesForCohort(cohortId));
        return Response.ok(instances).cacheControl(NO_CACHE).build();
    }

    @GET
    @Path("{cohortId}/students")
    @Produces({"application/xml", "application/json"})
    public Response getCohortStudents(@PathParam("cohortId") String cohortId) {
        ResultQuery query = new ResultQuery();
        query.setCohortId(cohortId);

        Set<String> students = new TreeSet<String>();
        for (Result r : dao().getResults(query)) {
            students.add(r.getCreator());
        }

        return Response.ok(new ISocialResourceUtils.StringCollection(students)).
                        cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("/new")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response newCohort(final Cohort cohort) {
        // if the cohort specified an id, return an error
        if (cohort.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("New cohorts cannot include an ID").build();
        }

        Cohort created = dao().addCohort(cohort);
        URI uri = uriInfo.getBaseUriBuilder().path(CohortsResource.class).
                                              path(created.getId()).build();
        return Response.created(uri).entity(created).build();
    }

    @POST
    @Path("{cohortId}")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response updateCohort(@PathParam("cohortId") final String cohortId,
                                 Cohort cohort)
    {
        if (!cohortId.equals(cohort.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Mismatched ids: " + cohortId + " and " +
                           cohort.getId()).build();
        }

        cohort = dao().updateCohort(cohort);
        return Response.ok(cohort).build();
    }

    @DELETE
    @Path("{cohortId}")
    public Response deleteCohort(@PathParam("cohortId") String cohortId) {
        if (dao().removeCohort(cohortId) != null) {
            return Response.ok().build();
        }

        // not found
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
