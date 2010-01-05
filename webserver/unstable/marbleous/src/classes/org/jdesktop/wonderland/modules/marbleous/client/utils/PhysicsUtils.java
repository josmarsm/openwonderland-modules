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
package org.jdesktop.wonderland.modules.marbleous.client.utils;

import com.jme.math.Vector3f;

/**
 * Utilities for physics computations given basic physical properties of
 * objects.
 * @author kevin
 */
public class PhysicsUtils {

    private PhysicsUtils() {
    }

    /**
     * Get the potential energy associated with the given
     * position, mass, and gravitational acceleration.
     * @param translation Translation of the object (in m)
     * @param mass Mass of the object (in kg)
     * @param g Gravitational acceleration relative to the positive vertical axis (in m/s/s)
     * @return Potential energy of the object (in J)
     */
    static public float getPotentialEnergy(Vector3f translation, float mass, float g) {
        return -translation.y * mass * g;
    }

    /**
     * Get the kinetic energy associated with the given velocity
     * and mass.
     * @param velocity Velocity of the object (in m/s)
     * @param mass Mass of the object (in kg)
     * @return Kinetic energy of the object (in J)
     */
    static public float getKineticEnergy(Vector3f velocity, float mass) {
        return 0.5f * velocity.lengthSquared() * mass;
    }
}
