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

package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell;

import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.StickyNoteSyncMessage;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 *
 * @author Ryan Babiuch
 */
public class MirroredStickyNoteCell extends StickyNoteCell implements SharedMapListenerCli {

    @UsesCellComponent
    private SharedStateComponent ssc = null;

    private SharedMapCli map;

    public MirroredStickyNoteCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if(status == CellStatus.ACTIVE && increasing == true) {
            map = ssc.get("state");
            map.addSharedMapListener(this);
            map.get("group", SharedString.class);
        }
        //begin grabbing sharedmap

    }

    @Override
    public void processMessage(final StickyNoteSyncMessage message) {
        System.out.println("* Received Sync message! *" + " CellID: " + this.getCellID());
        System.out.println("** NEW TEXT: " + message.getState().getNoteText() + " **");
        //getWindow().getStickynoteParentPanel().getChild().processMessage(message);
    }

    public void propertyChanged(SharedMapEventCli event) {
        //nothing to do or see here
        // the properties sheet should grab the ssc from here and set the
        // group that way.
    }

    public SharedMapCli getMap() {
        return map;
    }

}
