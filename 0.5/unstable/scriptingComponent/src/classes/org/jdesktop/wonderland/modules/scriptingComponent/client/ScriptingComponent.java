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
import imi.character.avatar.AvatarContext.TriggerNames;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentClientState;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentChangeMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentICEMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentTransformMessage;
//import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.textchat.client.TextChatConnection;
import org.jdesktop.wonderland.modules.textchat.client.TextChatConnection.TextChatListener;
import org.jdesktop.wonderland.modules.textchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;

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
    public static final int CHAT_EVENT = 20;
    public static final int PRESENCE_EVENT = 21;


    public static final int YES_NOTIFY = 0;
    public static final int NO_NOTIFY = 1;
/*
    private String[] eventNames;
    private String[] eventScriptType;
*/
    private String[] eventNames = new String[totalEvents];
    private String[] eventScriptType = new String[totalEvents];

    private WorldManager wm = null;
    private String info = null;
    private Vector watchMessages = new Vector();
    private SocketInterface sif = null;
    private IncomingSocketInterface isif = null;
    
    private int iAmICEReflector = 0;
    private TextChatConnection chatConnection = null;
    private PresenceManager pm = null;
    private WonderlandSession clientSession = null;

    private ArrayList presenceList = null;

    private ContentRepository       repo;

    private   int       ICECode             = 0;
    private   String    ICEMessage          = null;

    private   int       proximityBounds     = 0;
    private   Boolean   proximityDir        = false;

    private   String    chatMessage         = null;
    private   String    chatFrom            = null;
    private   String    chatTo              = null;

    private   float     initialX            = 0;
    private   float     initialY            = 0;
    private   float     initialZ            = 0;

    private   float     coorX               = 0;
    private   float     coorY               = 0;
    private   float     coorZ               = 0;

    private   int       ICEEventCode         = 0;
    private   String    ICEEventMessage      = null;

    private   Cell      theCell;
    private   AvatarImiJME avatarRenderer;

    @UsesCellComponent
    protected ChannelComponent channelComp;
    
    protected ChannelComponent.ComponentMessageReceiver msgReceiver=null;

    /**
     * The ScriptingComponent constructor
     *
     * @param cell
     */
    public ScriptingComponent(Cell cell) 
        {
        super(cell);
        theCell = cell;
        System.out.println("******************** Cell name = " + cell.getName());
        System.out.println("ScriptingComponent : Cell " + cell + " - id = " + cell.getCellID() + " : Enter ScriptingComponent constructor");

        wm = ClientContextJME.getWorldManager();
        wm.getRenderManager().setFrameRateListener(new FrameRateListener()
            {
            public void currentFramerate(float frames) 
                {
                frameRate = frames;
                }
            
            }, 100);
        repo = ContentRepositoryRegistry.getInstance().getRepository(cell.getCellCache().getSession().getSessionManager());
/*
        eventNames[MOUSE1_EVENT] = "mouse1.js";
        eventNames[MOUSE2_EVENT] = "mouse2.js";
        eventNames[MOUSE3_EVENT] = "mouse3.js";
        eventNames[MOUSE1S_EVENT] = "mouse1s.js";
        eventNames[MOUSE2S_EVENT] = "mouse2s.js";
        eventNames[MOUSE3S_EVENT] = "mouse3s.js";
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

        eventScriptType[MOUSE1_EVENT] = "javascript";
        eventScriptType[MOUSE2_EVENT] = "javascript";
        eventScriptType[MOUSE3_EVENT] = "javascript";
        eventScriptType[MOUSE1S_EVENT] = "javascript";
        eventScriptType[MOUSE2S_EVENT] = "javascript";
        eventScriptType[MOUSE3S_EVENT] = "javascript";
        eventScriptType[MOUSE1C_EVENT] = "javascript";
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
        eventScriptType[INTERCELL_EVENT] = "javascript";
        eventScriptType[CHAT_EVENT] = "javascript";
        eventScriptType[PRESENCE_EVENT] = "javascript";
*/
        }

    public  void    stopAvatarForward()
        {
        System.out.println("stopAvatarForward");
        avatarRenderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Forward);
        }

    public  void    startAvatarForward()
        {
        System.out.println("startAvatarForward");
        avatarRenderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Forward);
        }

    public  void    stopAvatarBack()
        {
        System.out.println("stopAvatarBack");
        avatarRenderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Back);
        }

    public  void    startAvatarBack()
        {
        System.out.println("startAvatarBack");
        avatarRenderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Back);
        }

    public  void    stopAvatarLeft()
        {
        System.out.println("stopAvatarLeft");
        avatarRenderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Left);
        }

    public  void    startAvatarLeft()
        {
        System.out.println("startAvatarLeft");
        avatarRenderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Left);
        }

    public  void    stopAvatarRight()
        {
        System.out.println("stopAvatarRight");
        avatarRenderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Right);
        }

    public  void    startAvatarRight()
        {
        System.out.println("startAvatarRight");
        avatarRenderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Right);
        }

    public  void    stopAvatarUp()
        {
        System.out.println("stopAvatarUp");
        avatarRenderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Up);
        }

    public  void    startAvatarUp()
        {
        System.out.println("startAvatarUp");
        avatarRenderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Up);
        }

    public  void    stopAvatarDown()
        {
        System.out.println("stopAvatarDown");
        avatarRenderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Down);
        }

    public  void    startAvatarDown()
        {
        System.out.println("startAvatarDown");
        avatarRenderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Down);
        }

    public  void    setAvatarRenderer(AvatarImiJME AvatarRenderer)
        {
        avatarRenderer = AvatarRenderer;
        }

