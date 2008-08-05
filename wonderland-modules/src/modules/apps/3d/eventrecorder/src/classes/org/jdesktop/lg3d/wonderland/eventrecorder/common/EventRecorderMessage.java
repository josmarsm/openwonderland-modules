/**
 * Project Looking Glass
 * 
 * $RCSfile: RecordingDeviceMessage.java,v $
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
 * $Revision: 1.1.2.5 $
 * $Date: 2008/02/20 10:41:57 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.eventrecorder.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.DataCellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarCellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarP2PMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellHierarchyMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataBoolean;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataByteArray;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.eventrecorder.RecordableCellGLO;

/**
 * This message is sent from the server to the client/s to let them know either
 * that the state of the server object has changed (i.e. someone is now recording, or not); 
 * or to let them (in practice just one client) know what the state of the world is.
 */
public class EventRecorderMessage extends Message {

    
    public enum ActionType {START_SYNC, END_SYNC, START_SYNC_CELLS, SYNC_CELL, END_SYNC_CELLS, START_SYNC_STATE, END_SYNC_STATE, SYNC_STATE, RECORDING};
    
    private ActionType actionType; 
    private boolean isRecording;
    private String userName;
    private Message wrappedMessage;
    private String channelName;
    private CellID cellId;
    
    public EventRecorderMessage() {
        
    }
    
    public static EventRecorderMessage startSync() {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.START_SYNC;
        return message;
    }
    
    public static EventRecorderMessage endSync() {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.END_SYNC;
        return message;
    }    
    
    public static EventRecorderMessage startSyncCells() {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.START_SYNC_CELLS;
        return message;
    }
    
    public static EventRecorderMessage endSyncCells() {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.END_SYNC_CELLS;
        return message;
    }
    
    public static EventRecorderMessage startSyncState() {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.START_SYNC_STATE;
        return message;
    }
    
    public static EventRecorderMessage endSyncState() {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.END_SYNC_STATE;
        return message;
    } 
    

    /**
     * 
     * @param cellHierarchyMessage
     * @return
     */
    public static EventRecorderMessage cellHierarchyMessage(CellHierarchyMessage cellHierarchyMessage) {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.SYNC_CELL;
        message.wrappedMessage = cellHierarchyMessage;
        return message;
    }
    
    public static EventRecorderMessage synchronizeStateMessage(CellMessage syncMessage, RecordableCellGLO cellGLO) {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.SYNC_STATE;
        message.wrappedMessage = syncMessage;
        message.channelName = cellGLO.getChannelName();
        message.cellId = cellGLO.getCellID();
        return message;
    }
    
    public static EventRecorderMessage synchronizeAvatarMessage(Message avatarMessage, String channelName, CellID cellID) {
        EventRecorderMessage message = new EventRecorderMessage();
        message.actionType = ActionType.SYNC_STATE;
        message.wrappedMessage = avatarMessage;
        message.channelName = channelName;
        message.cellId = cellID;
        return message;
    }
    
    
    
    public EventRecorderMessage(boolean isRecording, String userName) {
        this.actionType = ActionType.RECORDING;
        this.isRecording = isRecording;
        this.userName = userName;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getUserName() {
        return userName;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public Message getWrappedMessage() {
        return wrappedMessage;
    }
    
    public CellID getCellID() {
        return cellId;
    }
    
    public String getChannelName() {
        return channelName;
    }
    

    protected void extractMessageImpl(ByteBuffer data) {
        actionType = ActionType.values()[DataInt.value(data)];
        switch(actionType) {
            case RECORDING :
                isRecording = DataBoolean.value(data);
                userName = DataString.value(data);
                break;
            case SYNC_CELL:
                wrappedMessage = Message.extractMessage(DataByteArray.value(data));
                break;
            case SYNC_STATE:
                wrappedMessage = Message.extractMessage(DataByteArray.value(data));
                cellId = DataCellID.value(data);
                channelName = DataString.value(data);
                break;
        }
    }

    protected void populateDataElements() {
        dataElements.clear();
        dataElements.add(new DataInt(actionType.ordinal()));
        switch(actionType) {
            case RECORDING :
                dataElements.add(new DataBoolean(isRecording));
                dataElements.add(new DataString(userName));
                break;
            case SYNC_CELL:
                dataElements.add(new DataByteArray(wrappedMessage.getBytes()));
                break;
            case SYNC_STATE:
                dataElements.add(new DataByteArray(wrappedMessage.getBytes()));
                dataElements.add(new DataCellID(cellId));
                dataElements.add(new DataString(channelName));
                break;
        }
    }    
}
