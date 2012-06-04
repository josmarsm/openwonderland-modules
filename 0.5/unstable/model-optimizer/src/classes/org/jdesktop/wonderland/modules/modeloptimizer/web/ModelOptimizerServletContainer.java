/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.modeloptimizer.web;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.front.admin.AdminRegistration;

/**
 * Jersey servlet context that registers a web page
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ModelOptimizerServletContainer extends ServletContainer
    implements ServletContextListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ModelOptimizerServletContainer.class.getName());

    private static final String REG_KEY = "__modelOptimizerRegistration";

    private ServletContext context;

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();

        // register with the UI
        AdminRegistration ar = new AdminRegistration("Model Optimizer",
                                                     "/model-optimizer/model-optimizer");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);
        context.setAttribute(REG_KEY, ar);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        AdminRegistration ar = (AdminRegistration) context.getAttribute(REG_KEY);
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }
    }
}
