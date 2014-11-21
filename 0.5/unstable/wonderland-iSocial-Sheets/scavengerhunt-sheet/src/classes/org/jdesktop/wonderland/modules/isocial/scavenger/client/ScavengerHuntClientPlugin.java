/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * @author Vladimir Djurovic
 */
@Plugin
public class ScavengerHuntClientPlugin extends BaseClientPlugin {
    
    /** Menu item to show/hide leaderboard. */
    private JCheckBoxMenuItem leaderboardMenuItem;
    
    /** Indicates whether leaderboard is shown or hidden. */
    private boolean leaderboardShown = false;
    
    /**
     * Leaderboard HUD component.
     */
    private LeaderboardHUD leaderboardHUD;

    @Override
    protected void activate() {
        super.activate();
         leaderboardMenuItem = new JCheckBoxMenuItem(new LeaderboardAction());
        JmeClientMain.getFrame().addToWindowMenu(new JMenuItem(new SheetManagerAction()));
        JmeClientMain.getFrame().addToWindowMenu(leaderboardMenuItem);
    }
    
    /**
     * Action to show in-world sheet manager.
     */
    private class SheetManagerAction extends AbstractAction{

        public SheetManagerAction() {
            putValue(NAME, "Sheet Manager");
        }

        /**
         * Display sheet manager when menu item is clicked.
         * @param e 
         */
        public void actionPerformed(final ActionEvent e) {
            WorldSheetManager sheetManager = new WorldSheetManager(JmeClientMain.getFrame().getFrame(), true);
            sheetManager.setLocationRelativeTo(JmeClientMain.getFrame().getFrame());
            sheetManager.setVisible(true);
        }
    }
    
    /**
     * ACtion to show/hide leaderboard.
     */
    private class LeaderboardAction extends AbstractAction {
        
        public LeaderboardAction(){
            putValue(NAME, "Leaderboard");
        }

        public void actionPerformed(ActionEvent e) {
            leaderboardShown  = !leaderboardShown;
            leaderboardMenuItem.setSelected(leaderboardShown);
            if(leaderboardHUD == null){
                leaderboardHUD = new LeaderboardHUD();
            }
            leaderboardHUD.setHUDComponentVisible(leaderboardShown);
        }
    }
}
