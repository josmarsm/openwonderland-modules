/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.ezscript.client.animation;

//import com.wonderbuilders.modules.animation.client.AnimationComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 * This method will play frame range of Collada animation in model. Frame range is identified
 * by name assigned to it in Animation capability.
 *
 * @author Vladimir Djurovic
 */
@ScriptMethod
public class PlayFrameRange implements ScriptMethodSPI {
      
    /**
     * Function name as used in EZScript.
     */
    private static final String FUNCTION_NAME = "playFrameRange";
    
    /**
     * Object cell.
     */
    private Cell cell;
    
    /**
     * Name of the frame range to play.
     */
    private String frameRangeName;

    /**
     * Returns function name as used in EZScript.
     * 
     * @return function name
     */
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    /**
     * Set arguments for function. Two arguments are required: cell and name
     * of the frame range.
     * 
     * @param args function arguments
     */
    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        frameRangeName = (String)args[1];
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder("Usage: playFrameRange(cell,ranegName)\n");
        sb.append("- plays frame range defined with Animation capability");
        return sb.toString();
    }

    public String getCategory() {
        return "Object Movement";
    }

    /**
     * Execute function within script context.
     */
    public void run() {
//        AnimationComponent animationComp = cell.getComponent(AnimationComponent.class);
//        if(animationComp == null){
//            throw new IllegalStateException("Animation capability not present. Please add Animation capability to cell.");
//        }
//        animationComp.executeAnimationCommand(frameRangeName);
    }

}
