/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.timeline.server.layout;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;
import java.io.Serializable;
import java.util.Map;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.layout.DatedLayoutParticipant;
import org.jdesktop.wonderland.modules.timeline.common.layout.TimelineLayout;
import org.jdesktop.wonderland.modules.layout.server.cell.LayoutCellComponentMO;
import org.jdesktop.wonderland.modules.timeline.common.layout.TimelineLayout.TimelineLayoutStorage;
import org.jdesktop.wonderland.modules.timeline.common.layout.TimelineLayoutConfig;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.server.TimelineCellMO;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * Extends the layout component the use a properly-configured
 * TimelineLayout.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineLayoutComponentMO extends LayoutCellComponentMO {
    public TimelineLayoutComponentMO(CellMO cell) {
        super (cell);
    }

    @Override
    protected void setLive(boolean live) {
        if (live) {
            TimelineCellMO timelineCell = (TimelineCellMO) cellRef.get();
            TimelineLayout layout = new TimelineLayout(
                    timelineCell.getConfiguration(),
                    new TimelineLayoutStorageImpl());
            layout.setConfig(new TimelineLayoutConfig());
            setLayout(layout);
        }

        super.setLive(live);
    }

    private static class TimelineLayoutStorageImpl
            implements TimelineLayoutStorage, Serializable
    {
        private final ManagedReference<Map<TimelineSegment,
                                           DatedSet<DatedLayoutParticipant>>>
                                           segmentMapRef;

        public TimelineLayoutStorageImpl() {
            Map<TimelineSegment, DatedSet<DatedLayoutParticipant>> segmentMap =
                    new ScalableHashMap<TimelineSegment, DatedSet<DatedLayoutParticipant>>();
            segmentMapRef = AppContext.getDataManager().createReference(segmentMap);
        }

        public Map<TimelineSegment, DatedSet<DatedLayoutParticipant>> getSegmentMap() {
            return segmentMapRef.get();
        }
    }
}
