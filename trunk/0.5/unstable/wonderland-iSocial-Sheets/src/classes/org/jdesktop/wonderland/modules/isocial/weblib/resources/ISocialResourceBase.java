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
package org.jdesktop.wonderland.modules.isocial.weblib.resources;

import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialDAO;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebConnection;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebUtils;

/**
 * Base class for ISocial web services
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public abstract class ISocialResourceBase {
    @Context private SecurityContext security;
    @Context private ServletContext context;

    protected static final CacheControl NO_CACHE = new CacheControl();

    static {
        NO_CACHE.setNoCache(true);
    }

    /**
     * Get the security context
     */
    protected SecurityContext getSecurityContext() {
        return security;
    }
    
    /**
     * Get the servlet context
     */
    protected ServletContext getServletContext() {
        return context;
    }

    /**
     * Run in a transaction
     */
    protected Response transaction(Callable<Response> task) {
        try {
            return dao().runTransaction(task);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected ISocialWebConnection connection() {
        return (ISocialWebConnection)
                getServletContext().getAttribute(ISocialWebUtils.CONNECTION_KEY);
    }

    protected ISocialDAO dao() {
        return ISocialWebUtils.getDAO(context, security);
    }
}
