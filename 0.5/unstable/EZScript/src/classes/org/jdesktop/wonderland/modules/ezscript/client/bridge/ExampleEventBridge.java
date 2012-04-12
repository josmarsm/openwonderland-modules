/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bridge;

//import java.awt.Event;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventObjectSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.EventBridge;

/**
 * Example event bridge for mouse presses (not to be confused with mouse clicks
 * or mouse releases). 
 * @author JagWire
 */
//@EventBridge
public class ExampleEventBridge extends AbstractEventBridge {

    private static final Logger logger = Logger.getLogger(ExampleEventBridge.class.getName());
    
    public String getBridgeName() {
        return "MouseDispatcher";
    }

    @Override
    public void initialize(ScriptEngine engine, Bindings bindings) {
        super.initialize(engine, bindings);
    
        logger.warning("Adding example event dispatcher: "+getBridgeName());
        InputManager.inputManager().addGlobalEventListener(new MyMouseListener());
    }
    
    public EventObjectSPI[] getEventObjects() {
//        return new String[] { "mousePressed"};
        
        return new EventObjectSPI[] { new EventObjectSPI() {

            public String getEventName() {
                return "mousePressed";
            }

            public int getNumberOfArguments() {
                return 1;
            }
        
        }};
    }
    
    public void mousePressed(Event event) {
        try {
            //        throw new UnsupportedOperationException("Not supported yet.");
            long time = System.currentTimeMillis();
            String id = "event" + time;
            bindings.put(id, event);
            String script = getBridgeName() + ".mousePressed(" + id + ");";
            engine.eval(script, bindings);
        } catch (ScriptException ex) {
            Logger.getLogger(ExampleEventBridge.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    class MyMouseListener extends EventClassListener {
        public MyMouseListener() {
            super();
//            this.commitEvent(null);
        }
        
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class};
        }
        
        @Override
        public void commitEvent(Event e) {
            if(((MouseButtonEvent3D)e).isPressed()) {
                mousePressed(e);
                logger.warning("Mouse Pressed!");
            }
        }
        
    }
    
}
