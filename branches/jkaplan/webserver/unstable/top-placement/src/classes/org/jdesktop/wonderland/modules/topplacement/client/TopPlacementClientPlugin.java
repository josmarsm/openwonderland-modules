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
package org.jdesktop.wonderland.modules.topplacement.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JRadioButtonMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-size plugin for the top camera.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class TopPlacementClientPlugin extends BaseClientPlugin {

    private JRadioButtonMenuItem topCameraMI = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        topCameraMI = new JRadioButtonMenuItem("Top Placement");
        topCameraMI.setToolTipText("Layout a scene from a top view");
        topCameraMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ViewManager vm = ClientContextJME.getViewManager();
                vm.setCameraController(new TopPlacementCameraProcessor());
            }
        });

        super.initialize(loginInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activate() {
        // activate
        JmeClientMain.getFrame().addToCameraChoices(topCameraMI, 4);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        // deactivate
        JmeClientMain.getFrame().removeFromCameraChoices(topCameraMI);
    }
}
