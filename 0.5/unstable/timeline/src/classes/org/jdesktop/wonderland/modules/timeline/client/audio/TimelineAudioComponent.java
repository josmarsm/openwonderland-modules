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

import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;

import java.util.logging.Logger;

import com.jme.math.Vector3f;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;

import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;

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

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
	switch(status) {
        case DISK:
            break;

	case ACTIVE:
            if (increasing) {
                channelComp = cell.getComponent(ChannelComponent.class);
	        break;
            }
	}
    }
    
    public void createSegmentTreatment(TimelineSegment segment, Vector3f location, String treatment) {
    }

    public void changeSegment(TimelineSegment previousSegment, TimelineSegment currentSegment) {
    }

    public void playSegmentRecording(TimelineSegment segment, String recordingPath, boolean isPlaying) {
    }

    public void record(TimelineSegment segment, String recordinPath, boolean isRecording) {
    }

    public void messageReceived(CellMessage message) {
    }

}
