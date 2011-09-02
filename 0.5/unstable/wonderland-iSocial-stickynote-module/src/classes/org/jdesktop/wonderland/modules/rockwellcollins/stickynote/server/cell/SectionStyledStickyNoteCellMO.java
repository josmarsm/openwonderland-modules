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

import com.jme.math.Vector2f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
//import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellClientState;

import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.SectionStyledStickyNoteSyncMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.SectionStyledStickyNoteComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * A server cell associated with a  section styled post-it note
 *
 * @author Xiuzhen (mymegabyte)
 */
@ExperimentalAPI
public class SectionStyledStickyNoteCellMO extends App2DCellMO {
    // The communications component used to broadcast to all clients
    @UsesCellComponentMO(SectionStyledStickyNoteComponentMO.class)
    private ManagedReference<SectionStyledStickyNoteComponentMO> commComponentRef;
    private SectionStyledStickyNoteCellClientState stateHolder = new SectionStyledStickyNoteCellClientState();

    /** Default constructor, used when the cell is created via WFS */
    public SectionStyledStickyNoteCellMO() {
        super();
        //this.cellChannelRef
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell.SectionStyledStickyNoteCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new SectionStyledStickyNoteCellClientState(pixelScale);
        }
        ((SectionStyledStickyNoteCellClientState) cellClientState).copyLocal(stateHolder);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        SectionStyledStickyNoteCellServerState serverState = (SectionStyledStickyNoteCellServerState) state;
        stateHolder.setPreferredWidth(serverState.getPreferredWidth());
        stateHolder.setPreferredHeight(serverState.getPreferredHeight());
        stateHolder.setPixelScale(new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY()));


        stateHolder.setNoteText(serverState.getNoteText());
        stateHolder.setSecondNoteText(serverState.getSecondNoteText());
        stateHolder.setAllNoteAttributes(serverState.getAllNoteAttributes());
        stateHolder.setNoteType(serverState.getNoteType());
        stateHolder.setNoteAssignee(serverState.getNoteAssignee());
        stateHolder.setNoteDue(serverState.getNoteDue());
        stateHolder.setNoteName(serverState.getNoteName());
        stateHolder.setNoteStatus(serverState.getNoteStatus());
        stateHolder.setNoteColor(serverState.getColor());
        stateHolder.setSelectedTextStyle(serverState.getSelectedTextStyle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState stateToFill) {
        if (stateToFill == null) {
            stateToFill = new SectionStyledStickyNoteCellServerState();
        }

        super.getServerState(stateToFill);

        SectionStyledStickyNoteCellServerState state = (SectionStyledStickyNoteCellServerState) stateToFill;

        state.setPreferredWidth(stateHolder.getPreferredWidth());
        state.setPreferredHeight(stateHolder.getPreferredHeight());
        state.setPixelScaleX(stateHolder.getPixelScale().x);
        state.setPixelScaleY(stateHolder.getPixelScale().y);


        state.setNoteText(stateHolder.getNoteText());
        state.setSecondNoteText(stateHolder.getSecondNoteText());
        state.setAllNoteAttributes(stateHolder.getAllNoteAttributes());
        state.setNoteType(stateHolder.getNoteType());
        state.setNoteAssignee(stateHolder.getNoteAssignee());
        state.setNoteDue(stateHolder.getNoteDue());
        state.setNoteName(stateHolder.getNoteName());
        state.setNoteStatus(stateHolder.getNoteStatus());
        state.setColor(stateHolder.getNoteColor());
        state.setSelectedTextStyle(stateHolder.getSelectedTextStyle());

        return stateToFill;
    }

    /**
     * {@inheritDoc}
     */
//    @Override
//    protected void setLive(boolean live) {
//        super.setLive(live);
//
//        if (live == true) {
//            if (commComponentRef == null) {
//                StickyNoteComponentMO commComponent = new StickyNoteComponentMO(this);
//                commComponentRef = AppContext.getDataManager().createReference(commComponent);
//                addComponent(commComponent);
//            }
//        } else {
//            if (commComponentRef != null) {
//                StickyNoteComponentMO commComponent = commComponentRef.get();
//                AppContext.getDataManager().removeObject(commComponent);
//                commComponentRef = null;
//            }
//        }
//    }

    public void receivedMessage(WonderlandClientSender sender, WonderlandClientID clientID, SectionStyledStickyNoteSyncMessage message) {
        SectionStyledStickyNoteComponentMO commComponent = commComponentRef.getForUpdate();
        commComponent.sendAllClients(clientID, message);
        stateHolder.copyLocal(message.getState());
        //stateHolder = message.getState();
    }
}
