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
public class TridentAnimations
    {
    private float value;

    public void setValue(float newValue)
        {
        System.out.println(this.value + " -> " + newValue);
        this.value = newValue;
        }

    public void go()
        {
        System.out.println("Enter TridentAnimations.go()");
        TridentAnimations tt = new TridentAnimations();
        Timeline timeline = new Timeline(tt);
        timeline.addPropertyToInterpolate("value", 0.0f, 1.0f);
        timeline.play();

        try
            {
            Thread.sleep(3000);
            }
        catch (Exception exc)
            {
            }
        }

    }
