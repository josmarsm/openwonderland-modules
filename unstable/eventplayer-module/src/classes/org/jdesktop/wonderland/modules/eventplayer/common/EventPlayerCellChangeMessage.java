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

package org.jdesktop.wonderland.modules.eventplayer.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Represents a message between client and server.
 * @author Bernard Horan
 */

public class EventPlayerCellChangeMessage extends CellMessage {

    
    



    
    
    public enum EventPlayerAction {
        LOAD,
        PLAY,
        REQUEST_TAPE_STATE,
        PLAYBACK_DONE,
        ALL_CELLS_RETRIEVED, ADD_REPLAYED_CHILD, REMOVE_REPLAYED_CHILD};

    private EventPlayerAction action;
    private boolean isPlaying;
    private String userName;
    private double volume;
    private String tapeName;

    private EventPlayerCellChangeMessage(CellID cellID) {
        super(cellID);
    }

    public EventPlayerAction getAction() {
        return action;
    }

    public boolean isPlaying() {
        return isPlaying;
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

    

    public static EventPlayerCellChangeMessage loadRecording(CellID cellID, String tapeName) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.action = EventPlayerAction.LOAD;
        msg.tapeName = tapeName;
        return msg;
    }

    /**
     * Static method used to create an instance of EventPlayerCellChangeMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param playing boolean to indicate the state of the player
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static EventPlayerCellChangeMessage playRecording(CellID cellID, boolean playing, String userName) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.userName = userName;
        msg.action = EventPlayerAction.PLAY;
        msg.isPlaying = playing;
        return msg;
    }

    
    /**
     * Static method to create an instance of EventPlayerCellChangeMessage that has an action type
     * <code>REQUEST_TAPE_STATE</code>.
     * @param cellID The id of the cell for which this message is created
     * @return a message with appropriate state and action
     */
    public static EventPlayerCellChangeMessage selectingTape(CellID cellID) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.action = EventPlayerAction.REQUEST_TAPE_STATE;
        return msg;
    }
    
    /**
     * Static method used to create an instance of AudioRecorderCellMessage that has an action type
     * <code>PLAYBACK_DONE</code>.
     * @param cellID The id of the cell for which this message is created
     * @return a message with appropriate state and action
     */
    public static EventPlayerCellChangeMessage playbackDone(CellID cellID) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.action = EventPlayerAction.PLAYBACK_DONE;
        return msg;
    }

    public static EventPlayerCellChangeMessage allCellsRetrieved(CellID cellID) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.action = EventPlayerAction.ALL_CELLS_RETRIEVED;
        return msg;
    }

    public static EventPlayerCellChangeMessage addReplayedChild(CellID cellID) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.action = EventPlayerAction.ADD_REPLAYED_CHILD;
        return msg;
    }

    public static EventPlayerCellChangeMessage removeReplayedChild(CellID cellID) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.action = EventPlayerAction.REMOVE_REPLAYED_CHILD;
        return msg;
    }
    
}
