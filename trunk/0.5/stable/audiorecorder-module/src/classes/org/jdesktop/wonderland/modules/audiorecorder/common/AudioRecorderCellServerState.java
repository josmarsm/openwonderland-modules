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

package org.jdesktop.wonderland.modules.audiorecorder.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Class to represent the state of the audio recorder cell on the server
 * @author Bernard Horan
 */
@XmlRootElement(name="audiorecorder-cell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class AudioRecorderCellServerState extends CellServerState implements Serializable {   

    @XmlAttribute(required=true)
    private boolean isPlaying;

    @XmlAttribute(required=true)
    private boolean isRecording;

    private String userName;


    public AudioRecorderCellServerState() {
    }

    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.audiorecorder.server.AudioRecorderCellMO";
    }

    public void setPlaying(boolean b) {
        isPlaying = b;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setRecording(boolean b) {
        isRecording = b;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append("isPlaying=");
        builder.append(isPlaying);
        builder.append(" isRecording=");
        builder.append(isRecording);
        builder.append(" userName=");
        builder.append(userName);
        return builder.toString();
    }
}
