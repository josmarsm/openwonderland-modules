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
import java.awt.Image;

/**
 * Represents a type of a segment of roller coaster track.
 *
 * @author Bernard Horan, deronj
 */

public abstract class TrackSegmentType implements Comparable {

    private String name=null;
    private TCBKeyFrame[] defaultKeyFrames=null;
    protected String imageName = null;

    protected TrackSegmentType(String name) {
        this.name = name;
    }

    public TrackSegment createSegment() {
        return new TrackSegment(this);
    }

    public String getImageName () {
        return imageName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the defaultKeyFrames
     */
    public TCBKeyFrame[] getDefaultKeyFrames() {
        return defaultKeyFrames;
    }

    protected void setDefaultKeyFrames(TCBKeyFrame[] defaultKeyFrames) {
        this.defaultKeyFrames = defaultKeyFrames;
    }

    protected TCBKeyFrame createKeyFrame(float knot, Vector3f pos) {
        return createKeyFrame(knot, pos, 0, 0, 0, new Vector3f(0,1,0), 0f);
    }

    protected TCBKeyFrame createKeyFrame(float knot, Vector3f pos, float tension, float continuity, float bias, Vector3f rotAxis, float angle) {
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(angle, rotAxis);
        TCBKeyFrame ret = new TCBKeyFrame(knot, 0, pos, rot, new Vector3f(1,1,1), tension, continuity, bias);
        return ret;
    }

    /**
     * Return the transform of the endpoint of the track segment.
     *
     * These endpoints are concatenated to correctly orient subsequent segments
     * @return
     */
    abstract Matrix4f getEndpointTransform();

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + name + "]";
    }

    public int compareTo(Object anotherType) {
        return name.compareToIgnoreCase(((TrackSegmentType)anotherType).name);
    }

}