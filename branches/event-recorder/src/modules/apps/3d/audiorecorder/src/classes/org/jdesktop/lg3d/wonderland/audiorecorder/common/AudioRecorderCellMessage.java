/**
 * Project Looking Glass
 * 
 * $RCSfile: AudioRecorderCellMessage.java,v $
 * 
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.1.2.2 $
 * $Date: 2008/02/06 09:19:24 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.audiorecorder.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataBoolean;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataDouble;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;


/**
 * Message that's sent from a Cell to the server.
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderCellMessage extends CellMessage {
    
    public enum RecorderCellAction {
	RECORD,
	PLAY,
        SET_VOLUME
    };
    private RecorderCellAction action;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private String userName;
    private double volume;

    /**
     * Default constructor
     */
    public AudioRecorderCellMessage() {
        super();
    }
    
    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>PLAY</code>.
     * @param cellID The id of the cell for which this message is created
     * @param playing boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderCellMessage playingMessage(CellID cellID, boolean playing, String userName) {
        AudioRecorderCellMessage msg = new AudioRecorderCellMessage();
        msg.setCellID(cellID);
        msg.userName = userName;
        msg.action = RecorderCellAction.PLAY;
        msg.isPlaying = playing;
        return msg;
    }
    
    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param recording boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */public static AudioRecorderCellMessage recordingMessage(CellID cellID, boolean recording, String userName) {
        AudioRecorderCellMessage msg = new AudioRecorderCellMessage();
        msg.setCellID(cellID);
        msg.userName = userName;
        msg.action = RecorderCellAction.RECORD;
        msg.isRecording = recording;
        return msg;
    }
    
   
    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>SET_VOLUME</code>.
     * @param cellID The id of the cell for which this message is created
     * @param userName the name of the user that initiated this change
     * @param volume the level of the volume
     * @return a message with appropriate state
     */public static AudioRecorderCellMessage volumeMessage(CellID cellID, String userName, double volume) {
        AudioRecorderCellMessage msg = new AudioRecorderCellMessage();
        msg.setCellID(cellID);
        msg.userName = userName;
	msg.action = RecorderCellAction.SET_VOLUME;
        msg.volume = volume;
        return msg;
    }

    public RecorderCellAction getAction() {
	return action;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public double getVolume() {
	return volume;
    }
    
    public String getUserName() {
        return userName;
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
	super.extractMessageImpl(data);
        action = RecorderCellAction.values()[DataInt.value(data)];
        userName = DataString.value(data);
        switch (action) {
            case RECORD:
                isRecording = DataBoolean.value(data);
                break;
            case PLAY:
                isPlaying = DataBoolean.value(data);
                break;
            case SET_VOLUME:
                volume = DataDouble.value(data);
                break;
            default:
                System.err.println("Unknown action");     
        }
    }

    @Override
    protected void populateDataElements() {
	super.populateDataElements();
	dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataString(userName));
        switch(action) {
            case RECORD:
                dataElements.add(new DataBoolean(isRecording));
                break;
            case PLAY:
                dataElements.add(new DataBoolean(isPlaying));
                break;
            case SET_VOLUME:
                dataElements.add(new DataDouble(volume));
                break;
            default:
                System.err.println("Unknown action");
        }
    }

    

}
