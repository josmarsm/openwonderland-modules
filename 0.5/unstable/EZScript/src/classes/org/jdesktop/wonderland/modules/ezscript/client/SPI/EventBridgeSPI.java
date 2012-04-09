
package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.jdesktop.wonderland.client.input.Event;

/**
 *  For every event name returned by getEventNames(), there should be a
 * corresponding method implemented to fulfill. See ExampleEventBridge for details.
 * @author JagWire
 */
public interface EventBridgeSPI {
    public String getBridgeName();
   
    /*
     * Returns a list of event names for the dispatcher to send. There should
     */
    public EventObjectSPI[] getEventObjects();
    public void initialize(ScriptEngine engine, Bindings bs);
    
}
