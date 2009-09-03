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

import com.jme.math.Matrix3f;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;

/**
 * Simple converter from a CMU orthogonal matrix to a jME Matrix3f.
 * @author kevin
 */
public class OrthogonalMatrix3x3Converter {

    private Matrix3f matrix;

    /**
     * Stantdard constructor.
     * @param rotation The matrix to translate
     */
    public OrthogonalMatrix3x3Converter(OrthogonalMatrix3x3 rotation) {
        matrix = new Matrix3f(
                (float) rotation.right.x, (float) rotation.up.x, (float) rotation.backward.x,
                (float) rotation.right.y, (float) rotation.up.y, (float) rotation.backward.y,
                (float) rotation.right.z, (float) rotation.up.z, (float) rotation.backward.z);
    }

    /**
     * Get the jME-compatible equivalent matrix.
     * @return The jME matrix equivalent to the provided CMU matrix
     */
    public Matrix3f getMatrix3f() {
        return matrix;
    }
}
