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
public class LoopTrackSegmentType extends TrackSegmentType {

    private Matrix4f endpoint;

    public LoopTrackSegmentType() {
        super("Loop");
        imageName = "RampLoop.png";
        LoopSegmentSettings p = new LoopSegmentSettings();
        p.setEditorClassname("org.jdesktop.wonderland.modules.marbleous.client.ui.LoopSegmentEditor");
        p.setRadius(3);
        setDefaultSegmentSettings(p);
        setDefaultKeyFrames(generateSpline(((LoopSegmentSettings)getDefaultSegmentSettings())));
   }

    private TCBKeyFrame[] generateSpline(LoopSegmentSettings settings) {
        int samples = 16;        // Samples per loop
        int loops = 1;
        float radius=settings.getRadius();
        float finalAngle = (float)Math.PI*2*loops;
        float knot = 0;
        float xStepPerLoop = 2; // Distance between the loops

        TCBKeyFrame[] keys = new TCBKeyFrame[samples*loops]; // Plus end & start
        float knotInc = 1f/(keys.length-1);
        for(int i=0; i<keys.length; i++) {
            float angle = (finalAngle/(keys.length-1))*i;
            float x = ((xStepPerLoop*loops)/keys.length)*i;
            float y = radius-(float)Math.cos(angle)*radius;
            float z = -(float)Math.sin(angle)*radius;
            keys[i] = createKeyFrame(knot, new Vector3f(x,y,z), 0,0,0, new Vector3f(1,0,0), angle);
//            keys[i].debugPrint(""+Math.toDegrees(angle));
            knot += knotInc;
        }

//        TCBKeyFrame[] keys = new TCBKeyFrame[] {
//          createKeyFrame(0, new Vector3f(0,0,0)),
//          createKeyFrame(0.4f, new Vector3f(1,2.5f,-10), 1,0,0, new Vector3f(1,0,0), (float)Math.PI/2),
//          createKeyFrame(0.5f, new Vector3f(2,5,-5), 1, 0, 0, new Vector3f(1,0,0), (float)Math.PI),
//          createKeyFrame(0.6f, new Vector3f(3,2.5f,0), 1,0,0, new Vector3f(1,0,0), -(float)Math.PI/2),
//          createKeyFrame(1f, new Vector3f(3,0,-10))
//        };


        endpoint = new Matrix4f();
        endpoint.setTranslation(3,0,-10);

        return keys;
    }

    @Override
    Matrix4f getEndpointTransform() {
        return endpoint;
    }

    @Override
    public void updateSegment(TrackSegment segment, SegmentSettings settings) {
        segment.setKeyFrames(generateSpline((LoopSegmentSettings) settings));
    }
}