/**
 * contentCreateDir - method for calls from a script to create a directory path in the user area on the content area
 *
 * @param theDir String contains the directory path to create
 * @return
 */
    public int contentCreateDir(String theDir)
        {
        int     i = -1;
        int     j = 0;
        
        List<ContentNode> children = null;
        ContentCollection current = null;
        
        try {

            ContentCollection ccr = repo.getUserRoot();
            System.out.println("The root node = " + ccr.getName());
            current = ccr;
            
            String[] result = theDir.split("/");
            for (i = 0; i < result.length; i++) 
                {
                int size = current.getChildren().size();
                children = current.getChildren();
            
                for(j = 0; j < size; j++)
                    {
                    String name = children.get(j).getName();
                    System.out.println("Checking node = " + name);
                    if(name.equals(result[i]))
                        {
                        System.out.println("Don't need to create node - " + result[i] + " - get it instead");
                        current = (ContentCollection) current.getChild(result[i]);
                        break;
                        }
                    }
                 System.out.println("Exit for loop with " + j);
                 if(j == size)
                    {
                    System.out.println("Creating the node - " + result[i]);
                    current.createChild(result[i], Type.COLLECTION);
                    current = (ContentCollection) current.getChild(result[i]);
                    }
                }
            return 0;
            } 
        catch (ContentRepositoryException ex) 
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            return i;
            }
        }
    
    public void tryRepo(String dummy) 
        {
        List<ContentNode> children;
        
        try {
            ContentCollection cc = repo.getUserRoot();
//            cc.createChild("child1", Type.COLLECTION);
            System.out.println("The user root node = " + cc.getName());
            
            ContentCollection ccr = repo.getRoot();
            System.out.println("The root node = " + ccr.getName());

            int size = ccr.getChildren().size();
            children = ccr.getChildren();
            
            for(int i = 0; i < size; i++)
                {
                System.out.println("The node = " + children.get(i).getName());
                
                }
            ContentCollection ccsr = repo.getSystemRoot();
            System.out.println("The system root node = " + ccsr.getName());

            
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
   /**
    * sendChat - send a chat message through the WL text chat interface
    *
    * @param msg String the message to be send
    * @param from String the sender
    * @param to String the receiver
    */
    public void sendChat(String msg, String from, String to)
        {
        System.out.println("Enter sendChat with message = " + msg + " from = " + from + " to = " + to);
        chatConnection.sendTextMessage(msg, from, to);
        }
    /**
     * getChat - establish the connection out to the text chat interface and the chat listener
     * 
     */
    public void getChat()
        {
        System.out.println("Enter getChat");
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
        chatConnection = (TextChatConnection) session.getConnection(TextChatConnectionType.CLIENT_TYPE);
        chatConnection.addTextChatListener(new TextChatListener()
            {

            public void textMessage(String arg0, String arg1, String arg2)
                {
                System.out.println("Text message = " + arg0 + " - from " + arg1 + " - to " + arg2);
                chatMessage = arg0;
                chatFrom = arg1;
                chatTo = arg2;
                executeScript(CHAT_EVENT, null);
                }

            });
        chatConnection.sendTextMessage("This is a test", "morris", "morrisford");
        }

    /**
     * yat - a query to determine what avatars are present and where - method to be called from a script
     *
     */
    public  void    yat()
        {
        System.out.println("Enter yat");
        if(presenceList != null)
            {
            presenceList.clear();
            }
        else
            {
            presenceList = new ArrayList();
            }
        System.out.println("After presenceList");
        for (PresenceInfo pi : pm.getAllUsers())
            {
            System.out.println("Inside yat loop - pi = " + pi.toString());
            Cell myCell = ClientContext.getCellCache(clientSession).getCell(pi.cellID);
            CellTransform pos = myCell.getWorldTransform();

            Vector3f v3f = new Vector3f();
            pos.getTranslation(v3f);

            PresenceItem presenceItem = new PresenceItem();
            presenceItem.x = v3f.x;
            presenceItem.y = v3f.y;
            presenceItem.z = v3f.z;
            String[] result = pi.userID.toString().split("=");
            String[] piTokens = result[1].split(" ");

            presenceItem.name = piTokens[0];
            presenceItem.clientID = pi.clientID;
            presenceList.add(presenceItem);
            System.out.println("In yat - set item - x = " + presenceItem.x + " z = " + presenceItem.z);
            }
        }
    /**
     * unrollYatsForIncoming - take the results from the last yat and send them one at a time out the incoming socket connection
     *
     */
    public  void    unrollYatsForIncoming()
        {
        for(int i = 0; i < presenceList.size(); i++)
            {
            PresenceItem pi = (PresenceItem)presenceList.get(i);
            String msg = String.format("001,%s,%d,%f,%f", pi.name, pi.clientID, pi.x, pi.z);
            sendIncomingMessage(msg);
            }
        }
    /**
     * getYat - extablist a connection to the presence interface and a listener for presence events - script call method
     *
     */
    public  void    getYat()
        {
        clientSession = LoginManager.getPrimary().getPrimarySession();
        pm = PresenceManagerFactory.getPresenceManager(clientSession);
        pm.addPresenceManagerListener(new PresenceManagerListener()
            {

            public void presenceInfoChanged(PresenceInfo pi, ChangeType arg1)
                {
                if(presenceList != null)
                    {
                    presenceList.clear();
                    }
                else
                    {
                    presenceList = new ArrayList();
                    }

                Cell myCell = ClientContext.getCellCache(clientSession).getCell(pi.cellID);
                CellTransform pos = myCell.getWorldTransform();

                Vector3f v3f = new Vector3f();
                pos.getTranslation(v3f);

                PresenceItem presenceItem = new PresenceItem();
                presenceItem.x = v3f.x;
                presenceItem.y = v3f.y;
                presenceItem.z = v3f.z;
                String[] result = pi.userID.toString().split("=");
                String[] piTokens = result[1].split(" ");

                presenceItem.name = piTokens[0];
                presenceItem.clientID = pi.clientID;
                presenceList.add(presenceItem);
                System.out.println("In yat - set item - x = " + presenceItem.x + " z = " + presenceItem.z);

                executeScript(PRESENCE_EVENT, null);
                System.out.println("presenceInfoChanged - " + v3f + "Change type = " + arg1);
                }

            public void aliasChanged(String arg0, PresenceInfo arg1)
                {
                System.out.println("presence aliasChanged");
                }

            });
        }

    /**
     * getInfo - get the 'info' for this cell and return
     *
     * @return String the info
     */
    public String getInfo()
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In getInfo - info = " + this.info);
        return this.info;
        }
    /**
     * setClientState - set the properties for this cell
     *
     * @param clientState CellComponentClientState
     */
    @Override
    public void setClientState(CellComponentClientState clientState) 
        {
        super.setClientState(clientState);
        info = ((ScriptingComponentClientState)clientState).getInfo();
        eventNames = ((ScriptingComponentClientState)clientState).getEventNames();
        eventScriptType = ((ScriptingComponentClientState)clientState).getScriptType();
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setClientState - info = " + info);
        }

    /**
     * setStatus
     *
     * @param status CellStatus
     */
    @Override
    public void setStatus(CellStatus status)
        {
//        mySleep(1000);
        switch(status)
            {
            case ACTIVE:
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = ACTIVE");
/* Register the intercell listener */
                ClientContext.getInputManager().addGlobalEventListener(new IntercellListener());
/* Get local node */
                CellRendererJME ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);

                System.out.println("In component setStatus - renderer = " + ret);
                Entity mye = ret.getEntity();
                RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
                localNode = rc.getSceneRoot();
/* Register mouse event listener */
                MouseEventListener myListener = new MouseEventListener();
                myListener.addToEntity(mye);
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setStatus");
/* Register the change message listener */
                if (msgReceiver == null) 
                    {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() 
                        {
                        public void messageReceived(CellMessage message) 
                            {
                            if(message instanceof ScriptingComponentChangeMessage)
                                {
//                                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - Received change message - Message id = " + message.getCellID());
                                if(cell.getCellID().equals(message.getCellID()))
                                    {
//                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
                                    ScriptingComponentChangeMessage scm = (ScriptingComponentChangeMessage)message;
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
//                                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - Received ICE message - Message id = " + message.getCellID());
                                if(cell.getCellID().equals(message.getCellID()))
                                    {
//                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
                                    ScriptingComponentICEMessage ice = (ScriptingComponentICEMessage)message;
                                    if(iAmICEReflector == 1)
                                        {
                                        postMessageEvent(ice.getPayload(), ice.getIceCode());
                                        }
                                    if(watchMessages.contains(new Float(ice.getIceCode())))
                                        {
                                        ICECode = ice.getIceCode();
                                        ICEMessage = ice.getPayload();
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
//                                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - Received Transform message - Message id = " + message.getCellID());
                                if(cell.getCellID().equals(message.getCellID()))
                                    {
//                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
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
        }

    /**
     * makeMeICEREflector - Tell the message receiver for messages from server to broadcast incoming ICE messages - script method
     *
     */
    public void makeMeICEReflector()
        {
        iAmICEReflector = 1;
        }
    
    /**
     * makeMeNotICEReflector - Tell the message receiver to stop reflecting messages from server to broadcast incoming ICE messages - script method
     */
    public void makeMeNotICEReflector()
        {
        iAmICEReflector = 0;
        }

    /**
     * establishSocket - initialize an outgoing socket connection (int parameters) - script method
     *
     * @param code int code for ICE messages that will be incoming from this socket
     * @param errorCode int code for ICE error messages that may come in from this socket
     * @param ip String the ip address for the connection
     * @param port int the port for the connection
     */
    public void establishSocket(int code, int errorCode, String ip, int port)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket int version - Message code " + code + " Error code = " + errorCode);
        sif = new SocketInterface(ip, port, code, errorCode);
        sif.doIt();
        }
    
    /**
     * establishSocket - initialize an outgoing socket connection (float parameters) - script method
     *
     * @param code float code for ICE messages that will be incoming from this socket
     * @param errorCode float code for ICE error messages that may come in from this socket
     * @param ip String the ip address for the connection
     * @param port float the port for the connection
     */
    public void establishSocket(float code, float errorCode, String ip, float port)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket float version - Message code " + code + "Error code = " + errorCode);
        sif = new SocketInterface(ip, (int)port, (int)code, (int)errorCode);
        sif.doIt();
        }

    /**
     * establishIncomingSocket - initialize a socket to wait for incoming socket connections (int parameters) - script method
     *
     * @param code int code for ICE messages that will be incoming from this socket
     * @param errorCode int code for ICE error messages that may come in from this socket
     * @param port int port to use to listen for connections
     */
    public void establishIncomingSocket(int code, int errorCode, int port)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket int version - Message code " + code + " Error code = " + errorCode);
        isif = new IncomingSocketInterface(port, code, errorCode);
        isif.doIt();
        }
    
    /**
     * establishIncomingSocket - initialize a socket to wait for incoming socket connections (float parameters) - script method
     *
     * @param code float code for ICE messages that will be incoming from this socket
     * @param errorCode float code for ICE error messages that may come in from this socket
     * @param port int float to use to listen for connections
     */
    public void establishIncomingSocket(float code, float errorCode, float port)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket float version - Message code " + code + "Error code = " + errorCode);
        isif = new IncomingSocketInterface((int)port, (int)code, (int)errorCode);
        isif.doIt();
        }

    /**
     * sendMessage - send a message on the outgoing socket connection - script message
     *
     * @param buffer String the message to send
     */
    public void sendMessage(String buffer)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : sendMessage - Message code "); 
        sif.sendBuffer(buffer);
        }

    /**
     * sendIncomingMessage - send a message on the incoming socket connection - script method
     * 
     * @param buffer String the message to send
     */
    public void sendIncomingMessage(String buffer)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : sendMessage - Message code ");
        isif.sendSocketMessage(buffer);
        }

    /**
     * watchMessage - Tell the ICE interface to allow messages with this message code to execute the ice script - script method
     *
     * @param code float code
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

    /**
     * dontWatchMessage - Tell the ICE interface to stop allowing messages with this code - script method
     *
     * @param code float code
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

    /**
     * establishProximity - connect to the proximity interface and establish a proximity listener with three radii - script method
     *
     * @param outer float the outer most radius
     * @param middle float the middle radius
     * @param inner float the inner most radius
     */
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
                proximityBounds = proximityIndex;
                proximityDir = entered;
                executeScript(PROXIMITY_EVENT, null);
                }
            }, new BoundingVolume[] { new BoundingSphere((float)outer, new Vector3f()), new BoundingSphere((float)middle, new Vector3f()), new BoundingSphere((float)inner, new Vector3f())});
        cell.addComponent(comp);
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In establishProximity : Prox class = " + cell.getComponent(ProximityComponent.class));                
        }

    /**
     * postMessageEvent - send an ICE message (int parameter) - script method
     *
     * @param payload String contents of the message
     * @param Code int the message code
     */
    public void postMessageEvent(String payload, int Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEvent with payload = " + payload + " code = " + Code);
        ClientContext.getInputManager().postEvent(new IntercellEvent(payload, Code));
        }
    
    /**
     * postMessageEvent - send an ICE message (float parameter) - script method
     *
     * @param payload String contents of the message
     * @param Code float the message code
     */
    public void postMessageEvent(String payload, float Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEvent with payload = " + payload + " code = " + Code);
        ClientContext.getInputManager().postEvent(new IntercellEvent(payload, (int)Code));
        }

    /**
     * postMessageEventToServer - send an ICE message to the ScriptingComponentMO to be forwarded to the companion ScriptingComponent on other clients (int parameter)- script method
     *
     * @param payload String the message
     * @param Code int message code
     */
    public void postMessageEventToServer(String payload, float Code)
        {
        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEventToServer with payload = " + payload + " code = " + Code);
        ScriptingComponentICEMessage msg = new ScriptingComponentICEMessage(cell.getCellID(), (int)Code, payload);
        channelComp.send(msg);
        }

    /**
     * postMessageEventToServer - send an ICE message to the ScriptingComponentMO to be forwarded to the companion ScriptingComponent on other clients (float parameter)- script method
     *
     * @param payload String the message
     * @param Code float message code
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
    
    public void setScriptName(String name, int which)
        {
        if(eventNames[which].compareTo(name) != 0)
            {
            eventNames[which] = name;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), eventNames, eventScriptType);
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
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), eventNames, eventScriptType);
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

    public void clearScriptMap()
        {
        scriptMap.clear();
        }
    
    public void executeScript(int eventType, Vector3f coorW)
       {
       System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Start of executeScript - this = " + this + " Frame Rate = " + frameRate);
       worldCoor = coorW;
       getInitialPosition();
       
       try
           {
           String thePath = buildScriptPath(eventNames[eventType]);
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : scriptPath = " + thePath);
           System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script type = " + eventScriptType[eventType]);
           WonderlandSession session = cell.getCellCache().getSession();
           ClassLoader cl = session.getSessionManager().getClassloader();
           System.out.println("Inside the executeScript - just before engineManager");
           ScriptEngineManager engineManager = new ScriptEngineManager(cl);
           ScriptEngine jsEngine = engineManager.getEngineByName(eventScriptType[eventType]);  
           Bindings bindings = jsEngine.createBindings();
           
// This line passes 'this' instance over to the script
//           bindings.put("CommThread", mth);
           bindings.put("MyClass", this);
           bindings.put("stateString", stateString);
           bindings.put("stateInt", stateInt);
           bindings.put("stateBoolean", stateBoolean);
           bindings.put("stateFloat", stateFloat);
           bindings.put("Event", eventType);
           bindings.put("FrameRate", frameRate);
           bindings.put("eventNames", eventNames);
           bindings.put("eventScriptType", eventScriptType);
           bindings.put("initialX", initialX);
           bindings.put("initialY", initialY);
           bindings.put("initialZ", initialZ);
           bindings.put("coorX", coorX);
           bindings.put("coorY", coorY);
           bindings.put("coorZ", coorZ);
           bindings.put("chatMessage", chatMessage);
           bindings.put("chatFrom", chatFrom);
           bindings.put("chatTo", chatTo);
           bindings.put("ICECode", ICECode);
           bindings.put("ICEMessage", ICEMessage);
           bindings.put("ICEEventCode", ICEEventCode);
           bindings.put("ICEEventMessage", ICEEventMessage);
           bindings.put("proximityBounds", proximityBounds);
           bindings.put("proximityDir", proximityDir);
           bindings.put("aniFrame", aniFrame);
           

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
        String thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/scripts/" + theCell.getName() + "/" + theScript;
        return thePath;
        }
    
    public void getInitialPosition()
        {
        Vector3f v3f = localNode.getLocalTranslation();
        initialX = v3f.x;
        initialY = v3f.y;
        initialZ = v3f.z;
        System.out.append("Initial pos in getInitialPosition = " + initialX + ", " + initialY + ", " + initialZ);
        }

    public void getWorldCoor()
        {
        coorX = worldCoor.x;
        coorY = worldCoor.y;
        coorZ = worldCoor.z;
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
        public  float   delay;
        public  String  rest;
        }

    class PresenceItem
        {
        public  float   x;
        public  float   y;
        public  float   z;
        public  String  name;
        public  BigInteger  clientID;
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
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doNotify - Translate = " + cell.getLocalTransform());
        ScriptingComponentTransformMessage msg = new ScriptingComponentTransformMessage(cell.getCellID(), ScriptingComponentTransformMessage.TRANSLATE_TRANSFORM, translate);
        channelComp.send(msg);
//        cell.getComponent(MovableComponent.class).localMoveRequest(cell.getLocalTransform());
        }
    
    public void doNotifyRotate(Quaternion transform)
        {
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doNotify - Rotate = " + cell.getLocalTransform());
        ScriptingComponentTransformMessage msg = new ScriptingComponentTransformMessage(cell.getCellID(), ScriptingComponentTransformMessage.ROTATE_TRANSFORM, transform);
        channelComp.send(msg);
//        cell.getComponent(MovableComponent.class).localMoveRequest(cell.getLocalTransform());
        }
    
    public void doNotifyScale(Vector3f scale)
        {
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In doNotify - Scale = " + cell.getLocalTransform());
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
                ICEEventCode = ice.getCode();
                ICEEventMessage = ice.getPayload();
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
