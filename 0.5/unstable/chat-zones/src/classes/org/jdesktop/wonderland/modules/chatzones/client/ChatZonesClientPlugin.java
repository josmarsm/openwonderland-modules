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
package org.jdesktop.wonderland.modules.chatzones.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellServerState;
import org.jdesktop.wonderland.modules.presentationbase.client.PresentationToolbarManager;

/**
 * Client-side plugin for the ChatZones system.
 *
 * Provides a button for easily making chat zones at
 * your current position.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */

@Plugin
public class ChatZonesClientPlugin extends BaseClientPlugin implements ActionListener {

    private static final String CREATE_CHAT_ZONE_COMMAND = "CREATE_CHAT_ZONE";
    private JButton button;

    private static final Logger logger =
        Logger.getLogger(ChatZonesClientPlugin.class.getName());


    /**
     * @inheritDoc()
     */
    @Override
    public void activate() {
        if(button==null) {
            button = new JButton();
            button.setText("Create ChatZone");
            button.addActionListener(this);
            button.setActionCommand(CREATE_CHAT_ZONE_COMMAND);
        }

       PresentationToolbarManager.getManager().addToolbarButton(button);
    }

    @Override
    protected void deactivate() {
      PresentationToolbarManager.getManager().removeToolbarButton(button);
    }

    public void actionPerformed(ActionEvent arg0) {
        if(arg0.getActionCommand().equals(CREATE_CHAT_ZONE_COMMAND)) {
            logger.warning("Making a new Chat Zone underneath the avatar.");

            // Figure out where the avatar is.

            CellTransform currentAvatarTransform =  ClientContextJME.getCellCache(this.getSessionManager().getPrimarySession()).getViewCell().getWorldTransform();
            //            WonderlandClientSender sender = WonderlandContext.getCommsManager().getSender(CellEditConnectionType.CLIENT_TYPE);
            ClientConnection sender = this.getSessionManager().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE);

            // parent id, server state
            Collection<Cell> rootCells = ClientContext.getCellCache(this.getSessionManager().getPrimarySession()).getRootCells();

            // Loop through the list of root cells and take the first one that's not an avatar.

            logger.warning("got rootCells. total: " + rootCells.size() + ": " + rootCells);

            ChatZonesCellServerState state = new ChatZonesCellServerState();
            state.setInitialCellTransform(currentAvatarTransform);
            
            CellCreateMessage msg = new CellCreateMessage(null, state);


            // Is this really right? 
            this.getSessionManager().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE).getSession().send(sender, msg);

        }
    }
}
