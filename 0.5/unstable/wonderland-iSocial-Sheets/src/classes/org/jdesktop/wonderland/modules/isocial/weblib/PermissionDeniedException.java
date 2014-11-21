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
package org.jdesktop.wonderland.modules.isocial.weblib;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Indicates that permission to perform the given action was denied.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class PermissionDeniedException extends WebApplicationException {
    public PermissionDeniedException() {
        super (Status.FORBIDDEN);
    }

    public PermissionDeniedException(Response response) {
        super (response);
    }

    public PermissionDeniedException(Throwable cause) {
        super (cause, Status.FORBIDDEN);
    }

    public PermissionDeniedException(String reason) {
        super (Response.status(Status.FORBIDDEN).entity(reason).build());
    }
    
    public PermissionDeniedException(Throwable cause, String reason) {
        super (cause, Response.status(Status.FORBIDDEN).entity(reason).build());
    }
    
    public PermissionDeniedException( Throwable cause, Response response) {
        super (cause, response);
    }
}
