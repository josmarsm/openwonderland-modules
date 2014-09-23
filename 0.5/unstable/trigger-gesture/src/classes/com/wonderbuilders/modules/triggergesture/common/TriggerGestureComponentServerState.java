/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * server state for the trigger gesture component.
 * 
 * @author Abhishek Upadhyay.
 */
@XmlRootElement(name="trigger-gesture-component")
@ServerState
public class TriggerGestureComponentServerState extends CellComponentServerState implements Serializable {

    
    public enum Trigger {
        LEFT_CLICK,
        RIGHT_CLICK,
	IN_RANGE
    }
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
    
    @Override
    public String getServerComponentClassName() {
        return "com.wonderbuilders.modules.triggergesture.server.TriggerGestureComponentMO";
    }
    
}
