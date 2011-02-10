/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.animation;

import com.jme.math.Quaternion;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author JagWire
 */
public class QuaternionInterpolator implements PropertyInterpolator<Quaternion> {

    public Class getBasePropertyClass() {
        return Quaternion.class;
    }

    public Quaternion interpolate(Quaternion startRotation, Quaternion endRotation, float timelinePosition) {
        Quaternion rotation = new Quaternion();

        float[] fromAngles = startRotation.toAngles(null);
        float[] toAngles = endRotation.toAngles(null);

        rotation.fromAngles(fromAngles[0] + (toAngles[0] - fromAngles[0]) * timelinePosition,
                           fromAngles[1] + (toAngles[1] - fromAngles[1]) * timelinePosition,
                           fromAngles[2] + (toAngles[2] - fromAngles[2]) * timelinePosition);
        return rotation;
    }

}
