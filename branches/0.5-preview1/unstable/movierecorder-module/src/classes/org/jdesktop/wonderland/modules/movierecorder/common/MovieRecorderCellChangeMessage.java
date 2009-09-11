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

package org.jdesktop.wonderland.modules.movierecorder.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */

public class MovieRecorderCellChangeMessage extends CellMessage {

    private MovieRecorderCellChangeMessage(CellID cellID) {
        super(cellID);
    }

    public enum MovieRecorderAction {
        RECORD
    };

    private MovieRecorderAction action;
    private boolean isRecording;
    private String userName;

    public MovieRecorderAction getAction() {
        return action;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * Static method used to create an instance of MovieRecorderCellChangeMessage that has an action type
     * <code>RECORD</code>.
     * @param cellID The id of the cell for which this message is created
     * @param recording boolean to indicate the state of the recorder
     * @param userName the name of the user that initiated this change
     * @return a message with appropriate state
     */
    public static MovieRecorderCellChangeMessage recordingMessage(CellID cellID, boolean recording, String userName) {
        MovieRecorderCellChangeMessage msg = new MovieRecorderCellChangeMessage(cellID);
        msg.userName = userName;
        msg.action = MovieRecorderAction.RECORD;
        msg.isRecording = recording;
        return msg;
    }

    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(action);
        builder.append(": ");
        switch (action) {
            case RECORD:
                builder.append("isRecording: " + isRecording);
                break;
            default:
                throw new RuntimeException("Invalid action");
        }
        return builder.toString();
    }
}
