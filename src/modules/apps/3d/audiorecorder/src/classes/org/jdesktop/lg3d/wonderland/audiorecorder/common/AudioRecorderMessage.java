/**
 * Project Looking Glass
 * 
 * $RCSfile: AudioRecorderMessage.java,v $
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
 * $Revision: 1.1.2.4 $
 * $Date: 2008/02/06 11:57:58 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.audiorecorder.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataBoolean;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 *
 */
public class AudioRecorderMessage extends Message {
     private boolean isRecording;
     private boolean isPlaying;
     private String userName;
    
    public AudioRecorderMessage() {
        this (false, false, null);
    }
    
    public AudioRecorderMessage(boolean isRecording, boolean isPlaying, String userName) {
        this.isRecording = isRecording;
        this.isPlaying = isPlaying;
        this.userName = userName;
    }

    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying();
    }

    public String getUserName() {
        return userName;
    }
    

    protected void extractMessageImpl(ByteBuffer data) {
        isRecording = DataBoolean.value(data);
        isPlaying = DataBoolean.value(data);
        userName = DataString.value(data);
    }

    protected void populateDataElements() {
        dataElements.clear();
        dataElements.add(new DataBoolean(isRecording));
        dataElements.add(new DataBoolean(isPlaying));
        dataElements.add(new DataString(userName));
    }    
}
