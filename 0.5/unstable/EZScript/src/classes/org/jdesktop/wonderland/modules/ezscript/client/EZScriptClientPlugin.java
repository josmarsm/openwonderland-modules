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
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.ServerStatusListener;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;


/**
 *
 * @author JagWire
 *
 */
@Plugin
public class EZScriptClientPlugin extends BaseClientPlugin implements SessionLifecycleListener {

    JMenuItem menuItem;
    JDialog dialog;
    private ServerSessionManager manager;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        //ScriptManager.getInstance();
        menuItem = new JMenuItem("Script Editor");
        loginInfo.addLifecycleListener(this);
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

    public void sessionCreated(WonderlandSession session) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void primarySession(WonderlandSession session) {

    }

}
