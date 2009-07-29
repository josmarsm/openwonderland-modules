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

import java.util.Set;
import org.jdesktop.wonderland.common.cell.state.CellClientState;


/**
 * Class to represent the state of the event player client
 * @author Bernard Horan
 */
public class EventPlayerClientState extends CellClientState {
    private Set<Tape> tapes;
    private Tape selectedTape;
    private boolean isPlaying;
    private String userName;

    /** Default constructor */
    public EventPlayerClientState() {
        super();
    }

    /**
     * Constructor to set the state of fields
     * @param tapes the initial state of the tapes field
     * @param selectedTape the initial state of the selectedTape field
     * @param isPlaying the initial state of the isPlaying boolean field
     * @param userName the initial state of the userName field
     */
    public EventPlayerClientState(Set<Tape> tapes, Tape selectedTape, boolean isPlaying, String userName) {
        this();
        this.tapes = tapes;
        this.selectedTape = selectedTape;
        this.isPlaying = isPlaying;
        this.userName = userName;
    }

    /**
     * Access the collection of tapes
     * @return the set of tapes
     */
    public Set<Tape> getTapes() {
        return tapes;
    }

    /**
     * Access the selectedTape field
     * @return the selected Tape
     */
    public Tape getSelectedTape() {
        return selectedTape;
    }

    /**
     * Access the isPlaying field
     * @return isPlaying
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Access the userName field
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getClass().getSimpleName());
        buffer.append("(");
        buffer.append("isPlaying: " + isPlaying);
        buffer.append(" userName: " + userName);
        buffer.append(")");
        return buffer.toString();
    }
}

