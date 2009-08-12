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
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineAudioComponentClientState;

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
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);

	TimelineAudioComponentClientState state = (TimelineAudioComponentClientState) 
	    clientState;
    }
   
    public void createSegmentTreatment(TimelineSegment segment) {
	System.out.println("Create segment: " + segment);

	channelComp.send(new TimelineSegmentTreatmentMessage(cell.getCellID(), segment));
    }

    public void changeSegment(TimelineSegment previousSegment, TimelineSegment currentSegment) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("changeSegment: " + callID + " previous " + previousSegment 
	    + " current " + currentSegment);

	channelComp.send(new TimelineSegmentChangeMessage(cell.getCellID(), callID, previousSegment, currentSegment));
    }

    public void playSegmentRecording(TimelineSegment segment, String recordingPath, boolean isPlaying) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("playSegmentRecording " + segment + " path " + recordingPath + " isPlaying "
	    + isPlaying);

	channelComp.send(new TimelinePlayRecordingMessage(cell.getCellID(), segment, callID, recordingPath, isPlaying));
    }

    public void record(TimelineSegment segment, String recordingPath, boolean isRecording) {
	String callID = SoftphoneControlImpl.getInstance().getCallID();

	System.out.println("record " + segment + " path " + recordingPath + " isPlaying "
	    + isRecording);

	channelComp.send(new TimelineRecordMessage(cell.getCellID(), segment, callID, recordingPath, isRecording));
    }

    public void messageReceived(CellMessage message) {
    }

}
