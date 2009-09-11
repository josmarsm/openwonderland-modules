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
package org.jdesktop.wonderland.modules.timeline.client.audio;

import org.jdesktop.wonderland.modules.timeline.common.audio.TimelinePlayRecordingMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineRecordMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentTreatmentMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import java.util.Date;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineAudioComponentClientState;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineCallIDMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineResetMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineTreatmentDoneMessage;

import com.jme.math.Vector3f;

/**
 * A component that provides audio participant control
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class TimelineAudioComponent extends CellComponent implements ComponentMessageReceiver {
    
    private static Logger logger = Logger.getLogger(TimelineAudioComponent.class.getName());

    @UsesCellComponent
    private ChannelComponent channelComp;

    private ChannelComponent.ComponentMessageReceiver msgReceiver;

    public TimelineAudioComponent(Cell cell) {
        super(cell);
    }
    
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
	System.out.println("status " + status + " increasing " + increasing);

        switch (status) {
            case DISK:
		channelComp.removeMessageReceiver(TimelineTreatmentDoneMessage.class);
                break;

            case ACTIVE:
                if (increasing) {
		    channelComp.addMessageReceiver(TimelineTreatmentDoneMessage.class, this);

		    //test();
                }
                break;
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
	TimelineAudioComponentClientState state = (TimelineAudioComponentClientState) clientState;

        super.setClientState(clientState);
    }

    private TimelineSegment[] segments = new TimelineSegment[6];
    private int segmentIndex = 0;

    private boolean test;

    private void test() {
	test = true;

	reset();

	TimelineDate date = new TimelineDate(new Date(100, 0, 1));

	TimelineSegment s0 = new TimelineSegment(date);

	segments[1] = s0;

	createSegmentTreatment(s0, getTreatment(date));

	date = new TimelineDate(new Date(101, 0, 1), new Date(104, 11, 31));

	TimelineSegment s1 = new TimelineSegment(date);
	
	segments[2] = s0;
	segments[3] = s1;

	createSegmentTreatment(s1, getTreatment(date));

	date = new TimelineDate(new Date(105, 0, 1), new Date(109, 11, 31));

	TimelineSegment s2 = new TimelineSegment(date);

	segments[0] = s2;
	segments[4] = s1;
	segments[5] = s2;

	createSegmentTreatment(s2, getTreatment(date));

	changeSegment(null, s0);
    }
   
    String[] months = { "January", "February", "March", "April", "May",
	"June", "July", "August", "September", "October", "November", "December" };

    private String getTreatment(TimelineDate date) {
	Date min = date.getMinimum();
	Date max = date.getMaximum();

	if (min.equals(max)) {
	    return "tts:The start and end date for this segment is " + 
		months[min.getMonth()] + " ,, " + min.getDate() + " ,,,, " + (min.getYear() + 1900);
	}

	return "tts:The start date for this segment is " 
	    + months[min.getMonth()] + " ,, " + min.getDate() + " ,,,, " + (min.getYear() + 1900)
	    + ",,,, and the end date is " 
	    + months[max.getMonth()] + " ,, " + max.getDate() + " ,,,, " + (max.getYear() + 1900);
    }

    public void setCallID(String callID) {
	channelComp.send(new TimelineCallIDMessage(cell.getCellID(), callID));
    }

    private String getSegmentID(TimelineSegment segment) {
	String segmentID = segment.getDate().toString();
	return segmentID.replaceAll(":", "_");
    }

    public void createSegmentTreatment(TimelineSegment segment) {
	createSegmentTreatment(segment, segment.getTreatment());
    }

    public void createSegmentTreatment(TimelineSegment segment, String treatment) {
	System.out.println("Create segment: " + segment + " treatment " + treatment);

	Vector3f location = new Vector3f();

	CellTransform transform = segment.getTransform();

	if (transform != null) {
	    location = transform.getTranslation(null);
	}

	String segmentID = getSegmentID(segment);

	channelComp.send(new TimelineSegmentTreatmentMessage(cell.getCellID(), segmentID,
	    treatment, location));
    }

    public void changeSegment(TimelineSegment previousSegment, TimelineSegment currentSegment) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("changeSegment: " + callID + " previous " + previousSegment 
	    + " current " + currentSegment);

	String previousSegmentID = null;

	if (previousSegment != null) {
	    previousSegmentID = getSegmentID(previousSegment);
	}

	String currentSegmentID = getSegmentID(currentSegment);

	channelComp.send(new TimelineSegmentChangeMessage(cell.getCellID(), callID, previousSegmentID,
	    currentSegmentID));
    }

    public void playRecording(String recordingPath, boolean isPlaying) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("playRecording " + " path " + recordingPath + " isPlaying "
	    + isPlaying);

	channelComp.send(new TimelinePlayRecordingMessage(cell.getCellID(), 
	    callID, recordingPath, isPlaying));
    }

    public void record(String recordingPath, boolean isRecording) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("record " + " path " + recordingPath + " isPlaying "
	    + isRecording);

	channelComp.send(new TimelineRecordMessage(cell.getCellID(),
	    callID, recordingPath, isRecording));
    }

    public void reset() {
	channelComp.send(new TimelineResetMessage(cell.getCellID()));
    }

    public void messageReceived(CellMessage message) {
	if (test == false) {
	    return;
	}

	segmentIndex += 2;

	if (segmentIndex >= segments.length) {
	    segmentIndex = 0;
	}

	changeSegment(segments[segmentIndex], segments[segmentIndex + 1]);
    }

}
