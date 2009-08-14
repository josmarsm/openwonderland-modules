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
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelinePlayRecordingMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineRecordMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineResetMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentTreatmentMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineTreatmentDoneMessage;

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
//import com.sun.voip.client.connector.CallStatusListener;

/**
 *
 *  
 */
public class TimelineAudioComponentMO extends CellComponentMO {
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

    private ManagedReference<ComponentMessageReceiverImpl> receiverRef;

    @Override
    public void setLive(boolean live) {
	super.setLive(live);

        ChannelComponentMO channelComponent = (ChannelComponentMO)
            cellRef.get().getComponent(ChannelComponentMO.class);

	if (live) {
	    ComponentMessageReceiverImpl receiver = new ComponentMessageReceiverImpl(cellRef, this);
            receiverRef = AppContext.getDataManager().createReference(receiver);

            channelComponent.addMessageReceiver(TimelineSegmentTreatmentMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineSegmentChangeMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineRecordMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineResetMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelinePlayRecordingMessage.class, receiver);
        } else {
            channelComponent.removeMessageReceiver(TimelineSegmentTreatmentMessage.class);
            channelComponent.removeMessageReceiver(TimelineSegmentChangeMessage.class);
            channelComponent.removeMessageReceiver(TimelineRecordMessage.class);
            channelComponent.removeMessageReceiver(TimelineResetMessage.class);
            channelComponent.removeMessageReceiver(TimelinePlayRecordingMessage.class);

	    if (receiverRef != null) {
		receiverRef.get().done();
	    }
        }
    }

    protected String getClientClass() {
	return "org.jdesktop.wonderland.modules.timeline.client.audio.TimelineAudioComponent";
    }

    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver 
	    implements ManagedCallStatusListener {

        private ManagedReference<CellMO> cellRef;

        private CellID cellID;

        private ManagedReference<TimelineAudioComponentMO> compRef;

        public ComponentMessageReceiverImpl(ManagedReference<CellMO> cellRef,
                TimelineAudioComponentMO comp) {

            super(cellRef.get());

	    this.cellRef = cellRef;

	    cellID = cellRef.get().getCellID();

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

	    if (message instanceof TimelineResetMessage) {
		done();
	    }

	    if (message instanceof TimelinePlayRecordingMessage) {
		playRecording((TimelinePlayRecordingMessage) message);
		return;
	    }
        }

        private ConcurrentHashMap<String, Treatment> segmentTreatmentMap = new ConcurrentHashMap();

        private void setupTreatment(TimelineSegmentTreatmentMessage message) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    String segmentID = message.getSegmentID();

	    AppContext.getManager(VoiceManager.class).addCallStatusListener(this, segmentID);

            TreatmentGroup group = vm.createTreatmentGroup(segmentID);
	
            TreatmentSetup setup = new TreatmentSetup();

	    FullVolumeSpatializer spatializer = new FullVolumeSpatializer(.01);

	    setup.spatializer = spatializer;

            setup.treatment = message.getTreatment();

            String treatmentId = segmentID;

	    //setup.listener = this;

            if (setup.treatment == null || setup.treatment.length() == 0) {
                System.out.println("Invalid treatment '" + setup.treatment + "'");
	        return;
            }

            Vector3f location = message.getLocation();

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
	    String currentSegmentID = message.getCurrentSegmentID();

    	    Integer useCount = segmentUseMap.get(currentSegmentID);

            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    Player myPlayer = vm.getPlayer(message.getCallID());

	    Treatment treatment = segmentTreatmentMap.get(currentSegmentID);

	    if (treatment != null) {
		Call call = vm.getCall(treatment.getId());

		if (call != null) {
	            Player treatmentPlayer = call.getPlayer();

	            if (treatmentPlayer != null) {
		        myPlayer.setPrivateSpatializer(treatmentPlayer, new FullVolumeSpatializer());
			treatment.restart(false);
		        //new Fade(myPlayer, treatment, false);
	            } else {
		        System.out.println("Can't find player for " + treatment);
		    }
		} else {
		    System.out.println("No call for new treatment " + treatment + " setup " + treatment.getSetup());
		}
	    } else {
		System.out.println("No treatment in map for seg " + currentSegmentID);
	    }
	    
	    if (useCount == null) {
	        if (treatment == null) {
		    System.out.println("No treatment for " + currentSegmentID);
		    return;
	        }

	        useCount = new Integer(1);
		System.out.println("Unpausing treatment " + treatment);
		treatment.pause(false);
	    } else {
	        useCount = new Integer(useCount.intValue() + 1);
	    }

	    segmentUseMap.put(currentSegmentID, useCount);

	    String previousSegmentID = message.getPreviousSegmentID();	

	    if (previousSegmentID == null) {
		System.out.println("No previous segment");
	        return;
	    }

	    treatment = segmentTreatmentMap.get(previousSegmentID);

	    if (treatment == null) {
		return;
	    }

	    //new Fade(myPlayer, treatment, true);

	    Call call = vm.getCall(treatment.getId());

	    Player player;

	    if (call != null) {
	        player = call.getPlayer();
	        myPlayer.removePrivateSpatializer(player);
	    } else {
		System.out.println("No call for " + treatment + " setup " + treatment.getSetup());
	    }

	    useCount = segmentUseMap.get(previousSegmentID);

	    if (useCount == null) {
	        System.out.println("No use count map entry for " + previousSegmentID);
	    } else {
	        int i = useCount.intValue();

	        if (i == 1) {
		    segmentUseMap.remove(previousSegmentID);
		    System.out.println("Pausing treatment " + treatment);
		    treatment.pause(true);
	        }
	    }
        }

        private class Fade extends Thread {

	    private static final double fadeinValue = .05;
	    private static final double fadeoutValue = .1;

	    private Player myPlayer;
	    private boolean fadeout;
	    private Treatment treatment;

	    public Fade(Player myPlayer, Treatment treatment, boolean fadeout) {
		this.myPlayer = myPlayer;
	        this.treatment = treatment;
	        this.fadeout = fadeout;

	        start();
	    }

	    public void run() {	
	        Spatializer spatializer = treatment.getSetup().spatializer;

	        double attenuator;

	        if (fadeout == false) {
		    attenuator = fadeoutValue;
	        } else {
		    attenuator = DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME;
	        }

	        while (true) {
		    System.out.println("Attenuator " + attenuator + " " + treatment);

	    	    spatializer.setAttenuator(attenuator);

		    if (fadeout == true) {
		        attenuator -= fadeoutValue;

		        if (attenuator <= 0) {
            		    VoiceManager vm = AppContext.getManager(VoiceManager.class);
	    		    Player player = vm.getPlayer(treatment.getId());
			    myPlayer.removePrivateSpatializer(player);
			    System.out.println("Removing pm for " + player);
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

        public void callStatusChanged(CallStatus status) {
            String callId = status.getCallId();

	    if (status.getCode() == CallStatus.TREATMENTDONE) {
                ChannelComponentMO channelComponent = (ChannelComponentMO)
                    cellRef.get().getComponent(ChannelComponentMO.class);

		channelComponent.sendAll(null, new TimelineTreatmentDoneMessage(cellID));
	    }
        }

	public void done() {
            Treatment[] treatments = segmentTreatmentMap.values().toArray(new Treatment[0]);

	    for (int i = 0; i < treatments.length; i++) {
		treatments[i].stop();
	    }

	    segmentTreatmentMap.clear();
	    segmentUseMap.clear();
	}

    }

}
