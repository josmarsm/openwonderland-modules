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
package org.jdesktop.wonderland.modules.timeline.server.audio;

import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineAudioComponentClientState;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineAudioComponentServerState;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentTreatmentMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelinePlayRecordingMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineRecordMessage;

import com.sun.sgs.app.AppContext;

import com.sun.sgs.app.ManagedReference;

import com.jme.math.Vector3f;

import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.Treatment;
import com.sun.mpk20.voicelib.app.TreatmentGroup;
import com.sun.mpk20.voicelib.app.TreatmentSetup;
import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.VoiceManager;

import java.util.logging.Logger;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.concurrent.ConcurrentHashMap;

import com.sun.voip.client.connector.CallStatus;

/**
 *
 *  
 */
public class TimelineAudioComponentMO extends CellComponentMO implements ManagedCallStatusListener {
    private static final Logger logger =
            Logger.getLogger(TimelineAudioComponentMO.class.getName());

    private CellID cellID;

    public TimelineAudioComponentMO(CellMO cellMO) {
        super(cellMO);

	cellID = cellMO.getCellID();
    }

    @Override
    public void setServerState(CellComponentServerState serverState) {
        super.setServerState(serverState);

        // Fetch the component-specific state and set member variables
        TimelineAudioComponentServerState state = (TimelineAudioComponentServerState) serverState;
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState serverState) {
        TimelineAudioComponentServerState state = (TimelineAudioComponentServerState) serverState;

        if (state == null) {
            state = new TimelineAudioComponentServerState();
        }

        return super.getServerState(state);
    }

    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState clientState,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {

	if (clientState == null) {
	    clientState = new TimelineAudioComponentClientState();
	}

	//this.clientID = clientID;
	return super.getClientState(clientState, clientID, capabilities);
    }

    @Override
    public void setLive(boolean live) {
	super.setLive(live);

	System.out.println("TimelineAudioComponent setLive " + live);

        ChannelComponentMO channelComponent = (ChannelComponentMO)
            cellRef.get().getComponent(ChannelComponentMO.class);

	if (live) {
	    ComponentMessageReceiverImpl receiver = new ComponentMessageReceiverImpl(cellRef, this);

            channelComponent.addMessageReceiver(TimelineSegmentTreatmentMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineSegmentChangeMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineRecordMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelinePlayRecordingMessage.class, receiver);
        } else {
            channelComponent.removeMessageReceiver(TimelineSegmentTreatmentMessage.class);
            channelComponent.removeMessageReceiver(TimelineSegmentChangeMessage.class);
            channelComponent.removeMessageReceiver(TimelineRecordMessage.class);
            channelComponent.removeMessageReceiver(TimelinePlayRecordingMessage.class);
        }
    }

    protected String getClientClass() {
	return "org.jdesktop.wonderland.modules.timeline.client.audio.TimelineAudioComponent";
    }

    public void callStatusChanged(CallStatus status) {
        String callId = status.getCallId();

	System.out.println("Got status: " + status);
    }


    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver {

        private ManagedReference<TimelineAudioComponentMO> compRef;

        private String getSegmentID(TimelineSegment segment) {
	    return segment.toString();
        }

        public ComponentMessageReceiverImpl(ManagedReference<CellMO> cellRef,
                TimelineAudioComponentMO comp) {

            super(cellRef.get());

            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, 
	        WonderlandClientID clientID, CellMessage message) {

	    if (message instanceof TimelineSegmentTreatmentMessage) {
		setupTreatment((TimelineSegmentTreatmentMessage) message);
		return;
	    }

	    if (message instanceof TimelineSegmentChangeMessage) {
		changeSegment((TimelineSegmentChangeMessage) message);
		return;
	    }
	    
	    if (message instanceof TimelineRecordMessage) {
		record((TimelineRecordMessage) message);
		return;
	    }

	    if (message instanceof TimelinePlayRecordingMessage) {
		playRecording((TimelinePlayRecordingMessage) message);
		return;
	    }
        }

        private ConcurrentHashMap<String, Treatment> segmentTreatmentMap = new ConcurrentHashMap();

        private void setupTreatment(TimelineSegmentTreatmentMessage message) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    TimelineSegment segment = message.getSegment();

	    String segmentID = getSegmentID(segment);

            TreatmentGroup group = vm.createTreatmentGroup(segmentID);
	
            TreatmentSetup setup = new TreatmentSetup();

	    FullVolumeSpatializer spatializer = new FullVolumeSpatializer();

	    setup.spatializer = spatializer;

            setup.treatment = segment.getTreatment();

            String treatmentId = segmentID;

	    setup.managedListenerRef = 
	        AppContext.getDataManager().createReference((ManagedCallStatusListener) this);

            if (setup.treatment == null || setup.treatment.length() == 0) {
                System.out.println("Invalid treatment '" + setup.treatment + "'");
	        return;
            }

            Vector3f location = segment.getTransform().getTranslation(null);

            setup.x = location.getX();
            setup.y = location.getY();
            setup.z = location.getZ();

            System.out.println("Starting treatment " + setup.treatment + " at (" + setup.x 
	        + ":" + setup.y + ":" + setup.z + ")");

            try {
	        Treatment t = vm.createTreatment(treatmentId, setup);
                group.addTreatment(t);
	        t.pause(true);
	        segmentTreatmentMap.put(segmentID, t);
            } catch (IOException e) {
                System.out.println("Unable to create treatment " + setup.treatment + e.getMessage());
                return;
            }
        }

