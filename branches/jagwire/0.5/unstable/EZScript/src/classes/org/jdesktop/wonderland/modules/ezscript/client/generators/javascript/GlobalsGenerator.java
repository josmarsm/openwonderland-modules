/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.generators.javascript;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratorSPI;

/**
 *
 * @author Ryan
 */
public class GlobalsGenerator implements GeneratorSPI {
    private final ScriptEngine engine;
    private final Bindings bindings;
    private GlobalSPI global;
    
    public GlobalsGenerator(ScriptEngine engine, Bindings bindings) {
        this.engine = engine;
        this.bindings = bindings;
    }
    
    public void setActiveGlobal(GlobalSPI global) {
        this.global = global;
    }
    
    public String generateScriptBinding() {
        bindings.put(global.getName(), global);
        
        return "";
    }
}
