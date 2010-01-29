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
public class BigDropTrackSegmentType extends TrackSegmentType {

    private Matrix4f endpoint;
    private BigDropSegmentSettings settings = new BigDropSegmentSettings();

    public BigDropTrackSegmentType() {
        super("Big Drop");
        imageName = "RampDown.png";
        settings.setHeight(20);
        settings.setRadius(9);
        settings.setEditorClassname("org.jdesktop.wonderland.modules.marbleous.client.ui.BigDropSegmentEditor");
        setDefaultSegmentSettings(settings);
        setDefaultKeyFrames(generateSpline(settings));
    }

    private TCBKeyFrame[] generateSpline(BigDropSegmentSettings settings) {

        float topY = settings.getHeight();
        float radius = settings.getRadius();
        int samples = 8;
        float finalAngle = (float) Math.toRadians(60);
        float endHeight=-1;

//        TCBKeyFrame[] keys = new TCBKeyFrame[] {
//          createKeyFrame(0, new Vector3f(0,14,0)),
//          createKeyFrame(0.6f, new Vector3f(0,4f,-4f), 1, 0, 0, new Vector3f(1,0,0), 0),
//          createKeyFrame(0.7f, new Vector3f(0,3f,-6f), 1, 0, 0, new Vector3f(1,0,0), 0),
//          createKeyFrame(0.8f, new Vector3f(0,2f,-8f), 1, 0, 0, new Vector3f(1,0,0), 0),
//          createKeyFrame(0.9f, new Vector3f(0,1f,-10f), 1, 0, 0, new Vector3f(1,0,0), 0),
//          createKeyFrame(1f, new Vector3f(0,-1,-12))
//        };

        TCBKeyFrame[] keys = new TCBKeyFrame[samples+1]; // Plus end & start
        float knot = 1f;
        float knotInc = 1f/(samples)/4;
        for(int i=1; i<=samples; i++) {
            float angle = (finalAngle/(samples))*i;
            float x = 0;
            float y = endHeight+radius-(float)Math.cos(angle)*radius;
            float z = (float)Math.sin(angle)*radius;
            keys[keys.length-i] = createKeyFrame(knot, new Vector3f(x,y,z), 1,0,0, new Vector3f(1,0,0), -angle);
            knot -= knotInc;
        }
        Vector3f lastCurvePos = keys[1].position;
        Vector3f startPos = new Vector3f(lastCurvePos);
        startPos.y=topY;
        startPos.z -= (float) ((topY - lastCurvePos.y) / Math.tan(Math.PI - finalAngle));

        keys[0] = createKeyFrame(0, startPos, 0,0,0, new Vector3f(1,0,0), -finalAngle);

//        for(int i=0; i<keys.length; i++)
//            keys[i].debugPrint(""+i);

        endpoint = new Matrix4f();
        endpoint.setTranslation(keys[keys.length-1].position);

        return keys;
    }

    @Override
    Matrix4f getEndpointTransform() {
        return endpoint;
    }

    @Override
    public void updateSegment(TrackSegment segment, SegmentSettings settings) {
        segment.setKeyFrames(generateSpline((BigDropSegmentSettings) settings));
    }
}
