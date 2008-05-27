/**
 * Project Looking Glass
 * 
 * $RCSfile: RecordingDeviceCellMessage.java,v $
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
 * $Revision: 1.1.2.3 $
 * $Date: 2008/03/04 17:00:47 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.eventrecorder.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataBoolean;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 */
public class EventRecorderCellMessage extends CellMessage {

    

    
    public enum ActionType {SYNC_REQUEST, RECORDING};
    private boolean isRecording;
    private String userName;
    private ActionType actionType;
    
    public EventRecorderCellMessage() {
        super();
    }
    
    private EventRecorderCellMessage(CellID id) {
        super(id);
    }
    
    public EventRecorderCellMessage(CellID cellID, boolean isRecording, String userName) {
        this(cellID);
        this.isRecording = isRecording;
        this.userName = userName;
        this.actionType = ActionType.RECORDING;
    }
    
    public static EventRecorderCellMessage synchronizationRequest(CellID id) {
       EventRecorderCellMessage msg = new EventRecorderCellMessage(id);
       msg.actionType = ActionType.SYNC_REQUEST;
       return msg;
    }
   
    public boolean isRecording() {
        return isRecording;
    }

    public ActionType getActionType() {
        return actionType;
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
        actionType = ActionType.values()[DataInt.value(data)];
        switch(actionType) {
            case RECORDING :
                isRecording = DataBoolean.value(data);
                userName = DataString.value(data);
                break;
        }
    }

    @Override
    protected void populateDataElements() {
        super.populateDataElements();
        dataElements.add(new DataInt(actionType.ordinal()));
        switch(actionType) {
            case RECORDING :
                dataElements.add(new DataBoolean(isRecording));
                dataElements.add(new DataString(userName));
                break;
        }
    }

    public String getUserName() {
        return userName;
    }
}
