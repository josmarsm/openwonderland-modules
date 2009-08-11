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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + segmentType + "]";
    }
}
