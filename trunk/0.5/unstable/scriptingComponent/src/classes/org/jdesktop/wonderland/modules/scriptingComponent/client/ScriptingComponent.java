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

import com.jme.bounding.BoundingBox;
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
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentClientState;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

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
    public final int totalEvents = 200;
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
    
    private String[] eventNames = new String[totalEvents];
    private String[] eventScriptType = new String[totalEvents];
    
    private WorldManager wm = null;
    
    private String info = null;

    public ScriptingComponent(Cell cell) 
	{
        super(cell);
        eventNames[MOUSE1_EVENT] = "mouse1.js";
        eventNames[MOUSE2_EVENT] = "mouse2.js";
        eventNames[MOUSE3_EVENT] = "mouse3.js";
        eventNames[MOUSE1S_EVENT] = "mouse1s.py";
        eventNames[MOUSE2S_EVENT] = "mouse2s.js";
        eventNames[MOUSE3S_EVENT] = "mouse3s.js";
        eventNames[MOUSE1C_EVENT] = "mouse1c.fx";
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

        eventScriptType[MOUSE1_EVENT] = "javascript";
        eventScriptType[MOUSE2_EVENT] = "javascript";
        eventScriptType[MOUSE3_EVENT] = "javascript";
        eventScriptType[MOUSE1S_EVENT] = "jython";
        eventScriptType[MOUSE2S_EVENT] = "javascript";
        eventScriptType[MOUSE3S_EVENT] = "javascript";
        eventScriptType[MOUSE1C_EVENT] = "fx";
        eventScriptType[MOUSE2C_EVENT] = "javascript";
        eventScriptType[MOUSE3C_EVENT] = "java";
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

        wm = ClientContextJME.getWorldManager();
        wm.getRenderManager().setFrameRateListener(new FrameRateListener()
            {
            public void currentFramerate(float frames) 
                {
                frameRate = frames;
                }
            
            }, 100);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Enter ScriptingComponent constructor");
    }
    
@Override
    public void setClientState(CellComponentClientState clientState) 
        {
        super.setClientState(clientState);
        info = ((ScriptingComponentClientState)clientState).getInfo();
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setClientState - info = " + info);
        }

    @Override
    public void setStatus(CellStatus status)
        {
        switch(status)
            {
            case ACTIVE:
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = ACTIVE");

                CellRendererJME ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
//                System.out.println("In ScriptingComponent - renderer = " + ret);
                Entity mye = ret.getEntity();
//                System.out.println("In ScriptingComponent - entity = " + mye);
                RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
                localNode = rc.getSceneRoot();
//                System.out.println("ScriptingComponent rootNode = " + localNode);

                MouseEventListener myListener = new MouseEventListener();
                myListener.addToEntity(mye);
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setStatus : Cell Name = " + scriptClump);

                
                executeScript(STARTUP_EVENT, null);
                break;
                }
            default:
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In default for setStatus - status other than ACTIVE");
                }
            }
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
        eventNames[which] = name;
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptName " + which + " set to " + name);
        }
    
    public void setScriptType(String name, int which)
        {
        eventScriptType[which] = name;
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptType " + which + " set to " + name);
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
        scriptClump = name;
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptClump to " + name);
        }
    
    public String getScriptClump()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptClump - get " + scriptClump);
        return scriptClump;
        }
    
    public void setScriptURL(String name)
        {
        scriptURL = name;
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptURL to " + name);
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
   
    public void setTranslation(float x, float y, float z)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setTranslation - node = " + localNode);
        Vector3f v3f = localNode.getLocalTranslation();
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setTranslation - Original translation = " + v3f);
        Vector3f v3fn = new Vector3f(x, y, z);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setTranslation - New translation = " + v3fn);
        localNode.setLocalTranslation(v3fn);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }

    public void setRotation(float x, float y, float z, float w)
        {
        Quaternion orig = localNode.getLocalRotation();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setRotation - Original rotation = " + orig);
        Quaternion roll = new Quaternion();
        roll.fromAngleAxis( w , new Vector3f(x, y, z) );
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setRotation - New rotation = " + roll);
        localNode.setLocalRotation(roll);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }
 
    public void setScale(float x, float y, float z)
        {
        Vector3f orig = localNode.getLocalScale();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setScale - Original scale = " + orig);
        Vector3f scale = new Vector3f(x, y, z);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setScale - New scale = " + scale);
        localNode.setLocalScale(scale);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }
 
    public void moveObject(float x, float y, float z)
        {
        Vector3f v3f = localNode.getLocalTranslation();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In moveObject - Original translation = " + v3f);
        float X = v3f.x;
        float Y = v3f.y;
        float Z = v3f.z;
        Vector3f v3fn = new Vector3f(X + x, Y + y, Z + z);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In moveObject - Original translation = " + v3fn);
        localNode.setLocalTranslation(v3fn);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }

    public void rotateObject(float x, float y, float z, float w)
        {
        Quaternion orig = localNode.getLocalRotation();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Original rotation = " + orig);

        Quaternion roll = new Quaternion();
        roll.fromAngleAxis( w , new Vector3f(x, y, z) );
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() +" : In rotateObject - Change rotation = " + roll);
        Quaternion sum = orig.add(roll);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Sum rotation = " + sum);
        localNode.setLocalRotation(sum);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }
 
    public void scaleObject(float x, float y, float z)
        {
        Vector3f orig = localNode.getLocalScale();
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In scaleObject - Original scale = " + orig);
        Vector3f scale = new Vector3f(x, y, z);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In scaleObject - Change scale = " + scale);
        Vector3f sum = orig.add(scale);
//      System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In scaleObject - Final scale = " + sum);
        localNode.setLocalScale(sum);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
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
            setRotation(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("n"))
            {
// Send the current transform off to the server to let other clients know that the script wants them to know where we are
            doNotify();
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("m"))
            {
// move object - relative move           
            moveObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("t"))
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setTranslation");
//translate object - absolute move            
            setTranslation(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc);
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
//            aniFrame = 0;
            aniLast = 0;
// Notify other clients at the end of an animation
//            doNotify();
            return 0;
            }
        else
            {
            return 1;
            }
        }

    public void doTransform(double x, double y, double z, double rot, double xAxis, double yAxis, double zAxis)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doTransform - The parms " + x + "," + y + "," + z + "," + rot + "," + xAxis + "," + yAxis + "," + zAxis);
        setTranslation((float)x, (float)y, (float)z);
        setRotation((float)xAxis, (float)yAxis, (float)zAxis, (float)rot);
        }
    
    public void doNotify()
        {
/*        CellTransform transform = this.getLocalTransform();
        SimpleTerrainCellMHFChangeMessage newMsg = new SimpleTerrainCellMHFChangeMessage(getCellID(), SimpleTerrainCellMHFChangeMessage.MESSAGE_CODE_3, SimpleTerrainCellMHFChangeMessage.MESSAGE_TRANSFORM);
        Matrix4d m4d = new Matrix4d();
        transform.get(m4d);
        newMsg.setTransformMatrix(m4d);
        ChannelController.getController().sendMessage(newMsg);
 */
        }

       public void restoreRest()
       {
/*       Quaternion rot = atRest.getRotation(null);
       Vector3f trans = atRest.getTranslation(null);
       Vector3f scale = atRest.getScaling(null);
       node.setLocalRotation(rot);
       node.setLocalTranslation(trans);
       node.setLocalScale(scale);
 */
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

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) 
        {
        System.out.println("ScriptingComponent : Cell " + cell + " : Enter proximity listener");
        }
    }
