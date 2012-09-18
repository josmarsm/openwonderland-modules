/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import javax.script.Bindings;
import javax.script.ScriptEngine;

/**
 *
 * @author JagWire
 */
public interface IBindingsLoader {
    public void loadBindings();
    
    public ScriptEngine getEngine();
    
    public Bindings getBindings();
}
