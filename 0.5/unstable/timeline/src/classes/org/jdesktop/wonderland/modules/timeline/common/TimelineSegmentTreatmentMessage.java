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

import org.jdesktop.wonderland.common.messages.Message;

import com.jme.math.Vector3f;

/**
 *
 *  
 */
public class TimelineSegmentTreatmentMessage extends Message {

    private Vector3f[] points;
    private String segmentID;
    private String treatment;
    private double attenuator;

    public TimelineSegmentTreatmentMessage(Vector3f[] points, String segmentID, String treatment,
	    double attenuator) {

	this.points = points;
	this.segmentID = segmentID;
	this.treatment = treatment;
	this.attenuator = attenuator;
    }

    public Vector3f[] getPoints() {
	return points;
    }

    public String getSegmentID() {
	return segmentID;
    }

    public String getTreatment() {
	return treatment;
    }

    public double getAttenuator() {
	return attenuator;
    }

}
