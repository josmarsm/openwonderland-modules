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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 *
 * @author paulby
 */
public class SplineTest {

    public RotPosScaleTCBSplinePath interp=null;

    public SplineTest() {
        TCBKeyFrame[] keys = new TCBKeyFrame[] {
          createKeyFrame(0, new Vector3f(0,0,0)),
          createKeyFrame(0.5f, new Vector3f(0,0,5)),
          createKeyFrame(1f, new Vector3f(5,0,5))
        };

        Matrix4f up = new Matrix4f();
        new Quaternion().toRotationMatrix(up);

        interp = new RotPosScaleTCBSplinePath(up, keys );

        Matrix4f result = new Matrix4f();
        float alpha = 0;
        while(alpha<=1) {
            interp.computeTransform(alpha, result);
            System.err.println(result);
            alpha+=0.1;
        }
    }

    private TCBKeyFrame createKeyFrame(float knot, Vector3f pos) {
        float tension = 0;
        float continuity = 0;
        float bias = 0;
        Quaternion rot = new Quaternion();
        TCBKeyFrame ret = new TCBKeyFrame(knot, 0, pos, rot, new Vector3f(1,1,1), tension, continuity, bias);
        return ret;
    }

    public static void main(String args[]) {
        new SplineTest();
    }
}
