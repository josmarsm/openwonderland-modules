/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.hudnavigation.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;

/**
 * Client-side plugin for the HUD navigation buttons
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class HudNavigationClientPlugin extends BaseClientPlugin {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/hudnavigation/client/resources/Bundle");

    // The menu item to add to the Windows menu
    private JMenuItem hudNavigation = null;

    // The HUD Component displaying the navigation controls
    private HUDComponent hudComponent = null;
    private AvatarNavigationJPanel navigationJPanel = null;

    // Listener for changes in the primary view Cell
    private ViewManagerListener viewManagerListener = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // Create the Hud Navigation menu item. Upon activation, create the HUD
        // window that displays the map.
        hudNavigation = new JCheckBoxMenuItem(BUNDLE.getString("Hud_Navigation"));
        hudNavigation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Depending upon whether the checkbox is selected or not,
                // either hide or show the HUD Component and turn on/off the
                // camera taking the snapshots for the map.
                if (hudNavigation.isSelected() == true) {
                    if (hudComponent == null) {
                        hudComponent = createHUDComponent();
                    }
                    hudComponent.setVisible(true);
                }
                else {
                    hudComponent.setVisible(false);
                }
            }
        });

        // Wait until we get a "primary view" Cell. We need one before we can
        // display the navigation controls.
        viewManagerListener = new NavigationViewManagerListener();
        ViewManager.getViewManager().addViewManagerListener(viewManagerListener);
        
        super.initialize(loginInfo);
    }

    /**
     * Creates and returns the top map HUD component.
     */
    private HUDComponent createHUDComponent() {

        // Create the HUD Panel that displays the navigation controls.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        navigationJPanel = new AvatarNavigationJPanel();
        hudComponent = mainHUD.createComponent(navigationJPanel);
        hudComponent.setName(BUNDLE.getString("Hud_Navigation"));
        hudComponent.setPreferredLocation(Layout.SOUTHEAST);
        mainHUD.addComponent(hudComponent);

        // Track when the HUD Component is closed. We need to update the state
        // of the check box menu item too. We also need to turn off the camera.
        hudComponent.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent event) {
                if (event.getEventType() == HUDEventType.CLOSED) {
                    hudNavigation.setSelected(false);
                }
            }
        });
        
        return hudComponent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        // If there is a HUD Component, then remove it from the HUD and clean
        // it up. This really should happen when the primary view cell is
        // disconnected, but we do this here just in case.
        if (hudComponent != null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            mainHUD.removeComponent(hudComponent);
        }

        // Remove the listener from the view manager
        if (viewManagerListener != null) {
            ViewManager vm = ViewManager.getViewManager();
            vm.removeViewManagerListener(viewManagerListener);
            viewManagerListener = null;
        }
    }

    /**
     * Listener for the changes in the primary view cell.
     */
    private class NavigationViewManagerListener implements ViewManagerListener {
        /**
         * {@inheritDoc}
         */
        public void primaryViewCellChanged(ViewCell oldCell, ViewCell newCell) {
            
            // If there is an old Cell, then remove the old HUD component and
            // the main menu item.
            if (oldCell != null) {
                // If there is a HUD Component, then remove it from the HUD and
                // clean it up. This really should happen when the primary view
                // cell is disconnected, but we do this here just in case.
                if (hudComponent != null) {
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                    mainHUD.removeComponent(hudComponent);
                }

                // Remove the menu item
                JmeClientMain.getFrame().removeFromWindowMenu(hudNavigation);
            }

            // If there is a new Cell, the add the menu item
            if (newCell != null) {
                JmeClientMain.getFrame().addToWindowMenu(hudNavigation, -1);
            }
        }
    }
}
