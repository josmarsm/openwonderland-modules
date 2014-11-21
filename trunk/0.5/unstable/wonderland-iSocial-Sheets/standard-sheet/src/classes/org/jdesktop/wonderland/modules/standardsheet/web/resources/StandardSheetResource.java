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
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * WonderBuilders, Inc. designates this particular file as subject to the
 * "Classpath" exception as provided WonderBuilders, Inc. in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.web.resources;

import com.sun.jersey.api.view.Viewable;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceBase;
import org.jdesktop.wonderland.modules.isocial.weblib.resources.ISocialResourceUtils;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardSheet;

/**
 * Resource to provide data to the standard sheet web UI.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/")
public class StandardSheetResource extends ISocialResourceBase {

    @Context HttpServletRequest hsr;
    
    private static final Logger LOGGER = Logger.getLogger(StandardSheetResource.class.getName());

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getView(@QueryParam("unitId") String unitId,
                            @QueryParam("lessonId") String lessonId,
                            @QueryParam("sheetId") String sheetId) 
    {
        SheetBean sheetBean = new SheetBean(unitId, lessonId, sheetId);
        return Response.ok(new Viewable("/edit", sheetBean)).cacheControl(NO_CACHE).build();
    }
    
    @GET
    @Path("{unitId}/{lessonId}/{sheetId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response loadSheet(@PathParam("unitId") String unitId,
                             @PathParam("lessonId") String lessonId,
                             @PathParam("sheetId") String sheetId)
    {
        Sheet res = dao().getSheet(unitId, lessonId, sheetId);
        return Response.ok(res.getDetails()).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("{unitId}/{lessonId}/{sheetId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveSheet(@PathParam("unitId") String unitId,
                              @PathParam("lessonId") String lessonId,
                              @PathParam("sheetId") String sheetId,
                              StandardSheet details) 
    {
        Sheet sheet = dao().getSheet(unitId, lessonId, sheetId);
        sheet.setDetails(details);
        sheet.setPublished(true);
        dao().updateSheet(sheet);
        
        return Response.ok(getSheetURL(sheet)).cacheControl(NO_CACHE).build();
    }
    
    @POST
    @Path("{unitId}/{lessonId}/{sheetId}/duplicate")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response duplicateSheet(@PathParam("unitId") String unitId,
                                   @PathParam("lessonId") String lessonId,
                                   @PathParam("sheetId") String sheetId,
                                   StandardSheet details)
    {
        Sheet sheet = dao().getSheet(unitId, lessonId, sheetId);
        details.setName(details.getName() + " Copy");
        sheet.setDetails(details);
        sheet = dao().addSheet(sheet);
        
        return Response.ok(getSheetURL(sheet)).cacheControl(NO_CACHE).build();
    }
    
    @GET
    @Path("{unitId}/{lessonId}/{sheetId}/cancel")
    @Produces(MediaType.TEXT_PLAIN)
    public Response cancel(@PathParam("unitId") String unitId,
                           @PathParam("lessonId") String lessonId,
                           @PathParam("sheetId") String sheetId)
    {
        Sheet sheet = dao().getSheet(unitId, lessonId, sheetId);
        return Response.ok(getSheetURL(sheet)).cacheControl(NO_CACHE).build();
    }
    
    private String getSheetURL(Sheet sheet) {
        String query = "?expanded=" + sheet.getUnitId() +
                       "&expanded=" + sheet.getUnitId() + "-" + sheet.getLessonId();
        return "/isocial-sheets/isocial-sheets/lessons.jsp" + query;
    }
    
    @Provider
    public static class MyJSONContextResolver extends ISocialResourceUtils.ISocialJSONContextResolver {
        public MyJSONContextResolver() throws Exception {
            super();
        }
    }
    
    @Provider
    public static class MyXMLContextResolver extends ISocialResourceUtils.ISocialXMLContextResolver {
        public MyXMLContextResolver() throws Exception {
            super();
        }
    }
    
    
    public static class SheetBean {
        private final String unitId;
        private final String lessonId;
        private final String sheetId;
        
        public SheetBean(String unitId, String lessonId, String sheetId) {
            this.unitId = unitId;
            this.lessonId = lessonId;
            this.sheetId = sheetId;
        }
        
        public String getUnitId() {
            return unitId;
        }

        public String getLessonId() {
            return lessonId;
        }

        public String getSheetId() {
            return sheetId;
        }
    }
    
}