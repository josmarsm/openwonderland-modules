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
package org.jdesktop.wonderland.modules.timeline.common.provider;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;

/**
 * An audio file with an associated date
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedAudio implements DatedObject, Serializable {
    private TimelineDate date;
    private String audioURI;

    public DatedAudio(TimelineDate date, String audioURI) {
        this.date = date;
        this.audioURI = audioURI;
    }

    public TimelineDate getDate() {
        return date;
    }

    public String getAudioURI() {
        return audioURI;
    }

    @Override
    public String toString() {
        return "[DatedAudio " + audioURI + "]";
    }
}
