/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.wonderOSC.client;



import com.illposed.osc.*;
import imi.character.avatar. AvatarContext.TriggerNames;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jdesktop.wonderland.modules.wonderOSC.client.*;

/**
 *
 * @author sergio2
 */
final public class wonderOSC extends Thread {
    public static int   SERVERPORT=3010;
    private WlAvatarCharacter avatarCharacter;
    Iterable<String> actions;
    private DataInputStream is = null;
    Logger logger;
    OSCPortIn receiver;
    boolean isRunning=false;

    public wonderOSC(/*WlAvatarCharacter avatar*/){
        //avatarCharacter=avatar;
        //actions= avatarCharacter.getAnimationNames();
        logger = Logger.getLogger("wonderOSC");
        try{        
            receiver=new OSCPortIn(SERVERPORT);
        }catch( java.net.SocketException e){}
        return;
        
    }

    @Override
    public void run(){
        
        receiver.startListening();
        receiver.run();
            return;
    }

    public void addListener(String messageType,  wonderOSCListener listener ){
            if(receiver.isListening()){
                receiver.stopListening();

            }
            this.receiver.addListener(messageType, listener);
            receiver.startListening();
            return;
    }


    public void closeForever(){
        if(this.receiver.isListening()){
            receiver.stopListening();
            receiver.close();
        }
    }

    void reopen(){
        receiver.startListening();
    }

    public void closeForNow(){
        receiver.stopListening();
        
    }
}



