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
package org.jdesktop.wonderland.modules.cmu.player.conversions.math;

import com.jme.math.Vector3f;
import edu.cmu.cs.dennisc.math.Point3;


/**
 * Simple converter from a CMU Point3 object to a jME vector.
 * @author kevin
 */
public class Point3Converter {

    private Vector3f point;

    /**
     * Standard constructor.
     * @param point The point to translate
     */
    public Point3Converter(Point3 point) {
        this.point = new Vector3f((float)point.x, (float)point.y, (float)point.z);
    }

    /**
     * Get the jME vector equivalent to the point provided.
     * @return The equivalent jME vector
     */
    public Vector3f getVector3f() {
        return this.point;
    }
}
