/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.server;

import com.jme.math.Vector3f;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.voip.client.connector.CallStatus;
import java.io.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.standardsheet.common.AudioRecordingConnectionType;
import org.jdesktop.wonderland.modules.standardsheet.common.AudioRecordingMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;



/**
 * Handles text chat messages from the client.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AudioRecordingConnectionHandler implements ManagedCallStatusListener,ClientConnectionHandler, Serializable {

    Recorder recorder;
    String fpath;
    String fn;
    
    private static Logger logger = Logger.getLogger(AudioRecordingConnectionHandler.class.getName());

    /**
     * Stores the classes that have registered as listening for new chat messages.
     */
    private Set<ManagedReference> listeners = new HashSet<ManagedReference>();

//    public TextChatConnectionHandler() {
//        logger.info("DEFAULT CONSTRUCTOR called!");
//
//        listeners = new HashSet<ManagedReference>();
//    }

    public ConnectionType getConnectionType() {
        return AudioRecordingConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
    }

    public void clientConnected(WonderlandClientSender sender,
            WonderlandClientID clientID, Properties properties) {
        // ignore
    }

    public void clientDisconnected(WonderlandClientSender sender,WonderlandClientID clientID) {
        // ignore
    }

    public void messageReceived(WonderlandClientSender sender,
            WonderlandClientID clientID, Message message) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);
        AudioRecordingMessage tcm = (AudioRecordingMessage)message; 
        if(tcm.getTextMessage().equals("start")) {
            try {
                //Start Recorder
//                System.out.println("AudioRecordingConnectionHandler messageReceived() -Entered- : "+new Date().getTime());
                fn = tcm.getFname();
                vm.addCallStatusListener(this,tcm.getContentRepos());
                
                //setup the recorder
                RecorderSetup setup = new RecorderSetup();
                Vector3f origin = tcm.getVector();
                setup.x = origin.x;
                setup.y = origin.y;
                setup.z = origin.z;
                setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;
                
                //create Recorder
                recorder = vm.createRecorder(tcm.getContentRepos(),setup);
               
                //start Recorder
                recorder.startRecording("../../content/users/"+tcm.getFromUserName()+"/audiosheet/temp/"+tcm.getFname()+".au");
                
                } catch (IOException ex) {
                    Logger.getLogger(AudioRecordingConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                //get the recorder from callID
                Recorder rec = vm.getRecorder(tcm.getContentRepos());
                //stop recorder
                rec.stopRecording();
                
                
            } catch (IOException ex) {
                
                Logger.getLogger(AudioRecordingConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
//            System.out.println("AudioRecordingConnectionHandler messageReceived() -Exit- : "+new Date().getTime());
         }
    }
    
    public void callStatusChanged(CallStatus status) {
        
    }
    

    /**
     * Convenience method for the two paramter version that sends the message from
     * a fake "Server" user.
     *
     * @param msg The body of the text chat message.
     */
    public void sendGlobalMessage(String msg) {
        this.sendGlobalMessage("Server", msg);
    }

    /**
     * Sends a global text message to all users. You can decide who the message should
     * appear to be from; it doesn't need to map to a known user.
     *
     * @param from The name the message should be displayed as being from.
     * @param msg The body of the text chat message.
     */
    public void sendGlobalMessage(String from, String msg) {
        logger.finer("Sending global message from " + from + ": " + msg);
        // Originally included for the XMPP plugin, so people chatting with the XMPP bot
        // can have their messages replicated in-world with appropriate names.
        //
        // Of course, there are some obvious dangerous with this: it's not that hard
        // to fake an xmpp name to look like someone it's not. In an otherwise
        // authenticated world, this might be a way to make it look like
        // people are saying things they're not.

        CommsManager cm = WonderlandContext.getCommsManager();
        WonderlandClientSender sender = cm.getSender(AudioRecordingConnectionType.CLIENT_TYPE);

        // Send to all clients, because the message is originating from a non-client source.
        Set<WonderlandClientID> clientIDs = sender.getClients();

        // Construct a new message with appropriate fields.
        AudioRecordingMessage textMessage = new AudioRecordingMessage(msg, from, "",false,false,"",null);
        sender.send(clientIDs, textMessage);
    }

    /**
     * Adds a listener object that will be called whenever a text chat message is sent.
     * Global messages sent from sendGlobalMessage are not included in these notifications.
     *
     * @param listener The listener object.
     */
    public void addTextChatMessageListener(AudioRecordingMessageListener listener) {
        
        this.listeners.add(AppContext.getDataManager().createReference(listener));
    }

    /**
     * Removes the listener object from the list of listeners.
     * 
     * @param listener The listener object.
     */
    public void removeTextChatMessageListener(AudioRecordingMessageListener listener) {
        this.listeners.remove(AppContext.getDataManager().createReference(listener));
    }
}

