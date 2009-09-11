/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.server.sticky;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.StickyNoteCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A hack of sticky note to disable initial placement
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineStickyCellMO extends StickyNoteCellMO {

    @Override
    protected CellClientState getClientState(CellClientState cellClientState,
                                             WonderlandClientID clientID,
                                             ClientCapabilities capabilities)
    {
        StickyNoteCellClientState snccs = (StickyNoteCellClientState)
                super.getClientState(cellClientState, clientID, capabilities);

        // force the cell to report that it has already done initial layout
        snccs.setInitialPlacementDone(true);
        return snccs;
    }

}
