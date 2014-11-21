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
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVResultCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVResultSheet;
import org.jdesktop.wonderland.modules.isocial.common.model.query.ResultQuery;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceBase;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceUtils;

/**
 * Resource for managing results
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/results")
public class ResultsResource extends ISocialResourceBase {
    @Context private UriInfo uriInfo;

    @POST
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json", "application/zip"})
    public Response getResults(ResultQuery query) {
        Collection<Result> results = dao().getResults(query);
        Collection<CSVResultSheet> csvResults = ISocialResourceUtils.toCSV(results, dao(), false);
        CSVResultCollection out = new CSVResultCollection(csvResults);
        
        return Response.ok(out).build();
    }

    @POST
    @Path("results.csv.zip")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/zip")
    public Response getResults(@FormParam("cohortId")   String cohortId,
                               @FormParam("studentId")  String studentId,
                               @FormParam("unitId")     String unitId,
                               @FormParam("lessonId")   String lessonId,
                               @FormParam("sheetId")    String sheetId,
                               @FormParam("instanceId") String instanceId)
    {
        ResultQuery query = new ResultQuery();

        if (cohortId != null && cohortId.trim().length() > 0) {
            query.setCohortId(cohortId.trim());
        }

        if (studentId != null && studentId.trim().length() > 0) {
            query.setStudentId(studentId);
        }
        
        if (unitId != null && unitId.trim().length() > 0) {
            query.setUnitId(unitId);
        }

        if (lessonId != null && lessonId.trim().length() > 0) {
            query.setLessonId(lessonId);
        }

        if (sheetId != null && sheetId.trim().length() > 0) {
            query.setSheetId(sheetId);
        }

        if (instanceId != null && instanceId.trim().length() > 0) {
            query.setInstanceId(instanceId);
        }

        Collection<Result> results = dao().getResults(query);
        Collection<CSVResultSheet> csvResults = ISocialResourceUtils.toCSV(results, dao(), true);
        CSVResultCollection out = new CSVResultCollection(csvResults);

        return Response.ok(out).build();
    }

    @GET
    @Path("{instanceId}")
    @Produces({"application/xml", "application/json"})
    public Response getResults(@PathParam("instanceId") String instanceId) {
        Collection<Result> results = dao().getResults(instanceId);
        return Response.ok(new ISocialModelCollection<Result>(results)).build();
    }

    @POST
    @Path("{instanceId}/new")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response newResult(@PathParam("instanceId") String instanceId,
                              Result result)
    {
        // set the instance id
        result.setInstanceId(instanceId);

        if (result.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("New results cannot include an ID").build();
        }

        Result created = dao().addResult(result);
        URI uri = uriInfo.getBaseUriBuilder().path(ResultsResource.class).
                                              path(created.getId()).build();
        return Response.created(uri).entity(created).build();
    }

    @GET
    @Path("{instanceId}/{resultId}")
    @Produces({"application/xml", "application/json"})
    public Response getResult(@PathParam("instanceId") String instanceId,
                              @PathParam("resultId") String resultId)
    {
        Result res = dao().getResult(instanceId, resultId);
        return Response.ok(res).build();
    }

    @POST
    @Path("{instanceId}/{resultId}")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response updateResult(@PathParam("instanceId") final String instanceId,
                                 @PathParam("resultId") final String resultId,
                                 Result result)
    {
        if (!instanceId.equals(result.getInstanceId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + instanceId + " does not match instance id " + result.getInstanceId()).
                    build();
        }

        if (!resultId.equals(result.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + resultId + " does not match result id " +
                           result.getId()).
                    build();
        }

        result = dao().updateResult(result);
        return Response.ok(result).build();
    }

    @DELETE
    @Path("{instanceId}/{resultId}")
    public Response deleteResult(@PathParam("instanceId") String instanceId,
                                 @PathParam("resultId") String resultId)
    {
        dao().removeResult(instanceId, resultId);
        return Response.ok().build();
    }
}
