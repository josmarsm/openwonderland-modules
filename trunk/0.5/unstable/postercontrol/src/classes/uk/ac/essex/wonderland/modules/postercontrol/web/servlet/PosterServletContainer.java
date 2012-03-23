/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package uk.ac.essex.wonderland.modules.postercontrol.web.servlet;

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
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarRunner;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLogin.DarkstarServerListener;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLoginFactory;
import uk.ac.essex.wonderland.modules.postercontrol.web.PosterControlConnection;


/**
 * Extension to Jersey servlet adaptor to integrate into Wonderland, so that the adapter (and resource) has access to
 * the connection to the server.
 * @author Bernard Horan
 */
public class PosterServletContainer extends ServletContainer implements ServletContextListener, DarkstarServerListener  {
    private static final Logger logger = Logger.getLogger(PosterServletContainer.class.getName());
    private ServletContext context = null;

    /** the key to identify the connection in the servlet context */
    public static final String POSTER_RESOURCE_CONN_ATTR = "__posterresourceConfigConnection";

    /** the key to identify the darkstar session in the servlet context */
    public static final String SESSION_ATTR = "__posterresourceConfigSession";

    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();
        // add ourselves as a listener for when the Darkstar server changes
        DarkstarWebLoginFactory.getInstance().addDarkstarServerListener(this);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // remove the Darkstar server listener
        DarkstarWebLoginFactory.getInstance().removeDarkstarServerListener(this);

        // log out of any connected sessions
        WonderlandSession session = (WonderlandSession)context.getAttribute(SESSION_ATTR);
        if (session != null) {
            session.logout();
        }
    }

    public void serverStarted(DarkstarRunner runner, ServerSessionManager mgr) {
        // When a darkstar server starts up, open a connection to it, and
        // create the session with the classloader of the current class (the servlet classloader),
        // so that messages will be decoded correctly
        try {
            WonderlandSession session = mgr.createSession(
                    new SessionCreator<WonderlandSession>() {

                        public WonderlandSession createSession(ServerSessionManager sessionManager,
                                WonderlandServerInfo serverInfo,
                                ClassLoader loader) {
                            return new WonderlandSessionImpl(sessionManager,
                                    serverInfo,
                                    getClass().getClassLoader());
                        }
                    });
            context.setAttribute(SESSION_ATTR, session);

            PosterControlConnection conn = new PosterControlConnection();
            session.connect(conn);
            context.setAttribute(POSTER_RESOURCE_CONN_ATTR, conn);
            logger.warning("Login successful");
        } catch (ConnectionFailureException ex) {
            logger.log(Level.SEVERE, "Connection failed", ex);
        } catch (LoginFailureException ex) {
            logger.log(Level.SEVERE, "Login failed", ex);
        }
    }

    public void serverStopped(DarkstarRunner arg0) {
        // When the darkstar server stops, remove the keys from the servlet
        // context
        context.removeAttribute(SESSION_ATTR);
        context.removeAttribute(POSTER_RESOURCE_CONN_ATTR);
    }

}
