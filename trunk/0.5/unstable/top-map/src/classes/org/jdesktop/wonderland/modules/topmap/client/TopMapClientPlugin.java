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
package org.jdesktop.wonderland.modules.topmap.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.mtgame.WorldManager;
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
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.topmap.client.TopMapJPanel.ElevationListener;

/**
 * Client-side plugin for the top map.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class TopMapClientPlugin extends BaseClientPlugin {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/topmap/client/resources/Bundle");

    // The top map menu item to add to the Windows menu
    private JMenuItem topMapMI = null;

    // The HUD Component displaying the top map, stored as a weak reference
    // to help GC
    private WeakReference<HUDComponent> hudRef = null;

    // Create a new top camera Entity used to capture the scene
    private TopMapCameraEntity topMapEntity = null;

    // Listener for elevation changes in the HUD control
    private MapElevationListener elevationListener = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // Create the Top Map menu item. Upon activation, create the HUD window
        // that displays the map.
        topMapMI = new JCheckBoxMenuItem(BUNDLE.getString("Top_Map"));
        topMapMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Depending upon whether the checkbox is selected or not,
                // either hide or show the HUD Component
                if (topMapMI.isSelected() == true) {
                    if (hudRef == null || hudRef.get() == null) {
                        HUDComponent hudComponent = createHUD();
                        hudRef = new WeakReference<HUDComponent>(hudComponent);
                    }
                    HUDComponent hudComponent = hudRef.get();
                    hudComponent.setVisible(true);
                }
                else {
                    HUDComponent hudComponent = hudRef.get();
                    if (hudComponent != null) {
                        hudComponent.setVisible(false);
                    }
                }
            }
        });

        // Create a new listener for changes in the elevation on the HUD,
        // for later use
        elevationListener = new MapElevationListener();

        super.initialize(loginInfo);
    }

    /**
     * Creates and returns the top map HUD component.
     */
    private HUDComponent createHUD() {
        // Create the HUD Panel that displays the map.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        TopMapJPanel panel = new TopMapJPanel();
        HUDComponent hudComponent = mainHUD.createComponent(panel);
        hudComponent.setName(BUNDLE.getString("Top_Map_Title"));
        hudComponent.setPreferredLocation(Layout.SOUTHEAST);
        mainHUD.addComponent(hudComponent);

        // Track when the HUD Component is closed. We need to update the state
        // of the check box menu item too
        hudComponent.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent event) {
                if (event.getEventType() == HUDEventType.CLOSED) {
                    topMapMI.setSelected(false);
                }
            }
        });

        // Create the Entity that holds the camera and add it to the world
        CaptureJComponent captureComponent = panel.getCaptureJComponent();
        float elevation = panel.getElevation();
        topMapEntity = new TopMapCameraEntity(captureComponent, elevation);
        WorldManager wm = ClientContextJME.getWorldManager();
        wm.addEntity(topMapEntity);

        // Add a listener to the HUD panel for changes in the elevation
        panel.addElevationListener(elevationListener);
        
        return hudComponent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activate() {
        JmeClientMain.getFrame().addToWindowMenu(topMapMI, -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        JmeClientMain.getFrame().removeFromWindowMenu(topMapMI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        // If there is a HUD Component, then remove it from the HUD and clean
        // it up.
        HUDComponent hudComponent = hudRef.get();
        if (hudComponent != null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            mainHUD.removeComponent(hudComponent);
        }
    }

    /**
     * Listener for changes in the elevation on the HUD. Updates the map with
     * the new elevation
     */
    private class MapElevationListener implements ElevationListener {
        /**
         * {@inheritDoc}
         */
        public void elevationChanged(float elevation) {
            topMapEntity.setElevation(elevation);
        }
    }
}
