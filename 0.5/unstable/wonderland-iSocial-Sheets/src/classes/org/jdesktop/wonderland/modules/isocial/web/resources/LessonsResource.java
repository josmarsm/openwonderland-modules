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
import java.util.concurrent.Callable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Lesson;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;

/**
 * Resource for managing lessons, units and sheets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/lessons")
public class LessonsResource extends ISocialResourceBase {    
    @Context private UriInfo uriInfo;

    @GET
    @Path("{unitId}")
    @Produces({"application/xml", "application/json"})
    public Response getLessons(@PathParam("unitId") String unitId) {
        return Response.ok(new ISocialModelCollection<Lesson>(dao().getLessons(unitId))).
                cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{unitId}/new")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response newLesson(@PathParam("unitId") String unitId,
                              Lesson lesson)
    {
        // set the unit id
        lesson.setUnitId(unitId);

        if (lesson.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("New lessons cannot include an ID").build();
        }

        Lesson created = dao().addLesson(lesson);
        return Response.created(uri(created)).entity(created).build();
    }

    @GET
    @Path("{unitId}/{lessonId}")
    @Produces({"application/xml", "application/json"})
    public Response getLesson(@PathParam("unitId") String unitId,
                              @PathParam("lessonId") String lessonId)
    {
        Lesson res = dao().getLesson(unitId, lessonId);
        return Response.ok(res).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{unitId}/{lessonId}")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response updateLesson(@PathParam("unitId") final String unitId,
                                 @PathParam("lessonId") final String lessonId,
                                 Lesson lesson)
    {
        if (!unitId.equals(lesson.getUnitId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + unitId + " does not match unit id " + lesson.getUnitId()).
                    build();
        }

        if (!lessonId.equals(lesson.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + lessonId + " does not match lesson id " +
                           lesson.getId()).
                    build();
        }

        lesson = dao().updateLesson(lesson);
        return Response.ok(lesson).build();
    }

    @POST
    @Path("{unitId}/{lessonId}/copy")
    @Produces({"application/xml", "application/json"})
    public Response copyLesson(@PathParam("unitId") final String unitId,
                               @PathParam("lessonId") final String lessonId)
    {
        try {
            return dao().runTransaction(new Callable<Response>() {
                public Response call() throws Exception {
                    return copyLesson(unitId, lessonId, unitId);
                }
            });
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        }
    }

    @POST
    @Path("{unitId}/{lessonId}/move")
    @Produces({"application/xml", "application/json"})
    public Response moveLesson(@PathParam("unitId") final String unitId,
                               @PathParam("lessonId") final String lessonId,
                               @QueryParam("toUnitId") final String toUnitId)
    {
        if (toUnitId == null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("No toUnitId specified").build();
        }

        try {
            return dao().runTransaction(new Callable<Response>() {
                public Response call() throws Exception {
                    Response resp = copyLesson(unitId, lessonId, toUnitId);
                    if (resp.getStatus() == Response.Status.CREATED.getStatusCode()) {
                        // the new copy was created successfully -- remove
                        // the old copy
                        dao().removeLesson(unitId, lessonId);
                    }
                    return resp;
                }
            });
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        }
    }

    @DELETE
    @Path("{unitId}/{lessonId}")
    public Response deleteLesson(@PathParam("unitId") String unitId,
                                 @PathParam("lessonId") String lessonId)
    {
        dao().removeLesson(unitId, lessonId);
        return Response.ok().build();
    }

    /**
     * Create a URI for a lesson
     * @param sheet the sheet to get a URI for
     */
    private URI uri(Lesson lesson) {
        return uriInfo.getBaseUriBuilder().path(LessonsResource.class).
                path(lesson.getUnitId()).path(lesson.getId()).build();
    }

    /**
     * Copy a lesson, and optionally move it to a new unit
     * @param unitId the original unit
     * @param lessonId the original lesson
     * @param toUnitId the unit to copy the lesson into
     * @return a response indicating whether the operation succeeded
     */
    private Response copyLesson(String unitId, String lessonId, String toUnitId) {
        Lesson orig = dao().getLesson(unitId, lessonId);
        if (orig == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // copy the unit
        Lesson copy = dao().copy(orig);

        if (unitId.equals(toUnitId)) {
            // if we are copying into the same unit, change the name
            copy.setName(orig.getName() + " Copy");
        } else {
            // otherwise set the updated unitId in the copy
            copy.setUnitId(toUnitId);
        }

        copy = dao().addLesson(copy);

        // copy sheets
        for (Sheet sheet : dao().getSheets(unitId, lessonId)) {
            Sheet sheetCopy = dao().copy(sheet);
            sheetCopy.setUnitId(toUnitId);
            sheetCopy.setLessonId(copy.getId());
            dao().addSheet(sheetCopy);
        }

        return Response.created(uri(copy)).entity(copy).build();
    }
}
