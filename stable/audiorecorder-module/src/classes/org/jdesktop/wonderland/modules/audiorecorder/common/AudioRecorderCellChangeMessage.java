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

package org.jdesktop.wonderland.modules.audiorecorder.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */

public class AudioRecorderCellChangeMessage extends CellMessage {

    private AudioRecorderCellChangeMessage(CellID cellID) {
        super(cellID);
    }

    public enum AudioRecorderAction {

        SET_VOLUME,
        PLAYBACK_DONE,
        RECORD,
        PLAY,
        TAPE_USED,
        TAPE_SELECTED,
        NEW_TAPE
    };

    private AudioRecorderAction action;
    private boolean isRecording;
    private boolean isPlaying;
    private String userName;
    private double volume;
    private Tape aTape;

    public AudioRecorderAction getAction() {
        return action;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getUserName() {
        return userName;
    }

    public double getVolume() {
        return volume;
    }

    public String getTapeName() {
        return aTape.getTapeName();
    }

    public Tape getTape() {
        return aTape;
    }

    

    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>TAPE_SELECTED</code>.
     * @param cellID The id of the cell for which this message is created
     * @param aTape the selected tape
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage tapeSelected(CellID cellID, Tape aTape) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.TAPE_SELECTED;
        msg.aTape = aTape;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellChangeMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param aTape the tape onto which we are recording
     * @param recording boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage recording(CellID cellID, Tape aTape, boolean recording, String userName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.aTape = aTape;
        msg.userName = userName;
        msg.action = AudioRecorderAction.RECORD;
        msg.isRecording = recording;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>PLAY</code>.
     * @param cellID The id of the cell for which this message is created
     * @param aTape the tape to play
     * @param playing boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage playing(CellID cellID, Tape aTape, boolean playing, String userName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.aTape = aTape;
        msg.userName = userName;
        msg.action = AudioRecorderAction.PLAY;
        msg.isPlaying = playing;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>TAPE_USED</code>.
     * @param cellID The id of the cell for which this message is created
     * @param aTape the tape that is no longer fresh
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage setTapeUsed(CellID cellID, Tape aTape) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.TAPE_USED;
        msg.aTape = aTape;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>PLAYBACK_DONE</code>.
     * @param cellID The id of the cell for which this message is created
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage playbackDone(CellID cellID) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.PLAYBACK_DONE;
        return msg;
    }

    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(action);
        builder.append(": ");
        switch (action) {
            case SET_VOLUME:
                builder.append("set volume: " + volume);
                break;
            case PLAYBACK_DONE:
                //do nothing
                break;
            case RECORD:
                builder.append("isRecording: " + isRecording);
                if (isRecording) {
                    builder.append(" tape: " + aTape);
                }
                break;
            case PLAY:
                builder.append("isPlaying: " + isPlaying);
                if (isPlaying) {
                    builder.append("tape: " + aTape);
                }
                break;
            case TAPE_SELECTED:
                builder.append("tape selected: " + aTape);
                break;
            case TAPE_USED:
                builder.append("tape used: " + aTape);
                break;
            default:
                throw new RuntimeException("Invalid action");
        }
        return builder.toString();
    }
}
