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


package org.jdesktop.wonderland.modules.eventrecorder.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Bernard Horan
 */

public class EventRecorderCellChangeMessage extends CellMessage {


    private EventRecorderCellChangeMessage(CellID cellID) {
        super(cellID);
    }

    public enum EventRecorderAction {
        RECORD,
        TAPE_USED,
        TAPE_SELECTED,
        NEW_TAPE,
        REQUEST_TAPE_STATE
    };

    private EventRecorderAction action;
    private boolean isRecording;
    private String userName;
    private double volume;
    private String tapeName;

    public EventRecorderAction getAction() {
        return action;
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

    public static EventRecorderCellChangeMessage newTape(CellID cellID, String tapeName) {
        EventRecorderCellChangeMessage msg = new EventRecorderCellChangeMessage(cellID);
        msg.action = EventRecorderAction.NEW_TAPE;
        msg.tapeName = tapeName;
        return msg;
    }

    public static EventRecorderCellChangeMessage tapeSelected(CellID cellID, String tapeName) {
        EventRecorderCellChangeMessage msg = new EventRecorderCellChangeMessage(cellID);
        msg.action = EventRecorderAction.TAPE_SELECTED;
        msg.tapeName = tapeName;
        return msg;
    }

    public static EventRecorderCellChangeMessage selectingTape(CellID cellID) {
        EventRecorderCellChangeMessage msg = new EventRecorderCellChangeMessage(cellID);
        msg.action = EventRecorderAction.REQUEST_TAPE_STATE;
        return msg;
    }


    /**
     * Static method used to create an instance of EventRecorderCellChangeMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param recording boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static EventRecorderCellChangeMessage recordingMessage(CellID cellID, boolean recording, String userName) {
        EventRecorderCellChangeMessage msg = new EventRecorderCellChangeMessage(cellID);
        msg.userName = userName;
        msg.action = EventRecorderAction.RECORD;
        msg.isRecording = recording;
        return msg;
    }

    public static EventRecorderCellChangeMessage setTapeUsed(CellID cellID, String tapeName) {
        EventRecorderCellChangeMessage msg = new EventRecorderCellChangeMessage(cellID);
        msg.action = EventRecorderAction.TAPE_USED;
        msg.tapeName = tapeName;
        return msg;
    }

    
}
