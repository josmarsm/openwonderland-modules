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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;

/**
 *  The sticky note sync messages.
 * @author Ryan (mymegabyte)
 */
public class StickyNoteSyncMessage extends CellMessage implements Serializable {

    private StickyNoteCellClientState state;

    public StickyNoteSyncMessage(String newNote) {
        super();
        state = new StickyNoteCellClientState();
        state.setNoteType(StickyNoteTypes.GENERIC);
        state.setNoteText(newNote);
    }

    public StickyNoteSyncMessage(StickyNoteCellClientState state) {
        super();
        this.state = state;
    }

    public StickyNoteCellClientState getState() {
        return state;
    }

    public void setState(StickyNoteCellClientState state) {
        this.state = state;
    }
}
