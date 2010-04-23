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
 * Class to represent the state of the event player cell on the server
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

    @XmlAttribute(required=true)
    private boolean isPaused;


    /**
     * Add a tape to the collection of tapes
     * @param aTape a tape to be added
     */
    public void addTape(Tape aTape) {
        tapes.add(aTape);
    }

    /**
     * Empty the collection of tapes
     */
    public void clearTapes() {
        tapes.clear();
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.eventplayer.server.EventPlayerCellMO";
    }

    /**
     * Returns state of iPlaying boolean
     * @return true if isPlaying is true, false otherwise
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean b) {
        isPaused = b;
    }

    /**
     * Set the state of the iPlaying boolean
     * @param p the new state of the isPlaying boolean
     */
    public void setPlaying(boolean p) {
        isPlaying = p;
    }

    /**
     * Set the state of the selectedTape field
     * @param selectedTape the new state of the selectedTape field
     */
    public void setSelectedTape(Tape selectedTape) {
        this.selectedTape = selectedTape;
    }

    /**
     * Set the collection of tapes
     * @param tapes a collection of tapes
     */
    public void setTapes(Set<Tape> tapes) {
        this.tapes = tapes;
    }

    /**
     * Set the state of the userName field
     * @param userName the new state of the userName field
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Access the collection of tapes
     * @return a Set of tapes
     */
    public Set<Tape> getTapes() {
        return tapes;
    }

    /**
     * Access the state of the selectedTape field
     * @return the selectedTape
     */
    public Tape getSelectedTape() {
        return selectedTape;
    }

    /**
     * Access the state of the userName field
     * @return the userName
     */
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
