/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.webcaster.server;

import com.jme.math.Vector3f;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.voip.client.connector.CallStatus;
import java.io.IOException;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellChangeMessage;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCellMO extends CellMO implements ManagedCallStatusListener
{
    private transient boolean isWebcasting;
    private String streamID;

    private String callId;
    private Recorder recorder;
    
    public WebcasterCellMO(){
        super();
        isWebcasting = false;
        
        callId = getCellID().toString();
        int ix = callId.indexOf("@");
        if (ix >= 0) {
            callId = callId.substring(ix + 1);
        }
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            ChannelComponentMO channel = getChannel();
            if (channel == null) {
                throw new IllegalStateException("Cell does not have a ChannelComponent");
            }
            //Add the message receiver to the channel
            channel.addMessageReceiver(WebcasterCellChangeMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new WebcasterCellMOMessageReceiver(this));
        } else {
            getChannel().removeMessageReceiver(WebcasterCellChangeMessage.class);
        }
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities){
        return "org.jdesktop.wonderland.modules.webcaster.client.WebcasterCell";
    }
    
    @Override
    public void setServerState(CellServerState state){
        super.setServerState(state);
        this.streamID = ((WebcasterCellServerState)state).getStreamID();

        // Check to see if the CellServerState has a PositionComponentServerState
        // and takes it origin. This will only work upon the initial creation
        // of the cell and not when the cell is moved at all. This class should
        // add a transform change listener to listen for changes in the cell
        // origin after the cell has been created.
        CellComponentServerState s = state.getComponentServerState(PositionComponentServerState.class);
        if (s != null) {
            setupRecorder(((PositionComponentServerState) s).getTranslation());
        }
    }
    
    @Override
    public CellServerState getServerState(CellServerState state)
    {
        if (state == null) {
            state = new WebcasterCellServerState();
        }
        
        return super.getServerState(state);
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities)
    {
        if (cellClientState == null){
            cellClientState = new WebcasterCellClientState(isWebcasting, streamID);
        }

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    private void setWebcasting(boolean isWebcasting) {
        logger.warning("isWebcasting: " + isWebcasting);
        this.isWebcasting = isWebcasting;

        if (!isWebcasting){
            startRecording("webcaster" + this.streamID);
        }
        else{
            stopRecording();
        }
    }
    
    private void setupRecorder(Vector3f origin) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        vm.addCallStatusListener(this, callId);
        
        RecorderSetup setup = new RecorderSetup();

        setup.x = origin.x;
        setup.y = origin.y;
        setup.z = origin.z;

        logger.fine("Recorder Origin is " + "(" + origin.x + ":" + origin.y + ":" + origin.z + ")");

        setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;

        try {
            recorder = vm.createRecorder(callId, setup);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    private void startRecording(String recordingName) {
        try {
            recorder.startRecording("../../content/system/AudioRecordings/webcaster1.au");//recordingName + ".au");   //../../content/system/AudioRecordings/" + recordingName + ".au"
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void stopRecording() {
        try {
            recorder.stopRecording();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public void callStatusChanged(CallStatus cs) {
        //if (status.getCode() == CallStatus.RECORDERDONE) {
            //We can tell clients that the recording has finished
            //WebcasterCellChangeMessage arcm = WebcasterCellChangeMessage.recordingMessage(getCellID(), null, false);
            //getChannel().sendAll(recordingClientID, arcm);
            //recordingClientID = null;
        //}
    }
    
    private static class WebcasterCellMOMessageReceiver extends AbstractComponentMessageReceiver {
        public WebcasterCellMOMessageReceiver(WebcasterCellMO cellMO) {
            super(cellMO);
        }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            WebcasterCellMO cellMO = (WebcasterCellMO)getCell();
            WebcasterCellChangeMessage wccm = (WebcasterCellChangeMessage)message;
            cellMO.setWebcasting(wccm.isWebcasting());
            cellMO.getChannel().sendAll(clientID, wccm);
        }
    }
    
    
}
