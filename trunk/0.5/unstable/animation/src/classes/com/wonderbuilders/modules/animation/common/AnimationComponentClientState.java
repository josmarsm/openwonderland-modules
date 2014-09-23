/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Vladimir Djurovic
 */
public class AnimationComponentClientState extends CellComponentClientState {

    /** Current animation state. */
    private Animation animation;

    /**
     * Set current animation state.
     * 
     * @param animation  animation to set
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
