/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.quickreference.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
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

/**
 * Client-side plugin for the quick reference sheet
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class QuickReferenceClientPlugin extends BaseClientPlugin {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(QuickReferenceClientPlugin.class.getName());

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/quickreference/client/resources/Bundle");

    // The menu item to add to the Windows menu
    private JMenuItem menuItem = null;

    // The HUD Component displaying the navigation controls
    private HUDComponent hudComponent = null;
    private QuickReferenceJPanel quickReferenceJPanel = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // Create the Hud Navigation menu item. Upon activation, create the HUD
        // window that displays the map.
        menuItem = new JCheckBoxMenuItem(BUNDLE.getString("Quick_Reference"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Depending upon whether the checkbox is selected or not,
                // either hide or show the HUD Component and turn on/off the
                // camera taking the snapshots for the map.
                if (menuItem.isSelected() == true) {
                    if (hudComponent == null) {
                        hudComponent = createHUDComponent();
                    }
                    hudComponent.setMaximized();
                    hudComponent.setVisible(true);
                }
                else {
                    hudComponent.setVisible(false);
                }
            }
        });
        
        super.initialize(loginInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activate() {
        JmeClientMain.getFrame().addToHelpMenu(menuItem, -1);
        menuItem.setSelected(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        
        // If there is a HUD Component, then make it invisible
        if (hudComponent != null) {
            hudComponent.setVisible(false);
        }

        // Remove the menu item
        JmeClientMain.getFrame().removeFromHelpMenu(menuItem);
    }

    /**
     * Creates and returns the top map HUD component.
     */
    private HUDComponent createHUDComponent() {

        // Create the HUD Panel that displays the navigation controls.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        quickReferenceJPanel = new QuickReferenceJPanel();
        hudComponent = mainHUD.createComponent(quickReferenceJPanel);
        hudComponent.setName(BUNDLE.getString("Quick_Reference"));
        hudComponent.setPreferredLocation(Layout.SOUTHEAST);
        hudComponent.setSize(200, 200);
        mainHUD.addComponent(hudComponent);

        // Track when the HUD Component is closed. We need to update the state
        // of the check box menu item too. We also need to turn off the camera.
        hudComponent.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent event) {
            	HUDEventType hudEventType = event.getEventType();
                if (hudEventType == HUDEventType.CLOSED
                		|| hudEventType == HUDEventType.MINIMIZED) {
                	menuItem.setSelected(false);
                } else 
                if (hudEventType == HUDEventType.MAXIMIZED) {
                	menuItem.setSelected(true);
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
        super.cleanup();
    }
}
