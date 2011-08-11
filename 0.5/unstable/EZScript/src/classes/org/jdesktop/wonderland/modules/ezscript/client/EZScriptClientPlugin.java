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
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.ezscript.client.simplephysics.SimplePhysicsManager;

/** Testing online googlecode editing feature. */

/**
 *
 * @author JagWire
 *
 */
@Plugin
public class EZScriptClientPlugin extends BaseClientPlugin implements SessionLifecycleListener {

    JMenuItem physicsMenuItem;
    JMenuItem editorMenuItem;
    JDialog dialog;
    private ServerSessionManager manager;
    private ScriptImporter importer = null;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        //ScriptManager.getInstance();
        physicsMenuItem = new JMenuItem("Simple Physics");
        editorMenuItem = new JMenuItem("Script Editor");
        loginInfo.addLifecycleListener(this);
        super.initialize(loginInfo);

    }
    @Override
    public void activate() {
        importer = new ScriptImporter();
        
        physicsMenuItem.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) {
                //show physics control panel here.
                SimplePhysicsManager.INSTANCE.showControlPanel();
            }
        });
        
        editorMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ScriptManager.getInstance().showScriptEditor();
            }
        });
        
        JmeClientMain.getFrame().addToToolsMenu(physicsMenuItem);
        JmeClientMain.getFrame().addToToolsMenu(editorMenuItem);
        ContentImportManager.getContentImportManager().registerContentImporter(importer);
        
    }

    @Override
    public void deactivate() {
        JmeClientMain.getFrame().removeFromToolsMenu(physicsMenuItem);
        JmeClientMain.getFrame().removeFromToolsMenu(editorMenuItem);
        ContentImportManager.getContentImportManager().unregisterContentImporter(importer);
        importer = null;
    }

    @Override
    public void cleanup() {
        physicsMenuItem = null;
        editorMenuItem = null;
        super.cleanup();
    }

    public void sessionCreated(WonderlandSession session) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void primarySession(WonderlandSession session) {

    }

}
