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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
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

/**
 * Factory class for finding the singleton ISocialWebConnection
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialWebConnectionFactory {
    private static final Logger LOGGER =
            Logger.getLogger(ISocialWebConnectionFactory.class.getName());

    public synchronized static void registerContext(ServletContext context) {
        // if this is the first registration, add the global listener
        if (SingletonHolder.CONTEXTS.isEmpty()) {
            DarkstarWebLoginFactory.getInstance().addDarkstarServerListener(SingletonHolder.LISTENER);
        }

        // add this context to the set
        SingletonHolder.CONTEXTS.add(context);

        // set the current connection value
        context.setAttribute(ISocialWebUtils.SESSION_KEY,
                             SingletonHolder.LISTENER.getSession());
        context.setAttribute(ISocialWebUtils.CONNECTION_KEY,
                             SingletonHolder.LISTENER.getConnection());
    }

    public synchronized static void unregisterContext(ServletContext context) {
        // remove this context from the set
        SingletonHolder.CONTEXTS.remove(context);

        // remove the current connection values
        context.removeAttribute(ISocialWebUtils.SESSION_KEY);
        context.removeAttribute(ISocialWebUtils.CONNECTION_KEY);

        // if this was the last registration, remove the global listener
        if (SingletonHolder.CONTEXTS.isEmpty()) {
            DarkstarWebLoginFactory.getInstance().removeDarkstarServerListener(SingletonHolder.LISTENER);
            
            if (SingletonHolder.LISTENER.getSession() != null) {
                SingletonHolder.LISTENER.getSession().logout();
            }
        }
    }

    public synchronized static void setConnection(WonderlandSession session,
                                                  ISocialWebConnection connection)
    {
        for (ServletContext context : SingletonHolder.CONTEXTS) {
            context.setAttribute(ISocialWebUtils.SESSION_KEY, session);
            context.setAttribute(ISocialWebUtils.CONNECTION_KEY, connection);
        }
    }

    private static class SingletonHolder {
        private static final DarkstarListener LISTENER =
                new DarkstarListener();
        private static final Set<ServletContext> CONTEXTS =
                new LinkedHashSet<ServletContext>();
    }

    private static class DarkstarListener implements DarkstarServerListener {
        private WonderlandSession session;
        private ISocialWebConnection connection;

        public void serverStarted(DarkstarRunner dr, ServerSessionManager ssm) {
            try {
                WonderlandSession s = ssm.createSession(
                        new SessionCreator<WonderlandSession>() {

                            public WonderlandSession createSession(ServerSessionManager mgr,
                                    WonderlandServerInfo serverInfo, ClassLoader loader) {
                                // user our classloader
                                return new WonderlandSessionImpl(mgr, serverInfo,
                                        getClass().getClassLoader());
                            }
                        });

                // create our connection
                ISocialWebConnection conn = new ISocialWebConnection();
                s.connect(conn);

                // store the values
                synchronized (this) {
                    this.session = s;
                    this.connection = conn;
                }
                
                LOGGER.warning("[ISocialWebConnectionFactory] Created connection");

                setConnection(s, conn);
            } catch (ConnectionFailureException ex) {
                LOGGER.log(Level.SEVERE, "Connection failed", ex);
            } catch (LoginFailureException ex) {
                LOGGER.log(Level.WARNING, "Login failed", ex);
            }
        }

        public void serverStopped(DarkstarRunner dr) {
            synchronized (this) {
                session = null;
                connection = null;
            }

            setConnection(null, null);
        }

        private synchronized WonderlandSession getSession() {
            return session;
        }

        private synchronized ISocialWebConnection getConnection() {
            return connection;
        }
    }
}
