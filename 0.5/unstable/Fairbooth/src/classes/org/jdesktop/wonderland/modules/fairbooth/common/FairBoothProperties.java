/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.fairbooth.common;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author Nilang
 */
@ServerState
@XmlRootElement(name = "FairBoothConfig")
public class FairBoothProperties extends SharedData implements Serializable {

    private String infoText;
    private String boothName;
    private int colorTheme;
    private int leftPanelFrames;
    private int rightPanelFrames;
    private List<String> rightAppFrameList;
    private List<String> leftAppFrameList;
    
    @XmlElement
    public String getBoothName() {
        return boothName;
    }

    public void setBoothName(String boothName) {
        this.boothName = boothName;
    }
    
    @XmlElement
    public List getRightAppFrameList() {
        return rightAppFrameList;
    }

    public void setRightAppFrameList(List rightAppFrameList) {
        this.rightAppFrameList = rightAppFrameList;
    }
    
   @XmlElement
    public List getLeftAppFrameList() {
        return leftAppFrameList;
    }

    public void setLeftAppFrameList(List leftAppFrameList) {
        this.leftAppFrameList = leftAppFrameList;
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
    
    
    private boolean boothNameFrame;
    @XmlElement
    public boolean getBoothNameFrame() {
        return boothNameFrame;
    }

    public void setBoothNameFrame(boolean boothNameFrame) {
        this.boothNameFrame = boothNameFrame;
    }
    
}
