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
import org.jdesktop.wonderland.modules.marbleous.client.jme.TCBKeyFrame;

/**
 *
 * @author paulby
 */
public class Track {

    private ArrayList<TrackSegment> segments = new ArrayList<TrackSegment>();

    public Track() {
    }

    public void addTrackSegment(TrackSegment trackSegment) {
        segments.add(trackSegment);
    }

    public int getSegmentCount() {
        return segments.size();
    }

    public TrackSegment getTrackSegmentAt(int index) {
        return segments.get(index);
    }

    public int indexOf(TrackSegment oldSegment) {
        return segments.indexOf(oldSegment);
    }

    public void removeTrackSegment(TrackSegment trackSegment) {
        segments.remove(trackSegment);
    }

    public Iterable getTrackSegments() {
        return segments;
    }

    public Collection<TCBKeyFrame> buildTrack() {
        ArrayList<TCBKeyFrame> keyFrames = new ArrayList();
        Matrix4f currentEndpoint = new Matrix4f();
        int segmentNumber = 0;
        for(TrackSegment segment : segments) {
            keyFrames.addAll(segment.computeWorldKeyFrames(currentEndpoint, segmentNumber, segments.size()));
            currentEndpoint.multLocal(segment.getTrackSegmentType().getEndpointTransform());
            segmentNumber++;
        }
        return keyFrames;
    }
}
