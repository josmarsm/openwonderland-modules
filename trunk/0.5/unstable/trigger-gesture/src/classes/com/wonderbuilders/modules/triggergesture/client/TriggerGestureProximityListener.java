/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.client;

import com.jme.bounding.BoundingVolume;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Listener for proximity enter/exit event of avatar
 * The avatar animation will be played if the trigger is set to in range
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureProximityListener implements ProximityListener {

    private static final Logger logger = Logger.getLogger(TriggerGestureProximityListener.class.getName());
    private ScheduledExecutorService ser = null;
    
    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        if(entered) {
            ser = Executors.newSingleThreadScheduledExecutor();
            ser.scheduleAtFixedRate(new AvatarMovementExaminer(cell), 0, 500, TimeUnit.MILLISECONDS);
        } else {
            if(ser!=null) {
                ser.shutdown();
                ser = null;
            }
            logger.log(Level.WARNING, "proximity listener called for exit trigger...{0}", cell);
        }
    }
    
    private void playAnimation(Cell cell) {
        TriggerGestureComponent cellComp = cell.getComponent(TriggerGestureComponent.class);
        logger.log(Level.WARNING, "proximity listener called for enter trigger...{0}", cell);
        if(cellComp!=null && cellComp.getTrigger().equals(TriggerGestureComponentServerState.Trigger.IN_RANGE)) {
            logger.log(Level.WARNING, "triggering gesture...{0}", cell);
            cellComp.initializeGestures();
            cellComp.triggerGesture();
        }
    }
    
    private class AvatarMovementExaminer implements Runnable {
        
        private CellTransform prevPosition = null;
        private CellTransform currPosition = null;
        private Cell cell = null;
        
        public AvatarMovementExaminer(Cell cell) {
            this.cell = cell;
        }
        
        public void run() {
            ViewCell avatar = ClientContextJME.getViewManager().getPrimaryViewCell();
            currPosition = avatar.getWorldTransform();
            if(prevPosition!=null && prevPosition.equals(currPosition)) {
                logger.log(Level.WARNING, "avatar stop moving...");
                playAnimation(cell);
                ser.shutdown();
                ser = null;
            } else {
                prevPosition = currPosition;
            }
        }
    }
     
}
