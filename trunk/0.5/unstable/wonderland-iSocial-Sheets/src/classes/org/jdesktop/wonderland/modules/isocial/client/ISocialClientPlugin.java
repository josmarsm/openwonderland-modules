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
package org.jdesktop.wonderland.modules.isocial.client;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client plugin that manages in-world components of iSocial sheets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Plugin
public class ISocialClientPlugin extends BaseClientPlugin
    implements SessionLifecycleListener, SessionStatusListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ISocialClientPlugin.class.getName());
    private static final ResourceBundle BUNDLE =
                ResourceBundle.getBundle("org.jdesktop.wonderland.modules.isocial.client.Bundle");

    private final ISocialConnection connection;

    public ISocialClientPlugin() {
        connection = new ISocialConnection();
    }

    @Override
    protected void activate() {
        getSessionManager().addLifecycleListener(this);
    }

    @Override
    protected void deactivate() {
        getSessionManager().removeLifecycleListener(this);

        ISocialManager.INSTANCE.cleanup();
    }

    public void sessionCreated(WonderlandSession session) {
    }

    public void primarySession(WonderlandSession session) {
        if (session != null) {
            session.addSessionStatusListener(this);
            if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
                connectConnection(session);
            }
        }
    }

    public void sessionStatusChanged(WonderlandSession session,
                                     WonderlandSession.Status status)
    {
        if (status.equals(WonderlandSession.Status.CONNECTED)) {
            connectConnection(session);
        }
    }

    private void connectConnection(WonderlandSession session) {
        try {
            connection.connect(session);
            
            ISocialManager.INSTANCE.initialize(session.getSessionManager(),
                                               connection);
        } catch (ConnectionFailureException e) {
            LOGGER.log(Level.WARNING, "Connect client error", e);
        }
    }
}
