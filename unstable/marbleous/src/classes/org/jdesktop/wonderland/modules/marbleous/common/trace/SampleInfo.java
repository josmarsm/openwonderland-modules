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
package org.jdesktop.wonderland.modules.marbleous.common.trace;

import com.jme.math.Vector3f;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.marbleous.client.utils.PhysicsUtils;

/**
 * A sample in a time-series of simulation results.
 *
 * @author jslott, kmontag, deronj
 */
public class SampleInfo implements Serializable {

    // The time (in seconds) of this sample in the time series
    private float time = 0.0f;

    // The mass (in kilograms) of the mass of the object
    private float mass = 0.0f;

    // The acceleration due to gravity (negative number)
    private float gravity = 0.0f;

    // The instantaneous position (in meters) of the object
    private Vector3f position = Vector3f.ZERO;

    // The instantaneous velocity (in meters/second) of the object
    private Vector3f velocity = Vector3f.ZERO;

    // The instantaneous acceleration (in meters/second/second) of the object
    private Vector3f acceleration = Vector3f.ZERO;

    /**
     * Constructor, takes the mass, velocity, acceleration, position, time of
     * the sample, and the acceleration due to gravity
     */
    public SampleInfo(float m, Vector3f p, Vector3f v, Vector3f a, float t, float g) {
        this.time = t;
        this.mass = m;
        this.velocity = v;
        this.acceleration = a;
        this.position = p;
        this.gravity = g;
    }

    /**
     * Returns the time of the sample (seconds).
     *
     * @return The time (in seconds)
     */
    public float getTime() {
        return time;
    }

    public float getMass () {
        return mass;
    }

    public float getGravity () {
        return gravity;
    }

    /**
     * Returns the position of the object (in meters).
     *
     * @return The position (in meters)
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Returns the velocity of the object (in meters/second).
     *
     * @return The velocity (in meters/second)
     */
    public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * Returns the acceleration of the object (in meters/second/second).
     *
     * @return The acceleration (in meters/second/second)
     */
    public Vector3f getAcceleration() {
        return acceleration;
    }

    /**
     * Returns the net force on the object (in Newtons).
     *
     * @return The net force (in Newtons)
     */
    public Vector3f getForce() {
        return acceleration.mult(mass);
    }

    /**
     * Returns the momentum on the object (in kilogram-meters/second).
     *
     * @return The momentum (in kilogram-meters/second)
     */
    public Vector3f getMomentum() {
        return velocity.mult(mass);
    }

    /**
     * Returns the potential energy of the object (in Joules).
     *
     * @return The potential energy (in Joules)
     */
    public float getPotentialEnergy() {
        return PhysicsUtils.getPotentialEnergy(position, mass, gravity);
    }

    /**
     * Returns the kinetic energy of the object (in Joules).
     *
     * @return The kinetic energy (in Joules)
     */
    public float getKineticEnergy() {
        return PhysicsUtils.getKineticEnergy(velocity, mass);
    }

    /**
     * Returns the total energy (kinetic + potential).
     *
     * @return The total energy
     */
    public float getTotalEnergy() {
        return getPotentialEnergy() + getKineticEnergy();
    }
}
