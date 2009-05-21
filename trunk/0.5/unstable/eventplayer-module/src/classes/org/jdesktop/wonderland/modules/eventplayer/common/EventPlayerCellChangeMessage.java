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

package org.jdesktop.wonderland.modules.eventplayer.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Bernard Horan
 */

public class EventPlayerCellChangeMessage extends CellMessage {
    private EventRecorderAction action;
    private boolean isPlaying;
    private String userName;
    private double volume;
    private String tapeName;

    private EventPlayerCellChangeMessage(CellID cellID) {
        super(cellID);
    }

    public enum EventRecorderAction {
        LOAD,
        PLAY
    };

    

    public EventRecorderAction getAction() {
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
        msg.action = EventRecorderAction.LOAD;
        msg.tapeName = tapeName;
        return msg;
    }

    /**
     * Static method used to create an instance of EventRecorderCellChangeMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param playing boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static EventPlayerCellChangeMessage playRecording(CellID cellID, boolean playing, String userName) {
        EventPlayerCellChangeMessage msg = new EventPlayerCellChangeMessage(cellID);
        msg.userName = userName;
        msg.action = EventRecorderAction.PLAY;
        msg.isPlaying = playing;
        return msg;
    }

    

    

    
}
