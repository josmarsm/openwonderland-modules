/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;


/**
 *
 * @author JagWire
 *
 */
@Plugin
public class EZScriptClientPlugin extends BaseClientPlugin {

    JMenuItem menuItem;
    JDialog dialog;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        ScriptManager.getInstance();
        menuItem = new JMenuItem("Script Editor");
        
        super.initialize(loginInfo);
    }
    @Override
    public void activate() {

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ScriptManager.getInstance().showScriptEditor();
            }
        });
        JmeClientMain.getFrame().addToToolsMenu(menuItem);
    }

    @Override
    public void deactivate() {
        JmeClientMain.getFrame().removeFromToolsMenu(menuItem);
    }

    @Override
    public void cleanup() {
        menuItem = null;
        super.cleanup();
    }

}
