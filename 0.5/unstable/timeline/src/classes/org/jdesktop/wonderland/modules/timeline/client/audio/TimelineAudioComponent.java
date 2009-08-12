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

    public TimelineAudioComponent(Cell cell) {
        super(cell);
    }
    
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
	System.out.println("status " + status + " increasing " + increasing);

        switch (status) {
            case DISK:
                break;

            case ACTIVE:
                if (increasing) {
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

    private void test() {
	TimelineSegment s0 = new TimelineSegment(new TimelineDate(new Date(2009, 7, 12, 11, 0)));

	createSegmentTreatment(s0, "ring_tone.au");

	TimelineSegment s1 = new TimelineSegment(new TimelineDate(new Date(2009, 7, 12, 11, 1)));
	
	createSegmentTreatment(s1, "please_wait.au");

	TimelineSegment s2 = new TimelineSegment(new TimelineDate(new Date(2009, 7, 12, 11, 2)));

	createSegmentTreatment(s2, "singing_teapot.au");

	changeSegment(null, s0);

	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	}

	changeSegment(s0, s1);

	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	}

	changeSegment(s1, s2);
    }
   
    private String cleanSegmentID(String segmentID) {
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

	String segmentID = cleanSegmentID(segment.toString());

	channelComp.send(new TimelineSegmentTreatmentMessage(cell.getCellID(), segmentID,
	    treatment, location));
    }

    public void changeSegment(TimelineSegment previousSegment, TimelineSegment currentSegment) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("changeSegment: " + callID + " previous " + previousSegment 
	    + " current " + currentSegment);

	String previousSegmentID = null;

	if (previousSegment != null) {
	    previousSegmentID = cleanSegmentID(previousSegment.toString());
	}

	String currentSegmentID = cleanSegmentID(currentSegment.toString());

	channelComp.send(new TimelineSegmentChangeMessage(cell.getCellID(), callID, previousSegmentID,
	    currentSegmentID));
    }

    public void playSegmentRecording(TimelineSegment segment, String recordingPath, boolean isPlaying) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("playSegmentRecording " + segment + " path " + recordingPath + " isPlaying "
	    + isPlaying);

	String segmentID = cleanSegmentID(segment.toString());

	channelComp.send(new TimelinePlayRecordingMessage(cell.getCellID(), 
	    segmentID, callID, recordingPath, isPlaying));
    }

    public void record(TimelineSegment segment, String recordingPath, boolean isRecording) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("record " + segment + " path " + recordingPath + " isPlaying "
	    + isRecording);

	String segmentID = cleanSegmentID(segment.toString());

	channelComp.send(new TimelineRecordMessage(cell.getCellID(), segmentID,
	    callID, recordingPath, isRecording));
    }

    public void messageReceived(CellMessage message) {
    }

}
