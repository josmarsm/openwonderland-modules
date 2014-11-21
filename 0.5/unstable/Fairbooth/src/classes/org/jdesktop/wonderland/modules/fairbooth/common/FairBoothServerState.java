/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.fairbooth.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;


/**
 *
 * @author Nilang
 */
@XmlRootElement(name="fairbooth-cell")
@ServerState
public class FairBoothServerState extends CellServerState {

    private String boothName="Untitled Booth";
    private int colorTheme=0;
    private String infoText="Untitled";
    private int leftPanelFrames=1;
    private int rightPanelFrames=1;
    
    private boolean boothNameAdded=false;
    
    public FairBoothServerState() {
    }
    
    @XmlElement
    public boolean getBoothNameAdded() {
        return boothNameAdded;
    }

    public void setBoothNameAdded(boolean boothNameAdded) {
        this.boothNameAdded = boothNameAdded;
    }
    
    @XmlElement
    public String getBoothName() {
        return boothName;
    }

    public void setBoothName(String boothName) {
        this.boothName = boothName;
    }
    
    @XmlElement
    public int getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(int colorTheme) {
        this.colorTheme = colorTheme;
    }
    
    @XmlElement
    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }
    
    @XmlElement
    public int getLeftPanelFrames() {
        return leftPanelFrames;
    }

    public void setLeftPanelFrames(int leftPanelFrames) {
        this.leftPanelFrames = leftPanelFrames;
    }
    @XmlElement
    public int getRightPanelFrames() {
        return rightPanelFrames;
    }

    public void setRightPanelFrames(int rightPanelFrames) {
        this.rightPanelFrames = rightPanelFrames;
    }
    
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.fairbooth.server.FairBoothCellMO";
    }
}
