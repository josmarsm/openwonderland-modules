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
package org.jdesktop.wonderland.modules.timeline.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

/**
 * Basic configuration data common to timelines. This object is both Java
 * and JAXB serializable.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="timeline-config")
public abstract class TimelineConfiguration implements Serializable {
    /** Radians per segment. Default is PI / 4 (90 degrees)*/
    private float radsPerSegment = (float) (Math.PI / 4);

    /** The total number of segments. */
    private int numSegments = 10;

    /**
     * The pitch of the helix (which is the vertical distance in meters of one
     * complete turn).
     */
    private float pitch = 2.0f;

    /**
     * The date range this timeline covers.
     */
    private TimelineDate dateRange;

    /**
     * Default constructor
     */
    public TimelineConfiguration() {
    }

    /**
     * Convenience constructor
     * @param dateRange the range of dates this timeline covers
     */
    public TimelineConfiguration(TimelineDate dateRange) {
        this.dateRange = dateRange;
    }

    /**
     * Get the date range this timeline covers
     * @return the date range this timeline covers
     */
    @XmlElement
    public TimelineDate getDateRange() {
        return dateRange;
    }

    /**
     * Set the date range this timeline covers
     * @param dateRange the date range this timeline covers
     */
    public void setDateRange(TimelineDate dateRange) {
        this.dateRange = dateRange;
    }

    /**
     * Get the number of segments in the timeline
     * @return the number of segments in the timeline
     */
    @XmlElement
    public int getNumSegments() {
        return numSegments;
    }

    /**
     * Set the number of segments in this timeline
     * @param numSegments the number of segments
     */
    public void setNumSegments(int numSegments) {
        this.numSegments = numSegments;
    }

    /**
     * Get the pitch of the timeline
     * @return the pitch
     */
    @XmlElement
    public float getPitch() {
        return pitch;
    }

    /**
     * Set the pitch
     * @param pitch the pitch to set
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Get the number of radians each segment of the timeline represents
     * @return the number of radians per segment
     */
    @XmlElement
    public float getRadsPerSegment() {
        return radsPerSegment;
    }

    /**
     * Set the number of radians per segment
     * @param radsPerSegment the radians per segment
     */
    public void setRadsPerSegment(float radsPerSegment) {
        this.radsPerSegment = radsPerSegment;
    }

    /**
     *
     * @return The derived height of the timeline, based on the pitch, rads per segment, and number of segments.
     */
    public float getHeight() {
        float numTurns = (float) ((radsPerSegment * numSegments) / (Math.PI * 2));
        return numTurns * pitch;
    }

    public abstract void sendUpdatedConfiguration();
}
