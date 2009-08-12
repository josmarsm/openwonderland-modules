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

package org.jdesktop.wonderland.modules.timeline.common.audio;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import com.jme.math.Vector3f;

/**
 *
 *  
 */
public class TimelineSegmentChangeMessage extends CellMessage {

    private String callID;
    private String previousSegmentID;
    private String currentSegmentID;

    public TimelineSegmentChangeMessage(CellID cellID, String callID, 
	    String previousSegmentID, String currentSegmentID) {

	super(cellID);

	this.callID = callID;
	this.previousSegmentID = previousSegmentID;
	this.currentSegmentID = currentSegmentID;
    }

    public String getCallID() {
	return callID;
    }

    public String getPreviousSegmentID() {
	return previousSegmentID;
    }

    public String getCurrentSegmentID() {
	return currentSegmentID;
    }

}
