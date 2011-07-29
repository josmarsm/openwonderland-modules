/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class MovePickMethod implements ReturnableScriptMethodSPI {

private MouseMovePickEventListener listener;
    private Vector3f pickPosition;
    private Semaphore lock = new Semaphore(0);
    private static final Logger logger = Logger.getLogger(ClickPickMethod.class.getName());
    public String getDescription() {
        return "Returns the position (Vector3f) of the object the mouse clicked on.\n" +
                "-- Usage: var vec3 = MovePick();";
    }

    public String getFunctionName() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "MovePick";
    }

    public String getCategory() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "utilities";
    }

    public void setArguments(Object[] args) {
        //no arguments
        //listener = new MouseClickPickEventListener();
        listener = new MouseMovePickEventListener();
    }

    public Object returns() {
        return pickPosition;
    }

    public void run() {        
        InputManager.inputManager().addGlobalEventListener(listener);

        try {
            lock.acquire();
            InputManager.inputManager().removeGlobalEventListener(listener);
            listener = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    class MouseClickPickEventListener extends EventClassFocusListener {
//        @Override
//        public Class[] eventClassesToConsume () {
//            return new Class[] { MouseEvent3D.class };
//        }
//
//        @Override
//        public void commitEvent (Event event) {
//            if(event instanceof MouseButtonEvent3D) {
//              MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
//                if(mbe.isClicked()) {
//
//                    if(mbe.getPickDetails() == null) {
//                        lock.release();
//                        logger.warning("Pick(): received null pick details!");
//                        return;
//                    }
//
//                    pickPosition = mbe.getPickDetails().getPosition();
//                                       
//                    logger.warning("Pick(): received pickPosition "+pickPosition);
//                    lock.release();
//                }
//            }
//        }
//    }

    class MouseMovePickEventListener extends EventClassFocusListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseMovedEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {

            MouseMovedEvent3D m = (MouseMovedEvent3D)event;
            
            if(m.getPickDetails() == null) {
                lock.release();
                logger.warning("Pick(): received null pick details!");
                return;
            }
            
            pickPosition = m.getPickDetails().getPosition();
            logger.warning("Pick(): received pickPosition "+pickPosition);
            lock.release();
        }
    }

    
}
