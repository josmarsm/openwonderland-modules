/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.worldbuilder.persistence;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 * @author jkaplan
 */
public class PersistenceContextListener implements ServletContextListener {
    private static final Logger logger =
            Logger.getLogger(PersistenceContextListener.class.getName());
    
    public void contextInitialized(ServletContextEvent event) {
        logger.info("Context initialized");
        CellPersistence.setServletContext(event.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent event) {
        logger.info("Context destroyed -- shutting down persistence");
        CellPersistence cp = CellPersistence.get(false);
        if (cp != null) {
            cp.shutdown();
        }
    }
}