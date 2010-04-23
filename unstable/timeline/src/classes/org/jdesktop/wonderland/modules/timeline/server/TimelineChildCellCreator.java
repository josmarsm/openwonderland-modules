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
package org.jdesktop.wonderland.modules.timeline.server;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;

/**
 * Map from dated objects to corresponding cells to be added to the timeline.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineChildCellCreator extends Serializable {
    /**
     * Called after the object is created to associate it with a timeline
     * cell
     * @param cell the timeline cell
     */
    public void setLive(TimelineCellMO timeline);

    /**
     * Given a dated object, create an appropriate cell and add it to the
     * timeline
     * @param obj the dated object
     */
    public void createCell(DatedObject obj);

    /**
     * Given a DatedObject previously passed into <code>createCell()</code>,
     * clean up all references to the cell on the timeline.
     * @param obj the object to clean up
     */
    public void cleanupCell(DatedObject obj);
}
