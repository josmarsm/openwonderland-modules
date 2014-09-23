/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.common;

import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * 
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureMessage extends CellMessage {
    
    private String contextMenuName = "Trigger Gesture";
    private int radius = 3;
    private Trigger trigger = Trigger.LEFT_CLICK;
    private String gesture = "Answer Cell";

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
    
}
