/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "animation-component")
@ServerState
public class AnimationComponentServerState extends CellComponentServerState {
    
    /** Current animation state. */
    private Animation animation;
    
    /**
     * Creates new instance.
     */
    public AnimationComponentServerState(){
        animation = new Animation();
    }

    @Override
    public String getServerComponentClassName() {
        return "com.wonderbuilders.modules.animation.server.AnimationComponentMO";
    }

    /**
     * Set current animation.
     * 
     * @param animation animation state
     */
    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    /**
     * Get current animation.
     * 
     * @return animation
     */
    public Animation getAnimation() {
        return animation;
    }

}
