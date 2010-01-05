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
import java.util.logging.Logger;
import javax.swing.JButton;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellServerState;
import org.jdesktop.wonderland.modules.presentationbase.client.PresentationToolbarManager;
import org.jdesktop.wonderland.modules.presentationbase.client.PresentationsManager;

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
//            Collection<Cell> rootCells = ClientContext.getCellCache(this.getSessionManager().getPrimarySession()).getRootCells();

            // Loop through the list of root cells and take the first one that's not an avatar.
//            logger.warning("got rootCells. total: " + rootCells.size() + ": " + rootCells);

            // figure out which platform we should be a child of (if any) by getting all
            // the presentation cells in the
            Cell parent = PresentationsManager.getPresentationsManager().getParentCellByPosition(currentAvatarTransform.getTranslation(null));


            // To turn off this non-reparenting solution, just do a revert on ChatZonesClientPlugin
            // and ChatZonesCellMO to r1022 to take all this junk out in one fell swoop. That version
            // did reparenting properly.
            ChatZonesCellServerState state = new ChatZonesCellServerState();


            logger.warning("found a parent cell for this chat zone? parent: " + parent);
            CellTransform finalChatZoneTransform = currentAvatarTransform;
            if(parent!=null) {
                // Transform the avatar's position into platform (or pres-cell) local
                // coordinates.
//                finalChatZoneTransform = transform(currentAvatarTransform, new CellTransform(), parent.getWorldTransform());
                state.setParentID(parent.getCellID().toString());
            } else {
                finalChatZoneTransform = currentAvatarTransform;
            }
            
            logger.warning("final chat zone position: " + finalChatZoneTransform);


            PositionComponentServerState posState = new PositionComponentServerState();
            posState.setTranslation(finalChatZoneTransform.getTranslation(null));
            posState.setRotation(finalChatZoneTransform.getRotation(null));
            posState.setScaling(finalChatZoneTransform.getScaling(null));
//            state.setInitialCellTransform(currentAvatarTransform);

            state.addComponentServerState(posState);
            CellCreateMessage msg;
            
            // if we found a platform to attach to, include that parent's cellID
            // in the creat message so we're created as a child of that cell. 
//            if(parent==null)
                msg = new CellCreateMessage(null, state);
//            else
//                msg = new CellCreateMessage(parent.getCellID(), state);

            // Is this really right? 
            this.getSessionManager().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE).getSession().send(sender, msg);

        }
    }
  
  /**
    * A utility routine to convert the given transform from world coordinates
    * to another reference system. Typically, the given transform is the
    * initial transform of the Cell in world coordinates and we want to
    * transform with respect to some Cell in the world.
    *
    */
   private static CellTransform transform(CellTransform transform,
           CellTransform fromReferenceSystem, CellTransform toReferenceSystem) {

       CellTransform newTransform = toReferenceSystem.clone(null);
       newTransform.invert();
       newTransform.mul(fromReferenceSystem);
       newTransform.mul(transform);
       return newTransform;
   }
}
