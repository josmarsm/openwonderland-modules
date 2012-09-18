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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell;

import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;
//import javax.swing.text.SimpleAttributeSet;
import java.util.ArrayList;
import java.util.List;

/**
 * The sticky note client state
 * @author Xiuzhen
 */
@ExperimentalAPI
public class SectionStyledStickyNoteCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(StickyNoteCellClientState.class.getName());
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final String DEFAULT_TEXT = "";
    private static final String DEFAULT_TYPE = StickyNoteTypes.SECTION_STYLED;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private String noteText = DEFAULT_TEXT;
    private String secondNoteText = DEFAULT_TEXT;
    private List<NoteAttributeSet> allNoteAttributes;
    private String noteType = DEFAULT_TYPE;
    private String noteName = "Name";
    private String noteAssignee = "Person";
    private String noteDue = "Today";
    private String noteStatus = "Not yet";
    private String noteColor = "255:255:153";
    private int sectionNum = 0;
    private SelectedTextStyle selectedTextS = new SelectedTextStyle();

    ;

    public SectionStyledStickyNoteCellClientState() {
        this(null);
        selectedTextS = new SelectedTextStyle();
        //allNoteAttributes=new ArrayList();
    }

    public SectionStyledStickyNoteCellClientState(Vector2f pixelScale) {
        super(pixelScale);
        selectedTextS = new SelectedTextStyle();
    }

    /*
     * Set the preferred width of the Swing test
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    /*
     * Get the preferred width of the Swing test
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    /*
     * Set the preferred height of the Swing test
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /*
     * Get the preferred height of the Swing test
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteString) {
        this.noteText = noteString;
    }

    public String getSecondNoteText() {
        return secondNoteText;
    }

    public void setSecondNoteText(String secondNoteString) {
        this.secondNoteText = secondNoteString;
    }

    public List<NoteAttributeSet> getAllNoteAttributes() {
        return allNoteAttributes;
    }

    public void setAllNoteAttributes(List<NoteAttributeSet> allNoteAttributes) {
        this.allNoteAttributes = allNoteAttributes;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getNoteAssignee() {
        return noteAssignee;
    }

    public void setNoteAssignee(String noteAssignee) {
        this.noteAssignee = noteAssignee;
    }

    public String getNoteDue() {
        return noteDue;
    }

    public void setNoteDue(String noteDue) {
        this.noteDue = noteDue;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(String noteStatus) {
        this.noteStatus = noteStatus;
    }

    public String getNoteColor() {
        return noteColor;
    }

    public void setNoteColor(String noteColor) {
        this.noteColor = noteColor;
    }

    public void setSelectedTextStyle(SelectedTextStyle selectedStyle) {

        this.selectedTextS = selectedStyle;
    }

    public SelectedTextStyle getSelectedTextStyle() {
        return selectedTextS;
    }

    public int getSectionNum() {
        return sectionNum;
    }

    public void setsectionNum(int sectionNum) {
        this.sectionNum = sectionNum;
    }

    public void copyLocal(SectionStyledStickyNoteCellClientState state) {
        setPreferredWidth(state.getPreferredWidth());
        setPreferredHeight(state.getPreferredHeight());
        setNoteText(state.getNoteText());
        setSecondNoteText(state.getSecondNoteText());
        setAllNoteAttributes(state.getAllNoteAttributes());
        setNoteType(state.getNoteType());
        setNoteAssignee(state.getNoteAssignee());
        setNoteDue(state.getNoteDue());
        setNoteName(state.getNoteName());
        setNoteStatus(state.getNoteStatus());
        setNoteColor(state.getNoteColor());
        setSelectedTextStyle(state.getSelectedTextStyle());
        setsectionNum(state.getSectionNum());

    }

    public boolean hasChanges(SectionStyledStickyNoteCellClientState state) {
        if (!state.getNoteAssignee().equals(noteAssignee)) {
            return true;
        }
        if (!state.getNoteDue().equals(noteDue)) {
            return true;
        }
        if (!state.getNoteName().equals(noteName)) {
            return true;
        }
        if (!state.getNoteStatus().equals(noteStatus)) {
            return true;
        }
        if (!state.getNoteText().equals(noteText)) {
            return true;
        }
        if (!state.getSecondNoteText().equals(secondNoteText)) {
            return true;
        }
        if (!state.getNoteType().equals(noteType)) {
            return true;
        }
        if (!state.getNoteColor().equals(noteColor)) {
            return true;
        }
        if (state.getSectionNum() != sectionNum) {
            return true;
        }

        //if (!state.getSelectedTextStyle().equals(selectedTextS)) {
        if (!selectedTextS.getButtonName().equals("")) {

            return true;
        }
        return false;
    }
}
