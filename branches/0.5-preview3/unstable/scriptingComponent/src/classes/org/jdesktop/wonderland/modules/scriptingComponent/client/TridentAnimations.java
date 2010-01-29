/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TimelineScenario;
import org.pushingpixels.trident.ease.Spline;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author morrisford
 */
public class TridentAnimations
    {
    private float value;
    private ScriptingComponent sc;

    public TridentAnimations(ScriptingComponent SC)
        {
        this.sc = SC;
        }
    public TridentAnimations()
        {

        }
    public void setValue(float newValue)
        {
        this.value = newValue;
        sc.setTranslation(newValue * 2, 0, 0, 1);
        }

    public void go(ScriptingComponent SC)
        {
        System.out.println("Enter TridentAnimations.go() - scripting comp = " + SC);

        TridentAnimations tt = new TridentAnimations(SC);
        Timeline timeline = new Timeline(tt);
        timeline.addPropertyToInterpolate("value", 0.0f, 1.0f, new FloatPropertyInterpolator());
        timeline.setEase(new Spline(0.4f));
        timeline.play();
        }

    public void go1(ScriptingComponent SC)
        {
        TridentAnimations tt = new TridentAnimations(SC);
        TimelineScenario result = new TimelineScenario.Sequence();
        Timeline t = new Timeline(tt);
        t.addPropertyToInterpolate("value", 0.0f, 1.0f, new FloatPropertyInterpolator());
        t.setEase(new Spline(0.4f));
        t.setDuration(2000);
        result.addScenarioActor(t);

        Timeline u = new Timeline(tt);
        u.addPropertyToInterpolate("value", 1.0f, 0.0f, new FloatPropertyInterpolator());
        u.setDuration(4000);
        result.addScenarioActor(u);
        result.play();
        }
    }

class FloatPropertyInterpolator implements PropertyInterpolator<Float>
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
