/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.client;

import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;

/**
 * Listener for the menu item in context menu
 * The avatar animation will be played if the trigger is set to right click
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureContextMenuListener implements ContextMenuActionListener {

    private static final Logger logger = Logger.getLogger(TriggerGestureContextMenuListener.class.getName());
    
    public void actionPerformed(ContextMenuItemEvent event) {
        TriggerGestureComponent cellComp = event.getCell().getComponent(TriggerGestureComponent.class);
        logger.log(Level.WARNING, "context menu item listener called...{0}", event.getCell());
        if(cellComp!=null && cellComp.getTrigger().equals(Trigger.RIGHT_CLICK)) {
            logger.log(Level.WARNING, "triggering gesture...{0}", event.getCell());
            cellComp.initializeGestures();
            cellComp.triggerGesture();
        }
    }
    
}
