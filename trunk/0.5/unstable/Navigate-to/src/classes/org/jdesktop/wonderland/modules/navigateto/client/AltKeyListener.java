/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.navigateto.client;

import java.awt.event.KeyEvent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;

/**
 *
 */
class AltKeyListener extends EventClassListener {

    NavigateToComponent ntc = null;
    
    public AltKeyListener(NavigateToComponent ntc) {
        this.ntc = ntc;
    }
    
    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{KeyEvent3D.class};
    }

    @Override
    public void commitEvent(Event event) {
        if (event instanceof KeyEvent3D) {
            KeyEvent3D e = (KeyEvent3D) event;
            if (e.getKeyCode() == KeyEvent.VK_ALT && e.isPressed()) {
                ntc.setIsAltPressed(true);
            } else if (e.getKeyCode() == KeyEvent.VK_ALT && e.isReleased()) {
                ntc.setIsAltPressed(false);
            }
        }
    }
}
