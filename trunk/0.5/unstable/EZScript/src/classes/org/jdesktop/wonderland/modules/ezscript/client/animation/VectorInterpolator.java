/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.animation;

import com.jme.math.Vector3f;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author JagWire
 */
public class VectorInterpolator implements PropertyInterpolator<Vector3f> {

    public Class getBasePropertyClass() {
        return Vector3f.class;
    }

    public Vector3f interpolate(Vector3f startVector, Vector3f endVector, float timelinePosition) {

        float startX = startVector.x;
        float startY = startVector.y;
        float startZ = startVector.z;

        float endX = endVector.x;
        float endY = endVector.y;
        float endZ = endVector.z;

        Vector3f translation
                    = new Vector3f(startX + (endX - startX) * timelinePosition,
                                   startY + (endY - startY) * timelinePosition,
                                   startZ + (endZ - startZ) * timelinePosition);

        return translation;
    }

}
