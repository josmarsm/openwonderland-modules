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

import java.net.URL;
import java.net.URLClassLoader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Web utilities
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialWebUtils {
    public static final String SESSION_KEY = "__iSocialSession";
    public static final String CONNECTION_KEY = "__iSocialConnection";
    public static final String DAO_KEY = "__iSocialDAO";

    public static ThreadLocal<SecurityContext> currentSecurityContext =
            new ThreadLocal<SecurityContext>();

    public static boolean isAdmin(HttpServletRequest request) {
        return isUserInRole(request, "admin");
    }

    public static boolean isGuide(HttpServletRequest request) {
        return isUserInRole(request, "guide");
    }

    public static boolean isStudent(HttpServletRequest request) {
        return isUserInRole(request, "student");
    }

    public static boolean isUserInRole(HttpServletRequest request, String role) {
        return request.isUserInRole(role);
    }

    public static ISocialWebConnection getConnection(ServletContext context) {
        return (ISocialWebConnection)
                context.getAttribute(CONNECTION_KEY);
    }

    public static ISocialDAO getDAO(ServletContext context, SecurityContext security) {
        // set the current security context
        ISocialWebUtils.setSecurityContext(security);

        // return the DAO
        return (ISocialDAO)
                context.getAttribute(DAO_KEY);
    }

    public static ISocialDAO getDAO(ServletContext context, final HttpServletRequest request) {
        // wrap the request into a SecurityContext
        SecurityContext security = new SecurityContext() {
            public Principal getUserPrincipal() {
                return request.getUserPrincipal();
            }

            public boolean isUserInRole(String string) {
                return request.isUserInRole(string);
            }

            public boolean isSecure() {
                return request.isSecure();
            }

            public String getAuthenticationScheme() {
                return request.getAuthType();
            }
        };

        return getDAO(context, security);
    }

    public static void setSecurityContext(SecurityContext context) {
       currentSecurityContext.set(context);
    }

    public static SecurityContext getSecurityContext() {
        SecurityContext out = currentSecurityContext.get();
        if (out == null) {
            return getNoPermissionSecurityContext();
        }
        
        return out;
    }

    public static ScannedClassLoader getScannedClassLoader() {
        ClassLoader cl = ISocialWebUtils.class.getClassLoader();
        if (!(cl instanceof URLClassLoader)) {
            throw new RuntimeException("Not URLClassLoader: " + cl);
        }

        URL[] urls = ((URLClassLoader) cl).getURLs();
        return new ScannedClassLoader(urls, cl);
    }

    public static Collection<Class> getISocialModelTypes() {
        Collection<Class> types = new ArrayList<Class>();

        ScannedClassLoader scl = getScannedClassLoader();
        Set<String> classNames = scl.getClasses(ISocialModel.class);

        for (String className : classNames) {
            try {
                types.add(Class.forName(className));
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            }
        }

        return types;
    }

    public static SecurityContext getSystemSecurityContext() {
        return SecurityContextSingletonHolder.SYSTEM;
    }

    public static SecurityContext getNoPermissionSecurityContext() {
        return SecurityContextSingletonHolder.NO_PERM;
    }

    private static class SecurityContextSingletonHolder {
        private static final SecurityContext SYSTEM = new SecurityContext()
        {
            public Principal getUserPrincipal() {
                    return new Principal() {
                        public String getName() {
                            return "system";
                        }
                    };
                }

                public boolean isUserInRole(String string) {
                    return true;
                }

                public boolean isSecure() {
                    return false;
                }

                public String getAuthenticationScheme() {
                    return null;
                }
        };

        private static final SecurityContext NO_PERM = new SecurityContext()
        {
            public Principal getUserPrincipal() {
                    return new Principal() {
                        public String getName() {
                            return "unauthorized";
                        }
                    };
                }

                public boolean isUserInRole(String string) {
                    return false;
                }

                public boolean isSecure() {
                    return false;
                }

                public String getAuthenticationScheme() {
                    return null;
                }
        };
    }
}
