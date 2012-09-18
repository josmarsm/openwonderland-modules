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

/**
 *
 * @author xiuzhen
 */



public class SelectedTextStyle implements Serializable {
    private int selectedStartPos;
    private int selectedEndPos;
    /*private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private int fontSize;
     * */

    private String buttonName;

    public SelectedTextStyle()
    {
        selectedStartPos=0;
        selectedEndPos=0;
        /*isBold=false;
        isItalic=false;
        isUnderline=false;
        fontSize=16;
         * */

        buttonName="";

    }
    public String getButtonName()
    {
        return buttonName;
    }
    public void setButtonName(String buttonName)
    {
        this.buttonName=buttonName;
    }
    public int getSelectedStartPos() {
        return selectedStartPos;
    }

    public void setSelectedStartPos(int startPos) {
        this.selectedStartPos = startPos;
    }

    public int getSelectedEndPos() {
        return selectedEndPos;
    }

    public void setSelectedEndPos(int endPos) {
        this.selectedEndPos = endPos;
    }

    /*
    public int getSelectedFontSize() {
        return fontSize;
    }

    public void setSelectedFontSize(int newFontSize) {
        this.selectedEndPos = newFontSize;
    }


     public boolean getBold() {
        return isBold;
    }

    public void setBold(boolean isBold) {
        this.isBold = isBold;
    }


     public boolean getItalic() {
        return isItalic;
    }

    public void setItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }

     public boolean getUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }
     * */


}
