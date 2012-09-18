/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.flapper2.client;

import com.jme.math.Vector3f;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author morris
 */
public class flapper2VectorInterpolator implements PropertyInterpolator<Vector3f>
    {


    public Class getBasePropertyClass()
        {
        return Vector3f.class;
        }

    public Vector3f interpolate(Vector3f from, Vector3f to, float timelinePosition)
        {
        float FX = from.x;
        float FY = from.y;
        float FZ = from.z;

        float TX = to.x;
        float TY = to.y;
        float TZ = to.z;

        Vector3f newTranslation = new Vector3f(FX + (TX - FX) * timelinePosition, FY + (TY - FY) * timelinePosition, FZ + (TZ - FZ) * timelinePosition);

        return newTranslation;
        }
    }
