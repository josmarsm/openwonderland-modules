/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of jVoiceBridge.
 *
 * jVoiceBridge is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License version 2 as 
 * published by the Free Software Foundation and distributed hereunder 
 * to you.
 *
 * jVoiceBridge is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied this 
 * code. 
 */
package org.jdesktop.wonderland.modules.timeline.server;

import com.sun.sgs.app.AppContext;

import java.io.Serializable;

import com.jme.math.Vector3f;

import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.VoiceManager;

public class TrapozoidleSpatializer implements Spatializer, Serializable {

    Vector3f[] points; 

    double minX;
    double maxX = Double.MAX_VALUE;
    double minY;
    double maxY = Double.MAX_VALUE;
    double minZ;
    double maxZ = Double.MAX_VALUE;

    double attenuator = 1.0;

    private double scale;

    /*
     * A 3 dimension trapezoid is called a Trapozoidle:
     *
     *   http://wiki.answers.com/Q/What_is_a_three_dimensional_trapezoid_called
     *
     * (this is a special case of truncated pyramid.)
     *
     * This code spatializes audio within a trapozoidle.
     */
    public TrapozoidleSpatializer() {
    }
	
    public TrapozoidleSpatializer(Vector3f[] points) {
        scale =  AppContext.getManager(VoiceManager.class).getVoiceManagerParameters().scale;

	this.points = points;

	for (int i = 0; i < 8; i++) {
	    if (points[i].getX() < minX) {
		minX = points[i].getX();
	    }
	    if (points[i].getY() < minY) {
		minY = points[i].getY();
	    }
	    if (points[i].getZ() < minZ) {
		minZ = points[i].getZ();
	    }
	    if (points[i].getX() > maxX) {
		maxX= points[i].getX();
	    }
	    if (points[i].getY() > maxY) {
		maxY= points[i].getY();
	    }
	    if (points[i].getZ() > maxZ) {
		maxZ= points[i].getZ();
	    }
	}

        //minX = Math.min(lowerLeftX / scale, upperRightX / scale);
    }

    public double[] spatialize(double sourceX, double sourceY, 
                               double sourceZ, double sourceOrientation, 
                               double destX, double destY, 
                               double destZ, double destOrientation)
    {
	// see if the destination is inside the audio range

        if (isInside(destX, destY, destZ)) {
	    return new double[] { 0, 0, 0, attenuator };
        } else {
            return new double[] { 0, 0, 0, 0 };
        }
    }
        
    private boolean isInside(double x, double y, double z) {
	if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ) {
	    return false;
	}

	/*
	 * Okay, so we know it's inside a 3-d rectangle which includes our trapozoidle.
	 * Now we have to determine if it's really inside or not.
	 * This is the hard part.
	 * Doug and Paul using the pick engine to detect a hit.
	 * An easier approach might be to use a collision detection component.
	 * Yet another possibility is to define a new BoundingVolume that the ProximityComponentMO
	 * understands.
	 */
	
	return true;
    }

    public void setAttenuator(double attenuator) {
	this.attenuator = attenuator;
    }

    public double getAttenuator() {
	return attenuator;
    }

    public Object clone() {
        TrapozoidleSpatializer t = new TrapozoidleSpatializer();

	for (int i = 0; i < 8; i++) {
	    t.points[i] = points[i];
	}

	t.attenuator = attenuator;

	return t;
    }

    public String toString() {
	String s = "TrapozoidleSpatializer";

	for (int i = 0; i < 8; i++) {
	    s += "p" + i + "=" + points[i] + ", ";
	}

	return s + "attenuator=" + attenuator;
    }

}
