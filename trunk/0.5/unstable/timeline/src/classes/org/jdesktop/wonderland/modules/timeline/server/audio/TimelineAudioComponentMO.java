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
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineCallIDMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelinePlayRecordingMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineRecordMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineResetMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineSegmentTreatmentMessage;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineTreatmentDoneMessage;

import com.sun.sgs.app.AppContext;

import com.sun.sgs.app.ManagedReference;

import com.sun.sgs.kernel.KernelRunnable;

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

import java.util.ArrayList;

import java.util.logging.Logger;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.concurrent.ConcurrentHashMap;

import com.sun.voip.client.connector.CallStatus;
//import com.sun.voip.client.connector.CallStatusListener;

import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 *
 *  
 */
public class TimelineAudioComponentMO extends CellComponentMO {
    private static final Logger logger =
            Logger.getLogger(TimelineAudioComponentMO.class.getName());

    private CellID cellID;

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelCompRef;


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

	ChannelComponentMO channelComponent = channelCompRef.get();

	if (live) {
	    ComponentMessageReceiverImpl receiver = new ComponentMessageReceiverImpl(cellRef, this);
            receiverRef = AppContext.getDataManager().createReference(receiver);

            channelComponent.addMessageReceiver(TimelineCallIDMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineSegmentChangeMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelinePlayRecordingMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineRecordMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineResetMessage.class, receiver);
            channelComponent.addMessageReceiver(TimelineSegmentTreatmentMessage.class, receiver);
        } else {
            channelComponent.removeMessageReceiver(TimelineCallIDMessage.class);
            channelComponent.removeMessageReceiver(TimelinePlayRecordingMessage.class);
            channelComponent.removeMessageReceiver(TimelineRecordMessage.class);
            channelComponent.removeMessageReceiver(TimelineResetMessage.class);
            channelComponent.removeMessageReceiver(TimelineSegmentChangeMessage.class);
            channelComponent.removeMessageReceiver(TimelineSegmentTreatmentMessage.class);

	    if (receiverRef != null) {
		receiverRef.get().done();
		receiverRef = null;
	    }
        }
    }

    protected String getClientClass() {
	return "org.jdesktop.wonderland.modules.timeline.client.audio.TimelineAudioComponent";
    }

    private String getSegmentID(String segmentID) {
        return segmentID.replaceAll(":", "_");
    }

    public void setupTreatment(String segmentID, String treatment, Vector3f location) {
	if (receiverRef == null) {
	    System.out.println("changeSegment:  No Receiver!");
	    return;
	}

	receiverRef.get().setupTreatment(getSegmentID(segmentID), treatment, location);
    }

    public void changeSegment(String callID, String previousSegmentID, String currentSegmentID) {
	if (receiverRef == null) {
	    System.out.println("changeSegment:  No Receiver!");
	    return;
	}

	if (callID == null) {
	    System.out.println("changeSegment:  Call ID has not been specified!");
	    return;
	}

	receiverRef.get().changeSegment(callID, getSegmentID(previousSegmentID), getSegmentID(currentSegmentID));
    }

    public void record(String callID, String recordingPath, boolean isRecording) {
	if (receiverRef == null) {
	    System.out.println("record:  No Receiver!");
	    return;
	}

	if (callID == null) {
	    System.out.println("record:  Call ID has not been specified!");
	    return;
	}

	receiverRef.get().record(callID, recordingPath, isRecording);
    }

    public void playRecording(String callID, String recordingPath, boolean isPlaying) {
	if (receiverRef == null) {
	    System.out.println("playRecording:  No Receiver!");
	    return;
	}

	if (callID == null) {
	    System.out.println("playRecording:  Call ID has not been specified!");
	    return;
	}

	receiverRef.get().playRecording(callID, recordingPath, isPlaying);
    }

    public void reset() {
	if (receiverRef == null) {
	    System.out.println("reset:  No Receiver!");
	    return;
	}

	receiverRef.get().done();
    }

    private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver 
	    implements ManagedCallStatusListener {

        private ManagedReference<CellMO> cellRef;

        private CellID cellID;

        private ManagedReference<TimelineAudioComponentMO> compRef;

	private String currentSegmentID;

        private String serverURL;

        public ComponentMessageReceiverImpl(ManagedReference<CellMO> cellRef,
                TimelineAudioComponentMO comp) {

            super(cellRef.get());

	    this.cellRef = cellRef;

	    cellID = cellRef.get().getCellID();

            compRef = AppContext.getDataManager().createReference(comp);

	    serverURL = System.getProperty("wonderland.web.server.url");
        }

        public void messageReceived(WonderlandClientSender sender, 
	        WonderlandClientID clientID, CellMessage message) {

	    if (message instanceof TimelineCallIDMessage) {
		return;
	    }

	    if (message instanceof TimelineSegmentTreatmentMessage) {
		TimelineSegmentTreatmentMessage msg = (TimelineSegmentTreatmentMessage) message;
		setupTreatment(msg.getSegmentID(), msg.getTreatment(), msg.getLocation());
		return;
	    }

	    if (message instanceof TimelineSegmentChangeMessage) {
		TimelineSegmentChangeMessage msg = (TimelineSegmentChangeMessage) message;
		changeSegment(msg.getCallID(), msg.getPreviousSegmentID(), msg.getCurrentSegmentID());
		return;
	    }
	    
	    if (message instanceof TimelineRecordMessage) {
		TimelineRecordMessage msg = (TimelineRecordMessage) message;
		record(msg.getCallID(), msg.getRecordingPath(), msg.getIsRecording());
		return;
	    }

	    if (message instanceof TimelinePlayRecordingMessage) {
		TimelinePlayRecordingMessage msg = (TimelinePlayRecordingMessage) message;
		playRecording(msg.getCallID(), msg.getRecordingPath(), msg.getIsPlaying());
		return;
	    }

	    if (message instanceof TimelineResetMessage) {
		done();
	    }
        }

        private ConcurrentHashMap<String, TreatmentGroup> segmentTreatmentGroupMap = new ConcurrentHashMap();

        private ConcurrentHashMap<String, ArrayList<Treatment>> segmentTreatmentMap = new ConcurrentHashMap();

        public void setupTreatment(String segmentID, String treatment, Vector3f location) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    AppContext.getManager(VoiceManager.class).addCallStatusListener(this, segmentID);

            TreatmentGroup group = segmentTreatmentGroupMap.get(segmentID);

	    if (group == null) {
                group = vm.createTreatmentGroup(segmentID);
		segmentTreatmentGroupMap.put(segmentID, group);
	    }
	
            TreatmentSetup setup = new TreatmentSetup();

	    FullVolumeSpatializer spatializer = new FullVolumeSpatializer(.01);

	    setup.spatializer = spatializer;

	    String pattern = "wlcontent://";

	    if (treatment.startsWith(pattern)) {
		/*
                 * We need to create a URL
                 */
                String path = treatment.substring(pattern.length());

                URL url;

                try {
		    path = path.replaceAll(" ", "%20");

                    url = new URL(new URL(serverURL),
                            "webdav/content/" + path);

                    treatment = url.toString();
                    System.out.println("Treatment: " + treatment);
                } catch (MalformedURLException e) {
                    logger.warning("bad url:  " + e.getMessage());
		    return;
                }
	    } else {
		pattern = "wls://";

	        if (treatment.startsWith(pattern)) {
                    /*
                     * We need to create a URL from wls:<module>/path
                     */
                    treatment = treatment.substring(pattern.length());

                    int ix = treatment.indexOf("/");

                    if (ix < 0) {
                        logger.warning("Bad treatment:  " + treatment);
                        return;
                    }

                    String moduleName = treatment.substring(0, ix);

                    String path = treatment.substring(ix + 1);

                    logger.fine("Module:  " + moduleName + " treatment " + treatment);

                    URL url;

                    try {
			path = path.replaceAll(" ", "%20");

                        url = new URL(new URL(serverURL),
                            "webdav/content/modules/installed/" + moduleName + "/audio/" + path);

                        treatment = url.toString();
                        logger.fine("Treatment: " + treatment);
                    } catch (MalformedURLException e) {
                        logger.warning("bad url:  " + e.getMessage());
                        return;
                    }
		}
	    }

            setup.treatment = treatment;

            String treatmentId = segmentID;

	    //setup.listener = this;

            if (setup.treatment == null || setup.treatment.length() == 0) {
                System.out.println("Invalid treatment '" + setup.treatment + "'");
	        return;
            }

	    if (location != null) {
                setup.x = location.getX();
                setup.y = location.getY();
                setup.z = location.getZ();
	    }

            System.out.println("Starting treatment " + setup.treatment + " at (" + setup.x 
	        + ":" + setup.y + ":" + setup.z + ") for segment " + segmentID);

            try {
	        Treatment t = vm.createTreatment(treatmentId, setup);
                group.addTreatment(t);
	        t.pause(true);

		ArrayList<Treatment> treatments = segmentTreatmentMap.get(segmentID);

		if (treatments == null) {
		    treatments = new ArrayList();
		    segmentTreatmentMap.put(segmentID, treatments);
		    System.out.println("New map entry for " + segmentID);
		}

	        treatments.add(t);
            } catch (IOException e) {
                System.out.println("Unable to create treatment " + setup.treatment + e.getMessage());
                return;
            }
        }

        private ConcurrentHashMap<String, Integer> segmentUseMap = new ConcurrentHashMap();

	private void changeSegment(String callID, String previousSegmentID, String currentSegmentID) {

	    System.out.println("changeSegment:  " + callID + " previous " + previousSegmentID
		+ " current " + currentSegmentID);

    	    Integer useCount = segmentUseMap.get(currentSegmentID);

            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    ArrayList<Treatment> treatments = segmentTreatmentMap.get(currentSegmentID);

	    if (treatments != null) {
		startTreatments(callID, treatments);
	    } else {
		System.out.println("No treatment in map for seg " + currentSegmentID);
	    }
	    
	    if (useCount == null) {
	        if (treatments == null || treatments.size() == 0) {
		    System.out.println("No treatments for " + currentSegmentID);
		    return;
	        }

	        useCount = new Integer(1);

		pauseTreatments(treatments, false);
	    } else {
	        useCount = new Integer(useCount.intValue() + 1);
	    }

	    segmentUseMap.put(currentSegmentID, useCount);

	    this.currentSegmentID = currentSegmentID;

	    if (previousSegmentID == null) {
		System.out.println("No previous segment");
	        return;
	    }

	    treatments = segmentTreatmentMap.get(previousSegmentID);

	    if (treatments == null) {
		return;
	    }

	    stopTreatments(callID, treatments);

	    useCount = segmentUseMap.get(previousSegmentID);

	    if (useCount == null) {
	        System.out.println("No use count map entry for " + previousSegmentID);
	    } else {
	        int i = useCount.intValue();

	        if (i == 1) {
		    segmentUseMap.remove(previousSegmentID);
		    pauseTreatments(treatments, true);
	        }
	    }
        }

	private void startTreatments(String callID, ArrayList<Treatment> treatments) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    Player myPlayer = vm.getPlayer(callID);

	    if (myPlayer == null) {
		System.out.println("No player for callID " + callID);
		return;
	    }

	    for (Treatment treatment : treatments) {
		Call call = vm.getCall(treatment.getId());

		if (call != null) {
	            Player treatmentPlayer = call.getPlayer();

	            if (treatmentPlayer != null) {
			System.out.println("Setting pm for " + treatmentPlayer);
		        myPlayer.setPrivateSpatializer(treatmentPlayer, new FullVolumeSpatializer());
			treatment.restart(false);
		        new Fadein(myPlayer, treatmentPlayer);
	            } else {
		        System.out.println("Can't find player for " + treatment);
		    }
		} else {
		    System.out.println("No call for new treatment " + treatment + " setup " 
			+ treatment.getSetup() + " treatment call " + treatment.getCall());
		}
	    }
	}

	private void pauseCurrentTreatments(boolean isPaused) {
	    if (currentSegmentID == null) {
		return;
	    }

	    ArrayList<Treatment> treatments = segmentTreatmentMap.get(currentSegmentID);

	    if (treatments != null) {
		pauseTreatments(treatments, isPaused);
	    }
	}

	private void pauseTreatments(ArrayList<Treatment> treatments, boolean pause) {
	    for (Treatment treatment : treatments) {
		System.out.println(pause ? "" : "Un" + "pausing treatment " + treatment);
		treatment.pause(pause);
	    }
	}

	private void stopTreatments(String callID, ArrayList<Treatment> treatments) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    Player myPlayer = vm.getPlayer(callID);

	    if (myPlayer == null) {
		System.out.println("No player for callID " + callID);
		return;
	    }

	    for (Treatment treatment : treatments) {
	        Call call = vm.getCall(treatment.getId());

	        Player player;

	        if (call != null) {
	            Player treatmentPlayer = call.getPlayer();
		    new Fadeout(myPlayer, treatmentPlayer);

	        } else {
		    System.out.println("No call for " + treatment + " setup " + treatment.getSetup());
	        }
	    }
	}

        private class Fadein implements KernelRunnable {

	    private static final double FADEIN_VALUE = .05;

	    private Player myPlayer;
	    private Player treatmentPlayer;

	    double attenuator = FADEIN_VALUE;

	    public Fadein(Player myPlayer, Player treatmentPlayer) {
		this.myPlayer = myPlayer;
		this.treatmentPlayer = treatmentPlayer;

		schedule();
	    }

            public String getBaseTaskType() {
                return Fadein.class.getName();
            }

	    private void schedule() {
                AppContext.getManager(VoiceManager.class).scheduleTask(this, System.currentTimeMillis() + 100);
	    }

	    public void run() {	
		System.out.println("FADEIN:  Attenuator " + attenuator);

		Spatializer spatializer = new FullVolumeSpatializer();
		spatializer.setAttenuator(attenuator);

		myPlayer.setPrivateSpatializer(treatmentPlayer, spatializer);

		attenuator += FADEIN_VALUE;

		if (attenuator >= DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME) {
		    return;
		}

		schedule();
	    }

        }

        private class Fadeout implements KernelRunnable {

	    private static final double FADEOUT_VALUE = .1;

	    private Player myPlayer;
	    private Player treatmentPlayer;

	    double attenuator = DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME;

	    public Fadeout(Player myPlayer, Player treatmentPlayer) {
		this.myPlayer = myPlayer;
		this.treatmentPlayer = treatmentPlayer;

		schedule();
	    }

            public String getBaseTaskType() {
                return Fadeout.class.getName();
            }

	    private void schedule() {
                AppContext.getManager(VoiceManager.class).scheduleTask(this, System.currentTimeMillis() + 100);
	    }

	    public void run() {	
		System.out.println("FADEOUT:  Attenuator " + attenuator);

		Spatializer spatializer = new FullVolumeSpatializer();

	    	spatializer.setAttenuator(attenuator);

		myPlayer.setPrivateSpatializer(treatmentPlayer, spatializer);

		attenuator -= FADEOUT_VALUE;

		if (attenuator <= 0) {
	            myPlayer.removePrivateSpatializer(treatmentPlayer);
		    System.out.println("FADEOUT:  Removing pm for " + treatmentPlayer);
		    return;
		}

		schedule();
	    }

        }

        private void record(String callID, String recordingPath, boolean isRecording) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    Call call = vm.getCall(callID);

	    if (call == null) {
	        System.out.println("No call for " + callID);
	        return;
	    }

	    pauseCurrentTreatments(isRecording);

	    try {
   	        call.record(recordingPath, isRecording);
	    } catch (IOException e) {
		System.out.println("Unable to start/stop recording " 
		    + recordingPath + " " + e.getMessage());
	    }
        }

        private void playRecording(String callID, String recordingPath, boolean isPlaying) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

	    Call call = vm.getCall(callID);

	    if (call == null) {
	        System.out.println("No call for " + callID);
		return;
	    }
	    
	    pauseCurrentTreatments(isPlaying);

	    try {
		if (isPlaying) {
	            call.playTreatment(recordingPath);
		} else {
	            call.stopTreatment(recordingPath);
		}
	    } catch (IOException e) {
		System.out.println("Unable to play/stop treatment " + recordingPath);
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
	    if (segmentTreatmentMap == null || segmentTreatmentMap.values() == null) {
		return;
	    }

            Treatment[] treatments = segmentTreatmentMap.values().toArray(new Treatment[0]);

	    for (int i = 0; i < treatments.length; i++) {
		treatments[i].stop();
	    }

	    segmentTreatmentMap.clear();
	    segmentUseMap.clear();
	}

    }

}
