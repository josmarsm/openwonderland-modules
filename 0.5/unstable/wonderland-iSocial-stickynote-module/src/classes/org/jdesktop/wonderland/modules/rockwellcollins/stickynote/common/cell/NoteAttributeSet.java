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
import java.io.Serializable;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
/**
 *
 * @author xiuzhen
 */
public class NoteAttributeSet implements Serializable{

    //private AttributeSet singleNoteAttributeSet;
    private boolean isItalic,isBold,isUnderline;
    private int fontSize;


    public NoteAttributeSet()
    {

        //singleNoteAttributeSet=(AttributeSet) new SimpleAttributeSet();
        isItalic=false;
        isBold=false;
        isUnderline=false;
        fontSize=0;
    }
    public NoteAttributeSet(boolean isItalic,boolean isBold,boolean isUnderline,int fontSize)
    {
        this.isBold=isBold;
        this.isItalic=isItalic;
        this.isUnderline=isUnderline;
        this.fontSize=fontSize;
    }
    public int getFontSize()
    {
        return fontSize;
    }

    public void setFontSize(int fontSize)
    {
        this.fontSize=fontSize;
    }

    public boolean getBold()
    {
        return isBold;
    }
    public void setBold(boolean isBold)
    {

        this.isBold=isBold;
    }

    public boolean getItalic()
    {
        return isItalic;
    }
    public void setItalic(boolean isItalic)
    {

        this.isItalic=isItalic;
    }


    public boolean getUnderline()
    {
        return isUnderline;
    }
    public void setUnderline(boolean isUnderline)
    {

        this.isUnderline=isUnderline;
    }
}
