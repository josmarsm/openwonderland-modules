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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell;

import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;
import java.util.ArrayList;
import java.util.List;


/**
 * The sticky note client state
 * @author Ryan (mymegabyte)
 */
@ExperimentalAPI
public class StickyNoteCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(StickyNoteCellClientState.class.getName());
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final String DEFAULT_TEXT = "Post-it";
    private static final String DEFAULT_TYPE = StickyNoteTypes.GENERIC;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private String noteText = DEFAULT_TEXT;
    //private ArrayList  allNoteAttributes;
    private List<NoteAttributeSet> allNoteAttributes;
    private String noteType = DEFAULT_TYPE;
    private String noteName = "Name";
    private String noteAssignee = "Person";
    private String noteDue = "Today";
    private String noteStatus = "Not yet";
    private String noteColor = "255:255:153";

    private SelectedTextStyle selectedTextS=new SelectedTextStyle();;

    public StickyNoteCellClientState() {
        this(null);
        //allNoteAttributes=new ArrayList();
        //selectedTextS=new SelectedTextStyle();
    }

    public StickyNoteCellClientState(Vector2f pixelScale) {
        super(pixelScale);
        //allNoteAttributes=new ArrayList();
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

     public List<NoteAttributeSet> getAllNoteAttributes() {
        return allNoteAttributes;
    }
     public void setAllNoteAttributes(List<NoteAttributeSet> allNoteAttributes) {
        this.allNoteAttributes = allNoteAttributes;
    }
     /*

     public ArrayList getPartNoteAttributes(int startPos, int endPos) {
        ArrayList result;
        result = new ArrayList();
        int j=0;
       // if (endPos >= startPos && this.allNoteAttributes.size() > endPos) {
            for(int i=startPos;i<=endPos;i++)
            {
                result.add(j,this.allNoteAttributes.get(i));
                j++;
         }
       // }
        return result;
    }
    

    public void setPartNoteAttributes(ArrayList partNoteAttributes,int startPos,int endPos) {
        int j;
        j=0;
        for(int i=startPos;i<=endPos;i++)
        {

            this.allNoteAttributes.set(i, partNoteAttributes.get(j));
            j++;
        }
    }
*/
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
    public void setSelectedTextStyle(SelectedTextStyle selectedStyle)
    {

        this.selectedTextS=selectedStyle;
    }

    public SelectedTextStyle getSelectedTextStyle()
    {
        return selectedTextS;
    }

    public void copyLocal(StickyNoteCellClientState state) {
        setPreferredWidth(state.getPreferredWidth());
        setPreferredHeight(state.getPreferredHeight());
        setNoteText(state.getNoteText());
        setAllNoteAttributes(state.getAllNoteAttributes());
        setNoteType(state.getNoteType());
        setNoteAssignee(state.getNoteAssignee());
        setNoteDue(state.getNoteDue());
        setNoteName(state.getNoteName());
        setNoteStatus(state.getNoteStatus());
        setNoteColor(state.getNoteColor());
        setSelectedTextStyle(state.getSelectedTextStyle());

    }

    public boolean hasChanges(StickyNoteCellClientState state) {
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
        if (!state.getNoteType().equals(noteType)) {
            return true;
        }
        if (!state.getNoteColor().equals(noteColor)) {
            return true;
        }

        //if (!state.getSelectedTextStyle().equals(selectedTextS)) {
        if (!selectedTextS.getButtonName().equals("") ) {

            return true;
        }
        return false;
    }
}
