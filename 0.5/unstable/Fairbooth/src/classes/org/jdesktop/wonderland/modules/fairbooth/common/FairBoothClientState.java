/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.fairbooth.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;


/**
 *
 * @author Nilang
 */
public class FairBoothClientState extends CellClientState {

    private String boothName="Untitled Booth";
    private int colorTheme=0;
    private String infoText="Untitled";
    private int leftPanelFrames=1;
    private int rightPanelFrames=1;
    
    public FairBoothClientState() {
    }
    
    
    public String getBoothName() {
        return boothName;
    }

    public void setBoothName(String boothName) {
        this.boothName = boothName;
    }
    
    
    public int getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(int colorTheme) {
        this.colorTheme = colorTheme;
    }
    
    
    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }
    
    
    public int getLeftPanelFrames() {
        return leftPanelFrames;
    }

    public void setLeftPanelFrames(int leftPanelFrames) {
        this.leftPanelFrames = leftPanelFrames;
    }
    
    public int getRightPanelFrames() {
        return rightPanelFrames;
    }

    public void setRightPanelFrames(int rightPanelFrames) {
        this.rightPanelFrames = rightPanelFrames;
    }
    
}
