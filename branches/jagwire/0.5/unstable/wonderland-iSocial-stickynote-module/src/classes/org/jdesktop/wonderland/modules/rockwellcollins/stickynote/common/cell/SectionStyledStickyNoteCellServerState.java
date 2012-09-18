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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;
import java.util.ArrayList;
import java.util.List;
//import java util.List<NoteAttribute>;

/**
 * The WFS server state class for StickynoteCellMO.
 *
 * @author Ryan (mymegabyte)
 */
@XmlRootElement(name = "sectionStickynote-cell")
@ServerState
public class SectionStyledStickyNoteCellServerState extends App2DCellServerState {

    /** The user's preferred width of the Swing test window. */
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 300;
    /** The user's preferred height of the Swing test window. */
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 300;
    /** The X pixel scale of the Swing test window. */
    @XmlElement(name = "pixelScaleX")
    public float pixelScaleX = 0.01f;
    /** The Y pixel scale of the Swing test window. */
    @XmlElement(name = "pixelScaleY")
    public float pixelScaleY = 0.01f;
    /** The note type */
    @XmlElement(name = "noteType")
    public String noteType = StickyNoteTypes.SECTION_STYLED;
    /** The note text */
    @XmlElement(name = "noteText")
    public String noteText = "";
    /** The note text */
    @XmlElement(name = "secondNoteText")
    public String secondNoteText = "";
    /** The note text */
    @XmlElement(name = "allNoteAttributes")
    //public ArrayList allNoteAttributes = new ArrayList();
    private List<NoteAttributeSet> allNoteAttributes;
    /** Summary */
    @XmlElement(name = "noteName")
    public String noteName = "";
    /** Person working on it */
    @XmlElement(name = "noteAssignee")
    public String noteAssignee = "";
    /** Due/Completion date */
    @XmlElement(name = "noteDue")
    public String noteDue = "";
    /** ? */
    @XmlElement(name = "noteStatus")
    public String noteStatus = "";
    @XmlElement(name = "noteColor")
    public String color = "255:255:153";
    public SelectedTextStyle SelectedTextS = new SelectedTextStyle();

    /** Default constructor */
    public SectionStyledStickyNoteCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.SectionStyledStickyNoteCellMO";
    }

    @XmlTransient
    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    @XmlTransient
    public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    @XmlTransient
    public float getPixelScaleX() {
        return pixelScaleX;
    }

    public void setPixelScaleX(float pixelScale) {
        this.pixelScaleX = pixelScaleX;
    }

    @XmlTransient
    public float getPixelScaleY() {
        return pixelScaleY;
    }

    @Override
    public void setPixelScaleY(float pixelScale) {
        this.pixelScaleY = pixelScaleY;
    }

    @XmlTransient
    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    @XmlTransient
    public String getSecondNoteText() {
        return secondNoteText;
    }

    public void setSecondNoteText(String secondNoteText) {
        this.secondNoteText = secondNoteText;
    }

    @XmlTransient
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
    if (endPos >= startPos && this.allNoteAttributes.size() > endPos) {
    for(int i=startPos;i<=endPos;i++)
    result.add(this.allNoteAttributes.get(i));
    }
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
     * */
    @XmlTransient
    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    @XmlTransient
    public String getNoteAssignee() {
        return noteAssignee;
    }

    public void setNoteAssignee(String noteAssignee) {
        this.noteAssignee = noteAssignee;
    }

    @XmlTransient
    public String getNoteDue() {
        return noteDue;
    }

    public void setNoteDue(String noteDue) {
        this.noteDue = noteDue;
    }

    @XmlTransient
    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    @XmlTransient
    public String getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(String noteStatus) {
        this.noteStatus = noteStatus;
    }

    @XmlTransient
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public SelectedTextStyle getSelectedTextStyle() {
        return this.SelectedTextS;
    }

    public void setSelectedTextStyle(SelectedTextStyle thisSelectedTextStyle) {
        this.SelectedTextS = thisSelectedTextStyle;
    }

    /**
     * Returns a string representation of this class.
     *
     * @return The server state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [StickynoteCellServerState]: "
                + "preferredWidth=" + preferredWidth + ","
                + "preferredHeight=" + preferredHeight + ","
                + "pixelScaleX=" + pixelScaleX + ","
                + "pixelScaleY=" + pixelScaleY + ","
                + "noteType=" + noteType + ","
                + "SelectedTextStartPos=" + SelectedTextS.getSelectedStartPos() + ","
                + "SelectedTextEndPos=" + SelectedTextS.getSelectedEndPos() + ","
                + "SelectedButtonName=" + SelectedTextS.getButtonName() + ","
                + "noteText=" + noteText + ","
                + "secondNoteText=" + secondNoteText;
    }
}
