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
package org.jdesktop.wonderland.modules.admintools.client;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuInvocationSettings;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuManager;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Plugin that manages menus for the avatar attachment module
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class AdminToolsClientPlugin extends BaseClientPlugin
    implements ContextMenuActionListener, ViewManager.ViewManagerListener,
               SessionLifecycleListener, SessionStatusListener
{
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsClientPlugin.class.getName());
    private static final ResourceBundle BUNDLE =
                ResourceBundle.getBundle("org.jdesktop.wonderland.modules.admintools.client.Bundle");

    private final ContextMenuListener ctxListener;
    private final SimpleContextMenuItem invisibleItem;
    private final SimpleContextMenuItem visibleItem;
    private final SimpleContextMenuItem disconnectItem;
    private final SimpleContextMenuItem muteItem;

    private final AdminToolsConnection connection;

    // whether or not we are an admin
    private boolean admin = false;
    private boolean adminChecked = false;

    public AdminToolsClientPlugin() {
        ctxListener = new ContextMenuListener() {
            public void contextMenuDisplayed(ContextMenuEvent event) {
                // can we attach components to this cell
                Cell remote = event.getPrimaryCell();
                if (!(remote instanceof AvatarCell)) {
                    return;
                }

                Cell us = ViewManager.getViewManager().getPrimaryViewCell();
                
                // do we need to check if we are an administrator?
                synchronized (AdminToolsClientPlugin.this) {
                    if (!adminChecked) {
                        admin = (us.getComponent(AdminToolsComponent.class) != null);
                    }
                }

                // only enable the menu for administrators
                if (!admin) {
                    return;
                }

                ContextMenuInvocationSettings settings = event.getSettings();

                if (remote == us) {
                    // this is our cell -- add the attachments editor item
                    settings.addTempFactory(new InvisibleContextMenuFactory());
                } else {
                    // this is another cell -- add the other items
                    settings.addTempFactory(new RemoteContextMenuFactory());
                }
            }
        };

        invisibleItem = new SimpleContextMenuItem(BUNDLE.getString("Turn_Invisible"),
                                                  this);
        visibleItem = new SimpleContextMenuItem(BUNDLE.getString("Turn_Visible"),
                                                this);
        disconnectItem = new SimpleContextMenuItem(BUNDLE.getString("Disconnect"),
                                                  this);
        muteItem = new SimpleContextMenuItem(BUNDLE.getString("Mute"), this);

        connection = new AdminToolsConnection();
    }

    protected AdminToolsComponent getTools() {
        Cell us = ViewManager.getViewManager().getPrimaryViewCell();
        return us.getComponent(AdminToolsComponent.class);
    }

    @Override
    protected void activate() {
        ContextMenuManager.getContextMenuManager().addContextMenuListener(ctxListener);

        ClientContextJME.getViewManager().addViewManagerListener(this);
        getSessionManager().addLifecycleListener(this);
    }

    @Override
    protected void deactivate() {
        ContextMenuManager.getContextMenuManager().removeContextMenuListener(ctxListener);

        ClientContextJME.getViewManager().removeViewManagerListener(this);
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

    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
        // recheck if we are an administrator next time a menu is displayed
        synchronized (this) {
            adminChecked = false;
        }
    }

    public void actionPerformed(ContextMenuItemEvent event) {
        if (event.getContextMenuItem() == invisibleItem) {
            getTools().setInvisible(true);
        } else if (event.getContextMenuItem() == visibleItem) {
            getTools().setInvisible(false);
        } else if (event.getContextMenuItem() == disconnectItem) {
            getTools().disconnect(event.getCell().getCellID());
        } else if (event.getContextMenuItem() == muteItem) {
            getTools().mute(event.getCell().getCellID());
        }
    }

    private class InvisibleContextMenuFactory implements ContextMenuFactorySPI {
        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            ContextMenuItem item = getTools().isInvisible()?visibleItem:invisibleItem;

            return new ContextMenuItem[] {
                item
            };
        }
    }

    private class RemoteContextMenuFactory implements ContextMenuFactorySPI {
        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            return new ContextMenuItem[] {
                disconnectItem, muteItem
            };
        }
    }
}
