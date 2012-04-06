/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.generators.javascript;

import javax.script.ScriptException;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratorSPI;

/**
 *
 * @author JagWire
 */
public class BridgeGenerator implements GeneratorSPI {
    private EventBridgeSPI bridge;

    public BridgeGenerator() {
        
    }
    
    public void setActiveBridge(EventBridgeSPI bridge) {
        this.bridge = bridge;
    }
    
//    public String generateScriptBinding() {
//        return generateScriptBinding(bridge);
//    }
    
    public String generateScriptBinding() {
        String bridgeScript = ""
                + "var " + bridge.getBridgeName() + " = ({\n" //create the object given the name from the bridge
                + "     fs: new Array(),\n" //create an array of functions to be called for an event
                + "     enabled: new Boolean(),\n" //create a boolean to enable/disable the bridge
                + "     setEnabled: function(b) { this.enabled = b; },\n" //set whether or not the bridge is enabled
                + "     add: function(f) { this.fs.push(f); },\n" //create a method for the object to add a function to the array
                + "     event: function(e) {\n" //create an event function to call each function in the array
                + "         if(this.enabled) {\n"
                + "             for(var i in this.fs) {\n" //for every function...
                + "                 this.fs[i](e);\n" //pass the 'e' argument through
                + "             }\n"
                + "         }\n"
                + "     },\n";

        //add other event names...
        for (String name : bridge.getEventNames()) { //for every event name in the bridge...
            bridgeScript += buildEventlet(name); //
        }
        bridgeScript +=
                "     jagwire: \"isawesome\"\n" //easter egg :D, actually, I put this here so that we can add commas to the end of each event function definition. (see below);
                + "});"; //end the object definition. Now we can use the bridge in javascript!

        return bridgeScript;

    }

    private String buildEventlet(String name) {
        String stufflet = ""
                + "" + name + ": function(e) {\n"
                + "     if(this.enabled) {\n"
                + "         for(var i in this.fs) {\n"
                + "             this.fs[i](e);\n"
                + "         }\n"
                + "     }\n"
                + "},\n"; //the comma here is what I'm talking about up there ^
        return stufflet;
    }
    
}
