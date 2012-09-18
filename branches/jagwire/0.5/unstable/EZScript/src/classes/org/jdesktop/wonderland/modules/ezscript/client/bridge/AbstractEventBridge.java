/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bridge;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventObjectSPI;

/**
 *
 * @author JagWire
 */
public abstract class AbstractEventBridge implements EventBridgeSPI {
    protected ScriptEngine engine;
    protected Bindings bindings;
    
    public abstract String getBridgeName();
   
    public abstract EventObjectSPI[] getEventObjects();
    /**
     * Extend this in subclasses to register the event handler.
     * @param engine the script engine to send wonderland events to javascript land
     * @param bindings the bindings for the script engine
     */
    public void initialize(ScriptEngine engine, Bindings bindings) {
        this.engine = engine;
        this.bindings = bindings;
    }
    
   
}
