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
@XmlRootElement(name="eventplayer-cell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class EventPlayerCellServerState extends CellServerState implements Serializable {
    @XmlElementWrapper(name = "tapes")
    @XmlElement(name="tape")
    private Set<Tape> tapes = new HashSet<Tape>();

    private Tape selectedTape;

    private String userName;

    @XmlAttribute(required=true)
    private boolean isPlaying;


    public void addTape(Tape aTape) {
        tapes.add(aTape);
    }

    public void clearTapes() {
        tapes.clear();
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.eventplayer.server.EventPlayerCellMO";
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean p) {
        isPlaying = p;
    }

    public void setSelectedTape(Tape selectedTape) {
        this.selectedTape = selectedTape;
    }

    public void setTapes(Set<Tape> tapes) {
        this.tapes = tapes;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<Tape> getTapes() {
        return tapes;
    }

    public Tape getSelectedTape() {
        return selectedTape;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" isPlayinging=");
        builder.append(isPlaying);
        builder.append(" userName=");
        builder.append(userName);
        builder.append(" selectedTape=");
        builder.append(selectedTape);
        builder.append(" tapes=");
        builder.append(tapes);
        return builder.toString();
    }
}
