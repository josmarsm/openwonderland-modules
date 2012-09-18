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
package org.jdesktop.wonderland.modules.grouptools.client;

import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.userlist.client.HUDTabbedPanel;
import org.jdesktop.wonderland.modules.userlist.client.WonderlandUserList;

/**
 *
 * @author Ryan Babiuch
 */
@Plugin
public class GroupToolsClientPlugin extends BaseClientPlugin {
    private JMenuItem testMenuItem = null;
    private UserHUDPanel panel = null;
    private HUDComponent component = null;
    private GroupListHUDPanel groupPanel = null;
    private GroupChatManager chatManager = null;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // make sure we are using the tabbed HUD panel
        System.setProperty(AudioManagerClient.TABBED_PANEL_PROP, "true");

        //HUD mechanics


        /*HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        panel = new UserHUDPanel();
        component = mainHUD.createComponent(panel);
        component.setName("test - GroupTools");
        component.setPreferredLocation(Layout.NORTHWEST);
        mainHUD.addComponent(component);
        //menu mechanics
        testMenuItem = new JMenuItem("UserHUDPanel");
        testMenuItem.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               //show UserHUDPanel here.
               if(component.isVisible()) {
                   component.setVisible(false);
               }
               else {
                   component.setVisible(true);
               }
           }
        });*/
        super.initialize(loginInfo);
    }

    @Override
    protected void activate() {
        groupPanel = new GroupListHUDPanel();
        WonderlandUserList list = WonderlandUserList.INSTANCE;
        Cell cell = ViewManager.getViewManager().getPrimaryViewCell();
       
        groupPanel.setControls(list.getPresenceControls(),cell);
        

        HUDTabbedPanel.getInstance().addTab("groups", groupPanel);

        chatManager = new GroupChatManager(getSessionManager(), groupPanel);
       // JmeClientMain.getFrame().addToToolsMenu(testMenuItem);
        HUDTabbedPanel.getInstance().getTabbedPanel().setSelectedIndex(0);



    }

    @Override
    public void cleanup() {
        //testMenuItem = null;
        super.cleanup();
        chatManager.unregister();
        chatManager = null;
    }


}
