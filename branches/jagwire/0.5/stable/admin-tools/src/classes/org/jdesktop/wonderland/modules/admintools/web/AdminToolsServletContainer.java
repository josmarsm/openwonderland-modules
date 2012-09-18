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
package org.jdesktop.wonderland.modules.admintools.web;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionCreator;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.front.admin.StatusPageRegistration;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarRunner;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLogin.DarkstarServerListener;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLoginFactory;

/**
 * Jersey servlet context that connects the admin tools web connection
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class AdminToolsServletContainer extends ServletContainer
    implements ServletContextListener, DarkstarServerListener
{
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsServletContainer.class.getName());

    public static final String SESSION_KEY = "__adminToolsSession";
    public static final String CONNECTION_KEY = "__adminToolsConnection";
    private static final String ADMIN_REG_KEY = "__adminToolsAdminRegistration";
    private static final String STATUS_REG_KEY = "__adminToolsStatusRegistration";
    
    private ServletContext context;

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();
        DarkstarWebLoginFactory.getInstance().addDarkstarServerListener(this);

        // register with the UI
        AdminRegistration ar = new AdminRegistration("Connected Users",
                                                     "/admin-tools/admin-tools");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);
        context.setAttribute(ADMIN_REG_KEY, ar);
        
        StatusPageRegistration spr =
                new StatusPageRegistration("/admin-tools/admin-tools",
                                           "/status.jspf");
        StatusPageRegistration.register(spr, sce.getServletContext());
        context.setAttribute(STATUS_REG_KEY, spr);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        DarkstarWebLoginFactory.getInstance().removeDarkstarServerListener(this);

        WonderlandSession session =
                (WonderlandSession) context.getAttribute(SESSION_KEY);
        if (session != null) {
            session.logout();
        }

        AdminRegistration ar = (AdminRegistration) context.getAttribute(ADMIN_REG_KEY);
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }
        
        StatusPageRegistration spr = (StatusPageRegistration) context.getAttribute(STATUS_REG_KEY);
        if (spr != null) {
            StatusPageRegistration.unregister(spr, context);
        }
    }

    public void serverStarted(DarkstarRunner dr, ServerSessionManager ssm) {
        try {
            WonderlandSession session = ssm.createSession(
                new SessionCreator<WonderlandSession>() {
                    public WonderlandSession createSession(ServerSessionManager mgr,
                        WonderlandServerInfo serverInfo, ClassLoader loader)
                    {
                        // user our classloader
                        return new WonderlandSessionImpl(mgr, serverInfo,
                                    getClass().getClassLoader());
                    }
                });

            // remember the session
            context.setAttribute(SESSION_KEY, session);

            // create our connection
            AdminToolsWebConnection conn = new AdminToolsWebConnection();
            session.connect(conn);
            context.setAttribute(CONNECTION_KEY, conn);
        } catch (ConnectionFailureException ex) {
            LOGGER.log(Level.SEVERE, "Connection failed", ex);
        } catch (LoginFailureException ex) {
            LOGGER.log(Level.WARNING, "Login failed", ex);
        }
    }

    public void serverStopped(DarkstarRunner dr) {
        context.removeAttribute(SESSION_KEY);
        context.removeAttribute(CONNECTION_KEY);
    }
}
