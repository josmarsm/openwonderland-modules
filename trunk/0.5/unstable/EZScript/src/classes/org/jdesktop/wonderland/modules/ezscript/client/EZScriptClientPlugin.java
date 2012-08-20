/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Testing online googlecode editing feature.
 */
/**
 *
 * @author JagWire
 *
 */
@Plugin
public class EZScriptClientPlugin extends BaseClientPlugin implements SessionLifecycleListener {

    private JMenuItem physicsMenuItem;
    private JMenuItem editorMenuItem;
    private JDialog commandDialog;
    private CommandPanel commandPanel;
    private ServerSessionManager manager;
    private ScriptImporter importer = null;
    private CommandWindowListener listener = null;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        //ScriptManager.getInstance();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                physicsMenuItem = new JMenuItem("Simple Physics");
                editorMenuItem = new JMenuItem("Script Editor");
                commandDialog = new JDialog();
                commandPanel = new CommandPanel();
                commandPanel.setDialog(commandDialog);

            }
        });


        loginInfo.addLifecycleListener(this);
        super.initialize(loginInfo);

    }

    @Override
    public void activate() {
        importer = new ScriptImporter();


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initializeDialog();

                //Disable until we can do something useful with it.
//        physicsMenuItem.addActionListener(new ActionListener() { 
//            public void actionPerformed(ActionEvent e) {
//                //show physics control panel here.
//                SimplePhysicsManager.INSTANCE.showControlPanel();
//            }
//        });

                editorMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ScriptManager.getInstance().showScriptEditor();
                    }
                });

                JmeClientMain.getFrame().addToToolsMenu(physicsMenuItem);
                JmeClientMain.getFrame().addToToolsMenu(editorMenuItem);
            }
        });

        ContentImportManager.getContentImportManager().registerContentImporter(importer);
        ScriptedObjectDataSource init = ScriptedObjectDataSource.INSTANCE;
    }

    @Override
    public void deactivate() {
        JmeClientMain.getFrame().removeFromToolsMenu(physicsMenuItem);
        JmeClientMain.getFrame().removeFromToolsMenu(editorMenuItem);
        InputManager.inputManager().removeGlobalEventListener(listener);
        ContentImportManager.getContentImportManager().unregisterContentImporter(importer);
        importer = null;

    }

    @Override
    public void cleanup() {
        physicsMenuItem = null;
        editorMenuItem = null;
        listener = null;
        super.cleanup();
    }

    public void sessionCreated(WonderlandSession session) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void primarySession(WonderlandSession session) {
    }

    private void initializeDialog() {
        commandDialog.setTitle("Command Line");
        commandDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        commandDialog.setContentPane(commandPanel);
        commandDialog.pack();

        commandDialog.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == '`' || ke.getKeyChar() == '~') {
                    toggleDialog();
                }
            }

            public void keyPressed(KeyEvent ke) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyReleased(KeyEvent ke) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        if (listener == null) {
            listener = new CommandWindowListener();
            InputManager.inputManager().addGlobalEventListener(listener);
        }
    }

    private void toggleDialog() {
        commandDialog.setVisible(!commandDialog.isVisible());
    }

    class CommandWindowListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{KeyEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            //cast event to key event;
            KeyEvent3D keyEvent = (KeyEvent3D) event;

            //Did user press and release key?
            if (keyEvent.isTyped()) {
                //get character of key pressed.
                char key = keyEvent.getKeyChar();

                //if the key was the tilde key
                if (key == '`' || key == '~') {
                    //show or hide the dialog
                    toggleDialog();
                }
            }
        }
    }
}
