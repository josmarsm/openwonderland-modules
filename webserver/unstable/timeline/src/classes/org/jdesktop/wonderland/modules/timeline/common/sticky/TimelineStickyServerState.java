/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.sticky;

import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellServerState;

/**
 * Create a timeline sticky cell
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineStickyServerState extends StickyNoteCellServerState {
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.timeline.server.sticky.TimelineStickyCellMO";
    }
}
