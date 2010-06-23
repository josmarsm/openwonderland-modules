/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.modulator.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author morris
 */
public class ModulatorQuaternionInterpolator implements PropertyInterpolator<Quaternion>
    {


    public Class getBasePropertyClass()
        {
        return Quaternion.class;
        }

    public Quaternion interpolate(Quaternion startQuat, Quaternion endQuat, float timelinePosition)
        {
        Quaternion newQuat = new Quaternion();

        float[] fromAngles = startQuat.toAngles(null);
        float[] toAngles = endQuat.toAngles(null);

        newQuat.fromAngles(fromAngles[0] +(toAngles[0] - fromAngles[0]) * timelinePosition,
                           fromAngles[1] +(toAngles[1] - fromAngles[1]) * timelinePosition,
                           fromAngles[2] +(toAngles[2] - fromAngles[2]) * timelinePosition);
//        System.out.println("Enter interpolate value = " + timelinePosition);
//        if(timelinePosition == 1.0f)
//            {
//            System.out.println("---------------------- FINISH ---------------------------");
//            }
/*
        Vector3f fromAxis = new Vector3f();
        Vector3f toAxis = new Vector3f();
        Quaternion newQuat = new Quaternion();

        float fromAngle = startQuat.toAngleAxis(fromAxis);
        float toAngle = endQuat.toAngleAxis(toAxis);

        newQuat.fromAngleAxis(fromAngle +(toAngle - fromAngle) * timelinePosition,
                new Vector3f(fromAxis.x +(toAxis.x - fromAxis.x) * timelinePosition,
                             fromAxis.y +(toAxis.y - fromAxis.y) * timelinePosition,
                             fromAxis.z +(toAxis.z - fromAxis.z) * timelinePosition));
*/
        return newQuat;
        }
    }
