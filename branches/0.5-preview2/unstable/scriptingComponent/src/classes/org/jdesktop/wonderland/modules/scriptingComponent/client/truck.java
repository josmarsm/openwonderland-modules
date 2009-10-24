/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.pushingpixels.trident.Timeline;

/**
 *
 * @author morrisford
 */
public class truck extends vehicle
    {
    float XX = X;
    public truck(ScriptingComponent SC)
        {
        super(SC);
        }

    public void motivate(float XData, float ZData)
        {
        initialize(XData, ZData);

        timeline = new Timeline(this);
        if(ZData > moveThreshold)
            {
            timeline.addPropertyToInterpolate("XandZ", initialX, initialX + (Math.abs(ZData) * rotationX * moveMultiplier), new FloatPropertyInterpolator());
            timeline.addPropertyToInterpolate("ZandX", initialZ, initialZ + (Math.abs(ZData) * rotationY * moveMultiplier), new FloatPropertyInterpolator());
            }
        else if(ZData < -moveThreshold)
            {
            timeline.addPropertyToInterpolate("XandZ", initialX, initialX - (Math.abs(ZData) * rotationX * rotationMultiplier), new FloatPropertyInterpolator());
            timeline.addPropertyToInterpolate("ZandX", initialZ, initialZ - (Math.abs(ZData) * rotationY * rotationMultiplier), new FloatPropertyInterpolator());
            }
        if(XData > turnThreshold)
            {
            timeline.addPropertyToInterpolate("RotPlus", initialAngle, initialAngle + (float)(Math.PI / (rotationMultiplier / Math.abs(XData))), new FloatPropertyInterpolator());
            }
        else if(XData < -turnThreshold)
            {
            timeline.addPropertyToInterpolate("RotMinus", initialAngle, initialAngle - (float)(Math.PI / (rotationMultiplier / Math.abs(XData))), new FloatPropertyInterpolator());
            }
        timeline.setDuration(moveInterval);
        timeline.play();
        }
    }
