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
package org.jdesktop.wonderland.modules.keybindings.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin for testing Wonderland avatar key bindings
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class KeyBindingsClientPlugin extends BaseClientPlugin {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/keybindings/client/resources/Bundle");

    // The "Key Bindings" menu item on the "Edit" menu
    private JMenuItem keyBindingsMI = null;

    // A weak reference to the "Key Bindings" edit frame
    private WeakReference<EditKeyBindingsJFrame> keyBindingsFrameRef = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // Create the "Edit" menu item to set the key bindings
        keyBindingsMI = new JMenuItem(BUNDLE.getString("KEY_BINDINGS"));
        keyBindingsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // If not already displayed, display the dialog to edit the
                // key bindings
                JFrame mainFrame = JmeClientMain.getFrame().getFrame();
                EditKeyBindingsJFrame frame = getKeyBinginsJDialog();
                if (frame.isVisible() == false) {
                    frame.pack();
                    frame.setSize(450, 400);
                    frame.setLocationRelativeTo(mainFrame);
                    frame.setVisible(true);
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
        // Add the menu item to the "Edit" main menu
        JmeClientMain.getFrame().addToEditMenu(keyBindingsMI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        // Remove the menu item from the "Edit" main menu
        JmeClientMain.getFrame().removeFromEditMenu(keyBindingsMI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        // Clean up the "Key Bindings" menu item
        keyBindingsMI = null;
    }

    /**
     * Returns the single EditKeyBindingsJDialog for the system, creating it if
     * necessary
     */
    private EditKeyBindingsJFrame getKeyBinginsJDialog() {
        if (keyBindingsFrameRef == null || keyBindingsFrameRef.get() == null) {
            EditKeyBindingsJFrame frame = new EditKeyBindingsJFrame();
            keyBindingsFrameRef = new WeakReference(frame);
            return frame;
        }
        else {
            return keyBindingsFrameRef.get();
        }
    }
}
