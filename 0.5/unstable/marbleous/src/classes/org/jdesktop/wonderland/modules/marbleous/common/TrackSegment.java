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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.jdesktop.wonderland.modules.marbleous.client.jme.TCBKeyFrame;



/**
 * Represents a segment of roller coaster track.
 *
 * @author Bernard Horan
 */

public class TrackSegment  {
    private TrackSegmentType segmentType;
    private Properties segmentProperties = new Properties();
    private TCBKeyFrame[] keyFrames = null;

    TrackSegment(TrackSegmentType segmentType) {
        this.segmentType = segmentType;
        keyFrames = segmentType.getDefaultKeyFrames();
    }

    public TCBKeyFrame[] getKeyFrames() {
        return keyFrames;
    }

    public TrackSegmentType getTrackSegmentType() {
        return segmentType;
    }

    /**
     * Compute and return the KeyFrames for this segment in world coordinates
     * @param worldTransform the world transform
     * @return collection of KeyFrames in world coordinates
     */
    Collection<TCBKeyFrame> computeWorldKeyFrames(Matrix4f worldTransform, int segmentNumber, int totalSegments) {
        ArrayList<TCBKeyFrame> ret = new ArrayList();

        for(TCBKeyFrame f : keyFrames) {
            TCBKeyFrame worldFrame = new TCBKeyFrame(f);
            worldFrame.position = worldTransform.mult(worldFrame.position);
            System.err.println("old "+worldFrame.knot+" "+segmentNumber+"  "+totalSegments);

            worldFrame.knot = (worldFrame.knot+segmentNumber)/totalSegments;
            System.err.println("New Knot "+worldFrame.knot);
            ret.add(worldFrame);
        }

        return ret;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + segmentType + "]";
    }
}