        private ConcurrentHashMap<String, Integer> segmentUseMap = new ConcurrentHashMap();

        private void changeSegment(TimelineSegmentChangeMessage message) {
	    String currentSegmentID = getSegmentID(message.getCurrentSegment());

    	    Integer useCount = segmentUseMap.get(currentSegmentID);

            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    Treatment treatment = segmentTreatmentMap.get(currentSegmentID);

	    if (treatment != null) {
	        Player treatmentPlayer = vm.getPlayer(treatment.getId());

	        if (treatmentPlayer != null) {
	    	    Player myPlayer = vm.getPlayer(message.getCallID());

		    myPlayer.setPrivateSpatializer(treatmentPlayer, new FullVolumeSpatializer());
	        } else {
		    System.out.println("Can't find player for " + treatment);
		}
	    }
	    
	    if (useCount == null) {
	        if (treatment == null) {
		    System.out.println("No treatment for " + currentSegmentID);
		    return;
	        } else {
		    new Fade(treatment, false);
	        }

	        useCount = new Integer(1);
	    } else {
	        useCount = new Integer(useCount.intValue() + 1);
	    }

	    segmentUseMap.put(currentSegmentID, useCount);

	    String previousSegmentID = getSegmentID(message.getPreviousSegment());	

	    if (previousSegmentID == null) {
	        return;
	    }

	    treatment = segmentTreatmentMap.get(previousSegmentID);

	    if (treatment == null) {
		return;
	    }

	    Player treatmentPlayer = vm.getPlayer(treatment.getId());

	    if (treatmentPlayer != null) {
	    	Player myPlayer = vm.getPlayer(message.getCallID());

		myPlayer.setPrivateSpatializer(treatmentPlayer, new FullVolumeSpatializer());
	    } else {
		System.out.println("Can't find player for " + treatment);
	    }

	    useCount = segmentUseMap.get(previousSegmentID);

	    if (useCount == null) {
	        System.out.println("No use count map entry for " + previousSegmentID);
	    } else {
	        int i = useCount.intValue();

	        if (i == 1) {
		    segmentUseMap.remove(previousSegmentID);
		    new Fade(treatment, true);
	        }
	    }
        }

        private class Fade extends Thread {

	    private static final double fadeinValue = .05;
	    private static final double fadeoutValue = .1;

	    private boolean fadeout;
	    private Treatment treatment;

	    public Fade(Treatment treatment, boolean fadeout) {
	        this.treatment = treatment;
	        this.fadeout = fadeout;

	        start();
	    }

	    public void run() {	
	        Spatializer spatializer = treatment.getSetup().spatializer;

	        double attenuator;

	        if (fadeout == false) {
	            treatment.pause(false);
		    attenuator = fadeoutValue;
	        } else {
		    attenuator = DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME;
	        }

	        while (true) {
	    	    spatializer.setAttenuator(attenuator);

		    if (fadeout == true) {
		        attenuator -= fadeoutValue;
		        if (attenuator <= 0) {
	            	    treatment.pause(true);
			    break;
		        }
		    } else {
		        attenuator += fadeinValue;

		        if (attenuator >= DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME) {
			    break;
		        }
		    }

		    try {
		        Thread.sleep(100);
		    } catch (InterruptedException e) {
		    }
	        }
	    }

        }

        private void record(TimelineRecordMessage message) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    String callID = message.getCallID();

	    Call call = vm.getCall(callID);

	    if (call == null) {
	        System.out.println("No call for " + callID);
	        return;
	    }

	    try {
   	        call.record(message.getRecordingPath(), message.getIsRecording());
	    } catch (IOException e) {
		System.out.println("Unable to start/stop recording " 
		    + message.getRecordingPath() + " " + e.getMessage());
	    }
        }

        private void playRecording(TimelinePlayRecordingMessage message) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    String callID = message.getCallID();

	    Call call = vm.getCall(callID);

	    if (call == null) {
	        System.out.println("No call for " + callID);
		return;
	    }
	    
	    try {
		if (message.getIsPlaying()) {
	            call.playTreatment(message.getRecordingPath());
		} else {
	            call.stopTreatment(message.getRecordingPath());
		}
	    } catch (IOException e) {
		System.out.println("Unable to play/stop treatment " + message.getRecordingPath());
		return; 
	    }
        }

    }

}
