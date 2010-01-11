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

package org.jdesktop.wonderland.modules.cmu.client.events.wonderland;

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.common.events.WonderlandEventResponse;

/**
 * Listener for Wonderland proximity events.  Passes the appropriate response
 * on to the server when they are received.
 * @author kevin
 */
public class CMUProximityListener extends CMUWonderlandEventListener implements ProximityListener {

    private final boolean eventOnEnter;

    public CMUProximityListener(CMUCell parent, WonderlandEventResponse response, boolean eventOnEnter) {
        super(parent, response);
        this.eventOnEnter = eventOnEnter;
    }

    public boolean isEventOnEnter() {
        return eventOnEnter;
    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        if (entered == this.isEventOnEnter()) {
            this.eventOccurred();
        }
    }
}
