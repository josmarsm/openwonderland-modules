/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.web.resources;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.admintools.web.AdminToolsServletContainer;
import org.jdesktop.wonderland.modules.admintools.web.AdminToolsWebConnection;

/**
 * Resource that returns information about the users currently connected,
 * and lets administrators disconnect or mute them
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Path("/users")
public class UsersResource {
    private static final Logger LOGGER =
            Logger.getLogger(UsersResource.class.getName());

    // make sure results are not cached
    private static final CacheControl NO_CACHE = new CacheControl();

    @Context
    private ServletContext context;

    static {
        NO_CACHE.setNoCache(true);
    }

    @GET @Path("list")
    @Produces({"application/xml", "application/json"})
    public Response listUsers() {
        AdminToolsWebConnection conn = getConnection();
        if (conn == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        try {
            return Response.ok(conn.getUsers()).cacheControl(NO_CACHE).build();
        } catch (InterruptedException ie) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("{userId}/mute")
    public Response mute(@PathParam("userId") String userId) {
        AdminToolsWebConnection conn = getConnection();
        if (conn == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        conn.forceMute(new BigInteger(userId));
        return Response.ok().build();
    }
    
    @GET @Path("{userId}/errorReport")
    public Response errorReport(@PathParam("userId") String userId,
                                @QueryParam("reportId") String reportId) 
    {
        AdminToolsWebConnection conn = getConnection();
        if (conn == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        conn.forceErrorReport(new BigInteger(userId), reportId);
        return Response.ok().build();
    }

    @GET @Path("{userId}/disconnect")
    public Response disconnect(@PathParam("userId") String userId) {
        AdminToolsWebConnection conn = getConnection();
        if (conn == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        conn.forceDisconnect(new BigInteger(userId));
        return Response.ok().build();
    }

    @POST @Path("broadcast")
    @Consumes("text/plain")
    public Response broadcast(String text) {
        AdminToolsWebConnection conn = getConnection();
        if (conn == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        try {
            conn.broadcast(URLDecoder.decode(text, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
    }

    private AdminToolsWebConnection getConnection() {
        return (AdminToolsWebConnection)
                context.getAttribute(AdminToolsServletContainer.CONNECTION_KEY);
    }
}
