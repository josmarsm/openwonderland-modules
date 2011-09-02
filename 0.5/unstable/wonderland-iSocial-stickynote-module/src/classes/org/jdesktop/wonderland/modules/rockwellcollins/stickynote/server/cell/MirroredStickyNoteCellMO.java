/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.MirroredStickyNoteCellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.StickyNoteSyncMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.MirroredStickyRegistry;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapEventSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author ryan
 */
public class MirroredStickyNoteCellMO extends StickyNoteCellMO
    implements SharedMapListenerSrv {

	private String text = null;
        private StickyNoteSyncMessage lastMessage = null;
        private WonderlandClientSender lastSender = null;
        private String group = "global";

        @UsesCellComponentMO(SharedStateComponentMO.class)
        private ManagedReference<SharedStateComponentMO> sscRef;
        //private StickyNoteCellClientState stateHolder = new StickyNoteCellClientState();
	public MirroredStickyNoteCellMO() {
		super();
		MirroredStickyRegistry.getInstance().addListener(group, this);
	}

	public void setText(WonderlandClientSender sender, String text, CellMessage message, String g) {

            //another sanity check
            if(!group.equals(g)) {
                return;
            }
		this.text = text;
                message.setCellID(cellID);
                sender.send(message);
                System.out.println("** Sending Sync Message back! **");

                //this.sendCellMessage(null, lastMessage);

	}

        @Override
        protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
            return "org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell.MirroredStickyNoteCell";
        }

	@Override
	public CellServerState getServerState(CellServerState state) {
		if(state == null) {
			state = new MirroredStickyNoteCellServerState();
		}
                super.getServerState(state);

                ((MirroredStickyNoteCellServerState)state).setNoteText(text);

                return state;
	}

        @Override
        public void receivedMessage(WonderlandClientSender sender,
                WonderlandClientID clientID,
                StickyNoteSyncMessage message) {
            //super.receivedMessage(sender, clientID, message);
            lastMessage = message;
            lastSender = sender;
            System.out.println("* Received Sync Message *");
            //StickyNoteComponentMO commComponent = commComponentRef.getForUpdate();
            //getStateHolder().copyLocal(message.getState());
            MirroredStickyRegistry.getInstance().notifyChange(sender,
                     message.getState().getNoteText(),
                    message,group);
            //commComponent.sendAllClients(clientID, message);


        }

        @Override
        protected void setLive(boolean live) {
            super.setLive(live);
            if(live) {
                SharedMapSrv statusMap = sscRef.get().get("state");
                statusMap.put("group", SharedString.valueOf(group));//, null)
                statusMap.addSharedMapListener(this);
            }
        }

        public boolean propertyChanged(SharedMapEventSrv event) {

            //sanity checks
            if(event.getPropertyName().equals("group")) {

                SharedString ss = (SharedString)event.getNewValue();
                if(!group.equals(ss.getValue())) {
                    System.out.println("Switching CellMO from group: " +group
                            + " to: " +ss.getValue());
                    MirroredStickyRegistry.getInstance().removeListener(group, this);
                    MirroredStickyRegistry.getInstance().addListener(ss.getValue(), this);
                    group = ss.getValue();
                }
            }

            return true;
        }
}
