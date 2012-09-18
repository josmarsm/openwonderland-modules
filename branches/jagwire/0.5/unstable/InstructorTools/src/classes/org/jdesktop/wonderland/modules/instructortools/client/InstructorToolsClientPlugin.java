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
package org.jdesktop.wonderland.modules.instructortools.client;

import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.instructortools.client.presenters.InstructorToolsPresenter;
import org.jdesktop.wonderland.modules.userlist.client.DefaultUserListListener;
import org.jdesktop.wonderland.modules.userlist.client.HUDTabbedPanel;
import org.jdesktop.wonderland.modules.userlist.client.UserListPresenterManager;
import org.jdesktop.wonderland.modules.userlist.client.UserListCellRenderer;

/**
 *
 * @author Ryan
 */
@Plugin
public class InstructorToolsClientPlugin extends BaseClientPlugin
 implements ViewCellConfiguredListener,
            SessionLifecycleListener,
            SessionStatusListener,
            DefaultUserListListener {
    
    
    public static final String TABBED_PANEL_PROP = 
            "AudioManagerClient.Tabbed.Panel";
    
    private ImageIcon userListIcon = null;
    private static final Logger logger = Logger.getLogger(InstructorToolsClientPlugin.class.getName());
    private InstructorClientConnection connection = null;
    public InstructorToolsClientPlugin() {
        connection = InstructorClientConnection.getInstance();
    }
    
    @Override
    public void initialize(ServerSessionManager loginManager) {
        loginManager.addLifecycleListener(this);
        super.initialize(loginManager);
    }
    
    public void viewConfigured(LocalAvatar localAvatar) {
        logger.warning("ADDING USERLIST LISTENER");
        UserListPresenterManager.INSTANCE.addUserListListener(this);
    }

    public void sessionCreated(WonderlandSession session) {

    }

    public void sessionStatusChanged(WonderlandSession session,
                                    WonderlandSession.Status status) {
        if(status.equals(WonderlandSession.Status.CONNECTED)) {
            connectConnection(session);
        }
    }
    
    public void primarySession(WonderlandSession session) {
        logger.warning("ADDING VIEW CONFIGURED LISTENER");
        LocalAvatar a = ((CellClientSession) session).getLocalAvatar();
        a.addViewCellConfiguredListener(this);
        
        if(session != null) {
            session.addSessionStatusListener(this);
            if(session.getStatus() == WonderlandSession.Status.CONNECTED) {
                connectConnection(session);
            }
        }
    }

    public void listActivated() {
        
        logger.warning("INSTRUCTOR-TOOLS LIST ACTIVATED!");
        
        //If I am logged in as an instructor ('admin' or 'guide')
        if(InstructorSecurityModel.IAmAnInstructor()) {
            InstructorToolsUserList view =
                    new InstructorToolsUserList(new UserListCellRenderer());
            HUDComponent component = makeHUDComponent(view);
            InstructorToolsPresenter presenter =
                    new InstructorToolsPresenter(view, component);


            UserListPresenterManager.INSTANCE.addPresenter("instructortools", presenter);
            UserListPresenterManager.INSTANCE.setActivePresenter("instructortools");
        }
    }
    
    private HUDComponent makeHUDComponent(InstructorToolsUserList view) {
        HUDComponent hc = null;
        userListIcon = new ImageIcon(getClass().getResource(
                    "/org/jdesktop/wonderland/modules/userlist/client/"
                    + "resources/GenericUsers32x32.png"));
        
        HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
//        InstructorToolsUserList view =
//                new InstructorToolsUserList(new UserListCellRenderer());
        
        if(Boolean.parseBoolean(System.getProperty(TABBED_PANEL_PROP))) {
            HUDTabbedPanel tabbedPanel = HUDTabbedPanel.getInstance();
            tabbedPanel.addTab("Users", view);
            tabbedPanel.getTabbedPanel().setSelectedComponent(view);
            hc = main.createComponent(tabbedPanel);
            
        } else {
            hc = main.createComponent(view);
        }
        
        
        hc.setDecoratable(true);
        hc.setPreferredLocation(CompassLayout.Layout.NORTHWEST);
        hc.setIcon(userListIcon);
        hc.setName("Users (0)");
        hc.addEventListener(UserListPresenterManager.INSTANCE);
        main.addComponent(hc);                
        
        return hc;
    }
    
    private void connectConnection(WonderlandSession session) {
        try {
            connection.connect(session);
        } catch(ConnectionFailureException e) {
            logger.warning("Connect Client Error!");
            e.printStackTrace();
        }
    }
}
