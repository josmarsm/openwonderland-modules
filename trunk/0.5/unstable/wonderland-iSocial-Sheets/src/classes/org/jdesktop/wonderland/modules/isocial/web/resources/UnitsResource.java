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

import java.util.logging.Logger;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Lesson;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.Unit;

/**
 * Resource for managing lessons, units and sheets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/units")
public class UnitsResource extends ISocialResourceBase {    
    private static final Logger LOGGER =
            Logger.getLogger(UnitsResource.class.getName());

    @Context private UriInfo uriInfo;

    @GET
    @Produces({"application/xml", "application/json"})
    public Response getUnits() {
        return Response.ok(new ISocialModelCollection<Unit>(dao().getUnits())).
                cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("new")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response newUnit(Unit unit) {
        // if the unti specified an id, return an error
        if (unit.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("New units cannot include an ID").build();
        }

        Unit created = dao().addUnit(unit);
        URI uri = uriInfo.getBaseUriBuilder().path(UnitsResource.class).
                                              path(created.getId()).build();
        return Response.created(uri).entity(created).build();
    }

    @GET
    @Path("{unitId}")
    @Produces({"application/xml", "application/json"})
    public Response getUnit(@PathParam("unitId") String id) {
        Unit res = dao().getUnit(id);
        return Response.ok(res).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{unitId}")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response updateUnit(@PathParam("unitId") final String id,
                               Unit unit)
    {
        if (!id.equals(unit.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + id + " does not match unit id " + unit.getId()).
                    build();
        }

        unit = dao().updateUnit(unit);
        return Response.ok(unit).build();
    }

    @POST
    @Path("{unitId}/copy")
    @Produces({"application/xml", "application/json"})
    public Response copyUnit(@PathParam("unitId") final String id)
    {
        try {
            return dao().runTransaction(new Callable<Response>() {
                public Response call() throws Exception {
                    Unit orig = dao().getUnit(id);
                    if (orig == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }

                    // copy the unit
                    Unit copy = dao().copy(orig);
                    copy.setName(orig.getName() + " Copy");
                    copy = dao().addUnit(copy);

                    // copy lessons
                    for (Lesson lesson : dao().getLessons(orig.getId())) {
                        Lesson lessonCopy = dao().copy(lesson);
                        lessonCopy.setUnitId(copy.getId());
                        dao().addLesson(lessonCopy);

                        // copy sheets
                        for (Sheet sheet : dao().getSheets(orig.getId(),
                                                           lesson.getId()))
                        {
                            Sheet sheetCopy = dao().copy(sheet);
                            sheetCopy.setUnitId(copy.getId());
                            sheetCopy.setLessonId(lessonCopy.getId());
                            dao().addSheet(sheetCopy);
                        }
                    }

                    URI uri = uriInfo.getBaseUriBuilder().path(UnitsResource.class).
                                              path(copy.getId()).build();
                    return Response.created(uri).entity(copy).build();
                }
            });
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        }
    }

    @DELETE
    @Path("{unitId}")
    public Response deleteUnit(@PathParam("unitId") String id) {
        dao().removeUnit(id);
        return Response.ok().build();
    }
}
