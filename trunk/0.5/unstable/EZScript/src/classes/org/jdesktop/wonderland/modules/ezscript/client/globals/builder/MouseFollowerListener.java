/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder;

import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;

/**
 *
 * @author Ryan
 */
public abstract class MouseFollowerListener extends EventClassListener {

    public abstract void mouseFollowerClicked(MouseButtonEvent3D event);

    public abstract void mouseFollowerMoved(MouseMovedEvent3D mme3d);
    private static final Logger logger = Logger.getLogger(MouseFollowerListener.class.getName());

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{MouseButtonEvent3D.class, MouseMovedEvent3D.class};
    }

    @Override
    public void commitEvent(Event event) {
        if (event instanceof MouseButtonEvent3D) {
            MouseButtonEvent3D mbe3d = (MouseButtonEvent3D) event;

            if (mbe3d.isClicked()) {
                mouseFollowerClicked(mbe3d);
            }


        } else if (event instanceof MouseMovedEvent3D) {
            MouseMovedEvent3D mme3d = (MouseMovedEvent3D) event;

            mouseFollowerMoved(mme3d);
        } else {
            throw new UnsupportedOperationException("EVENT NOT A BUTTON CLICK OR NOR A MOVED EVENT");
        }
    }
}
