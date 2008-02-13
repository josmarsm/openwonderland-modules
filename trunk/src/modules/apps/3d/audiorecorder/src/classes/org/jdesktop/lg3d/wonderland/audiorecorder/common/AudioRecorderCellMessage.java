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
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 */
public class AudioRecorderCellMessage extends CellMessage {
    private boolean isRecording;
    private boolean isPlaying;
    private String userName;
    
    public AudioRecorderCellMessage() {
        super();
    }
    
    public AudioRecorderCellMessage(CellID cellID, boolean isRecording, boolean isPlaying, String userName) {
        super(cellID);
        this.isRecording = isRecording;
        this.isPlaying = isPlaying;
        this.userName = userName;
    }
   
    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
        isRecording = DataBoolean.value(data);
        isPlaying = DataBoolean.value(data);
        userName = DataString.value(data);
    }

    @Override
    protected void populateDataElements() {
        super.populateDataElements();
        dataElements.add(new DataBoolean(isRecording));
        dataElements.add(new DataBoolean(isPlaying));
        dataElements.add(new DataString(userName));
    }

    public String getUserName() {
        return userName;
    }
}
