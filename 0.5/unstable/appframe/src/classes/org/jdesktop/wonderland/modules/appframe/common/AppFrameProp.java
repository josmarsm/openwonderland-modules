/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author nilang
 */
@ServerState
@XmlRootElement(name = "AppFrameProp")
public class AppFrameProp extends SharedData implements Serializable {

    private String orientation;
    private String aspectRatio;
    private String maxHistory;
    private String borderColor;
    public AppFrameProp() {
   
    }
    public AppFrameProp(String orientation, String aspectRatio, String maxHistory,String borderColor) {
        this.aspectRatio=aspectRatio;
        this.maxHistory=maxHistory;
        this.orientation=orientation;
        this.borderColor = borderColor; 
    }
    
      @XmlElement
    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
      @XmlElement
    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
     @XmlElement
    public String getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(String maxHistory) {
        this.maxHistory = maxHistory;
    }
    @XmlElement
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
}