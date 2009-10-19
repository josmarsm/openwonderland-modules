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

/**
 * The WFS server state class for StickynoteCellMO.
 * 
 * @author Ryan (mymegabyte)
 */
@XmlRootElement(name="stickynote-cell")
@ServerState
public class StickyNoteCellServerState extends App2DCellServerState {

    /** The user's preferred width of the Swing test window. */
    @XmlElement(name="preferredWidth")
    public int preferredWidth = 300;

    /** The user's preferred height of the Swing test window. */
    @XmlElement(name="preferredHeight")
    public int preferredHeight = 300;

    /** The X pixel scale of the Swing test window. */
    @XmlElement(name="pixelScaleX")
    public float pixelScaleX = 0.01f;

    /** The Y pixel scale of the Swing test window. */
    @XmlElement(name="pixelScaleY")
    public float pixelScaleY = 0.01f;

    /** The note type */
    @XmlElement(name="noteType")
    public String noteType = StickyNoteTypes.GENERIC;
    
    /** The note text */
    @XmlElement(name="noteText")
    public String noteText = "";

    /** Summary */
    @XmlElement(name="noteName")
    public String noteName = "";

    /** Person working on it */
    @XmlElement(name="noteAssignee")
    public String noteAssignee = "";

    /** Due/Completion date */
    @XmlElement(name="noteDue")
    public String noteDue = "";

    /** ? */
    @XmlElement(name="noteStatus")
    public String noteStatus = "";

    @XmlElement(name="noteColor")
    public String color = "255:255:153";

    /** Default constructor */
    public StickyNoteCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.StickyNoteCellMO";
    }

    @XmlTransient public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    @XmlTransient public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    @XmlTransient public float getPixelScaleX() {
        return pixelScaleX;
    }

    public void setPixelScaleX(float pixelScale) {
        this.pixelScaleX = pixelScaleX;
    }

    @XmlTransient public float getPixelScaleY() {
        return pixelScaleY;
    }

    @Override public void setPixelScaleY(float pixelScale) {
        this.pixelScaleY = pixelScaleY;
    }

    @XmlTransient public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    @XmlTransient public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    @XmlTransient public String getNoteAssignee() {
        return noteAssignee;
    }

    public void setNoteAssignee(String noteAssignee) {
        this.noteAssignee = noteAssignee;
    }

    @XmlTransient public String getNoteDue() {
        return noteDue;
    }

    public void setNoteDue(String noteDue) {
        this.noteDue = noteDue;
    }

    @XmlTransient public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    @XmlTransient public String getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(String noteStatus) {
        this.noteStatus = noteStatus;
    }

    @XmlTransient public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }



    /**
     * Returns a string representation of this class.
     *
     * @return The server state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [StickynoteCellServerState]: " +
                "preferredWidth=" + preferredWidth + "," +
                "preferredHeight=" + preferredHeight + "," +
                "pixelScaleX=" + pixelScaleX + "," +
                "pixelScaleY=" + pixelScaleY + "," +
                "noteType=" + noteType + "," +
                "noteText=" + noteText;
    }
}
