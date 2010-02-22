/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;

/**
 *
 * @author morrisford
 */
public class mobile
    {
    protected float   initialX = 0.0f;
    protected float   initialY = 0.0f;
    protected float   initialZ = 0.0f;
    protected float   initialRotationX = 0.0f;
    protected float   initialRotationY = 0.0f;
    protected float   initialRotationZ = 0.0f;
    protected float   initialAngle = 0.0f;
    protected boolean firstEntry = true;
    protected float   moveThreshold = 0.1f;
    protected float   turnThreshold = 0.1f;
    protected float   rotationMultiplier = 1f;
    protected float   moveMultiplier = 1f;
    protected int     moveInterval = 1000;
    protected int     rotateInterval = 1000;
    protected float   X;
    protected float   Z;
    protected float   Rot;
    protected float   RotRollIn;
    protected Timeline timeline;
    protected float   rotationX;
    protected float   rotationY;
    protected float   totalRollIn = 8;
    protected int   currentRollIn = 0;
    protected int     rollInClicks = 0;
    protected Quaternion initialQuat;
    protected Quaternion rotQuat;

    protected ScriptingComponent    sc;


    public mobile(ScriptingComponent SC)
        {
        this.sc = SC;
        }

    public void setXandZ(float x)
        {
        this.X = x;
//        System.out.println("setXandZ - x = " + x + " - Z = " + Z);
        }

    public void setZandX(float z)
        {
        this.Z = z;
//        System.out.println("set ZandX - x = " + X + " - z = " + z);
        sc.setTranslation(X, initialY, Z, 0);
        }

    public void setRotPlus(float rot)
        {
        if(rot > Math.PI * 2)
            this.Rot = rot - (float)(Math.PI * 2);
        else
            this.Rot = rot;

        sc.setRotation(0, 1, 0, Rot, 0);
//        System.out.println("Rotating - Rot = " + Rot);
        }

    public void setRotMinus(float rot)
        {
        if(rot < 0.0f)
            {
            float temp = rot + (float)(Math.PI * 2);
            this.Rot = temp;
            }
        else
            this.Rot = rot;
        sc.setRotation(0, 1, 0, Rot, 0);
//        System.out.println("Rotating - Rot = " + Rot);
        }

    public void setRotQuatPlus(Quaternion rot)
        {
        Vector3f axis = new Vector3f();
        System.out.println("setRotQuat - quat = " + rot);
        
        this.rotQuat = rot;
        float angle = rot.toAngleAxis(axis);
        rollInClicks++;
        sc.setRotation(axis.x, axis.y, axis.z, angle, 0);
        System.out.println("setRotQuat - angle = " + angle + " - axis = " + axis + " Roll clicks now = " + rollInClicks);
        }

    public void setRotQuatMinus(Quaternion rot)
        {
        Vector3f axis = new Vector3f();
        System.out.println("setRotQuat - quat = " + rot);

        this.rotQuat = rot;
        float angle = rot.toAngleAxis(axis);
        rollInClicks--;
        sc.setRotation(axis.x, axis.y, axis.z, angle, 0);
        System.out.println("setRotQuat - angle = " + angle + " - axis = " + axis + " - Roll clicks now = " + rollInClicks);
        }

    public void configureMobile(String command, float value1)
        {
        if(command.equals("moveThreshold"))
            {
            moveThreshold = value1;
            System.out.println("moveThreshold set to " + moveThreshold);
            }
        if(command.equals("turnThreshold"))
            {
            turnThreshold = value1;
            System.out.println("turnThreshold set to " + turnThreshold);
            }
        if(command.equals("rotationMultiplier"))
            {
            rotationMultiplier = value1;
            System.out.println("rotationMultiplier set to " + rotationMultiplier);
            }
        if(command.equals("moveMultiplier"))
            {
            moveMultiplier = value1;
            System.out.println("moveMultiplier set to " + moveMultiplier);
            }
        if(command.equals("moveInterval"))
            {
            moveInterval = (int)value1;
            System.out.print("moveInterval set to " + moveInterval);
            }
        if(command.equals("rotateInterval"))
            {
            rotateInterval = (int)value1;
            System.out.print("rotateInterval set to " + rotateInterval);
            }
        }

    public void configureMobile(String command, float value1, float value2)
        {

        }

    public void configureMobile(String command, int value1)
        {
        }
    
    public void initialize(float XData, float ZData)
        {
        if(XData > turnThreshold || XData < -turnThreshold || ZData > moveThreshold || ZData < -moveThreshold)
            {
            if(!firstEntry)
                timeline.cancel();
            sc.getInitialPosition();
            this.initialX = sc.getInitialX();
            this.initialY = sc.getInitialY();
            this.initialZ = sc.getInitialZ();
            sc.getInitialRotation();
            this.initialRotationX = sc.getInitialRotationX();
            this.initialRotationY = sc.getInitialRotationY();
            this.initialRotationZ = sc.getInitialRotationZ();
            this.initialAngle = sc.getInitialAngle();
            this.initialQuat = sc.getInitialQuat();
//            if(initialAngle == 0f)
//                initialAngle = (float)Math.PI * 2;
//            System.out.println("Initial rotation = " + initialAngle);
//            System.out.println("initial rotation - X = " + initialRotationX + " - Y = " + initialRotationY + " - Z = " + initialRotationZ);
            rotationX = (float)Math.sin(initialAngle);
            rotationY = (float)Math.cos(initialAngle);
            firstEntry = false;
            }
        }
    }
