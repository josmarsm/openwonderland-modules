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
	PLAY,
        TAPE_USED,
        TAPE_SELECTED,
        NEW_TAPE
    };

    private RecorderGLOAction action;
    private boolean isRecording;
    private boolean isPlaying;
    private String userName;
    private double volume;
    private String tapeName;

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
     * @param recording boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderMessage recordingMessage(boolean recording, String userName) {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.userName = userName;
        msg.action = RecorderGLOAction.RECORD;
        msg.isRecording = recording;
        return msg;
    }
    
    public static AudioRecorderMessage tapeUsedMessage(String tapeName) {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.action = RecorderGLOAction.TAPE_USED;
        msg.tapeName = tapeName;
        return msg;
    }
    
    public static AudioRecorderMessage tapeSelectedMessage(String tapeName) {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.action = RecorderGLOAction.TAPE_SELECTED;
        msg.tapeName = tapeName;
        return msg;
    }
    
    public static AudioRecorderMessage newTapeMessage(String tapeName) {
        AudioRecorderMessage msg = new AudioRecorderMessage();
        msg.action = RecorderGLOAction.NEW_TAPE;
        msg.tapeName = tapeName;
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
    
    public String getTapeName() {
        return tapeName;
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
            case TAPE_USED:
                tapeName = DataString.value(data);
                break;
            case TAPE_SELECTED:
                tapeName = DataString.value(data);
                break;
            case NEW_TAPE:
                tapeName = DataString.value(data);
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
            case TAPE_USED:
                dataElements.add(new DataString(tapeName));
                break;
            case TAPE_SELECTED:
                dataElements.add(new DataString(tapeName));
                break;
            case NEW_TAPE:
                dataElements.add(new DataString(tapeName));
                break;
            default:
                 
        }
    }    
}
