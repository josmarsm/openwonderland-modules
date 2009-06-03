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

import java.util.Set;
import org.jdesktop.wonderland.common.cell.state.CellClientState;


/**
 *
 * @author bh37721
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

     public EventPlayerClientState(Set<Tape> tapes, Tape selectedTape, boolean isPlaying, String userName) {
        this();
        this.tapes = tapes;
        this.selectedTape = selectedTape;
        this.isPlaying = isPlaying;
        this.userName = userName;
    }

    public Set getTapes() {
        return tapes;
    }

    public Tape getSelectedTape() {
        return selectedTape;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

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

