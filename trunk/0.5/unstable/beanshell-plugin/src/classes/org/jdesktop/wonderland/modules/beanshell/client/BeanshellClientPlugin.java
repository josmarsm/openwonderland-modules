/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.beanshell.client;

import bsh.EvalError;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import bsh.Interpreter;
import bsh.util.JConsole;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

/**
 * Client-side plugin for the bean shell.
 * 
 * @author Bernard Horan (adapted from Jordan's top map)
 */
@Plugin
public class BeanshellClientPlugin extends BaseClientPlugin {

    // The error logger
    private static Logger pluginLogger =
            Logger.getLogger(BeanshellClientPlugin.class.getName());

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/beanshell/client/resources/Bundle");
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/beanshell/client/resources/Bundle");

    // The beanshell menu item to add to the Tools menu
    private JMenuItem beanShellMI = null;

    // The HUD Component displaying the beanshell console.
    private HUDComponent hudComponent = null;

    //The beanshell interpreter
    private Interpreter interpreter;

    @Override
    public void initialize(final ServerSessionManager loginInfo) {
        // Create the beanshell menu item. Upon activation, create the HUD window
        // that displays the beanshell console.
        beanShellMI = new JCheckBoxMenuItem(bundle.getString("BEANSHELL_CONSOLE"));
        beanShellMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Depending upon whether the checkbox is selected or not,
                // either hide or show the HUD Component
                if (beanShellMI.isSelected() == true) {
                    if (hudComponent == null) {
                        hudComponent = createHUDComponent(loginInfo);
                    }
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
     * Creates and returns the beanshell HUD component.
     */
    private HUDComponent createHUDComponent(ServerSessionManager loginInfo) {
        // Create the HUD Panel that displays the beanshell console.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        JConsole console = new JConsole();
        console.setPreferredSize(new Dimension(400, 600));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(console, BorderLayout.CENTER);
        interpreter = new Interpreter(console);
        
        Thread beanThread = new Thread(interpreter);
        hudComponent = mainHUD.createComponent(panel);
        hudComponent.setName(BUNDLE.getString("BEANSHELL_CONSOLE"));
        hudComponent.setPreferredLocation(Layout.SOUTHEAST);
        mainHUD.addComponent(hudComponent);
        try {
            interpreter.set("loginInfo", loginInfo);
            interpreter.set("beanHUD", hudComponent);
        } catch (EvalError ex) {
            pluginLogger.log(Level.SEVERE, "Failed to set variables in interpreter", ex);
        }

        // Track when the HUD Component is closed. We need to update the state
        // of the check box menu item too. 
        hudComponent.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent event) {
                if (event.getEventType() == HUDEventType.CLOSED) {
                    beanShellMI.setSelected(false);
                }
            }
        });
        beanThread.start();
        return hudComponent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        super.cleanup();
        // If there is a HUD Component, then remove it from the HUD and clean
        // it up. This really should happen when the primary view cell is
        // disconnected, but we do this here just in case.
        if (hudComponent != null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            mainHUD.removeComponent(hudComponent);
        }
        JmeClientMain.getFrame().removeFromToolsMenu(beanShellMI);
    }

    @Override
    protected void activate() {
        JmeClientMain.getFrame().addToToolsMenu(beanShellMI);
    }

    @Override
    protected void deactivate() {
        JmeClientMain.getFrame().removeFromToolsMenu(beanShellMI);
    }


}
