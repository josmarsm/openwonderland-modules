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
package org.jdesktop.wonderland.modules.huddebug.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin for the HUD debugging tool. Adds a "HUD Debugger" menu item
 * to the Tools menu.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class HUDDebuggerClientPlugin extends BaseClientPlugin {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/huddebug/client/resources/Bundle");

    // The HUD Debugger menu item to add to the Tools menu
    private JMenuItem hudDebugMI = null;

    // The frame that displays the HUD information
    private HUDDebuggerJFrame hudJFrame = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // Create the HUD Debugger menu item. Upon activation, create the Swing
        // window that displays the debugger.
        hudDebugMI = new JCheckBoxMenuItem(BUNDLE.getString("HUD_Debugger"));
        hudDebugMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Depending upon whether the checkbox is selected or not,
                // either hide or show the Swing window
                if (hudDebugMI.isSelected() == true) {
                    if (hudJFrame == null) {
                        // If the frame does not exist, then create it. Listen
                        // for when the frame is closing so we can update the
                        // state of the checkbox menu item
                        hudJFrame = new HUDDebuggerJFrame();
                        hudJFrame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                hudDebugMI.setSelected(false);
                            }
                        });
                    }
                    hudJFrame.setVisible(true);
                }
                else {
                    hudJFrame.setVisible(false);
                }
            }
        });
        
        super.initialize(loginInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate() {
        // Add the HUD Debugger menu item
        JmeClientMain.getFrame().addToToolsMenu(hudDebugMI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate() {
        // Remove the HUD Debugger menu item
        JmeClientMain.getFrame().removeFromToolsMenu(hudDebugMI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        hudDebugMI = null;

        // If there is a frame, then dispose of it
        if (hudJFrame != null) {
            hudJFrame.setVisible(false);
            hudJFrame.dispose();
            hudJFrame = null;
        }
    }
}
