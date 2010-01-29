/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.server.childcell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipant;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipantProvider;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.StickyNoteCellMO;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.server.layout.DatedLayoutParticipantImpl;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A hack of sticky note to disable initial placement and return a
 * DatedLayoutParticipant.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineStickyCellMO extends StickyNoteCellMO 
        implements LayoutParticipantProvider
{
    private DatedObject datedObj;
    
    public TimelineStickyCellMO(DatedObject datedObj) {
        this.datedObj = datedObj;
    }

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

    public LayoutParticipant getLayoutParticipant() {
        return new TimelineStickyLayoutParticipant(this, datedObj);
    }

    private static class TimelineStickyLayoutParticipant
            extends DatedLayoutParticipantImpl
    {
        public TimelineStickyLayoutParticipant(CellMO cell, DatedObject obj) {
            super (cell, obj);
        }

        @Override
        public BoundingVolume getSize() {
            return new BoundingBox(new Vector3f(0f, 0f, 0f), 1.5f, 1.5f, 1.5f);
        }
    }
}
