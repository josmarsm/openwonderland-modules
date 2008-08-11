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
 * $Revision$
 * $Date$
 * $State$ 
 */
package org.jdesktop.lg3d.wonderland.audiorecorder.common;

import java.util.Set;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;

/**
 * Object that represents the setup data for an AudioRecorderCell.
 * Essentially acts as a record/Bean object, with appropriate getters and setters
 * @author Bernard Horan
 */
public class AudioRecorderCellSetup implements CellSetup {
    private boolean isRecording;
    private boolean isPlaying;
    private String userName;

    private String baseURL;
    private Set<Tape> tapes;
    private Tape selectedTape;

    public Tape getSelectedTape() {
        return selectedTape;
    }

    public Set<Tape> getTapes() {
        return tapes;
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }

    public void setSelectedTape(Tape selectedTape) {
        this.selectedTape = selectedTape;
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

    public void setTapes(Set<Tape> tapes) {
        this.tapes = tapes;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setBaseURL(String baseURL) {
	this.baseURL = baseURL;
    }

    public String getBaseURL() {
	return baseURL;
    }

}
