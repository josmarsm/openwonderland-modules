/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.marbleous.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.utils.jaxb.QuaternionAdapter;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;

/**
 * This class represents a Key Frame that can be used for Kochanek-Bartels
 * (TCB) spline interpolation.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( namespace="marbleous" )
public class TCBKeyFrame implements Serializable {
  
    // Position, Rotation and Scale
    /**
     *
     */
    @XmlJavaTypeAdapter(Vector3fAdapter.class)
    public Vector3f position;

    @XmlJavaTypeAdapter(QuaternionAdapter.class)
    public Quaternion  quat;

    @XmlJavaTypeAdapter(Vector3fAdapter.class)
    public Vector3f scale;

    // Tension, Continuity & Bias
    public float  tension;
    public float  continuity;
    public float  bias;

    // Sample Time 
    public float  knot;

    // Interpolation type (linear = 0 -> spline interpolation)
    public int linear; 

    // default constructor 
    public TCBKeyFrame () {
        tension = continuity = bias = 0.0f;
    }

    public TCBKeyFrame (TCBKeyFrame kf) {
        this(kf.knot, kf.linear, kf.position, kf.quat, kf.scale,
                     kf.tension, kf.continuity, kf.bias);
        
    }

    /**
     * Creates a key frame using the given inputs. 
     *
     * @param k knot value for this key frame 
     * @param l the linear flag (0 - Spline Interp, 1, Linear Interp
     * @param pos the position at the key frame
     * @param q the rotations at the key frame
     * @param s the scales at the key frame
     * @param t tension (-1.0 < t < 1.0)
     * @param c continuity (-1.0 < c < 1.0)
     * @param b bias (-1.0 < b < 1.0)
     */
    public TCBKeyFrame (float k, int l, Vector3f pos, Quaternion q, Vector3f s,
                        float t, float c, float b) {

        knot     = k; 
        linear   = l;
        position = new Vector3f(pos);
        quat     = new Quaternion(q);
        scale    = new Vector3f(s);

        // Check for valid tension continuity and bias values
        if (t < -1.0f || t > 1.0f) {
            throw new IllegalArgumentException("TCBKeyFrame0");
        }

        if (b < -1.0f || b > 1.0f) {
            throw new IllegalArgumentException("TCBKeyFrame1");
        }

        if (c < -1.0f || c > 1.0f) {
            throw new IllegalArgumentException("TCBKeyFrame2");
        }

        // copy valid tension, continuity and bias values
        tension = t;
        continuity = c;
        bias = b;
    }

    /**
     * Prints information comtained in this key frame 
     * @param tag string tag for identifying debug message
     */
    public void debugPrint (String tag) {
        System.out.println ("\n" + tag); 
        System.out.println (" knot = " + knot); 
        System.out.println (" linear = " + linear); 
        System.out.println (" position(x,y,z) = " + position.x + " " 
                               + position.y + " " + position.z);
              
        System.out.println (" tension = " + tension); 
        System.out.println (" continuity = " + continuity); 
        System.out.println (" bias = " + bias); 
    }
}

