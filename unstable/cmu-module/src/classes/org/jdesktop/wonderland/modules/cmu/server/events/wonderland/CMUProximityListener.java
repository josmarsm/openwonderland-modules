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
package org.jdesktop.wonderland.modules.cmu.server.events.wonderland;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.NoArgumentFunction;
import org.jdesktop.wonderland.modules.cmu.server.CMUCellMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

/**
 * Listener for Wonderland proximity events.
 * @author kevin
 */
public class CMUProximityListener extends WonderlandServerEventListener
        implements ProximityListenerSrv, ManagedObject {

    private final boolean eventOnEnter;

    public CMUProximityListener(ManagedReference<CMUCellMO> parent, NoArgumentFunction response, boolean eventOnEnter) {
        super(parent, response);
        this.eventOnEnter = eventOnEnter;
    }

    public boolean isEventOnEnter() {
        return eventOnEnter;
    }

    public void viewEnterExit(boolean entered, CellID cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        if (entered == this.isEventOnEnter()) {
            this.eventOccurred();
        }
    }
}
