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

import javax.ws.rs.core.SecurityContext;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelBase;

/**
 * Security rules for making changes to ISocial objects
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public interface ISocialSecurityPolicy {
    /** the system property to read to find the provider class */
    public static final String PROVIDER_PROP =
                "org.jdesktop.wonderland.modules.isocial.weblib.ISocialSecurityPolicy.provider";

    /** the class name of the default provider */
    public static final String DEFAULT_PROVIDER =
                "org.jdesktop.wonderland.modules.isocial.weblib.DefaultSecurityPolicyImpl";

    /**
     * Check whether the given context has permission to read the given
     * object. If permission is granted, return normally. If permission is
     * denied, throw a PermissionDeniedException.
     *
     * @param obj the object to check
     * @param context the security context to perform the check in
     * @throws PermissionDeniedException if permission is denied
     */
    public void checkRead(ISocialModelBase obj, SecurityContext context);

    /**
     * Check whether the given context has permission to write the given
     * object. If permission is granted, return normally. If permission is
     * denied, throw a PermissionDeniedException.
     *
     * @param obj the object to check
     * @param context the security context to perform the check in
     * @throws PermissionDeniedException if permission is denied
     */
    public void checkWrite(ISocialModelBase obj, SecurityContext context);
}
