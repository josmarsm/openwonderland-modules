/**
 * Project Wonderland
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
 * $Revision$
 * $Date$
 * $State$
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
    private String tapeName;

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
        return tapeName;
    }

    public static AudioRecorderCellChangeMessage newTape(CellID cellID, String tapeName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.NEW_TAPE;
        msg.tapeName = tapeName;
        return msg;
    }

    public static AudioRecorderCellChangeMessage tapeSelected(CellID cellID, String tapeName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.TAPE_SELECTED;
        msg.tapeName = tapeName;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellChangeMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param recording boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage recordingMessage(CellID cellID, boolean recording, String userName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.userName = userName;
        msg.action = AudioRecorderAction.RECORD;
        msg.isRecording = recording;
        return msg;
    }

    public static AudioRecorderCellChangeMessage setTapeUsed(CellID cellID, String tapeName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.TAPE_USED;
        msg.tapeName = tapeName;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>PLAY</code>.
     * @param cellID The id of the cell for which this message is created
     * @param playing boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static AudioRecorderCellChangeMessage playingMessage(CellID cellID, boolean playing, String userName) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.userName = userName;
        msg.action = AudioRecorderAction.PLAY;
        msg.isPlaying = playing;
        return msg;
    }

    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>PLAYBACK_DONE</code>.
     * @param cellID The id of the cell for which this message is created
     */
    public static AudioRecorderCellChangeMessage playbackDoneMessage(CellID cellID) {
        AudioRecorderCellChangeMessage msg = new AudioRecorderCellChangeMessage(cellID);
        msg.action = AudioRecorderAction.PLAYBACK_DONE;
        return msg;
    }
}
