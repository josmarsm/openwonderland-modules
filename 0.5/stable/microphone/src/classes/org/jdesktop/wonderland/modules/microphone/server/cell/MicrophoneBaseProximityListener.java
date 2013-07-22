/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.microphone.server.cell;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

import com.jme.bounding.BoundingVolume;
import com.sun.mpk20.voicelib.app.*;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo.ChatType;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;

import java.io.Serializable;
import java.util.Map;

import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneComponentMO.Status;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellResourceManager;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.Resource;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecureTask;
import org.jdesktop.wonderland.server.security.SecurityManager;

/**
 * The base class for microphone proximity listeners
 */
public abstract class MicrophoneBaseProximityListener implements ProximityListenerSrv,
	ManagedObject, Serializable
{

    private static final Logger logger =
            Logger.getLogger(MicrophoneBaseProximityListener.class.getName());

    private final CellID cellID;
    private final String name;
    private final double speakingVolume;
    private final double listenVolume;
    private final ManagedReference<Map<String, Status>> statusMapRef;

    public MicrophoneBaseProximityListener(CellMO cellMO, String name, 
            double speakingVolume, double listenVolume,
            ManagedReference<Map<String, Status>> statusMapRef)
    {
        this.cellID = cellMO.getCellID();
        this.name = name;
	this.speakingVolume = speakingVolume;
        this.listenVolume = listenVolume;
        this.statusMapRef = statusMapRef;
    }

    protected double getListenVolume() {
        return listenVolume;
    }

    protected double getSpeakingVolume() {
        return speakingVolume;
    }

    protected Map<String, Status> getStatusMap() {
        return statusMapRef.get();
    }

    protected abstract String getAreaType();
    protected abstract Action getAction();
    protected abstract Status entered(Status prev);
    protected abstract Status exited(Status prev);

    public void viewEnterExit(boolean entered, CellID cellID,
            CellID viewCellID, BoundingVolume proximityVolume,
            int proximityIndex)
    {

	logger.fine("View " + viewCellID + " " + (entered?"Entered":"Exitted") +
                    " " + getAreaType() + " area for cell " + cellID);

        String callId = CallID.getCallID(viewCellID);

        // OWL issue #XX: Handle enter / exit in a separate task. This is
        // required because the voicemanager does not update until a transaction
        // commits, and we therefore run into trouble if the user does more than
        // one action in a single transaction
        // AppContext.getTaskManager().scheduleTask(new EnterExitTask(this, callId,
        //                                                           entered));

        // handle enter and exit directly to avoid out-of-order commits
        if (entered) {
            proximityEntered(callId);
        } else {
            proximityExited(callId);
        }
    }

    // a separate task for enter and exit
    private static class EnterExitTask implements Task, Serializable {
        private final ManagedReference<MicrophoneBaseProximityListener> listenerRef;
        private final String callId;
        private boolean entered;

        public EnterExitTask(MicrophoneBaseProximityListener listener,
                             String callId, boolean entered)
        {
            this.listenerRef = AppContext.getDataManager().createReference(listener);
            this.callId = callId;
            this.entered = entered;
        }

        public void run() throws Exception {
            if (entered) {
                listenerRef.get().proximityEntered(callId);
            } else {
                listenerRef.get().proximityExited(callId);
            }
        }
    }

    private void proximityEntered(String callId) {
	// get the security manager
        SecurityManager security = AppContext.getManager(SecurityManager.class);
        CellResourceManager crm = AppContext.getManager(CellResourceManager.class);

        // create a request
        Action action = getAction();
        Resource resource = crm.getCellResource(this.cellID);
        if (resource != null) {
            // there is security on this cell perform the enter notification
            // securely
            ActionMap am = new ActionMap(resource, new Action[] { action });
            ResourceMap request = new ResourceMap();
            request.put(resource.getId(), am);

            // perform the security check
            security.doSecure(request, new CellEnteredTask(this, resource.getId(), callId));
        } else {
            // no security, just make the call directly
            proximityEnteredSecure(callId);
        }
    }

    // OWL issue #79 - make sure this is a static inner class that refers
    // to the listener via a ManagedReference
    private static class CellEnteredTask implements SecureTask, Serializable {
        private final ManagedReference<MicrophoneBaseProximityListener> listenerRef;
        private final String resourceID;
        private final String callId;

        public CellEnteredTask(MicrophoneBaseProximityListener listener,
                               String resourceID, String callId)
        {
            this.listenerRef = AppContext.getDataManager().createReference(listener);
            this.resourceID = resourceID;
            this.callId = callId;
        }

        public void run(ResourceMap granted) {
            ActionMap am = granted.get(resourceID);
            if (am != null && !am.isEmpty()) {
                // request was granted -- the user has permission to enter
                listenerRef.get().proximityEnteredSecure(callId);
            } else {
                logger.warning("Access denied to enter microphone area.");
            }
        }
    }

    private void proximityEnteredSecure(String callId) {
        Status prev = getStatusMap().get(callId);
        Status cur = entered(prev);
        updateAudioGroup(callId, cur);
    }

    private void proximityExited(String callId) {
	Status prev = getStatusMap().get(callId);
        Status cur = exited(prev);
        updateAudioGroup(callId, cur);
    }

    protected void updateAudioGroup(final String callId, Status status) {

        VoiceManager vm = AppContext.getManager(VoiceManager.class);
        // map the call ID to a player
        Player player = vm.getPlayer(callId);
        if (player == null) {
            logger.warning("Can't find player for " + callId);
            
            //Create Player
            PlayerSetup ps = new PlayerSetup();
            ps.isLivePlayer=true;
            ps.isOutworlder=false;
            ps.isVirtualPlayer=false;
            ps.orientation=90;
            player = vm.createPlayer(callId,ps);
            player.setMasterVolume(1);
            //return;
        } else {
            logger.warning("Can find player for " + callId);
        }

        logger.fine("Player " + callId + " status " + status + " for " + name);

        // update the status map
        getStatusMap().put(callId, status);

        // get the associated audio group
        AudioGroup audioGroup = vm.getAudioGroup(name);
	if (audioGroup == null && status != null) {
	    CellMO cellMO = CellManagerMO.getCell(cellID);

	    MicrophoneComponentMO microphoneComponentMO =
		cellMO.getComponent(MicrophoneComponentMO.class);

	    audioGroup = microphoneComponentMO.createAudioGroup(name);
	}

        // if the status is null, we are removing the player from the group
        if (status == null) {
            if (audioGroup == null) {
                logger.warning("Group " + name + " does not exist");
                return;
            }

            logger.fine("Remove player " + callId + " from " + name);
            audioGroup.removePlayer(player);
            return;
        }

        // construct and appropriate AudioGroupInfo for the given player
        AudioGroupPlayerInfo info = null;
        switch (status) {
            case LISTENING:
                info = new AudioGroupPlayerInfo(false, ChatType.PUBLIC);
                info.listenAttenuation = getListenVolume();
                info.speakingAttenuation = 0;
                break;
            case SPEAKING:
                info = new AudioGroupPlayerInfo(true, ChatType.PUBLIC);
                // it would be better if we could set this to 0, so you could
                // speak without being heard, but that causes other people
                // in the speaking area not to be able to hear you
                info.listenAttenuation = getListenVolume();
                info.speakingAttenuation = getSpeakingVolume();
                break;
            case BOTH:
                info = new AudioGroupPlayerInfo(true, ChatType.PUBLIC);
                info.listenAttenuation = getListenVolume();
                info.speakingAttenuation = getSpeakingVolume();
                break;
        }

        // update the player
        logger.fine("Update player " + callId + " in group " + name +
                    " info = " + info);
        audioGroup.addPlayer(player, info);
    }


    
}
