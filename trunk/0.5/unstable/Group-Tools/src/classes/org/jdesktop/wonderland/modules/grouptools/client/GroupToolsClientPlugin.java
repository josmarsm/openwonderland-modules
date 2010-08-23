/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.client;

import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.audiomanager.client.HUDTabbedPanel;

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
        
        groupPanel.setControls(HUDTabbedPanel.getInstance().getPresenceControls(),
                HUDTabbedPanel.getInstance().getCell());

        HUDTabbedPanel.getInstance().addTab("groups", groupPanel);
        
        groupPanel.setHUDComponent(
                HUDTabbedPanel.getInstance().getHUDComponent()
                );

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
