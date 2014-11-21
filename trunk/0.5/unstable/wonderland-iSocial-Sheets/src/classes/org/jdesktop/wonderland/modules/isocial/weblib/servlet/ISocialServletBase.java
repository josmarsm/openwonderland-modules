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
package org.jdesktop.wonderland.modules.isocial.weblib.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialDAO;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebConnection;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebUtils;

/**
 * Base class for ISocial servlets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialServletBase extends HttpServlet {
    /**
     * Get the connection to the Darkstar server
     * @return the connection to the Darkstar server (may be null if the
     * server is not running).
     */
    protected ISocialWebConnection connection() {
        return (ISocialWebConnection)
                getServletContext().getAttribute(ISocialWebUtils.CONNECTION_KEY);
    }

    /**
     * Get the ISocial DAO
     * @param request the request to use to get the current servlet context
     * @return the DAO, setup for this instance
     */
    protected ISocialDAO dao(HttpServletRequest request) {
        return ISocialWebUtils.getDAO(getServletContext(), request);
    }

    /**
     * Handle a WebApplicatonException by returning an error response. The
     * response is no longer valid after this method is called.
     * @param ex the WebApplicationException
     * @param response the response object
     * @throws IOException if there is an error writing to the response
     */
    protected void handleException(WebApplicationException ex,
                                   HttpServletResponse response)
        throws IOException
    {
        int status = ex.getResponse().getStatus();
        String message = ex.getMessage();

        response.sendError(status, message);
    }
}
