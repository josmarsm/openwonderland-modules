/**
 * Project Looking Glass
 * 
 * $RCSfile: AudioRecorderMessage.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.1.2.4 $
 * $Date: 2008/02/06 11:57:58 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.audiorecorder.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataBoolean;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataDouble;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;


/**
 * Message from GLO to Cell to indicate state of server object.
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderMessage extends Message {


    public enum RecorderGLOAction {
	SET_VOLUME,
	PLAYBACK_DONE,
        RECORD,
	PLAY
    };

    private RecorderGLOAction action;
    private boolean isRecording;
    private boolean isPlaying;
    private String userName;
    private double volume;

    /**
     * Default constructor
     */
    public AudioRecorderMessage() {
        super();
    }
    
    /**
     * Static method to create an instance of AudioRecorderMessage that
     * indicates that playback has finished. 
     * @return an instance of class AudioRecorderMessage with action <code>PLAYBACK_DONE</code>
     */public static AudioRecorderMessage playbackDone() {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.action = RecorderGLOAction.PLAYBACK_DONE;
        return msg;
    }
     
     /**
     * Static method used to create an instance of AudioRecorderMessage that has an action type
     * <code>PLAY</code>.
     * @param playing boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderMessage playingMessage(boolean playing, String userName) {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.userName = userName;
        msg.action = RecorderGLOAction.PLAY;
        msg.isPlaying = playing;
        return msg;
    }
    
    /**
     * Static method used to create an instance of AudioRecorderMessage that has an action type
     * <code>RECORD</code>.
     * @param playing boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderMessage recordingMessage(boolean playing, String userName) {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.userName = userName;
        msg.action = RecorderGLOAction.RECORD;
        msg.isPlaying = playing;
        return msg;
    }
    

    public RecorderGLOAction getAction() {
	return action;
    }

    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }

    public String getUserName() {
        return userName;
    }
    
    

    public void setVolume(double volume) {
	this.volume = volume;
    }

    public double getVolume() {
	return volume;
    }

    protected void extractMessageImpl(ByteBuffer data) {
        action = RecorderGLOAction.values()[DataInt.value(data)];
        isRecording = DataBoolean.value(data);
        isPlaying = DataBoolean.value(data);
        userName = DataString.value(data);
        switch (action) {
            case SET_VOLUME:
                volume = DataDouble.value(data);
                break;
            case PLAYBACK_DONE:
                break;
            default:
                
        }
    }

    protected void populateDataElements() {
        dataElements.clear();
        dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataBoolean(isRecording));
        dataElements.add(new DataBoolean(isPlaying));
        dataElements.add(new DataString(userName));
        switch (action) {
            case SET_VOLUME:
                dataElements.add(new DataDouble(volume));
                break;
            case PLAYBACK_DONE:
                 break;
            default:
                 System.err.println("No such action: " + action);
        }
    }    
}
