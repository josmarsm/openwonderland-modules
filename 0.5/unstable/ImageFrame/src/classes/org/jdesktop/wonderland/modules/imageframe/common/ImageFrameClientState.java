/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * client state of image frame
 */
public class ImageFrameClientState extends CellClientState {

    private int fit=-1;
    private int aspectRatio=-1;
    private int orientation=-1;
    private int frameWidth=-1;
    private int frameHeight=-1;
    private String imageURL=null;
    
    public ImageFrameClientState() {
    }
    public void setFit(int fit) {
        this.fit = fit;
    }
    public int getFit() {
        return fit;
    }

    public void setAspectRatio(int aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    public int getAspectRatio() {
        return aspectRatio;
    }
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    public int getOrientation() {
        return orientation;
    }
    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }
    public int getFrameWidth() {
        return frameWidth;
    }
    
    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }
    public int getFrameHeight() {
        return frameHeight;
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public String getImageURL() {
        return imageURL;
    }
}
