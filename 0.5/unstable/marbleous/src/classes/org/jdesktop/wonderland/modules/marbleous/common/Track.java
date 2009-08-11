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

import java.util.ArrayList;

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

    public void removeTrackSegment(TrackSegment trackSegment) {
        segments.remove(trackSegment);
    }

    public Iterable getTrackSegments() {
        return segments;
    }
}
