/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.scriptingComponent.client;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentClientState;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentChangeMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentICEMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentTransformMessage;

/**
 *
 * A Component that provides scripting interface 
 * 
 * @author morrisford
 */
@ExperimentalAPI
public class ScriptingComponent extends CellComponent
    {
    private Node localNode;
    private String scriptClump = "default";
    private String scriptURL = "http://localhost:8800/test/compiled_models";
    public String stateString[] = {null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
    public int stateInt[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public boolean stateBoolean[] = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    public float stateFloat[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    Map<String, CompiledScript> scriptMap = new HashMap<String, CompiledScript>();
//    myThread mth = new myThread();
    private ArrayList aniList;
    private int aniFrame = 0;
    private int aniLast = 0;
    private CellTransform atRest;
    private int animation = 0;
    public String testName = "morrisford";
    public int testInt = 99;
    private Vector3f worldCoor = null;
    private float frameRate = (float)0.0;
    public final int totalEvents = 30;
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

    public static final int YES_NOTIFY = 0;
    public static final int NO_NOTIFY = 1;
    private String[] eventNames;
    private String[] eventScriptType;
    
    private WorldManager wm = null;
    private String info = null;
    private Vector watchMessages = new Vector();
    private SocketInterface sif = null;
    
    private int iAmICEReflector = 0;
    
    @UsesCellComponent
    protected ChannelComponent channelComp;
    
    protected ChannelComponent.ComponentMessageReceiver msgReceiver=null;
    
    public ScriptingComponent(Cell cell) 
	{
        super(cell);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Enter ScriptingComponent constructor");
        wm = ClientContextJME.getWorldManager();
        wm.getRenderManager().setFrameRateListener(new FrameRateListener()
            {
            public void currentFramerate(float frames) 
                {
                frameRate = frames;
                }
            
            }, 100);

            // get a named map
        }

    public String getInfo()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In getInfo - info = " + this.info);
        return this.info;
        }
    
    public String getCellName()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In getCellName - cellName = " + this.scriptClump);
        return this.scriptClump;
        }
    
@Override
    public void setClientState(CellComponentClientState clientState) 
        {
        super.setClientState(clientState);
        info = ((ScriptingComponentClientState)clientState).getInfo();
        scriptClump = ((ScriptingComponentClientState)clientState).getCellName();
        scriptURL = ((ScriptingComponentClientState)clientState).getScriptURL();
        eventNames = ((ScriptingComponentClientState)clientState).getEventNames();
        eventScriptType = ((ScriptingComponentClientState)clientState).getScriptType();
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setClientState - info = " + info + " cellName (scriptClump) = " + scriptClump + " scriptURL = " + scriptURL);
        }

    @Override
    public void setStatus(CellStatus status)
        {
        switch(status)
            {
            case ACTIVE:
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = ACTIVE");
/* Register the change message listener */
                if (msgReceiver == null) 
                    {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() 
                        {
                        public void messageReceived(CellMessage message) 
                            {
                            if(message instanceof ScriptingComponentChangeMessage)
                                {
                                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - Received change message - Message id = " + message.getCellID());
                                if(cell.getCellID().equals(message.getCellID()))
                                    {
                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
                                    ScriptingComponentChangeMessage scm = (ScriptingComponentChangeMessage)message;
                                    scriptClump = scm.getCellName();
                                    scriptURL = scm.getScriptURL();
                                    eventNames = scm.getEventNames();
                                    eventScriptType = scm.getScriptType();
                                    }
                                else
                                    {
                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is new message - Ignore it");
                                    }
                                }
                            else if(message instanceof ScriptingComponentICEMessage)
                                {
                                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - Received ICE message - Message id = " + message.getCellID());
                                if(cell.getCellID().equals(message.getCellID()))
                                    {
                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
                                    ScriptingComponentICEMessage ice = (ScriptingComponentICEMessage)message;
                                    if(iAmICEReflector == 1)
                                        {
                                        postMessageEvent(ice.getPayload(), ice.getIceCode());
                                        }
                                    if(watchMessages.contains(new Float(ice.getIceCode())))
                                        {
                                        stateInt[19] = ice.getIceCode();
                                        stateString[19] = ice.getPayload();
                                        executeScript(INTERCELL_EVENT, null);
                                        }
                                    else
                                        {
                                        System.out.println("ScriptingComponent : Cell " + cell + " : In Intercell listener in commitEvent - Code not in list - payload = " + ice.getPayload() + " Code = " + ice.getIceCode());
                                        }
                                    }
                                else
                                    {
                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is new message - Ignore it");
                                    }
                                }
                            else if(message instanceof ScriptingComponentTransformMessage)
                                {
                                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - Received Transform message - Message id = " + message.getCellID());
                                if(cell.getCellID().equals(message.getCellID()))
                                    {
                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
                                    final ScriptingComponentTransformMessage trm = (ScriptingComponentTransformMessage)message;
                                    int transformCode = trm.getTransformCode();
                                    switch(transformCode)
                                        {
                                        case ScriptingComponentTransformMessage.TRANSLATE_TRANSFORM:
                                            {
                                            SceneWorker.addWorker(new WorkCommit() 
                                                {
                                                public void commit() 
                                                    {
                                                    localNode.setLocalTranslation(trm.getVector());
                                                    ClientContextJME.getWorldManager().addToUpdateList(localNode);
                                                    }
                                                });
                                            break;
                                            }
                                        case ScriptingComponentTransformMessage.ROTATE_TRANSFORM:
                                            {
                                            SceneWorker.addWorker(new WorkCommit() 
                                                {
                                                public void commit() 
                                                    {
                                                    localNode.setLocalRotation(trm.getTransform());
                                                    ClientContextJME.getWorldManager().addToUpdateList(localNode);
                                                    }
                                                });
                                            break;
                                            }
                                        case ScriptingComponentTransformMessage.SCALE_TRANSFORM:
                                            {
                                            SceneWorker.addWorker(new WorkCommit() 
                                                {
                                                public void commit() 
                                                    {
                                                    localNode.setLocalScale(trm.getVector());
                                                    ClientContextJME.getWorldManager().addToUpdateList(localNode);
                                                    }
                                                });
                                            break;
                                            }
                                        default:
                                            {
                                            
                                            }
                                        }
                                    }
                                else
                                    {
                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is new message - Ignore it");
                                    }
                                }
                            }

                        };
                    channelComp.addMessageReceiver(ScriptingComponentChangeMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(ScriptingComponentICEMessage.class, msgReceiver);
                    channelComp.addMessageReceiver(ScriptingComponentTransformMessage.class, msgReceiver);
                    }
/* Execute the startup script */
                executeScript(STARTUP_EVENT, null);
                break;
                }
            default:
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In default for setStatus - status other than ACTIVE");
                }
            }
/* Register the intercell listener */
        ClientContext.getInputManager().addGlobalEventListener(new IntercellListener());
/* Get local node */
        CellRendererJME ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity mye = ret.getEntity();
        RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
        localNode = rc.getSceneRoot();
/* Register mouse event listener */
        MouseEventListener myListener = new MouseEventListener();
        myListener.addToEntity(mye);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setStatus : Cell Name = " + scriptClump);
        }

/*
Tell the message receiver for messages from server to broadcast incoming ICE messages
*/
    public void makeMeICEReflector()
        {
        iAmICEReflector = 1;
        }
    
    public void makeMeNotICEReflector()
        {
        iAmICEReflector = 0;
        }
    
    public void establishSocket(int code, int errorCode, String ip, int port)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket int version - Message code " + code + " Error code = " + errorCode);
        sif = new SocketInterface(ip, port, code, errorCode);
        sif.doIt();
        }
    
    public void establishSocket(float code, float errorCode, String ip, float port)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket float version - Message code " + code + "Error code = " + errorCode);
        sif = new SocketInterface(ip, (int)port, (int)code, (int)errorCode);
        sif.doIt();
        }
