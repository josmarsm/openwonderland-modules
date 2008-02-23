/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    }

    public void contextDestroyed(ServletContextEvent event) {
        logger.info("Context destroyed -- shutting down persistence");
        CellPersistence.get().shutdown();
    }
}