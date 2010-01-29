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
package org.jdesktop.wonderland.modules.marbleous.common;

import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 *
 * @author paulby
 */
public class LeftTurnTrackSegmentType extends TrackSegmentType {

    private Matrix4f endpoint;

    public LeftTurnTrackSegmentType() {
        super("Left Turn");
        imageName = "TurnLeft.png";

        TCBKeyFrame[] keys = new TCBKeyFrame[] {
          createKeyFrame(0, new Vector3f(0,0,0)),
          createKeyFrame(0.5f, new Vector3f(-1,0,-4), -1, 0, 0, new Vector3f(-1,0,1), (float)Math.toRadians(-45)),
          createKeyFrame(1f, new Vector3f(-5,0,-5))
        };

        setDefaultKeyFrames(keys);

        endpoint = new Matrix4f();
        endpoint.setTranslation(-5,0,-5);
        Quaternion quat = new Quaternion();
        quat.fromAngleAxis((float)Math.PI/2f, new Vector3f(0,1,0));
        endpoint.setRotationQuaternion(quat);
    }

    @Override
    Matrix4f getEndpointTransform() {
        return endpoint;
    }
}
