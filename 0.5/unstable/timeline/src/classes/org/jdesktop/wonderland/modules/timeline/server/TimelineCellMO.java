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
package org.jdesktop.wonderland.modules.timeline.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellClientState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import org.jdesktop.wonderland.modules.timeline.common.TimelineSegmentTreatmentMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegmentChangeMessage;

import com.sun.sgs.app.AppContext;

import com.sun.sgs.app.ManagedReference;

import com.jme.math.Vector3f;

import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Treatment;
import com.sun.mpk20.voicelib.app.TreatmentGroup;
import com.sun.mpk20.voicelib.app.TreatmentSetup;
import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.VoiceManager;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.concurrent.ConcurrentHashMap;

import com.sun.voip.client.connector.CallStatus;

/**
 *
 *  
 */
public class TimelineCellMO extends CellMO {

    public TimelineCellMO() {
        super();
    }

    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.timeline.client.TimelineCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new TimelineCellServerState();
        }

        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TimelineCellClientState();

        }
        
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {
            channel.addMessageReceiver(TimelineCellChangeMessage.class, 
		(ChannelComponentMO.ComponentMessageReceiver)new TimelineCellMessageReceiver(this));

            channel.addMessageReceiver(TimelineSegmentTreatmentMessage.class, 
		(ChannelComponentMO.ComponentMessageReceiver) 
		new TimelineSegmentTreatmentMessageReceiver(this, segmentTreatmentMap));

            channel.addMessageReceiver(TimelineSegmentChangeMessage.class, 
		(ChannelComponentMO.ComponentMessageReceiver) 
		new TimelineSegmentChangeMessageReceiver(this, segmentTreatmentMap));
        } else {
            channel.removeMessageReceiver(TimelineCellChangeMessage.class);
            channel.removeMessageReceiver(TimelineSegmentTreatmentMessage.class);
            channel.removeMessageReceiver(TimelineSegmentChangeMessage.class);
        }
    }

    private static class TimelineCellMessageReceiver extends AbstractComponentMessageReceiver {

        public TimelineCellMessageReceiver(TimelineCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, 
		CellMessage message) {

            TimelineCellMO cellMO = (TimelineCellMO)getCell();

            TimelineCellChangeMessage msg = (TimelineCellChangeMessage)message;
         
        }

    }

    private ConcurrentHashMap<String, Treatment> segmentTreatmentMap = new ConcurrentHashMap();

    private static class TimelineSegmentTreatmentMessageReceiver extends 
	    AbstractComponentMessageReceiver implements ManagedCallStatusListener {

	private ConcurrentHashMap<String, Treatment> segmentTreatmentMap;

        public TimelineSegmentTreatmentMessageReceiver(TimelineCellMO cellMO,
		ConcurrentHashMap<String, Treatment> segmentTreatmentMap) {

            super(cellMO);

	    this.segmentTreatmentMap = segmentTreatmentMap;
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, 
		CellMessage message) {

            TimelineCellMO cellMO = (TimelineCellMO) getCell();

	    setTreatment((TimelineSegmentTreatmentMessage) message);
  	}

	private void setTreatment(TimelineSegmentTreatmentMessage message) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);

            TreatmentGroup group = vm.createTreatmentGroup(message.getSegmentID());
	
            TreatmentSetup setup = new TreatmentSetup();

	    FullVolumeSpatializer spatializer = new FullVolumeSpatializer();

	    setup.spatializer = spatializer;

            spatializer.setAttenuator(message.getAttenuator());

            String treatment = message.getTreatment();

            String treatmentId = message.getSegmentID();

            setup.treatment = treatment;

	    setup.managedListenerRef = 
	        AppContext.getDataManager().createReference((ManagedCallStatusListener) this);

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
		segmentTreatmentMap.put(message.getSegmentID(), t);
            } catch (IOException e) {
                System.out.println("Unable to create treatment " + setup.treatment + e.getMessage());
                return;
            }
        }

        public void callStatusChanged(CallStatus status) {
            String callId = status.getCallId();

	    System.out.println("Got status: " + status);
        }
    }

    private static ConcurrentHashMap<String, Integer> segmentUseMap = new ConcurrentHashMap();

    private static class TimelineSegmentChangeMessageReceiver extends 
	    AbstractComponentMessageReceiver {

	private ConcurrentHashMap<String, Treatment> segmentTreatmentMap;

        public TimelineSegmentChangeMessageReceiver(TimelineCellMO cellMO,
		ConcurrentHashMap<String, Treatment> segmentTreatmentMap) {

            super(cellMO);

	    this.segmentTreatmentMap = segmentTreatmentMap;
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, 
		CellMessage message) {

            TimelineCellMO cellMO = (TimelineCellMO) getCell();

	    setCurrentSegment((TimelineSegmentChangeMessage) message);
	}

	private void setCurrentSegment(TimelineSegmentChangeMessage message) {
	    String currentSegmentID = message.getCurrentSegmentID();

	    Integer useCount = segmentUseMap.get(currentSegmentID);

	    if (useCount == null) {
		Treatment treatment = segmentTreatmentMap.get(currentSegmentID);

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

	    String previousSegmentID = message.getPreviousSegmentID();	

	    if (previousSegmentID == null) {
		return;
	    }

	    useCount = segmentUseMap.get(previousSegmentID);

	    if (useCount == null) {
		System.out.println("No map entry for " + previousSegmentID);
	    } else {
		int i = useCount.intValue();

		if (i == 1) {
		    Treatment treatment = segmentTreatmentMap.get(previousSegmentID);

		    if (treatment == null) {
			System.out.println("No treatment for " + previousSegmentID);
		    } else {
			new Fade(treatment, true);
		    }

		    segmentUseMap.remove(previousSegmentID);
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

    }

}
