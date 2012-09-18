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
package org.jdesktop.wonderland.modules.quickstartguide.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.help.WebBrowserLauncher;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Plugin that manages menus for the library module
 * @author Jonathan Kaplan <jonathan@wonderbuilders.com>
 */
@Plugin
public class QuickstartGuideClientPlugin extends BaseClientPlugin {

    private static final Logger LOGGER =
            Logger.getLogger(QuickstartGuideClientPlugin.class.getName());
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org/jdesktop/wonderland/modules/quickstartguide/client/Bundle");
    private JMenuItem menu;

    public QuickstartGuideClientPlugin() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                menu = new JMenuItem(BUNDLE.getString("Quick_Start_Guide"));
                menu.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        showQuickstartGuide();
                    }
                });
            }
        });

    }

    @Override
    protected void activate() {
        JmeClientMain.getFrame().addToHelpMenu(menu);
    }

    @Override
    protected void deactivate() {
        JmeClientMain.getFrame().removeFromHelpMenu(menu);
    }

    protected void showQuickstartGuide() {
        String baseURL = getSessionManager().getServerURL();
        String quickstartURL = baseURL +
                "/quickstartguide/quickstartguide/QuickstartTOC.html";
        
        try {
            WebBrowserLauncher.openURL(quickstartURL);
        } catch (Exception excp) {
            LOGGER.log(Level.WARNING, "Failed to open Help URL: "
                    + quickstartURL, excp);
        }
    }
}
