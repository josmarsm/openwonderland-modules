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

import com.bulletphysics.dynamics.RigidBody;
import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * A component that describes the physics for the marble.
 *
 * @author kevin, jslott
 */
public class MarblePhysicsComponent extends CellComponent {

    /** The default mass of the marble */
    public static final float MASS = 1.0f;

    /** The default acceleration due to gravity */
    public static final float G = -9.80665f;

    /** The number of simulation samples per second (Hz) */
    public static final int FREQ = 60;

    // The RigidBody corresponding to the marble
    private RigidBody marbleBody = null;

    private JBulletDynamicCollisionSystem collisionSystem = null;
    private JBulletPhysicsSystem physicsSystem = null;

    public MarblePhysicsComponent(Cell cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == true) {
            // Initialize the collision and physics system
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

    /**
     * Returns the marble's rigid body, or null if not set.
     * @return The RigidBody
     */
    public RigidBody getMarbleRigidBody() {
        return marbleBody;
    }

    /**
     * Sets the marble's rigid body.
     *
     * @param rigidBody The marble's rigid body
     */
    public void setMarbleRigidBody(RigidBody marbleBody) {
        this.marbleBody = marbleBody;
    }
}
