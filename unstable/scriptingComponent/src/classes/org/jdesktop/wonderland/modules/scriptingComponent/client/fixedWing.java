/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.pushingpixels.trident.Timeline;

/**
 *
 * @author morrisford
 */
public class fixedWing extends aircraft
    {
    public fixedWing(ScriptingComponent SC)
        {
        super(SC);
        }
    
    public void motivate(float XData, float ZData)
        {
        System.out.println("Enter motivate in fixedWing");
        initialize(XData, ZData);

        timeline = new Timeline(this);
        
        if(ZData > moveThreshold)
            {
            timeline.addPropertyToInterpolate("XandZ", initialX, initialX + (Math.abs(ZData) * rotationX * moveMultiplier), new FloatPropertyInterpolator());
            timeline.addPropertyToInterpolate("ZandX", initialZ, initialZ + (Math.abs(ZData) * rotationY * moveMultiplier), new FloatPropertyInterpolator());
            }

        if(XData > turnThreshold)
            {
            Quaternion toTurn = new Quaternion();
            Quaternion toRoll = new Quaternion();
            Quaternion step = new Quaternion();
            Quaternion to = new Quaternion();
            Vector3f axis = new Vector3f();
            float angle;

            toTurn.fromAngleAxis((float) (Math.PI / 12), new Vector3f(0, 1, 0));

            step = toTurn.mult(initialQuat);
            angle = step.toAngleAxis(axis);

            toRoll.fromAngleAxis((float) -(Math.PI / 12), new Vector3f(1, 0, -(float)Math.sin(angle)));
            angle = initialQuat.toAngleAxis(axis);
            System.out.println("In greater - Angle = " + angle + " - totalRollin = " + totalRollIn);
            if(rollInClicks >= totalRollIn)
                { 
                to.set(step);
                System.out.println(" Greater - Using no RollIn");
                }
            else
                { 
                to = toRoll.mult(step);
                System.out.println(" Greater - Using RollIn");
                }
 
            angle = to.toAngleAxis(axis);
            System.out.println("To before initiate rot - angle = " + angle + " - axis " + axis);
            timeline.addPropertyToInterpolate("RotQuatPlus", initialQuat, to, new QuaternionPropertyInterpolator());
            }
        else if(XData < -turnThreshold)
            {
            Quaternion toTurn = new Quaternion();
            Quaternion toRoll = new Quaternion();
            Quaternion step = new Quaternion();
            Quaternion to = new Quaternion();
            Vector3f axis = new Vector3f();
            float angle;

            toTurn.fromAngleAxis((float) -(Math.PI / 12), new Vector3f(0, 1, 0));

            step = toTurn.mult(initialQuat);
            angle = step.toAngleAxis(axis);
            toRoll.fromAngleAxis((float) (Math.PI / 12), new Vector3f(1, 0, 0));
//            toRoll.fromAngleAxis((float) (Math.PI / 12), new Vector3f(1, 0, (float)Math.sin(angle)));

            angle = initialQuat.toAngleAxis(axis);
            System.out.println("In less - Angle = " + angle + " - totalRollin = " + totalRollIn);
            if(rollInClicks <= -totalRollIn)
                { 
                to.set(step);
                System.out.println(" Less - Using no RollOut");
                }
            else
                { 
                to = toRoll.mult(step);
                System.out.println(" Less - Using RollOut");
                }
 
            angle = to.toAngleAxis(axis);
            System.out.println("To before initiate rot - angle = " + angle + " - axis " + axis);
            timeline.addPropertyToInterpolate("RotQuatMinus", initialQuat, to, new QuaternionPropertyInterpolator());
            }
/*        else if(XData < -turnThreshold)
            {
            timeline.addPropertyToInterpolate("RotMinusAircraft", 0f, - (float)(Math.PI / (rotationMultiplier / Math.abs(XData))), new FloatPropertyInterpolator());
            timeline.addPropertyToInterpolate("RotRollIn", currentRollIn, -(float)(totalRollIn / (rotationMultiplier / Math.abs(XData))), new FloatPropertyInterpolator());
            }
*/
        timeline.setDuration(100);
        timeline.play();
        }
    }
