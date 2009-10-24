/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author morrisford
 */
public class QuaternionPropertyInterpolator implements PropertyInterpolator<Quaternion>
    {

    public Class getBasePropertyClass()
        {
        return Quaternion.class;
        }

    public Quaternion interpolate(Quaternion from, Quaternion to, float timelinePosition)
        {
        Vector3f fromAxis = new Vector3f();
        Vector3f toAxis = new Vector3f();
        Quaternion newQuat = new Quaternion();

        float fromAngle = from.toAngleAxis(fromAxis);
        float toAngle = to.toAngleAxis(toAxis);

        newQuat.fromAngleAxis(fromAngle +(toAngle - fromAngle) * timelinePosition,
                new Vector3f(fromAxis.x +(toAxis.x - fromAxis.x) * timelinePosition,
                             fromAxis.y +(toAxis.y - fromAxis.y) * timelinePosition,
                             fromAxis.z +(toAxis.z - fromAxis.z) * timelinePosition));
        return newQuat;
        }
    }
