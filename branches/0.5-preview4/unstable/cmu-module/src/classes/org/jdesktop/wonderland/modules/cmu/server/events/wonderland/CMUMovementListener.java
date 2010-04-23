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
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.AvatarPositionFunction;
import org.jdesktop.wonderland.modules.cmu.server.CMUCellMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
import org.jdesktop.wonderland.server.spatial.UniverseManager;

/**
 * Proximity listener for avatar movement - when an avatar enters the proximity
 * to which it is assigned, begins listening for position updates.
 * @author kevin
 */
public class CMUMovementListener extends WonderlandServerEventListener
        implements ProximityListenerSrv, ManagedObject {

    private final AvatarMovementListener movementListener;

    public CMUMovementListener(ManagedReference<CMUCellMO> parent, AvatarPositionFunction response) {
        super(parent, response);

        this.movementListener = new AvatarMovementListener(getParent().getBindingText(), response);
    }

    public void viewEnterExit(boolean entered, CellID cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        CellMO avatarCell = CellManagerMO.getCell(viewCellID);
        UniverseManager um = AppContext.getManager(UniverseManager.class);

        if (entered) {
            um.addTransformChangeListener(avatarCell, movementListener);
        } else {
            um.removeTransformChangeListener(avatarCell, movementListener);
        }
    }

    protected static class AvatarMovementListener implements TransformChangeListenerSrv {

        private final String binding;
        private final AvatarPositionFunction response;

        public AvatarMovementListener(String binding, AvatarPositionFunction response) {
            this.binding = binding;
            this.response = response;
        }

        public String getBinding() {
            return binding;
        }

        public AvatarPositionFunction getResponse() {
            return response;
        }

        public void transformChanged(ManagedReference<CellMO> cellRef, CellTransform localTransform, CellTransform worldTransform) {
            //TODO: Make transform match up with CMU transform
            this.getResponse().setAvatarPosition(localTransform.getTranslation(null));

            CMUCellMO cellMO = (CMUCellMO) AppContext.getDataManager().getBinding(this.getBinding());
            cellMO.processEventResponse(this.getResponse());
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other instanceof AvatarMovementListener && this.getClass().equals(other.getClass())) {
                AvatarMovementListener otherListener = (AvatarMovementListener) other;
                if ((this.getBinding() == null) ? (otherListener.getBinding() != null)
                        : !(this.getBinding().equals(otherListener.getBinding()))) {
                    return false;
                }
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.binding != null ? this.binding.hashCode() : 0);
            return hash;
        }
    }
}
