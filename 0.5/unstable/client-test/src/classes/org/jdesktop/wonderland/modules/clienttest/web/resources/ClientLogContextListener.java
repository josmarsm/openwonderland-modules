/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.clienttest.web.resources;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.front.admin.AdminRegistration;

/**
 *
 * @author jkaplan
 */
public class ClientLogContextListener implements ServletContextListener {
    private static final String CLIENT_TEST_LOG_REG_KEY = "__clientTestLogRegistration";

    private ServletContext context;

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();

        // register with the UI
        AdminRegistration ar = new AdminRegistration("Client Test Logs",
                                                     "/client-test/client-test/ClientTestLogs.html");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);
        context.setAttribute(CLIENT_TEST_LOG_REG_KEY, ar);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        AdminRegistration ar = (AdminRegistration) context.getAttribute(CLIENT_TEST_LOG_REG_KEY);
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }
    }
}
