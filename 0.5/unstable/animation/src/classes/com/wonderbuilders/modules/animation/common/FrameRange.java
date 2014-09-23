/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a frame range inside animation
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "frame-range")
public class FrameRange implements Comparable<FrameRange>, Serializable {
    
    /** Starting frame index. */
    private int start;
    
    /** End frame index. */
    private int end;
    
    /** Command string. */
    private String command;
    
    /** Maximum frame number allowed. */
    private int maxFrame;

    /**
     * Set start frame index.
     * 
     * @param start index
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns start frame index.
     * 
     * @return index
     */
    public int getStart() {
        return start;
    }

    /**
     * Set end frame index.
     * 
     * @param end index
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Returns end frame index.
     * 
     * @return index
     */
    public int getEnd() {
        return end;
    }

    /**
     * Set command for this frame range.
     * 
     * @param command command name
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Returns command for this frame range.
     * 
     * @return command name
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set maximum allowed frame number.
     * 
     * @param maxFrame  frame number
     */
    public void setMaxFrame(int maxFrame) {
        this.maxFrame = maxFrame;
    }

    /**
     * Returns maximum allowed frame number.
     * 
     * @return frame number
     */
    public int getMaxFrame() {
        return maxFrame;
    }
    
    
    @Override
    public int compareTo(FrameRange o) {
        int status = 0;
        if(o.getStart() > start && o.getEnd() > end){
            status = -1;
        } else if(start > o.getStart() && end > o.getEnd()){
            status = 1;
        }
        return status;
    }

}
