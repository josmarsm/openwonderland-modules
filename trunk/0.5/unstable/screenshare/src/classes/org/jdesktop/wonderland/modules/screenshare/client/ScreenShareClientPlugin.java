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
package org.jdesktop.wonderland.modules.screenshare.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.screenshare.common.ScreenShareConstants;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.common.state.SharedStateComponentServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.state.SharedStateComponentServerState.MapEntry;
import org.jdesktop.wonderland.modules.sharedstate.common.state.SharedStateComponentServerState.SharedDataEntry;

/**
 * Client plugin to add share screen menu
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class ScreenShareClientPlugin extends BaseClientPlugin
    implements ActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ScreenShareClientPlugin.class.getName());
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org.jdesktop.wonderland.modules.screenshare.client.resources.Bundle");

    private final JMenuItem menuItem;

    public ScreenShareClientPlugin() {
        menuItem = new JMenuItem(BUNDLE.getString("Share_Screen"));
        menuItem.addActionListener(this);
    }

    @Override
    protected void activate() {
        JmeClientMain.getFrame().addToToolsMenu(menuItem);
    }

    @Override
    protected void deactivate() {
        JmeClientMain.getFrame().removeFromToolsMenu(menuItem);
    }

    public void actionPerformed(ActionEvent e) {
        // generate a server state
        CellFactorySPI factory = new ScreenShareCellFactory();
        CellServerState state = factory.getDefaultCellServerState(new Properties());

        // add the initial creator sessionID and username
        String sessionID = getSessionManager().getPrimarySession().getID().toString();
        String username = getSessionManager().getUsername();

        SharedStateComponentServerState sscss = (SharedStateComponentServerState)
                 state.getComponentServerState(SharedStateComponentServerState.class);
        if (sscss == null) {
            sscss = new SharedStateComponentServerState();
            state.addComponentServerState(sscss);
        }

        MapEntry me = new MapEntry(ScreenShareConstants.STATUS_MAP);
        me.setData(new SharedDataEntry[] {
            new SharedDataEntry(ScreenShareConstants.CONTROLLER,
                                SharedString.valueOf(sessionID)),
            new SharedDataEntry(ScreenShareConstants.CONTROLLER_NAME,
                                SharedString.valueOf(username))
        });
        sscss.setMaps(new MapEntry[] { me });

        // create the cell
        try {
            CellUtils.createCell(state);
        } catch (CellCreationException ex) {
            LOGGER.log(Level.WARNING, "Unable to create cell", ex);
        }
    }
}
