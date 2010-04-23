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

import java.io.Serializable;

import com.jme.math.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author paulby, Bernard Horan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( namespace="marbleous" )
public class Track implements Serializable {

    private static int COUNTER = 1;

    private ArrayList<TrackSegment> segments = new ArrayList<TrackSegment>();

    public Track() {
    }

    public void addTrackSegment(TrackSegment trackSegment) {
        trackSegment.setID(COUNTER++);
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

    public void replaceTrackSegment(TrackSegment trackSegment) {
        int index = indexOf(trackSegment);
        segments.set(index, trackSegment);
    }

    public Iterable getTrackSegments() {
        return segments;
    }

    public Collection<TCBKeyFrame> buildTrack() {
        ArrayList<TCBKeyFrame> keyFrames = new ArrayList<TCBKeyFrame>();
        Matrix4f currentEndpoint = new Matrix4f();
        int segmentNumber = 0;
        for(TrackSegment segment : segments) {
            keyFrames.addAll(segment.computeWorldKeyFrames(currentEndpoint, segmentNumber, segments.size()));
            currentEndpoint.multLocal(segment.getEndpointTransform());
            segmentNumber++;
        }
        return keyFrames;
    }

    public Vector3f getMarbleStartPosition() {
        TrackSegment firstSegment = segments.get(0);
        TCBKeyFrame f = firstSegment.getKeyFrames()[0];
        Vector3f ret = new Vector3f(f.position);
        ret.z-=0.565f; 
        return ret;
    }
}
