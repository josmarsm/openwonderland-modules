package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;


@CellComponentFactory
public class ScriptingComponentFactory implements CellComponentFactorySPI 
    {
    private final int totalEvents = 30;
    private String[] eventNames = new String[totalEvents];
    private String[] eventScriptType = new String[totalEvents];
    private Boolean[] eventResource = new Boolean[totalEvents];
    private String cellOwner;
    private boolean useGlobalScripts;
    public static final int MOUSE1_EVENT = 0;
    public static final int MOUSE2_EVENT = 1;
    public static final int MOUSE3_EVENT = 2;
    public static final int MOUSE1S_EVENT = 3;
    public static final int MOUSE2S_EVENT = 4;
    public static final int MOUSE3S_EVENT = 5;
    public static final int MOUSE1C_EVENT = 6;
    public static final int MOUSE2C_EVENT = 7;
    public static final int MOUSE3C_EVENT = 8;
    public static final int MOUSE1A_EVENT = 9;
    public static final int MOUSE2A_EVENT = 10;
    public static final int MOUSE3A_EVENT = 11;
    
    public static final int TIMER_EVENT = 12;
    public static final int STARTUP_EVENT = 13;
    public static final int PROXIMITY_EVENT = 14;
    
    public static final int MESSAGE1_EVENT = 15;
    public static final int MESSAGE2_EVENT = 16;
    public static final int MESSAGE3_EVENT = 17;
    public static final int MESSAGE4_EVENT = 18;

    public static final int INTERCELL_EVENT = 19;
    public static final int CHAT_EVENT = 20;
    public static final int PRESENCE_EVENT = 21;
    public static final int CONTROLLER_EVENT = 22;
    public static final int PROPERTIES_EVENT = 23;
    public static final int AVATAR_EVENT = 24;

    public String getDisplayName() 
        {
        System.out.println("ScriptingComponentFactory : In getDisplayName");
        return "Scripting Component";
        }

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() 
        {
        System.out.println("ScriptingComponentFactory : In getDefaultCellComponentService");
        ScriptingComponentServerState state = new ScriptingComponentServerState();
        state.setInfo("Default");
        eventNames[MOUSE1_EVENT] = "mouse1.js";
        eventNames[MOUSE2_EVENT] = "mouse2.js";
        eventNames[MOUSE3_EVENT] = "mouse3.js";
        eventNames[MOUSE1S_EVENT] = "mouse1s.js";
        eventNames[MOUSE2S_EVENT] = "mouse2s.js";
        eventNames[MOUSE3S_EVENT] = "mouse3s.java";
        eventNames[MOUSE1C_EVENT] = "mouse1c.js";
        eventNames[MOUSE2C_EVENT] = "mouse2c.js";
        eventNames[MOUSE3C_EVENT] = "mouse3c.java";
        eventNames[MOUSE1A_EVENT] = "mouse1a.js";
        eventNames[MOUSE2A_EVENT] = "mouse2a.js";
        eventNames[MOUSE3A_EVENT] = "mouse3a.js";
        eventNames[TIMER_EVENT] = "timer.js";
        eventNames[STARTUP_EVENT] = "startup.js";
        eventNames[PROXIMITY_EVENT] = "prox.js";
        eventNames[MESSAGE1_EVENT] = "message1.js";
        eventNames[MESSAGE2_EVENT] = "message2.js";
        eventNames[MESSAGE3_EVENT] = "message3.js";
        eventNames[MESSAGE4_EVENT] = "message4.js";
        eventNames[INTERCELL_EVENT] = "ice.js";
        eventNames[CHAT_EVENT] = "chat.js";
        eventNames[PRESENCE_EVENT] = "presence.js";
        eventNames[CONTROLLER_EVENT] = "controller.js";
        eventNames[PROPERTIES_EVENT] = "properties.java";
        eventNames[AVATAR_EVENT] = "avatar.js";

        eventScriptType[MOUSE1_EVENT] = "javascript";
        eventScriptType[MOUSE2_EVENT] = "javascript";
        eventScriptType[MOUSE3_EVENT] = "javascript";
        eventScriptType[MOUSE1S_EVENT] = "javascript";
        eventScriptType[MOUSE2S_EVENT] = "javascript";
        eventScriptType[MOUSE3S_EVENT] = "javascript";
        eventScriptType[MOUSE1C_EVENT] = "javascript";
        eventScriptType[MOUSE2C_EVENT] = "javascript";
        eventScriptType[MOUSE3C_EVENT] = "javascript";
        eventScriptType[MOUSE1A_EVENT] = "javascript";
        eventScriptType[MOUSE2A_EVENT] = "javascript";
        eventScriptType[MOUSE3A_EVENT] = "javascript";
        eventScriptType[TIMER_EVENT] = "javascript";
        eventScriptType[STARTUP_EVENT] = "javascript";
        eventScriptType[PROXIMITY_EVENT] = "javascript";
        eventScriptType[MESSAGE1_EVENT] = "javascript";
        eventScriptType[MESSAGE2_EVENT] = "javascript";
        eventScriptType[MESSAGE3_EVENT] = "javascript";
        eventScriptType[MESSAGE4_EVENT] = "javascript";
        eventScriptType[INTERCELL_EVENT] = "javascript";
        eventScriptType[CHAT_EVENT] = "javascript";
        eventScriptType[PRESENCE_EVENT] = "javascript";
        eventScriptType[CONTROLLER_EVENT] = "javascript";
        eventScriptType[PROPERTIES_EVENT] = "java";
        eventScriptType[AVATAR_EVENT] = "javascript";

        eventResource[MOUSE1_EVENT] = false;
        eventResource[MOUSE2_EVENT] = false;
        eventResource[MOUSE3_EVENT] = false;
        eventResource[MOUSE1S_EVENT] = false;
        eventResource[MOUSE2S_EVENT] = false;
        eventResource[MOUSE3S_EVENT] = false;
        eventResource[MOUSE1C_EVENT] = false;
        eventResource[MOUSE2C_EVENT] = false;
        eventResource[MOUSE3C_EVENT] = false;
        eventResource[MOUSE1A_EVENT] = false;
        eventResource[MOUSE2A_EVENT] = false;
        eventResource[MOUSE3A_EVENT] = false;
        eventResource[TIMER_EVENT] = false;
        eventResource[STARTUP_EVENT] = false;
        eventResource[PROXIMITY_EVENT] = false;
        eventResource[MESSAGE1_EVENT] = false;
        eventResource[MESSAGE2_EVENT] = false;
        eventResource[MESSAGE3_EVENT] = false;
        eventResource[MESSAGE4_EVENT] = false;
        eventResource[INTERCELL_EVENT] = false;
        eventResource[CHAT_EVENT] = false;
        eventResource[PRESENCE_EVENT] = false;
        eventResource[CONTROLLER_EVENT] = false;
        eventResource[PROPERTIES_EVENT] = true;
        eventResource[AVATAR_EVENT] = false;

        cellOwner = "";
        useGlobalScripts = true;

        state.setEventNames(eventNames);
        state.setScriptType(eventScriptType);
        state.setEventResource(eventResource);
        state.setCellOwner(cellOwner);
        state.setUseGlobalScripts(useGlobalScripts);
        return (T)state;
        }

    public String getDescription() 
        {
        System.out.println("ScriptingComponentFactory : In getDescription");
        return "The Scripting Component";
        }
    }
