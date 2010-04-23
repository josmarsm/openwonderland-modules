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
package org.jdesktop.wonderland.modules.clientmonitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.clientmonitor.client.cell.ClientMonitorComponent;

/**
 *
 * @author paulby
 */
@Plugin
public class ClientMonitorPlugin extends BaseClientPlugin
    implements ViewManagerListener
{
    private JMenuItem menuItem = new JMenuItem("Client Monitor...");
    private ClientMonitorFrame frame = null;

    public ClientMonitorPlugin() {
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (frame == null) {
                    frame = new ClientMonitorFrame();
                }

                frame.setVisible(true);
            }
        });
    }

    @Override
    protected void activate() {
        super.activate();

        ViewManager vm = ViewManager.getViewManager();
        vm.addViewManagerListener(this);
        if (vm.getPrimaryViewCell() != null) {
            primaryViewCellChanged(null, vm.getPrimaryViewCell());
        }

        JmeClientMain.getFrame().addToWindowMenu(menuItem, Integer.MAX_VALUE);
    }

    @Override
    protected void deactivate() {
        super.deactivate();

        ViewManager.getViewManager().removeViewManagerListener(this);
        JmeClientMain.getFrame().removeFromWindowMenu(menuItem);

        if (frame != null) {
            frame.dispose();
        }
    }


    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
        if (oldViewCell!=null) {
            oldViewCell.removeComponent(ClientMonitorComponent.class);
        }

        if (newViewCell!=null) {
            newViewCell.addComponent(new ClientMonitorComponent(newViewCell));
        }
    }
}
