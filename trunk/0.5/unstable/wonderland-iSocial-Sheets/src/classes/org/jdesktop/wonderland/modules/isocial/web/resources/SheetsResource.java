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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelCollection;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebUtils;

/**
 * Resource for managing lessons, units and sheets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/sheets")
public class SheetsResource extends ISocialResourceBase {
    private static final Logger LOGGER =
            Logger.getLogger(SheetsResource.class.getName());

    @Context private UriInfo uriInfo;

    @GET
    @Path("{unitId}/{lessonId}")
    @Produces({"application/xml", "application/json"})
    public Response getSheets(@PathParam("unitId") String unitId,
                              @PathParam("lessonId") String lessonId) 
    {
        Collection<Sheet> sheets = dao().getSheets(unitId, lessonId);
        return Response.ok(new ISocialModelCollection<Sheet>(sheets)).
                cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{unitId}/{lessonId}/new")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response newSheet(@PathParam("unitId") String unitId,
                             @PathParam("lessonId") String lessonId,
                             Sheet sheet)
    {
        // set the lessonID and unitID
        sheet.setUnitId(unitId);
        sheet.setLessonId(lessonId);

        if (sheet.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("New units cannot include an ID").build();
        }

        Sheet created = dao().addSheet(sheet);
        return Response.created(uri(created)).entity(created).build();
    }

    @GET
    @Path("{unitId}/{lessonId}/{sheetId}")
    @Produces({"application/xml", "application/json"})
    public Response getSheet(@PathParam("unitId") String unitId,
                             @PathParam("lessonId") String lessonId,
                             @PathParam("sheetId") String sheetId)
    {
        System.out.println("==-- SheetResources --==");
        Sheet res = dao().getSheet(unitId, lessonId, sheetId);
        return Response.ok(res).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{unitId}/{lessonId}/{sheetId}")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response updateSheet(@PathParam("unitId") final String unitId,
                                @PathParam("lessonId") final String lessonId,
                                @PathParam("sheetId") final String sheetId,
                                Sheet sheet)
    {
        if (!unitId.equals(sheet.getUnitId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + unitId + " does not match unit id " +
                           sheet.getUnitId()).
                    build();
        }

        if (!lessonId.equals(sheet.getLessonId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + lessonId + " does not match lesson id " +
                           sheet.getLessonId()).
                    build();
        }

        if (!sheetId.equals(sheet.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("Id " + sheetId + " does not match sheet id " +
                           sheet.getId()).
                    build();
        }

        sheet = dao().updateSheet(sheet);
        return Response.ok(sheet).build();
    }

    @POST
    @Path("{unitId}/{lessonId}/{sheetId}/copy")
    @Produces({"application/xml", "application/json"})
    public Response copySheet(@PathParam("unitId") final String unitId,
                              @PathParam("lessonId") final String lessonId,
                              @PathParam("sheetId") final String sheetId)
    {
        try {
            return dao().runTransaction(new Callable<Response>() {
                public Response call() throws Exception {
                    return copySheet(unitId, lessonId, sheetId,
                                     unitId, lessonId);
                }
            });
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        }
    }

    @POST
    @Path("{unitId}/{lessonId}/{sheetId}/move")
    @Produces({"application/xml", "application/json"})
    public Response moveSheet(@PathParam("unitId") final String unitId,
                              @PathParam("lessonId") final String lessonId,
                              @PathParam("sheetId") final String sheetId,
                              @QueryParam("toUnitId") final String toUnitId,
                              @QueryParam("toLessonId") final String toLessonId)
    {
        if (toUnitId == null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("No toUnitId specified").build();
        }

        if (toLessonId == null) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity("No toLessonId specified").build();
        }

        try {
            return dao().runTransaction(new Callable<Response>() {
                public Response call() throws Exception {
                    Response resp = copySheet(unitId, lessonId, sheetId,
                                              toUnitId, toLessonId);
                    if (resp.getStatus() == Response.Status.CREATED.getStatusCode()) {
                        // the new copy was created successfully -- remove
                        // the old copy
                        dao().removeSheet(unitId, lessonId, sheetId);
                    }
                    return resp;
                }
            });
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        }
    }

    @DELETE
    @Path("{unitId}/{lessonId}/{sheetId}")
    public Response deleteSheet(@PathParam("unitId") String unitId,
                                @PathParam("lessonId") String lessonId,
                                @PathParam("sheetId") String sheetId)
    {
        dao().removeSheet(unitId, lessonId, sheetId);
        return Response.ok().build();
    }

    @GET
    @Path("types")
    @Produces({"application/xml", "application/json"})
    public Response getSheetTypes() {
        SheetTypes types = new SheetTypes();

        ScannedClassLoader scl = ISocialWebUtils.getScannedClassLoader();
        for (Iterator<SheetDetails> sci = scl.getAll(XmlRootElement.class, SheetDetails.class);
             sci.hasNext();)
        {
            SheetDetails sc = sci.next();
            Class clazz = sc.getClass();
            XmlRootElement e = (XmlRootElement) clazz.getAnnotation(XmlRootElement.class);

            SheetType type = new SheetType(sc.getTypeName(), e.name(),
                                           sc.getTypeDescription());
            types.getSheetTypes().add(type);
        }

        return Response.ok(types).cacheControl(NO_CACHE).build();
    }
    
    /**
     * Create a URI for a sheet
     * @param sheet the sheet to get a URI for
     */
    private URI uri(Sheet sheet) {
        return uriInfo.getBaseUriBuilder().path(SheetsResource.class).
                path(sheet.getUnitId()).path(sheet.getLessonId()).
                path(sheet.getId()).build();
    }

    /**
     * Copy a sheet, and optionally move it to a new unit and lesson
     * @param unitId the original unit
     * @param lessonId the original lesson
     * @param sheetId the original sheet
     * @param toUnitId the unit to copy the sheet into
     * @param toLessonId the lesson to copy the sheet into
     * @return a response indicating whether the operation succeeded
     */
    private Response copySheet(String unitId, String lessonId, String sheetId,
                               String toUnitId, String toLessonId)
    {
        Sheet orig = dao().getSheet(unitId, lessonId, sheetId);
        if (orig == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // copy the sheet
        Sheet copy = dao().copy(orig);
        copy.setUnitId(toUnitId);
        copy.setLessonId(toLessonId);
        copy = dao().addSheet(copy);

        return Response.created(uri(copy)).entity(copy).build();
    }

    @XmlRootElement(name="sheet-types")
    @ISocialModel
    public static class SheetTypes {
        private final List<SheetType> types = new ArrayList<SheetType>();

        @XmlElement
        public List<SheetType> getSheetTypes() {
            return types;
        }
    }

    @XmlRootElement(name="sheet-type")
    @ISocialModel
    public static class SheetType {
        private String name;
        private String type;
        private String description;

        public SheetType() {}
        public SheetType(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
