/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.securitysession.auth.weblib;

/**
 * Utility class for authentication
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class AuthUtils {
    private static final String ALLOW_GUEST_PROP =
            "wonderland.security.allow.guest.login";

    /**
     * Determine if guest login is allowed
     * @return true if guest login is allowed, or false if not
     */
    public static boolean isGuestLoginAllowed() {
        return Boolean.parseBoolean(System.getProperty(ALLOW_GUEST_PROP));
    }
}
