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
import edu.cmu.cs.dennisc.math.Matrix3x3;

/**
 * Simple converter from a CMU scale matrix to a scalar for use in
 * jME scaling.
 * @author kevin
 */
public class ScaleConverter {

    private final Vector3f scale;

    /**
     * Standard constructor.
     * @param scale The matrix to translate
     */
    public ScaleConverter(Matrix3x3 scale) {
        this.scale = new Vector3f((float)scale.right.x, (float)scale.up.y, (float)scale.backward.z);
    }

    /**
     * Get the scalar scale represented by the provided matrix.
     * @return Scale represented by the matrix
     */
    public Vector3f getScale() {
        return this.scale;
    }
}
