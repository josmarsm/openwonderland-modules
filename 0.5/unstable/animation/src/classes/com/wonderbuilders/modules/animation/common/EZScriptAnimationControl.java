/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Implementation of {@link AnimationControl} for handling EZScript animations.
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "ezscript-control")
public class EZScriptAnimationControl implements Serializable {
    
    /** EZScript function declaration. */
    private String function;
    
    /** Command name as it will appear on menu. */
    private String command;

    /**
     * Return command name.
     * 
     * @return command name
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set command name.
     * 
     * @param command command name
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Set function declaration. This should include function name and
     * arguments. For example myfunction(x,y).
     * 
     * @param function function
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     * Returns EZScript function.
     * 
     * @return function
     */
    public String getFunction() {
        return function;
    }

}
