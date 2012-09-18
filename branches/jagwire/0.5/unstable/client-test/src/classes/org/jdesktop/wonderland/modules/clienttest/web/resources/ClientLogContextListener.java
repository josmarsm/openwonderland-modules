/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
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
import org.jdesktop.wonderland.front.admin.FrontPageRegistration;

/**
 *
 * @author jkaplan
 */
public class ClientLogContextListener implements ServletContextListener {
    private static final String CLIENT_TEST_LOG_REG_KEY = "__clientTestLogRegistration";
    private static final String CLIENT_TEST_FPR_KEY = "__clientTestFrontPageRegistration";

    private ServletContext context;

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();

        // register with the UI
        AdminRegistration ar = new AdminRegistration("Client Test Logs",
                                                     "/client-test/client-test/ClientTestLogs.html");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);
        context.setAttribute(CLIENT_TEST_LOG_REG_KEY, ar);
        
        
        FrontPageRegistration fpr = new FrontPageRegistration("/client-test/client-test", "/front.jspf");
        FrontPageRegistration.register(fpr, context);
        context.setAttribute(CLIENT_TEST_FPR_KEY, fpr);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        AdminRegistration ar = (AdminRegistration) context.getAttribute(CLIENT_TEST_LOG_REG_KEY);
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }

        FrontPageRegistration fpr = (FrontPageRegistration) context.getAttribute(CLIENT_TEST_FPR_KEY);
        if (fpr != null) {
            FrontPageRegistration.unregister(fpr, context);
        } 
    }
}
