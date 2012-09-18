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


package org.jdesktop.wonderland.modules.eventrecorder.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
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
@XmlRootElement(name="eventrecorder-cell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class EventRecorderCellServerState extends CellServerState implements Serializable {
    @XmlElementWrapper(name = "tapes")
    @XmlElement(name="tape")
    private Set<Tape> tapes = new HashSet<Tape>();
    
    private Tape selectedTape;

    @XmlAttribute(required=true)
    private boolean isRecording;

    private String userName;

    public void addTape(Tape aTape) {
        tapes.add(aTape);
    }

    public void clearTapes() {
        tapes.clear();
    }

    public Tape getSelectedTape() {
        return selectedTape;
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.eventrecorder.server.EventRecorderCellMO";
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public void setSelectedTape(Tape selectedTape) {
        this.selectedTape = selectedTape;
    }

    public String getUserName() {
        return userName;
    }

    public void setTapes(Set<Tape> tapes) {
        this.tapes = tapes;
    }

    public Set<Tape> getTapes() {
        return tapes;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" isRecording=");
        builder.append(isRecording);
        builder.append(" userName=");
        builder.append(userName);
        builder.append(" selectedTape=");
        builder.append(selectedTape);
        builder.append(" tapes=");
        builder.append(tapes);
        return builder.toString();
    }
}
