/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author morrisford
 */
public class FloatPropertyInterpolator implements PropertyInterpolator<Float>
    {

    public Class getBasePropertyClass()
        {
        return Float.class;
        }

    public Float interpolate(Float from, Float to, float timelinePosition)
        {
        return from +(to - from) * timelinePosition;
        }

    }