/*
Send a message on the socket connection
*/    
    public void sendMessage(String buffer)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : sendMessage - Message code "); 
        sif.sendBuffer(buffer);
        }
/*
Tell the ICE interface to allow messages with this message code to execute the ice script
*/    
    public void watchMessage(float code)
        {
        if(watchMessages.contains(new Float(code)))
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : watchMessage - Message code " + code + " already in watch list");
            }
        else
            {
            watchMessages.add(new Float(code));
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : watchMessage - Message code " + code + " added to watch list");
            }
        }
/*
Tell the ICE interface to start ignoring messages with this message code
*/    
    public void dontWatchMessage(float code)
        {
        if(watchMessages.contains(new Float(code)))
            {
            watchMessages.remove(new Float(code));
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : watchMessage - Message code " + code + " removed from watch list");
            }
        else
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : watchMessage - Message code " + code + " not in watch list");
            }
        }
    
    public void establishProximity(float outer, float middle, float inner)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishProximity - outer, middle, inner = " + outer + ", " + middle + ", " + inner);
        ProximityComponent comp = new ProximityComponent(cell);
        comp.addProximityListener(new ProximityListener() 
            {
            public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) 
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : proximity listener - entered = "+ entered + " - index = " + proximityIndex);
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : proximity listener - proximityVolume = "+ proximityVolume);
                stateInt[18] = proximityIndex;
                stateBoolean[18] = entered;
                executeScript(PROXIMITY_EVENT, null);
                }
            }, new BoundingVolume[] { new BoundingSphere((float)outer, new Vector3f()), new BoundingSphere((float)middle, new Vector3f()), new BoundingSphere((float)inner, new Vector3f())});
        cell.addComponent(comp);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In establishProximity : Prox class = " + cell.getComponent(ProximityComponent.class));                
        }

