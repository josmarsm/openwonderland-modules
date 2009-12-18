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
import com.jme.math.Vector3f;

/**
 *
 * @author paulby
 */
public class StraightDropTrackSegmentType extends TrackSegmentType {

    private Matrix4f endpoint;

    public StraightDropTrackSegmentType() {
        super("Straight Drop");
        imageName = "RampDown.png";
        TCBKeyFrame[] keys = new TCBKeyFrame[] {
          createKeyFrame(0, new Vector3f(0,4,0)),
//          createKeyFrame(0.8f, new Vector3f(0,-0.4f,-4.9f), 1, 0, 0),
          createKeyFrame(1f, new Vector3f(0,0,-15))
        };

        setDefaultKeyFrames(keys);

        endpoint = new Matrix4f();
        endpoint.setTranslation(0,0,-15);
    }

    @Override
    Matrix4f getEndpointTransform() {
        return endpoint;
    }
}
