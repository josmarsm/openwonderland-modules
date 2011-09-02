/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SelectedTextStyle;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellClientState;

/**
 *  The sticky note sync messages.
 * @author Xiuzhen (mymegabyte)
 */
public class SectionStyledStickyNoteSyncMessage  extends CellMessage implements Serializable {
    private SectionStyledStickyNoteCellClientState state;

    public SectionStyledStickyNoteSyncMessage (String newNote,int sectionNum) {
        super();
        state = new SectionStyledStickyNoteCellClientState();
        state.setNoteType(StickyNoteTypes.SECTION_STYLED);
        state.setNoteText(newNote);
        state.setSelectedTextStyle(new SelectedTextStyle());
        state.setsectionNum(sectionNum);
    }

    public SectionStyledStickyNoteSyncMessage(SectionStyledStickyNoteCellClientState state) {
        super();
        this.state = state;

    }

    public SectionStyledStickyNoteCellClientState getState() {
        return state;
    }

    public void setState(SectionStyledStickyNoteCellClientState state) {
        this.state = state;
    }
}
