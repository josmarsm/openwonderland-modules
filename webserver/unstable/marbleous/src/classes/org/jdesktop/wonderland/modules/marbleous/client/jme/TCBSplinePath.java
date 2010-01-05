/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.marbleous.client.jme;

import org.jdesktop.wonderland.modules.marbleous.common.TCBKeyFrame;
import com.jme.math.Matrix4f;

/**
 * TCBSplinePath behavior.  This class defines the base class for
 * all TCB (Kochanek-Bartels) Spline Path Interpolators.  
 */

public abstract class TCBSplinePath {
  
    private   int             keysLength;
    
    /**
     * An array of KBKeyFrame for interpolator
     */
    protected TCBKeyFrame[]   keyFrames;

    /**
     * This value is the distance between knots 
     * value which can be used in further calculations by the subclass.
     */
    protected float currentU;

    /**
     * The lower knot
     */
    protected int lowerKnot;

    /**
     * The upper knot
     */
    protected int upperKnot;

    protected Matrix4f axis;
    protected Matrix4f axisInverse;

    /**
     * Constructs a TCBSplinePath node with a null alpha value and
     * a null target of TransformGroup
     */

    TCBSplinePath() {
    }
  
    /**
     * Constructs a new TCBSplinePath object that interpolates
     * between keyframes with specified alpha, target and default axisOfTransform
     * set to identity. It takes at least two key frames. The first key 
     * frame's knot must have a value of 0.0 and the last knot must have a
     * value of 1.0.  An intermediate key frame with index k must have a 
     * knot value strictly greater than the knot value of a key frame with 
     * index less than k. Once this constructor has all the valid key frames
     * it creates its own list of key fames that duplicates the first key frame
     * at the beginning of the list and the last key frame at the end of the
     * list. 
     * @param alpha the alpha object for this interpolator
     * @param target the TransformGroup node effected by this TCBSplinePath
     * @param keys an array of TCBKeyFrame. Requires at least two key frames.
     */
    public TCBSplinePath(TCBKeyFrame keys[]) {
        processKeyFrames(keys);
    }
    
    /**
     * Constructs a new TCBSplinePath object that interpolates
     * between keyframes with specified alpha, target and axisOfTransform.
     * It takes at least two key frames. The first key 
     * frame's knot must have a value of 0.0 and the last knot must have a
     * value of 1.0.  An intermediate key frame with index k must have a 
     * knot value strictly greater than the knot value of a key frame with 
     * index less than k. Once this constructor has all the valid key frames
     * it creates its own list of key fames that duplicates the first key frame
     * at the beginning of the list and the last key frame at the end of the
     * list. 
     * @param alpha the alpha object for this interpolator
     * @param target the TransformGroup node effected by this TCBSplinePath
     * @param axisOfTransform the transform that defines the local coordinate
     * @param keys an array of TCBKeyFrame. Requires at least two key frames.
     */
    public TCBSplinePath(Matrix4f axisOfTransform,
				     TCBKeyFrame keys[]) {
        this.axis = axisOfTransform;
        this.axisInverse = this.axis.invert();
	processKeyFrames(keys);
    }
    
    /**
     * Process the new array of key frames
     */
    private void processKeyFrames( TCBKeyFrame keys[] ){
	
    // Make sure that we have at least two key frames
        keysLength = keys.length;
        if (keysLength < 2) {
	    throw new IllegalArgumentException("TCBSplinePathInterpolator0");

        }

        // Make sure that the first key frame's knot is equal to 0.0 
	if (keys[0].knot < -0.0001 || keys[0].knot > 0.0001) {
	    throw new IllegalArgumentException("TCBSplinePathInterpolator1");
	}
	
        // Make sure that the last key frames knot is equal to 1.0 
	if (keys[keysLength-1].knot -1.0 < -0.0001 || keys[keysLength-1].knot -1.0 > 0.0001) {
	    throw new IllegalArgumentException("TCBSplinePathInterpolator2");	
	}

        // Make sure that all the knots are in sequence 
	for (int i = 0; i < keysLength; i++)  {
	    if (i>0 && keys[i].knot < keys[i-1].knot) {
		throw new IllegalArgumentException("TCBSplinePathInterpolator3");
	    }
	}

        // Make space for a leading and trailing key frame in addition to 
        // the keys passed in
        keyFrames = new TCBKeyFrame[keysLength+2];
        keyFrames[0] = new TCBKeyFrame();
        keyFrames[0] = keys[0];
        for (int i = 1; i < keysLength+1; i++) {
	    keyFrames[i] = keys[i-1]; 
        }
        keyFrames[keysLength+1] = new TCBKeyFrame();
        keyFrames[keysLength+1] = keys[keysLength-1]; 

        // Make key frame length reflect the 2 added key frames
        keysLength += 2;
    }
    
    /**
     * This method retrieves the length of the key frame array.
     * @return the number of key frames 
     */
    public int getArrayLength(){
	return keysLength-2;
    }
  
    /**
     * This method retrieves the key frame at the specified index.
     * @param index the index of the key frame requested
     * @return the key frame at the associated index
     */
    public TCBKeyFrame getKeyFrame (int index) {

        // We add 1 to index because we have added a leading keyFrame
	return this.keyFrames[index+1];
    }

    /**
     * This method computes the bounding knot indices and interpolation value
     * "CurrentU" given the specified value of alpha and the knots[] array.
     * @param alphaValue alpha value between 0.0 and 1.0
     */
    protected void computePathInterpolation(float alphaValue) {

        // skip knots till we find the two we fall between  
        int i = 1;
	int len = keysLength - 2;
        while ((alphaValue > keyFrames[i].knot) && (i < len)) {
	    i++;
        }
	
        if (i == 1) {
            currentU = 0f;
            lowerKnot = 1;
            upperKnot = 2;
        } else {
	    currentU = (alphaValue - keyFrames[i-1].knot)/
		(keyFrames[i].knot - keyFrames[i-1].knot);
	    lowerKnot = i-1;
	    upperKnot = i;
        }
    }

}
