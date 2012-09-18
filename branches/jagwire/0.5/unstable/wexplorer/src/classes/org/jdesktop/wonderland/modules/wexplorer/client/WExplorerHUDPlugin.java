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
 * @author jos (edited by Alexios)
 */

package org.jdesktop.wonderland.modules.wexplorer.client;

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.*;


@Plugin
public class WExplorerHUDPlugin extends BaseClientPlugin {

    private JMenuItem explorerHUDMI = null;
    private WExplorerHUD explorerHUD = null;
    private boolean explorerHUDEnabled = false;

    /**
     * Creates a new Menu Item for the HUD that will allow to show/hide it.
     * @param loginInfo
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        explorerHUDMI = new JCheckBoxMenuItem("Warehouse Explorer");
        explorerHUDMI.setSelected(false);
        explorerHUDMI.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                explorerHUDEnabled = !explorerHUDEnabled;
                explorerHUDMI.setSelected(explorerHUDEnabled);
                if (explorerHUD == null) {
                    explorerHUD = new WExplorerHUD();
                } else {
                    explorerHUD.setHudComponentVisible(explorerHUDEnabled);
                }
            }
        });

        super.initialize(loginInfo);
    }

    /**
     * Adds the Menu Item created in initialize to the Window Menu in the
     * Wonderland Client
     */
    @Override
    public void activate() {
        JmeClientMain.getFrame().addToEditMenu(explorerHUDMI);
    }

    /**
     * Removes the Menu Item created in initialize to the Window Menu in the
     * Wonderland Client
     */
    @Override
    public void deactivate() {
        JmeClientMain.getFrame().removeFromEditMenu(explorerHUDMI);
        explorerHUDMI.setSelected(false);
    }
}

class WExplorerHUD {

    private static final Logger logger = Logger.getLogger(WExplorerHUD.class.getName());
    private HUD mainHUD;
    private HUDComponent explorerHud;

    /**
     * Constructor to grab the main HUD area, and display the HUD within it.
     */
    public WExplorerHUD() {
        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        displayHud();
    }

    private void displayHud() {
        createPanelForHUD();
        createHUDComponent();
        setHudComponentVisible(true);
    }

    /**
     * Creates a JPanel which will contain the elements to be shown in the HUD.
     * @return panelForHUD
     */
    private JPanel createPanelForHUD() {
        return new MainPanel();
    }

    /**
     * Creates the HUD Component, if it does not exist yet, and adds it to the
     * CENTER of the main HUD area (entire screen above the 3D scene).
     */
    private void createHUDComponent() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (explorerHud == null) {
                    JPanel panelForHUD = createPanelForHUD();
                    explorerHud = mainHUD.createComponent(panelForHUD);
                    explorerHud.setDecoratable(true);
                    explorerHud.setName("Warehouse Explorer");
                    explorerHud.setPreferredLocation(Layout.CENTER);
                    mainHUD.addComponent(explorerHud);
                }
            }
        });

    }

    /**
     * Changes the visibility of the HUD according to the boolean passed.
     * @param show
     */
    public void setHudComponentVisible(final boolean show) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                explorerHud.setVisible(show);
            }
        });

    }
}
