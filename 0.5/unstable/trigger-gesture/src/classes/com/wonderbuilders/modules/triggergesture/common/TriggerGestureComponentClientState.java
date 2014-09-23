/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.common;

import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * client state for the trigger gesture component.
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureComponentClientState extends CellComponentClientState {
    
    
    private Trigger trigger = Trigger.LEFT_CLICK;
    private String gesture = "Answer Cell";
    private String contextMenuName = "Trigger Gesture";
    private int radius = 3;

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public String getGesture() {
        return gesture;
    }

    public void setGesture(String gesture) {
        this.gesture = gesture;
    }

    public String getContextMenuName() {
        return contextMenuName;
    }

    public void setContextMenuName(String contextMenuName) {
        this.contextMenuName = contextMenuName;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
    
}
