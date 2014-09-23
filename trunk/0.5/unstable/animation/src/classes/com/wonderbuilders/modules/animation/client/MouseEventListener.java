/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.client;

import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;

/**
 * Event listener for mouse left click.
 *
 * @author Vladimir Djurovic
 */
public class MouseEventListener extends EventClassListener {

    /**
     * Component that uses this listener.
     */
    private AnimationComponent animationComp;

    /**
     * Creates new instance.
     *
     * @param comp animation component
     */
    public MouseEventListener(AnimationComponent comp) {
        this.animationComp = comp;
    }

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{MouseButtonEvent3D.class};
    }

    @Override
    public void commitEvent(Event event) {
        MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
        if (mbe.isClicked() && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
            animationComp.toggleAnimation();
            animationComp.sendUpdateMessage("toggleAnimation", null);

        }
    }
}
