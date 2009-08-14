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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

/**
 * Basic configuration data common to timelines. This object is both Java
 * and JAXB serializable.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="timeline-config")
public class TimelineConfiguration implements Serializable {
    private float radsPerSegment;

    /** The total number of segments. */
    private int numSegments = 8;

    /**
     * The pitch of the helix (which is the vertical distance in meters of one
     * complete turn).
     */
    private float pitch = 4.5f;

    /**
     * The date range this timeline covers.
     */
    private TimelineDate dateRange = new TimelineDate();

    public enum TimelineUnits{HOURS, DAYS, WEEKS, MONTHS, YEARS};
    TimelineUnits units = TimelineUnits.DAYS;

    // default to 4 units/rev
    private float unitsPerRev = 4;



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
      calculateRadsPerSegment();
    }

    public TimelineConfiguration(TimelineConfiguration config) {
        this.dateRange = config.getDateRange();
        this.numSegments = config.getNumSegments();
        this.pitch = config.getPitch();
        this.radsPerSegment = config.getRadsPerSegment();
//        calculateRadsPerSegment();
    }

    /**
     * Convenience constructor
     * @param dateRange the range of dates this timeline covers
     */
    public TimelineConfiguration(TimelineDate dateRange) {
        this.dateRange = dateRange;
        calculateRadsPerSegment();
    }

    private void calculateRadsPerSegment() {
  //    radsPerSegment = (float) (Math.PI / 3);
      radsPerSegment = ((float) (Math.PI * 2))/unitsPerRev;
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

    public TimelineUnits getUnits() {
      return units;
    }

    public void setUnits(TimelineUnits units) {
      this.units = units;
    }

    public float getUnitsPerRev() {
      return unitsPerRev;
    }

    public void setUnitsPerRev(float unitsPerRev) {
      this.unitsPerRev = unitsPerRev;
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

        long dateIncrement = getDateRange().getRange() / getNumSegments();
        long curTime = getDateRange().getMinimum().getTime();
        float radius = getOuterRadius() - getInnerRadius();
        
        float angle = 0;

        for(int i=0; i< getNumSegments(); i++) {

            TimelineSegment newSeg = new TimelineSegment(new TimelineDate(new Date(curTime), new Date(curTime + dateIncrement)));

            Vector3f pos = new Vector3f(((float)(radius * Math.sin(i*getRadsPerSegment()))), i*getHeight()/getNumSegments(),(float) ((float)radius * Math.cos(i*getRadsPerSegment())));
            newSeg.setTransform(new CellTransform(new Quaternion(), pos));
            newSeg.setStartAngle(angle);
            newSeg.setEndAngle(angle+getRadsPerSegment());

            angle += getRadsPerSegment();
            
            out.add(newSeg);

            curTime += dateIncrement;
        }

        return out;
    }
}
