/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;



/**
 * Represents a segment of roller coaster track.
 *
 * @author Bernard Horan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( namespace="marbleous" )
public class TrackSegment implements Serializable {
    private String segmentTypeClassName;
    private SegmentSettings segmentProperties;
    private TCBKeyFrame[] keyFrames = null;
    private String name;
    private Matrix4f endpointTransform;
    private int id;

    public TrackSegment() {
        
    }

    TrackSegment(TrackSegmentType segmentType) {
        segmentTypeClassName = segmentType.getClass().getCanonicalName();
        keyFrames = segmentType.getDefaultKeyFrames();
        name = segmentType.getName();
        endpointTransform = segmentType.getEndpointTransform();
        segmentProperties = segmentType.getDefaultSegmentSettings();
    }

    public String getName () {
        return name;
    }

    public TCBKeyFrame[] getKeyFrames() {
        return keyFrames;
    }

    void setKeyFrames(TCBKeyFrame[] keyFrames) {
        this.keyFrames = keyFrames;
    }

    public Matrix4f getEndpointTransform() {
        return endpointTransform;
    }

    public SegmentSettings getSegmentSettings() {
        return segmentProperties;
    }

    /**
     * Compute and return the KeyFrames for this segment in world coordinates
     * @param worldTransform the world transform
     * @return collection of KeyFrames in world coordinates
     */
    Collection<TCBKeyFrame> computeWorldKeyFrames(Matrix4f worldTransform, int segmentNumber, int totalSegments) {
        ArrayList<TCBKeyFrame> ret = new ArrayList<TCBKeyFrame>();

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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object aThat){
        if ( this == aThat ) {
            return true;
        }
      if ( !(aThat instanceof TrackSegment) ) {
          return false;
      }
      TrackSegment that = (TrackSegment)aThat;
      return id == that.id;
    }

    void setID(int i) {
        id = i;
    }

    public Object saveToMemento() {
        //System.out.println("Originator: Saving to Memento.");
        TCBKeyFrame[] savedKeyFrames = new TCBKeyFrame[keyFrames.length];
        for (int i = 0; i < keyFrames.length; i++) {
            savedKeyFrames[i] = new TCBKeyFrame(keyFrames[i]);
        }
        return new Memento(savedKeyFrames);
    }

    public void restoreFromMemento(Object m) {
        if (m instanceof Memento) {
            Memento memento = (Memento) m;
            keyFrames = memento.getSavedKeyFrames();
            //System.out.println("Originator: State after restoring from Memento: " + keyFrames);
        }
    }

    private static class Memento {

        private TCBKeyFrame[] keyFrames = null;

        public Memento(TCBKeyFrame[] keyFramesToSave) {
            keyFrames = keyFramesToSave;
        }

        public TCBKeyFrame[] getSavedKeyFrames() {
            return keyFrames;
        }

    }
}
