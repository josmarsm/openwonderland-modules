/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import com.jme.math.Vector3f;

/**
 *
 * @author JagWire
 */
public class VectorMathUtils {
    private VectorMathUtils() {
        
    }

    public static Vector3f getRightVector(Vector3f lookVector) {
        Vector3f upVector = new Vector3f(0, 1, 0);

        return lookVector.cross(upVector).normalize();
    }

    public static Vector3f getLeftVector(Vector3f lookVector) {
        Vector3f upVector = new Vector3f(0, 1, 0);

        return upVector.cross(lookVector).normalize();
    }

}
