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

import java.awt.Image;


/**
 * Represents a type of a segment of roller coaster track.
 *
 * @author Bernard Horan, deronj
 */

public class TrackSegmentType  {

    // TODO: define preview images in this array
    private static TrackSegmentType[] supportedTypes = new TrackSegmentType[] {
        new TrackSegmentType("Straight"),
        new TrackSegmentType("Loop"),
        new TrackSegmentType("Down Ramp"),
        new TrackSegmentType("Up Ramp")
    };

    private String name;
    private Image previewImage;

    public TrackSegmentType (String name) {
        this(name, null);
    }

    public TrackSegmentType (String name, Image previewImage) {
        this.name = name;
        this.previewImage = previewImage;
    }

    public String getName () {
        return name;
    }

    public Image getPreviewImage () {
        return previewImage;
    }

    public static TrackSegmentType[] getSupportedTypes () {
        return supportedTypes;
    }
}
