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
package org.jdesktop.wonderland.modules.marbleous.client.cell;

import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author kevin
 */
public class MarblePhysicsComponent extends CellComponent {

    private JBulletDynamicCollisionSystem collisionSystem = null;
    private JBulletPhysicsSystem physicsSystem = null;

    public MarblePhysicsComponent(Cell cell) {
        super(cell);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == true) {
            WorldManager wm = ClientContextJME.getWorldManager();
            collisionSystem = (JBulletDynamicCollisionSystem) wm.getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);
            physicsSystem = (JBulletPhysicsSystem) wm.getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, collisionSystem);
        }
    }

    /**
     * Get the created collision system.
     * @return The created collision system
     */
    public JBulletDynamicCollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    /**
     * Get the created physics system.
     * @return The created physics system
     */
    public JBulletPhysicsSystem getPhysicsSystem() {
        return physicsSystem;
    }
}
