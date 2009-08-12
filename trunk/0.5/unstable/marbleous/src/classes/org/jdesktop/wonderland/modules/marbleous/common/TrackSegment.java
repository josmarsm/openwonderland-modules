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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;



/**
 * Represents a segment of roller coaster track.
 *
 * @author Bernard Horan
 */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType( namespace="marbleous" )
public class TrackSegment implements Serializable {
    private String segmentTypeClassName;
    private Properties segmentProperties = new Properties();
    private TCBKeyFrame[] keyFrames = null;
    private String name;
    private Matrix4f endpointTransform;

    public TrackSegment() {
        
    }

    TrackSegment(TrackSegmentType segmentType) {
        segmentTypeClassName = segmentType.getClass().getCanonicalName();
        keyFrames = segmentType.getDefaultKeyFrames();
        name = segmentType.getName();
        endpointTransform = segmentType.getEndpointTransform();
    }

    public String getName () {
        return name;
    }

    public TCBKeyFrame[] getKeyFrames() {
        return keyFrames;
    }

    public Matrix4f getEndpointTransform() {
        return endpointTransform;
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
            worldFrame.knot = (worldFrame.knot+segmentNumber)/totalSegments;
            ret.add(worldFrame);
        }

        return ret;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + segmentTypeClassName + "]";
    }
}
