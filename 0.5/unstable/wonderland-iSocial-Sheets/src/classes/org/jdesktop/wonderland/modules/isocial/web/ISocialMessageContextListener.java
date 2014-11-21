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
package org.jdesktop.wonderland.modules.isocial.web;

import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelBase;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialDAO;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialDAOListener;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebConnection;
import org.jdesktop.wonderland.modules.isocial.weblib.ISocialWebUtils;
import org.jdesktop.wonderland.modules.isocial.weblib.servlet.ISocialContextListener;

/**
 * Extends the ISocialContextListener to send messages about relevant
 * changes.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialMessageContextListener extends ISocialContextListener {
    private static final Logger LOGGER =
            Logger.getLogger(ISocialMessageContextListener.class.getName());
    
    private static final String ADAPER_KEY = "__DAOMessageAdapter";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);

        ServletContext context = sce.getServletContext();

        // create and store the adapter
        DAOMessageAdapter adapter = new DAOMessageAdapter(context);
        context.setAttribute(ADAPER_KEY, adapter);

        // add it as a listener to the DAO
        ISocialDAO dao = (ISocialDAO)
                sce.getServletContext().getAttribute(ISocialWebUtils.DAO_KEY);
        dao.addDAOListener(adapter);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // get the adapter
        DAOMessageAdapter adapter = (DAOMessageAdapter)
                context.getAttribute(ADAPER_KEY);
        if (adapter != null) {
            context.removeAttribute(ADAPER_KEY);

            // remove the listener from the DAO
            ISocialDAO dao = (ISocialDAO)
                sce.getServletContext().getAttribute(ISocialWebUtils.DAO_KEY);
            dao.removeDAOListener(adapter);
        }

        super.contextDestroyed(sce);
    }

    private static class DAOMessageAdapter implements ISocialDAOListener {
        private final ServletContext context;

        public DAOMessageAdapter(ServletContext context) {
            this.context = context;
        }

        public void added(ISocialModelBase obj) {
            LOGGER.warning("[DOMMessageAdapter] added " + obj);
            
            if (obj instanceof Result) {
                String resultId = ((Result) obj).getId();

                ISocialWebConnection conn = getConnection();
                LOGGER.warning("[DOMMessageAdapter] sending result to " + conn);
                if (conn != null) {
                    conn.resultAdded(resultId);
                }
            }
        }

        public void updated(ISocialModelBase oldObj, ISocialModelBase newObj) {
            if (newObj instanceof Result) {
                String resultId = ((Result) newObj).getId();

                ISocialWebConnection conn = getConnection();
                if (conn != null) {
                    conn.resultUpdated(resultId);
                }
            }
        }

        public void removed(ISocialModelBase obj) {
        }

        public void currentInstanceChanged(String instanceId) {
            ISocialWebConnection conn = getConnection();
            if (conn != null) {
                conn.currentInstanceChanged(instanceId);
            }
        }

        private ISocialWebConnection getConnection() {
            return (ISocialWebConnection)
                    context.getAttribute(ISocialWebUtils.CONNECTION_KEY);
        }
    }
}
