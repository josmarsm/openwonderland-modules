/**
 * Project Looking Glass
 * 
 * $RCSfile: AudioRecorderCellSetup.java,v $
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
 * $Date: 2008/02/06 12:05:18 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.audiorecorder.common;

import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;

/**
 *
 */
public class AudioRecorderCellSetup implements CellSetup {
    private boolean isRecording;
    private boolean isPlaying;
    private String userName;
    
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }
    
    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
