/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * properties image frame
 */
@ServerState
@XmlRootElement(name = "ImageFrameProperty")
public class ImageFrameProperties extends SharedData implements Serializable {

    private int aspectRatio;
    private int orientation;
    private int frameWidth;
    private int fit;
    private int frameHeight;
    private boolean isRemoveImage=false;
    
    @XmlElement
    public int getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(int aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    
    @XmlElement
    public int getFit() {
        return fit;
    }

    public void setFit(int fit) {
        this.fit = fit;
    }
    
    @XmlElement
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    
    @XmlElement
    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }
  
    @XmlElement
    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    @XmlElement
    public boolean getIsRemoveImage() {
        return isRemoveImage;
    }

    public void setIsRemoveImage(boolean isRemoveImage) {
        this.isRemoveImage = isRemoveImage;
    }
    
    
    
}
