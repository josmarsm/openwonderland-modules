/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.client;

import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.scenemanager.event.SceneEvent;

/**
 * Listener for the mouse left click event
 * The avatar animation will be played if trigger is set to left click
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureMouseListener extends EventClassListener {

    private static final Logger logger = Logger.getLogger(TriggerGestureMouseListener.class.getName());
    
    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{MouseButtonEvent3D.class};
    }

    @Override
    public void commitEvent(org.jdesktop.wonderland.client.input.Event event) {
        MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
        if(mbe.isClicked() == true && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
            Entity e = mbe.getEntity();
            Cell cell = SceneEvent.getCellForEntity(e);
            TriggerGestureComponent cellComp = cell.getComponent(TriggerGestureComponent.class);
            logger.log(Level.WARNING, "mouse listener called for left click trigger...{0}", cell);
            if(cellComp!=null && cellComp.getTrigger().equals(Trigger.LEFT_CLICK)) {
                logger.log(Level.WARNING, "triggering gesture...{0}", cell);
                cellComp.initializeGestures();
                cellComp.triggerGesture();
            }
        }
    }
    
}
