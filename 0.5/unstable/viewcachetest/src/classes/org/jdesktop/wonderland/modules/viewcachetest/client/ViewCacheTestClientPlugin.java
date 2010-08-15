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

package org.jdesktop.wonderland.modules.viewcachetest.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class ViewCacheTestClientPlugin extends BaseClientPlugin
    implements SessionStatusListener, SessionLifecycleListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ViewCacheTestClientPlugin.class.getName());

    private final ViewCacheTestConnection connection;
    private final JMenuItem menu;

    public ViewCacheTestClientPlugin() {
        connection = new ViewCacheTestConnection();

        menu = new JMenuItem("View Cache Test");
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFrame jf = new ViewCacheTestJFrame(connection);
                jf.pack();
                jf.setVisible(true);
            }
        });
    }


    @Override
    protected void activate() {
        JmeClientMain.getFrame().addToToolsMenu(menu);
        getSessionManager().addLifecycleListener(this);
    }

    @Override
    protected void deactivate() {
        JmeClientMain.getFrame().removeFromToolsMenu(menu);
        getSessionManager().removeLifecycleListener(this);
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
        } catch (ConnectionFailureException e) {
            LOGGER.log(Level.WARNING, "Connect client error", e);
        }
    }
}
