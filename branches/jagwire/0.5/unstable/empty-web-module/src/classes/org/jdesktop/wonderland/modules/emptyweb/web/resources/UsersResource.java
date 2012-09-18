/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.emptyweb.web.resources;

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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.emptyweb.web.EmptyServletContainer;


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
    
}
