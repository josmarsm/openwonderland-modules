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
package org.jdesktop.wonderland.modules.emptyweb.web;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarRunner;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLogin.DarkstarServerListener;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLoginFactory;

/**
 * Jersey servlet context that connects the admin tools web connection
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class EmptyServletContainer extends ServletContainer
    implements ServletContextListener, DarkstarServerListener
{
    private static final Logger LOGGER =
            Logger.getLogger(EmptyServletContainer.class.getName());

    private static final String ADMIN_REG_KEY = "__adminToolsAdminRegistration";

    private ServletContext context;

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();
        DarkstarWebLoginFactory.getInstance().addDarkstarServerListener(this);

        // register with the UI
        AdminRegistration ar = new AdminRegistration("Connected Users",
                                                     "/empty-web-module/empty-web-module");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);
        context.setAttribute(ADMIN_REG_KEY, ar);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        DarkstarWebLoginFactory.getInstance().removeDarkstarServerListener(this);

        AdminRegistration ar = (AdminRegistration) context.getAttribute(ADMIN_REG_KEY);
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }
    }

    public void serverStarted(DarkstarRunner dr, ServerSessionManager ssm) {
        
    }

    public void serverStopped(DarkstarRunner dr) {

    }
}
