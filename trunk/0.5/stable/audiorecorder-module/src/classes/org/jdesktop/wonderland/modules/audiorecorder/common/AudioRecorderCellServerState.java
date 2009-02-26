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

package org.jdesktop.wonderland.modules.audiorecorder.common;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Bernard Horan
 */
@XmlRootElement(name="audiorecorder-cell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class AudioRecorderCellServerState extends CellServerState implements Serializable {
    private String recordingDirectory;

    @XmlElementWrapper(name = "tapes")
    @XmlElement(name="tape")
    private Set<Tape> tapes = new HashSet<Tape>();

    private Tape selectedTape;

    @XmlAttribute(required=true)
    private boolean isPlaying;

    @XmlAttribute(required=true)
    private boolean isRecording;

    private String userName;


    public AudioRecorderCellServerState() {
    }

    public void addTape(Tape aTape) {
        tapes.add(aTape);
    }

    public Tape getSelectedTape() {
        return selectedTape;
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.audiorecorder.server.AudioRecorderCellMO";
    }

    public Set<Tape> getTapes() {
        return tapes;
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

    public void setRecordingDirectory(String recordingDirectory) {
        this.recordingDirectory = recordingDirectory;
    }

    public String getRecordingDirectory() {
        return recordingDirectory;
    }

    public void setSelectedTape(Tape aTape) {
        selectedTape = aTape;
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
        builder.append(" recordingDirectory=");
        builder.append(recordingDirectory);
        builder.append(" userName=");
        builder.append(userName);
        builder.append(" selectedTape=");
        builder.append(selectedTape);
        builder.append(" tapes=");
        builder.append(tapes);
        return builder.toString();
    }
    

    /**
     * Test marshalling and unmarshalling
     * @param args ignored
     * @throws javax.xml.bind.JAXBException
     */
    public static void main(String[] args) throws JAXBException {
        AudioRecorderCellServerState originalState = new AudioRecorderCellServerState();
        originalState.setUserName("Bernard");
        originalState.setRecordingDirectory("recording directory");
        Tape aTape = new Tape("tapeName");
        originalState.setSelectedTape(aTape);
        originalState.addTape(aTape);
        aTape = new Tape("another tape");
        originalState.addTape(aTape);
        CharArrayWriter writer = new CharArrayWriter();
        System.out.println(originalState);
        originalState.encode(writer);
        writer.flush();
        System.out.println(writer.toString());
        CharArrayReader reader = new CharArrayReader(writer.toCharArray());
        AudioRecorderCellServerState inputState = (AudioRecorderCellServerState) CellServerState.decode(reader);

        System.out.println(inputState);
    }

}
