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
package org.jdesktop.wonderland.modules.timeline.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDateRange;

/**
 * Basic configuration data common to timelines. This object is both Java
 * and JAXB serializable.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name = "timeline-config")
public class TimelineConfiguration implements Serializable {

    private float radsPerSegment;
    /** The total number of segments. */
    private int numSegments = -1;
    /**
     * The pitch of the helix (which is the vertical distance in meters of one
     * complete turn).
     */
    private float pitch = 4.5f;
    /**
     * The date range this timeline covers.
     */
    private TimelineDate dateRange;

    public enum TimelineUnits {

        HOURS(Calendar.HOUR_OF_DAY), DAYS(Calendar.DAY_OF_YEAR),
        WEEKS(Calendar.WEEK_OF_YEAR), MONTHS(Calendar.MONTH),
        YEARS(Calendar.YEAR);
        private int calendarUnit;

        TimelineUnits(int calendarUnit) {
            this.calendarUnit = calendarUnit;
        }

        public int getCalendarUnit() {
            return calendarUnit;
        }
    };
    TimelineUnits units = TimelineUnits.MONTHS;
    // default to 3 units/rev
    private float unitsPerRev = 3;
    /**
     * The inner radius of the spiral, in meters.
     */
    private float innerRadius = 5.0f;
    /**
     * The outer radius of the spiral, in meters.
     */
    private float outerRadius = 12.5f;

    /**
     * Default constructor
     */
    public TimelineConfiguration() {
        Calendar now = Calendar.getInstance();
        Calendar yearAgo = Calendar.getInstance();
        yearAgo.roll(Calendar.YEAR, -1);
        dateRange = new TimelineDate(yearAgo.getTime(), now.getTime());
        calculateRadsPerSegment();
    }

    public TimelineConfiguration(TimelineConfiguration config) {
        this.dateRange = config.getDateRange();
        this.units = config.getUnits();
        this.pitch = config.getPitch();
        this.radsPerSegment = config.getRadsPerSegment();

        calculateNumSegments();

//        calculateRadsPerSegment();
    }

    /**
     * Convenience constructor
     * @param dateRange the range of dates this timeline covers
     */
    public TimelineConfiguration(TimelineDate dateRange, TimelineUnits units) {
        this.dateRange = dateRange;
        this.units = units;

        calculateNumSegments();
        calculateRadsPerSegment();
    }

    private void calculateRadsPerSegment() {
        //    radsPerSegment = (float) (Math.PI / 3);
        radsPerSegment = ((float) (Math.PI * 2)) / unitsPerRev;
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
        calculateNumSegments();
    }

    /**
     * Get the number of segments in the timeline
     * @return the number of segments in the timeline
     */
    @XmlTransient
    public int getNumSegments() {
        return numSegments;
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

    public TimelineUnits getUnits() {
        return units;
    }

    public void setUnits(TimelineUnits units) {
        this.units = units;
        calculateNumSegments();
    }

    public float getUnitsPerRev() {
        return unitsPerRev;
    }

    public void setUnitsPerRev(float unitsPerRev) {
        this.unitsPerRev = unitsPerRev;
    }

    private void calculateNumSegments() {
        TimelineDateRange range = new TimelineDateRange(getDateRange().getMinimum(),
                getDateRange().getMaximum(),
                getUnits().getCalendarUnit());
        dateRange = new TimelineDate(range.getMinimum(), range.getMaximum());
        numSegments = range.getIncrementCount();
    }

    /**
     *
     * @return The derived height of the timeline, based on the pitch, rads per segment, and number of segments.
     */
    public float getHeight() {
        return getNumTurns() * pitch;
    }

    public float getNumTurns() {

        return (float) ((radsPerSegment * numSegments) / (Math.PI * 2));
    }

    @XmlElement
    public float getInnerRadius() {
        return innerRadius;
    }

    @XmlElement
    public float getOuterRadius() {
        return outerRadius;
    }

    public void setInnerRadius(float innerRadius) {
        this.innerRadius = innerRadius;
    }

    public void setOuterRadius(float outerRadius) {
        this.outerRadius = outerRadius;
    }
    public static final float RADS_PER_MESH = (float) (Math.PI / 18);

    /**
     * Given the values in this configuration object, generate a set of
     * TimelineSegment objects that represent the proper division of the
     * dateRange into numSegments Segments. Also calculates their CellTransforms,
     * which we can use both to lay out the entities on the client as well as
     * do Cell layout for Viewer cells on the server. Routing both
     * server and client through this method guarantees that their models of
     * time match up.
     *
     * @return A set of TimelineSegments that divide dateRange into numSegments intervals, with cellTransforms set in a spiral.
     */
    public DatedSet generateSegments() {
        DatedSet out = new DatedSet();

        float radius = getOuterRadius() - getInnerRadius();
        float angle = 0;

        TimelineDateRange range = new TimelineDateRange(getDateRange().getMinimum(),
                                                        getDateRange().getMaximum(),
                                                        getUnits().getCalendarUnit());
        int i = 0;
        for (TimelineDate increment : range.getIncrements()) {
            TimelineSegment newSeg = new TimelineSegment(increment);

            Vector3f pos = new Vector3f(((float) (radius * Math.sin(i * getRadsPerSegment()))), i * getHeight() / getNumSegments(), (float) ((float) radius * Math.cos(i * getRadsPerSegment())));
            newSeg.setTransform(new CellTransform(new Quaternion(), pos));
            newSeg.setStartAngle(angle);
            newSeg.setEndAngle(angle + getRadsPerSegment());

            angle += getRadsPerSegment();

            out.add(newSeg);

            i++;
        }

        return out;
    }
}