/*
PostMessageEvent - Send a message on the intercell interface
This method is for scripting engines that pass integer variable as integers 
*/
    public void postMessageEvent(String payload, int Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEvent with payload = " + payload + " code = " + Code);
        ClientContext.getInputManager().postEvent(new IntercellEvent(payload, Code));
        }
    
/*
PostMessageEvent - Send a message on the intercell interface
This method is for scripting engines that pass integer variable as floats 
*/
    public void postMessageEvent(String payload, float Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEvent with payload = " + payload + " code = " + Code);
        ClientContext.getInputManager().postEvent(new IntercellEvent(payload, (int)Code));
        }

/* 
 postMessageEventToServer - Send a message to the CellMO for forwarding to mirror cells on other clients
 This method is for scripting engines that pass integer variable as floats 
*/
    public void postMessageEventToServer(String payload, float Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEventToServer with payload = " + payload + " code = " + Code);
        ScriptingComponentICEMessage msg = new ScriptingComponentICEMessage(cell.getCellID(), (int)Code, payload);
        channelComp.send(msg);
        }
/* 
 postMessageEventToServer - Send a message to the CellMO for forwarding to mirror cells on other clients
 This method is for scripting engines that pass integer variable as integers 
*/

    public void postMessageEventToServer(String payload, int Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEventToServer with payload = " + payload + " code = " + Code);
        ScriptingComponentICEMessage msg = new ScriptingComponentICEMessage(cell.getCellID(), Code, payload);
        channelComp.send(msg);
        }

    public void testMethod(String ibid)
        {
        System.out.println(ibid);
        }
   
    public void setStateString(String value, int which)
        {
        stateString[which] = value;
        }
    
    public String getStateString(int which)
        {
        return stateString[which];
        }
    
    public void setStateInt(int value, int which)
        {
        stateInt[which] = value;
        }
    
    public int getStateInt(int which)
        {
        return stateInt[which];
        }
    
    public void setStateFloat(float value, int which)
        {
        stateFloat[which] = value;
        }
    
    public float getStateFloat(int which)
        {
        return stateFloat[which];
        }
    
    public void setStateBoolean(boolean value, int which)
        {
        stateBoolean[which] = value;
        }
    
    public boolean getStateBoolean(int which)
        {
        return stateBoolean[which];
        }
    
    public void getFrameRate()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Enter getFrameRate fr = " + frameRate + " st = " + new Float(frameRate).toString());
        stateFloat[3] = frameRate;
        }
    
    public void setFrameRate(float frames)
        {
        frameRate = frames;
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Enter setFrameRate - rate = " + frameRate);
        }
    
    public String getName()
        {
        return testName;
        }
    
    public void putName(String theName)
        {
    testName = theName;    
        }
    
    public void getAniFrame()
        {
        stateInt[0] = aniFrame;
        }
    
    public void setScriptName(String name, int which)
        {
        if(eventNames[which].compareTo(name) != 0)
            {
            eventNames[which] = name;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), scriptClump, scriptURL, eventNames, eventScriptType);
            channelComp.send(msg);
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptName " + which + " set to " + name);
            }
        else
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptName " + which + " already " + name);
            }
        }
    
    public void setScriptType(String name, int which)
        {
        if(eventScriptType[which].compareTo(name) != 0)
            {
            eventScriptType[which] = name;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), scriptClump, scriptURL, eventNames, eventScriptType);
            channelComp.send(msg);
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptType " + which + " set to " + name);
            }
        else
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptType " + which + " already " + name);
            }
        }
    
    public String getScriptName(int which)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptName " + which + " get " + eventNames[which]);
        return eventNames[which];
        }
    
    public String getScriptType(int which)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptType " + which + " get " + eventScriptType[which]);
        return eventScriptType[which];
        }

    public void setScriptClump(String name)
        {
        if(scriptClump.compareTo(name) != 0)
            {
            scriptClump = name;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), scriptClump, scriptURL, eventNames, eventScriptType);
            channelComp.send(msg);
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptClump to " + name);
            }
        else
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : scriptClump already " + name);
            }
        }
    
    public String getScriptClump()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptClump - get " + scriptClump);
        return scriptClump;
        }
    
    public void setScriptURL(String name)
        {
        if(scriptURL.compareTo(name) != 0)
            {
            scriptURL = name;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), scriptClump, scriptURL, eventNames, eventScriptType);
            channelComp.send(msg);
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptURL to " + name);
            }
        else
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : scriptURL already " + name);
            }
        }
    
    public String getScriptURL()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptURL - get " + scriptURL);
        return scriptURL;
        }
    
    public void executeScript(int eventType, Vector3f coorW)
       {
       System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Start of executeScript - this = " + this + " Frame Rate = " + frameRate);
       worldCoor = coorW;
       
       stateString[0] = "Morris - state string 0";
       
       try
           {
           String thePath = buildScriptPath(eventNames[eventType]);
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : scriptPath = " + thePath);
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script type = " + eventScriptType[eventType]);
           ScriptEngineManager engineManager = new ScriptEngineManager();
           ScriptEngine jsEngine = engineManager.getEngineByName(eventScriptType[eventType]);  
           Bindings bindings = jsEngine.createBindings();
           
// This line passes 'this' instance over to the script
//           bindings.put("CommThread", mth);
           bindings.put("MyClass", this);
           bindings.put("stateString", stateString);
           bindings.put("stateInt", stateInt);
           bindings.put("stateBoolean", stateBoolean);
           bindings.put("stateFloat", stateFloat);
           bindings.put("name", testName);
           bindings.put("testInt", testInt);
           bindings.put("Event", eventType);
           bindings.put("FrameRate", frameRate);
           bindings.put("eventNames", eventNames);
           bindings.put("eventScriptType", eventScriptType);
           
 //          if((jsEngine instanceof Compilable) && !(jsEngine instanceof Invocable))
           if(jsEngine instanceof Compilable)
               {
               System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : This script takes Compiled path");
               CompiledScript  theScript = scriptMap.get(eventNames[eventType]);
               if(theScript == null)
                   {
                   Compilable compilingEngine = (Compilable)jsEngine;
                   URL myURL = new URL(thePath);
                   BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
                   theScript = compilingEngine.compile(in);
                   scriptMap.put(eventNames[eventType], theScript);
                   }
               else
                   {
                   System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script " + eventNames[eventType] + " was already compiled and was already in the script map");
                   }
               theScript.eval(bindings);
               }
           else
               {
               System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : This script is Not compilable - " + eventNames[eventType]);
               URL myURL = new URL(thePath);
               BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
//               if(jsEngine instanceof Invocable)
//                   {
//                   jsEngine.eval(in);
//                   }
//               else
//                   {
                   jsEngine.eval(in, bindings);
//                   }
//               if(jsEngine instanceof Invocable)
//                    {
//                    if(scriptFunction.compareTo("") != 0)
//                        {
//                        Invocable inv = (Invocable) jsEngine;
//                        inv.invokeFunction(scriptFunction, this);
//                        }
//                    }
               }
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Return from script - Test data - stateString[0] = " + stateString[0] + " name = " + testName + " test int = " + testInt);
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Return from script - eventNames[0] = " + eventNames[0]);
           }
       catch(ScriptException ex)
           {
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script exception from the whole mechanism of compiling and executing the script " + ex);
           ex.printStackTrace();
           }
       catch(Exception e)
           {
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : General exception in the whole mechanism of compiling and executing the script  " + e);
           e.printStackTrace();
           }
       }
    
    private String buildScriptPath(String theScript)
        {
        String thePath = scriptURL + "/scripts/" + scriptClump + "/" + theScript;
        return thePath;
        }
    
    public void getInitialPosition()
        {
        Vector3f v3f = localNode.getLocalTranslation();
        stateFloat[0] = v3f.x;
        stateFloat[1] = v3f.y;
        stateFloat[2] = v3f.z;
        }

    public void getWorldCoor()
        {
        stateFloat[0] = worldCoor.x;
        stateFloat[1] = worldCoor.y;
        stateFloat[2] = worldCoor.z;
        }
   
    public void setTranslation(float x, float y, float z, int notify)
        {
        final Vector3f v3fn;
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setTranslation - node = " + localNode);
        Vector3f v3f = localNode.getLocalTranslation();
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setTranslation - Original translation = " + v3f);
        v3fn = new Vector3f(x, y, z);
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setTranslation - New translation = " + v3fn);

        SceneWorker.addWorker(new WorkCommit() 
            {
            public void commit() 
                {
                localNode.setLocalTranslation(v3fn);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyTranslate(v3fn);
        }

    public void setRotation(float x, float y, float z, float w, int notify)
        {
        final Quaternion roll;
        
        Quaternion orig = localNode.getLocalRotation();
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setRotation - Original rotation = " + orig);
        roll = new Quaternion();
        roll.fromAngleNormalAxis( w , new Vector3f(x, y, z) );
//        roll.fromAngleAxis( w , new Vector3f(x, y, z) );
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setRotation - New rotation = " + roll);
        SceneWorker.addWorker(new WorkCommit() 
            {
            public void commit() 
                {
                localNode.setLocalRotation(roll);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyRotate(roll);
        }
 
    public void setScale(float x, float y, float z, int notify)
        {
        final Vector3f scale;
        
        Vector3f orig = localNode.getLocalScale();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setScale - Original scale = " + orig);
        scale = new Vector3f(x, y, z);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setScale - New scale = " + scale);
        SceneWorker.addWorker(new WorkCommit() 
            {
            public void commit() 
                {
                localNode.setLocalScale(scale);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyScale(scale);
        }
 
    public void moveObject(float x, float y, float z, int notify)
        {
        final Vector3f v3fn;
        
        Vector3f v3f = localNode.getLocalTranslation();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In moveObject - Original translation = " + v3f);
        float X = v3f.x;
        float Y = v3f.y;
        float Z = v3f.z;
        v3fn = new Vector3f(X + x, Y + y, Z + z);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In moveObject - Original translation = " + v3fn);
        SceneWorker.addWorker(new WorkCommit() 
            {
            public void commit() 
                {
                localNode.setLocalTranslation(v3fn);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyTranslate(v3fn);
        }

    public void rotateObject(float x, float y, float z, float w, int notify)
        {
        final Quaternion sum;
        Vector3f axis = new Vector3f();
        float angle;

        Quaternion orig = localNode.getLocalRotation();
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Original quat = " + orig);
        angle = orig.toAngleAxis(axis);
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Original angle/axis = " + angle + " / " + axis);
        
        Quaternion roll = new Quaternion();
        roll.fromAngleAxis( w , new Vector3f(x, y, z) );
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() +" : In rotateObject - Change quat = " + roll);
        angle = roll.toAngleAxis(axis);
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Change angle/axis = " + angle + " / " + axis);

        sum = roll.mult(orig);
        angle = sum.toAngleAxis(axis);
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Sum quat = " + sum);
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Sum angle/axis = " + angle + " / " + axis);
        SceneWorker.addWorker(new WorkCommit() 
            {
            public void commit() 
                {
                localNode.setLocalRotation(sum);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyRotate(sum);
        }
 
    public void scaleObject(float x, float y, float z, int notify)
        {
        final Vector3f sum;
        
        Vector3f orig = localNode.getLocalScale();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In scaleObject - Original scale = " + orig);
        Vector3f scale = new Vector3f(x, y, z);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In scaleObject - Change scale = " + scale);
        sum = orig.add(scale);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In scaleObject - Final scale = " + sum);
        SceneWorker.addWorker(new WorkCommit() 
            {
            public void commit() 
                {
                localNode.setLocalScale(sum);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyScale(sum);
        }

    public void mySleep(int milliseconds)
        {
        try
            {
            Thread.sleep(milliseconds);
            }
        catch(Exception e)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Sleep exception in mySleep(int) method");
            }
       }
    
    public void mySleep(Float milliseconds)
        {
        try
            {
            Thread.sleep(milliseconds.intValue());
            }
        catch(Exception e)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Sleep exception in mySleep(float) method");
            }
       }
    
    public ArrayList buildAnimation(String animationName) 
        {
        String line;
        aniList = new ArrayList();
        String thePath = buildScriptPath(animationName);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In buildAnimation - The path = " + thePath);
        try
            {
            URL myURL = new URL(thePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
            while((line = in.readLine()) != null)
                {
                aniLast++;
                String[] result = line.split(",");
                Animation ani = new Animation();
                ani.xLoc = new Float(result[0]).floatValue();
                ani.yLoc = new Float(result[1]).floatValue();
                ani.zLoc = new Float(result[2]).floatValue();
                ani.xAxis = new Float(result[3]).floatValue();
                ani.yAxis = new Float(result[4]).floatValue();
                ani.zAxis = new Float(result[5]).floatValue();
                ani.rot = new Float(result[6]).floatValue();
                ani.delay = new Float(result[7]).floatValue();
                ani.rest = new String(result[8]);
                aniList.add(ani);
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In buildAnimation - Loading animation - Ani step -> " + ani.xLoc + "," + ani.yLoc + "," + ani.zLoc + "," + 
                        ani.xAxis + "," + ani.yAxis + "," + ani.zAxis + "," + ani.rot + "," + ani.delay + "," + ani.rest);
                }
            }
        catch(Exception e)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Exception reading while in the process of reading animation - The path = " + thePath);
            e.printStackTrace();
            }
        aniFrame = 0;
        return aniList;
        }
    
    class expired extends TimerTask
        {
        public void run()
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In method expired - This is the normal path for a timer expiration");
            if(aniFrame < aniLast || animation == 0)
                executeScript(TIMER_EVENT, worldCoor);
            }
        }
    
    public void startTimer(int timeValue)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In start timer - This is the method called to initiate a timer");
        Timer timer = new Timer();
        timer.schedule(new expired(), timeValue);
        }

    class Animation
        {
        public  float   xLoc;
        public  float   yLoc;
        public  float   zLoc;
        public  float   rot;
        public  float   xAxis;
        public  float   yAxis;
        public  float   zAxis;
        public  float     delay;
        public  String  rest;
        }

    public int playAnimationFrame()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - Frame = " + aniFrame + " of " + aniLast + " rest = " + ((Animation)aniList.get(aniFrame)).rest);
        if(((Animation)aniList.get(aniFrame)).rest.equals("r"))
            {
// set rotation - absolute
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setRotation");
            setRotation(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("q"))
            {
// set rotation - absolute
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call rotateObject");
            rotateObject(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("R"))
            {
// set rotation - absolute
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setRotation - Notify");
            setRotation(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("Q"))
            {
// set rotation - absolute
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call rotateObject - Notify");
            rotateObject(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("m"))
            {
// move object - relative move           
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call moveObject");
            moveObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("t"))
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setTranslation");
//translate object - absolute move            
            setTranslation(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("M"))
            {
// move object - relative move           
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call moveObject - Notify");
            moveObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("T"))
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setTranslation - Notify");
//translate object - absolute move            
            setTranslation(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("c"))
            {
// scale object - relative scale           
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call scaleObject");
            scaleObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("s"))
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setScale");
//scale object - absolute scale            
            setScale(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("C"))
            {
// scale object - relative scale           
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call scaleObject - Notify");
            scaleObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("S"))
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setScale - Notify");
//scale object - absolute scale            
            setScale(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        
        if(((Animation)aniList.get(aniFrame)).delay > 0)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call mySleep ");
            mySleep(((Animation)aniList.get(aniFrame)).delay);
            }
        aniFrame++;
        if(aniFrame >= aniLast)
            {
            aniList.clear();
            aniLast = 0;
            return 0;
            }
        else
            {
            return 1;
            }
        }
/*
    public void doTransform(double x, double y, double z, double rot, double xAxis, double yAxis, double zAxis)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doTransform - The parms " + x + "," + y + "," + z + "," + rot + "," + xAxis + "," + yAxis + "," + zAxis);
        setTranslation((float)x, (float)y, (float)z);
        setRotation((float)xAxis, (float)yAxis, (float)zAxis, (float)rot);
        }
*/    
    public void doNotifyTranslate(Vector3f translate)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doNotify - Translate = " + cell.getLocalTransform());
        ScriptingComponentTransformMessage msg = new ScriptingComponentTransformMessage(cell.getCellID(), ScriptingComponentTransformMessage.TRANSLATE_TRANSFORM, translate);
        channelComp.send(msg);
//        cell.getComponent(MovableComponent.class).localMoveRequest(cell.getLocalTransform());
        }
    
    public void doNotifyRotate(Quaternion transform)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doNotify - Rotate = " + cell.getLocalTransform());
        ScriptingComponentTransformMessage msg = new ScriptingComponentTransformMessage(cell.getCellID(), ScriptingComponentTransformMessage.ROTATE_TRANSFORM, transform);
        channelComp.send(msg);
//        cell.getComponent(MovableComponent.class).localMoveRequest(cell.getLocalTransform());
        }
    
    public void doNotifyScale(Vector3f scale)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doNotify - Scale = " + cell.getLocalTransform());
        ScriptingComponentTransformMessage msg = new ScriptingComponentTransformMessage(cell.getCellID(), ScriptingComponentTransformMessage.SCALE_TRANSFORM, scale);
        channelComp.send(msg);
//        cell.getComponent(MovableComponent.class).localMoveRequest(cell.getLocalTransform());
        }

    class MouseEventListener extends EventClassListener
        {
        @Override
        public Class[] eventClassesToConsume()
            {
            return new Class[]{MouseButtonEvent3D.class};
            }

        @Override
        public void computeEvent(Event event)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In computeEvent for mouse event");
            }
        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In commitEvent for mouse event");
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
//            if (mbe.isClicked() == false)
//                {
//                return;
//                }
            MouseEvent awt = (MouseEvent) mbe.getAwtEvent();
            if(awt.getID() != MouseEvent.MOUSE_PRESSED)
                {
                return;
                }

            Vector3f coorW = mbe.getIntersectionPointWorld();
//            System.out.println("World = " + coorW);
//            System.out.println("Shift down = " + MouseEvent.SHIFT_DOWN_MASK + " Modifiers = " + awt.getModifiersEx());
            int mask = 77;
            mask = awt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK;
//            System.out.println("Condition = " + mask + " Button = " + mbe.getButton() + " awt.id = " + awt.getID());
/*        CellTransform transform = this.getLocalTransform();
        SimpleTerrainCellMHFChangeMessage newMsg = new SimpleTerrainCellMHFChangeMessage(getCellID(), SimpleTerrainCellMHFChangeMessage.MESSAGE_CODE_3, SimpleTerrainCellMHFChangeMessage.MESSAGE_TRANSFORM);
        Matrix4d m4d = new Matrix4d();
        transform.get(m4d);
        newMsg.setTransformMatrix(m4d);
        ChannelController.getController().sendMessage(newMsg);
 */

            if((awt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0)
                {
//                System.out.println("Inside the shift down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1 and shift");
                    executeScript(MOUSE1S_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2 and shift");
                    executeScript(MOUSE2S_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse3 and shift");
                    executeScript(MOUSE3S_EVENT, coorW);
                    }
                }
            else if((awt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0)
                {
//                System.out.println("Inside the control down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1 and control");
                    executeScript(MOUSE1C_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2 and control");
                    executeScript(MOUSE2C_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse 3 and control");
                    executeScript(MOUSE3C_EVENT, coorW);
                    }
                }
            else if((awt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) > 0)
                {
//                System.out.println("Inside the alt down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1 and alt");
//                    System.out.println("Inside button 1 test");
                    executeScript(MOUSE1A_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2 and alt");
                    executeScript(MOUSE2A_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse 3 and alt");
                    executeScript(MOUSE3A_EVENT, coorW);
                    }
                }

            else
                {
//                System.out.println("Inside the no shift down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1");
                    executeScript(MOUSE1_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2");
                    executeScript(MOUSE2_EVENT, coorW);
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse 3");
                    executeScript(MOUSE3_EVENT, coorW);
                    }
                }
           }
        }
    class IntercellListener extends EventClassListener 
        {
@Override
        public Class[] eventClassesToConsume() 
            {
            return new Class[] { IntercellEvent.class };
            }

@Override
        public void commitEvent(Event event) 
            {
            IntercellEvent ice = (IntercellEvent)event;
            if(watchMessages.contains(new Float(ice.getCode())))
                {
                stateInt[19] = ice.getCode();
                stateString[19] = ice.getPayload();
                executeScript(INTERCELL_EVENT, null);
                System.out.println("ScriptingComponent : Cell " + cell + " : In Intercell listener in commitEvent - payload = " + ice.getPayload() + " Code = " + ice.getCode());
                }
            else
                {
                System.out.println("ScriptingComponent : Cell " + cell + " : In Intercell listener in commitEvent - Code not in list - payload = " + ice.getPayload() + " Code = " + ice.getCode());
                }
            }
        }
    
    }
