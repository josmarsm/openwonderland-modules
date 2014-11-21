/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.server;

import com.sun.sgs.app.AppContext;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.Question;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionComponentClientState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionComponentServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.MyMessage;

import com.jme.math.Vector3f;
import com.sun.mpk20.voicelib.app.*;
import com.sun.sgs.app.ManagedReference;
import com.sun.voip.client.connector.CallStatus;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
/**
 *
 * @author Vladimir Djurovic
 */
public class QuestionComponentMO extends CellComponentMO implements ManagedCallStatusListener{
    
    private String sheetId;
    private int questionSrc;
    private Question question;
    private static CellMO parentCellMo;
    private static Recorder recorder;
    private static VoiceManager  vm;
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;
//    private static QuestionComponentMO  abc;
    
    public QuestionComponentMO(CellMO cellMo){
        super(cellMo);
        this.parentCellMo = cellMo;
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.isocial.scavenger.client.components.QuestionComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if(state == null){
            state = new QuestionComponentClientState();
            ((QuestionComponentClientState)state).setSheetId(sheetId);
            ((QuestionComponentClientState)state).setQuestionSrc(questionSrc);
            ((QuestionComponentClientState)state).setQuestion(question);
        }
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if(state == null){
            state = new QuestionComponentServerState();
            ((QuestionComponentServerState)state).setSheetId(sheetId);
            ((QuestionComponentServerState)state).setQuestionSrc(questionSrc);
            ((QuestionComponentServerState)state).setQuestion(question);
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        if(state == null){
            state = new QuestionComponentServerState();
        }
        sheetId = ((QuestionComponentServerState)state).getSheetId();
        questionSrc = ((QuestionComponentServerState)state).getQuestionSrc();
        question = ((QuestionComponentServerState)state).getQuestion();
    }
   
    // ADDED FOR ESL AUDIO Feature
    private static class ESLAudioMessageReceiver extends AbstractComponentMessageReceiver {
        public ESLAudioMessageReceiver(CellMO cellMO) {
            super(cellMO);
        }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            try { 
                //parentCellMo = (CellMO)getCell();  
                MyMessage msg = (MyMessage) message;           
                String action = msg.getAction();           
                System.out.println("msg.getAction() : "+msg.getAction());
                if(action.equals("START_RECORDING"))
                {
                        startRecording(msg.getFileName(),msg.getCallId(),msg.getVector(),msg.getUsername()); 
                        return;
                }
                if(action.equals("END_RECORDING"))
                {       
                        stopRecording(msg.getCallId(),msg.getUsername());
                        return;
                } 
            } catch(Exception e) {
                e.printStackTrace();
            }        
        }
        
    }
    //ADDED FOR ESL AUDIO  
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
          
        ChannelComponentMO channelComponent = channelRef.get();
        //ChannelComponentMO channelComponent = parentCellMo.getComponent(ChannelComponentMO.class);
        
        if (live == true) {
            ESLAudioMessageReceiver receiver = new ESLAudioMessageReceiver(cellRef.get());
            channelComponent.addMessageReceiver(MyMessage.class,receiver);
        }
        else {
            channelComponent.removeMessageReceiver(MyMessage.class);
        }
    }

    //ADDED FOR ESL AUDIO  
    public void callStatusChanged(CallStatus status) {
        System.out.println("Got call status " + status);
    }
    //ADDED FOR ESL AUDIO  
    public static void startRecording(String fileName,String callId,Vector3f vec,String username) {
        try {
           vm = AppContext.getManager(VoiceManager.class);
           vm.addCallStatusListener(new MyC(), callId);
            RecorderSetup setup = new RecorderSetup();
            Vector3f origin = vec;
            setup.x = origin.x;
            setup.y = origin.y;
            setup.z = origin.z;
            setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;
            try {
                recorder = vm.createRecorder(callId, setup);
            } catch (Exception e) {           
              
            }
            recorder.startRecording("../../content/users/"+username+"/question-capability-audio/temp/"+fileName+".au");                 
        } catch (Exception e) {          
           
        }
    }
    //ADDED FOR ESL AUDIO  
    private static void stopRecording(String callId,String username) {
        try {
           
            Recorder rec = vm.getRecorder(callId);
            rec.stopRecording();        
        } catch (Exception e) {           
            System.err.println(e);           
        }
    }
    //ADDED FOR ESL AUDIO  
    public static class MyC implements ManagedCallStatusListener {
        public void callStatusChanged(CallStatus cs) {}
    }
    
  
    
}
