package org.jdesktop.wonderland.modules.scriptingComponent.client;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoTo;
import imi.character.statemachine.GameContext;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JMenuItem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.help.WebBrowserLauncher;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
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
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarAnimationEvent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
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
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentCellCreateMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentNpcMoveMessage;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;

/**
 *
 * A Component that provides a scripting interface
 * 
 * @author morrisford
 */
@ExperimentalAPI
public class ScriptingComponent extends CellComponent
    {
    private int traceLevel = 1;
    private Node localNode = null;
    public String stateString[] = {null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
    public int stateInt[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public boolean stateBoolean[] = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    public float stateFloat[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    Map<String, CompiledScript> scriptMap = new HashMap<String, CompiledScript>();
    private ScriptingActionClass actionObject;
    private String cellType = "";
    private String     thisCellID = null;

//    myThread mth = new myThread();
    private ArrayList aniList;
    private ArrayList robotList;

    private int aniFrame = 0;
    private int aniLast = 0;
    private int robotFrame = 0;
    private int robotLast = 0;

    private CellTransform atRest;
    private int animation = 0;
    public String testName = "morrisford";
    public int testInt = 99;
    private Vector3f worldCoor = null;
    private float myFrameRate;
    public final int totalEvents = 25;
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

    public static final int YES_NOTIFY = 0;
    public static final int NO_NOTIFY = 1;

    public static final int CONTENT_USER = 0;
    public static final int CONTENT_ROOT = 1;
    public static final int CONTENT_SYSTEM = 2;

    public static final int CHANGE_SCRIPTS_MESSAGE = 1;
    public static final int CHANGE_USER_MESSAGE = 2;

/*
    private String[] eventNames;
    private String[] eventScriptType;
*/
    private String[] eventNames = new String[totalEvents];
    private String[] eventScriptType = new String[totalEvents];
    private Boolean[] eventResource = new Boolean[totalEvents];

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
    private   String    proximityUserName   = "user";

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

    private   float     initialRotationX    = 0;
    private   float     initialRotationY    = 0;
    private   float     initialRotationZ    = 0;
    private   float     initialAngle        = 0;
    private   Vector   contentRead;

    private   Cell      theCell;
    
    private   boolean             firstEntry = false;
    private   boolean             iceEventInFlight = false;

    private   String cellName;
    private   String userName;
    
    private   CellRendererJME ret = null;

    @UsesCellComponent
    protected ChannelComponent channelComp;
    
    protected ChannelComponent.ComponentMessageReceiver msgReceiver=null;
    private Quaternion initialQuat;

    private JMenuItem editScripts;
    private HUDComponent hudComponent = null;
    private boolean     useGlobalScripts = true;
    private ScriptingComponent thees;
    private String      cellOwner = "";
    private int         changeMessageType = 0;

    private AudioInputStream soundInputStream;
    private String clipFile;
    private int responseCode;
    private int localOrGlobal;

    private MouseEventListener myListener = null;
    private KeyEventListener myKeyListener = null;
    private myTextChatListener myChatListener = null;
    private IntercellListener intercellListener = null;

    private WlAvatarCharacter myAvatar = null;

    private ScheduledFuture futureTask = null;

    private Vector3f animationStartTranslate = new Vector3f(0.0f, 0.0f, 0.0f);
    private float[] animationStartRotation = {0.0f, 0.0f, 0.0f};
    private int animationTimeMultiplier = 1;
    private int animationStartKeyframe = 0;
    private int animationEndKeyframe = 0;
    private int animationIceCode = 0;
    private boolean animationSaveTransform = false;
    private boolean animationPlayReverse = false;

    private int avatarEventEnable = 0;
    private String avatarEventType = null;
    private String avatarEventSource = null;
    private String avatarEventAnimation = null;

    private int audioEventsEnable = 0;
    private String npcAvatarName = null;

    private ArrayList sitTargetGroup;
    private boolean takeAvatar = false;

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;
    
    ProximityComponent comp = null;
    ProximityListener proxListener = null;

    PresenceManagerListener presenceListener = null;

    private int presenceListSize = 0;
    private String presenceListItem = null;

    /**
     * The ScriptingComponent constructor
     *
     * @param cell
     */
    public ScriptingComponent(Cell cell) 
        {
        super(cell);
        firstEntry = false;
        theCell = cell;

        if(traceLevel > 1)
            {
            System.out.println("******************** Cell name = " + cell.getName());
            System.out.println("ScriptingComponent : Cell " + cell + " - id = " + cell.getCellID() + " : Enter ScriptingComponent constructor");
            System.out.println("******************** class name = " + cell.getClass());
            }
        this.cellName = cell.getName();
        this.thisCellID = cell.getCellID().toString();
        this.thees = this;
        repo = ContentRepositoryRegistry.getInstance().getRepository(cell.getCellCache().getSession().getSessionManager());
        }

    public void setTakeAvatar(boolean take)
        {
        takeAvatar = take;
        }

    public void setTraceLevel(int trace)
        {
        traceLevel = trace;
        }

    public void launchBrowser(String theUrl)
        {
        if (!theUrl.contains("://"))
            {
            theUrl = "http://" + theUrl;
            }

        try
            {
            WebBrowserLauncher.openURL(theUrl);
            }
        catch (Exception ex)
            {
            if(traceLevel > 0)
                {
                System.out.println("Failed to open URL: " + theUrl + " - Exception " + ex);
                }
            }
        }

    public void setNpcAvatarName(String name)
        {
        npcAvatarName = name;
        }

    public void enableAudioEvents()
        {
        audioEventsEnable = 1;
        }

    public void disableAudioEvents()
        {
        audioEventsEnable = 0;
        }

    public void enableAvatarEvents()
        {
        avatarEventEnable = 1;
        }

    public void disableAvatarEvents()
        {
        avatarEventEnable = 0;
        }

    public class DummyTask implements Runnable
        {
        private int eventNumber = 0;

        public DummyTask(int event)
            {
            eventNumber = event;
            }

        public void run()
            {
            if(traceLevel > 1)
                {
                System.out.println("Starting");
                }
            executeScript(eventNumber, null);
            if(traceLevel > 1)
                {
                System.out.println("Ending");
                }
            }
        }

    public void startRepeater(int initialDelay, int delayAmount, int eventNumber)
        {
        if(futureTask == null)
            {
            Runnable task = new DummyTask(eventNumber);
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            futureTask = executor.scheduleAtFixedRate(task, initialDelay, delayAmount, TimeUnit.SECONDS);
            }
        else
            {
            if(traceLevel > 1)
                {
                System.out.println("Repeater already running");
                }
            }
        }

    public void stopRepeater()
        {
        if(futureTask != null)
            {
            futureTask.cancel(false);
            futureTask = null;
            }
        }

    public void playSound(String clipFile, int responseCode, int localOrGlobal)
        {
        this.clipFile = clipFile;
        this.responseCode = responseCode;
        this.localOrGlobal = localOrGlobal;
        new playSoundThread().start();
        }

    class playSoundThread extends Thread
        {
@Override
        public void run()
            {
            URL soundURL = null;

            try
                {
                if(localOrGlobal == 0)
                    {
                    String thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/sounds/" + theCell.getName() + "/" + clipFile;
                    soundURL = new URL(thePath);
                    if(traceLevel > 0)
                        {
                        System.out.println("Gonna try to play - " + thePath + " - URL - " + soundURL);
                        }
                    }
                else
                    {
                    String thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/sounds/" + clipFile;
                    soundURL = new URL(thePath);
                    if(traceLevel > 0)
                        {
                        System.out.println("Gonna try to play - " + thePath + " - URL - " + soundURL);
                        }
                    }

                soundInputStream = AudioSystem.getAudioInputStream(soundURL);
                AudioFormat audioFormat = soundInputStream.getFormat();
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

                SourceDataLine line = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
                line.open(audioFormat);
                line.start();

                int nBytesRead = 0;
                byte[] abData = new byte[10000];
                while(nBytesRead != -1)
                    {
                    nBytesRead = soundInputStream.read(abData, 0, abData.length);
                    if(nBytesRead >= 0)
                        {
                        int nBytesWritten = line.write(abData, 0, nBytesRead);
                        }
                    }
                line.drain();
                line.close();
                if(audioEventsEnable == 1)
                    ClientContext.getInputManager().postEvent(new IntercellEvent("Finished", responseCode));
                }
            catch(UnsupportedAudioFileException ex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Exception in playSound " + ex);
                    ex.printStackTrace();
                    }
                }
            catch(LineUnavailableException luex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Exception in playSound " + luex);
                    luex.printStackTrace();
                    }
                }
            catch(IOException ioex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Exception in playSound " + ioex);
                    ioex.printStackTrace();
                    }
                }
            }
        }

    public void getMyAvatar()
        {
        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        myAvatar = ((AvatarImiJME)rend).getAvatarCharacter();
        if(traceLevel > 3)
            {
            System.out.println(" avatar X = " + myAvatar.getPositionRef().getX() + " - Y = " + myAvatar.getPositionRef().getY() + " - Z = " + myAvatar.getPositionRef().getZ());
            }
        }

    public float[] getAvatarLocation()
        {
        float[] location = {0.0f, 0.0f, 0.0f};

        getMyAvatar();
        location[0] = myAvatar.getPositionRef().getX();
        location[1] = myAvatar.getPositionRef().getY();
        location[2] = myAvatar.getPositionRef().getZ();

        return location;
        }
    
    public void moveAvatarForward()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStart(TriggerNames.Move_Forward);
            }
        }

    public void stopAvatarForward()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStop(TriggerNames.Move_Forward);
            }
        }


    public void moveAvatarBack()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStart(TriggerNames.Move_Back);
            }
        }

    public void stopAvatarBack()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStop(TriggerNames.Move_Back);
            }
        }

    public void moveAvatarRight()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStart(TriggerNames.Move_Right);
            }
        }

    public void stopAvatarRight()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStop(TriggerNames.Move_Right);
            }
        }

    public void moveAvatarLeft()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStart(TriggerNames.Move_Left);
            }
        }

    public void stopAvatarLeft()
        {
        if(myAvatar != null)
            {
            myAvatar.triggerActionStop(TriggerNames.Move_Left);
            }
        }

    public void testMethod(String message)
        {
        System.out.println(message);
        }
    
    public String getCellOwner()
        {
        return cellOwner;
        }

    public String getCellName()
        {
        return cellName;
        }

    public void setGlobalScripts(boolean value)
        {
        useGlobalScripts = value;
        ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), cellOwner, useGlobalScripts, CHANGE_USER_MESSAGE);
        channelComp.send(msg);
        if(traceLevel > 3)
            {
            System.out.println("Set useGlobalScripts to " + value);
            }
        }

    public boolean getGlobalScripts()
        {
        return useGlobalScripts;
        }

    public Vector getContentRead()
        {
        return contentRead;
        }

    public int getScriptIndex(String script)
        {
        if(traceLevel > 3)
            {
            System.out.println("Enter getScriptIndex with script = " + script);
            }
        for(int i = 0; i < 23; i++)
            {
            if(traceLevel > 3)
                {
                System.out.println("Inside - i = " + i + " - evName = " + eventNames[i]);
                }
            if(eventNames[i].equals(script))
                {
                return i;
                }
            }
        return -1;
        }
    
    public int getTest()
        {
        return testInt;
        }

    public void putActionObject(ScriptingActionClass actionObject)
        {
        if(traceLevel > 3)
            {
            System.out.println("In scriptingComponent - enter putActionObject");
            }
        this.actionObject = actionObject;
        this.cellType = actionObject.getName();
        }

    public void executeAction(String Name, float x, float y, float z)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - three floats");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setPoint(x, y, z);
        runny.run();
        }

    public void executeAction(String Name, float x, float y, float z, float xx, float yy, float zz)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - six floats");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setPoint(x, y, z);
        runny.setOtherPoint(xx, yy, zz);
        runny.run();
        }

    public void executeAction(String Name)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - no parms");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.setTakeAvatar(takeAvatar);
        runny.run();
        }

    public void executeAction(String Name, int a)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - int param");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setSingleInt(a);
        runny.run();
        }

    public void executeAction(String Name, String avatar)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - String param");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setAvatar(avatar);
        runny.run();
        }

    public void executeAction(String Name, String one, String two)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - 2 String params");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.set2Strings(one, two);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.run();
        }

    public void executeAction(String Name, String one, String two, String three)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - 3 String params");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.set3Strings(one, two, three);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.run();
        }

    public void executeAction(String Name, String one, String two, String three, String four)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - 4 String params");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.set4Strings(one, two, three, four);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.run();
        }

    public void executeAction(String Name, String one, String two, String three, String four, String five)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - 5 String params");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.set5Strings(one, two, three, four, five);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.run();
        }

    public void executeAction(String Name, String one, String two, String three, String four, String five, String six)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - 6 String params");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.set6Strings(one, two, three, four, five, six);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.run();
        }

    public void executeAction(String Name, String one, String two, String three, String four, String five, String six, String seven)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - 7 String params");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.set7Strings(one, two, three, four, five, six, seven);
        runny.setAnimationStartTranslate(animationStartTranslate);
        runny.setAnimationStartRotation(animationStartRotation);
        runny.setAnimationTimeMultiplier(animationTimeMultiplier);
        runny.setAnimationStartKeyframe(animationStartKeyframe);
        runny.setAnimationEndKeyframe(animationEndKeyframe);
        runny.setAnimationIceCode(animationIceCode);
        runny.run();
        }

    public void executeAction(String Name, String animation, int animationNumber)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - one string - one int parms");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setAnimation(animation);
        runny.setSingleInt(animationNumber);
        runny.run();
        }

    public void executeAction(String Name, boolean flag)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent - enter executeAction - one boolean");
            }
        ScriptingRunnable runny = actionObject.getCmdMap(Name);
        runny.setBooleanFlag(flag);
        runny.run();
        }

    public void setAnimationStartTranslate(float X, float Y, float Z)
        {
        Vector3f temp = new Vector3f(X, Y, Z);
        animationStartTranslate = temp;
        }

    public void setAnimationStartRotation(float X, float Y, float Z)
        {
        System.out.println("********** In setAnimationStarrtRotation " + X + " : " + Y + " : " + Z);
        animationStartRotation[0] = X;
        animationStartRotation[1] = Y;
        animationStartRotation[2] = Z;
        }

    public void setAnimationTimeMultiplier(int thyme)
        {
        animationTimeMultiplier = thyme;
        }

    public void setAnimationStartKeyframe(int start)
        {
        animationStartKeyframe = start;
        }
    
    public void setAnimationEndKeyframe(int end)
        {
        animationEndKeyframe = end;
        }

    public void setAnimationIceCode(int code)
        {
        animationIceCode = code;
        }

    public void setAnimationSaveTransform(int save)
        {
        if(save == 1)
            animationSaveTransform = true;
        else
            animationSaveTransform = false;
        }

    public void setAnimationPlayReverse(int reverse)
        {
        if(reverse == 1)
            animationPlayReverse = true;
        else
            animationPlayReverse = false;
        }

    public void contentReadResource(String theScript)
        {
        if(traceLevel > 3)
            {
            System.out.println("Enter contentReadResource");
            }
        String strLine;
        Vector tempBuf = new Vector();

        int     i;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("resources/" + theScript)));
            try
                {
                i = 0;
                while ((strLine = br.readLine()) != null)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("Line read = " + strLine);
                        }
                    tempBuf.addElement(new String(strLine));
                    i++;
                    if(traceLevel > 4)
                        {
                        System.out.println("Line from content file" + strLine);
                        }
                    }
                contentRead = tempBuf;
                }
            catch (IOException ex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("Exception in content readline - " + ex);
                    }
                }
            }
        catch (Exception ex)
            {
            if(traceLevel > 0)
                {
                System.err.println("Exception in contentReadResource - " + ex);
                }
            }
        }


/**
 * contentCreateFile - method for calls from a script to create a directory path in the user area on the content area
 *
 * @param theDir String contains the directory path to read
 * @param theFile String contains the filename to read
 * @return
 */
    public int contentReadFile(String thePath, int repository)
        {
        if(traceLevel > 0)
            {
            System.out.println("Enter contentReadFile - path = " + thePath);
            }
        ContentResource current = null;
        ContentCollection ccr = null;
        String strLine;
        Vector tempBuf = new Vector();

        int     i;

//        StringBuffer sb = new StringBuffer();

        try {
            switch(repository)
                {
                case CONTENT_USER:
                    {
                    ccr = repo.getUserRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The user root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_ROOT:
                    {
                    ccr = repo.getRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The content root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_SYSTEM:
                    {
                    ccr = repo.getSystemRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The system root node = " + ccr.getName());
                        }
                    break;
                    }
                default:
                    {
                    break;
                    }
                }
            current = (ContentResource)ccr.getChild(thePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(current.getInputStream()));
            try
                {
                i = 0;
                while ((strLine = br.readLine()) != null)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("Line read = " + strLine);
                        }
                    tempBuf.addElement(new String(strLine));
                    i++;
                    if(traceLevel > 4)
                        {
                        System.out.println("Line from content file" + strLine);
                        }
                    }
                contentRead = tempBuf;
                }
            catch (IOException ex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("Exception in content readline - " + ex);
                    }
                return -1;
                }
            return 0;
            }
        catch (ContentRepositoryException ex)
            {
            if(traceLevel > 0)
                {
                System.err.println("Exception in contentReadFile - " + ex);
                }
            return -1;
            }
        }

/**
 * contentCreateFile - method for calls from a script to create a directory path in the user area on the content area
 *
 * @param theDir String contains the directory path to create
 * @param theFile String contains the filename to create
 * @return
 */
    public int contentWriteFile(String theDir, String theFile, String theData, int repository)
        {
        int     i = -1;
        int     j = 0;

        if(traceLevel > 4)
            {
            System.out.println("Enter contentWriteFile - path = " + theDir + " - file = " + theFile);
            }

        List<ContentNode> children = null;
        ContentCollection current = null;
        ContentCollection whereFileGoes = null;
        ContentCollection ccr = null;

        try {
            switch(repository)
                {
                case CONTENT_USER:
                    {
                    ccr = repo.getUserRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The user root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_ROOT:
                    {
                    ccr = repo.getRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The content root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_SYSTEM:
                    {
                    ccr = repo.getSystemRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The system root node = " + ccr.getName());
                        }
                    break;
                    }
                default:
                    {
                    break;
                    }
                }

            current = ccr;

            String[] result = theDir.split("/");
            for (i = 0; i < result.length; i++)
                {
                int size = current.getChildren().size();
                children = current.getChildren();

                for(j = 0; j < size; j++)
                    {
                    String name = children.get(j).getName();
                    if(traceLevel > 4)
                        {
                        System.out.println("Checking node = " + name);
                        }
                    if(name.equals(result[i]))
                        {
                        if(traceLevel > 4)
                            {
                            System.out.println("Don't need to create node - " + result[i] + " - get it instead");
                            }
                        current = (ContentCollection) current.getChild(result[i]);
                        if(traceLevel > 4)
                            {
                            System.out.println("Current = " + current.getName());
                            }
                        whereFileGoes = current;
                        break;
                        }
                    }
                 if(traceLevel > 4)
                    {
                    System.out.println("Exit for loop with " + j);
                    }
                 if(j == size)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("Creating the node - " + result[i]);
                        }
                    current.createChild(result[i], Type.COLLECTION);
                    current = (ContentCollection) current.getChild(result[i]);
                    if(traceLevel > 4)
                        {
                        System.out.println("Current = " + current.getName());
                        }
                    whereFileGoes = current;
                    }
                }
            if(traceLevel > 4)
                {
                System.out.println("whereFileGoes = " + whereFileGoes.getName() + " - theFile = " + theFile);
                }
            whereFileGoes.removeChild(theFile);
            ContentResource tf = (ContentResource) whereFileGoes.createChild(theFile, Type.RESOURCE);

            tf.put(theData.getBytes());
            return 0;
            }
        catch (ContentRepositoryException ex)
            {
            if(traceLevel > 0)
                {
                System.err.println("Exception in contentCreateFile - " + ex);
                }
            return i;
            }
        }

/**
 * contentCreateDir - method for calls from a script to create a directory path in the user area on the content area
 *
 * @param theDir String contains the directory path to create
 * @return
 */
    public int contentCreateDir(String theDir, int repository)
        {
        int     i = -1;
        int     j = 0;
        
        List<ContentNode> children = null;
        ContentCollection current = null;
        ContentCollection ccr = null;

        try {
            switch(repository)
                {
                case CONTENT_USER:
                    {
                    ccr = repo.getUserRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The user root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_ROOT:
                    {
                    ccr = repo.getRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The content root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_SYSTEM:
                    {
                    ccr = repo.getSystemRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The system root node = " + ccr.getName());
                        }
                    break;
                    }
                default:
                    {
                    break;
                    }
                }

            current = ccr;
            
            String[] result = theDir.split("/");
            for (i = 0; i < result.length; i++) 
                {
                int size = current.getChildren().size();
                children = current.getChildren();
            
                for(j = 0; j < size; j++)
                    {
                    String name = children.get(j).getName();
                    if(traceLevel > 4)
                        {
                        System.out.println("Checking node = " + name);
                        }
                    if(name.equals(result[i]))
                        {
                        if(traceLevel > 4)
                            {
                            System.out.println("Don't need to create node - " + result[i] + " - get it instead");
                            }
                        current = (ContentCollection) current.getChild(result[i]);
                        break;
                        }
                    }
                 if(traceLevel > 4)
                    {
                    System.out.println("Exit for loop with " + j);
                    }
                 if(j == size)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("Creating the node - " + result[i]);
                        }
                    current.createChild(result[i], Type.COLLECTION);
                    current = (ContentCollection) current.getChild(result[i]);
                    }
                }
            return 0;
            } 
        catch (ContentRepositoryException ex) 
            {
            if(traceLevel > 0)
                {
                Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
            return i;
            }
        }
    
    public void tryRepo(String dummy) 
        {
        List<ContentNode> children;
        
        try
            {
            ContentCollection cc = repo.getUserRoot();
//            cc.createChild("child1", Type.COLLECTION);
            if(traceLevel > 4)
                {
                System.out.println("The user root node = " + cc.getName());
                }

            ContentCollection ccr = repo.getRoot();
            if(traceLevel > 4)
                {
                System.out.println("The root node = " + ccr.getName());
                }

            int size = ccr.getChildren().size();
            children = ccr.getChildren();

            if(traceLevel > 4)
                {
                for(int i = 0; i < size; i++)
                    {
                    System.out.println("The node = " + children.get(i).getName());
                    }
                }
            ContentCollection ccsr = repo.getSystemRoot();
            if(traceLevel > 4)
                {
                System.out.println("The system root node = " + ccsr.getName());
                }
            }
        catch (ContentRepositoryException ex)
            {
            if(traceLevel > 0)
                {
                Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    public Vector getFileList(String thePath, int repository)
        {
        List<ContentNode> children = null;
        ContentCollection current = null;
        ContentResource currentChild = null;
        int     i;
        int     j;

        if(traceLevel > 3)
            {
            System.out.println("Enter getFileList");
            }
        ContentCollection ccr = null;
        Vector tempBuf = new Vector();

        try {
            switch(repository)
                {
                case CONTENT_USER:
                    {
                    ccr = repo.getUserRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The user root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_ROOT:
                    {
                    ccr = repo.getRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The content root node = " + ccr.getName());
                        }
                    break;
                    }
                case CONTENT_SYSTEM:
                    {
                    ccr = repo.getSystemRoot();
                    if(traceLevel > 4)
                        {
                        System.out.println("The system root node = " + ccr.getName());
                        }
                    break;
                    }
                default:
                    {
                    break;
                    }
                }
//             children = ccr.getChildren();

            current = ccr;

            String[] result = thePath.split("/");
            for (i = 0; i < result.length; i++)
                {
                int size = current.getChildren().size();
                children = current.getChildren();

                for(j = 0; j < size; j++)
                    {
                    String name = children.get(j).getName();
                    if(traceLevel > 4)
                        {
                        System.out.println("Checking node = " + name);
                        }
                    if(name.equals(result[i]))
                        {
                        if(traceLevel > 4)
                            {
                            System.out.println("Found the node - " + result[i] + " - get it");
                            }
                        current = (ContentCollection) current.getChild(result[i]);
                        break;
                        }
                    }
                 if(traceLevel > 4)
                    {
                    System.out.println("Exit for loop with " + j);
                    }
                 if(j == size)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("Cannot find the node - " + result[i]);
                        }
                    return null;
//                    current.createChild(result[i], Type.COLLECTION);
//                    current = (ContentCollection) current.getChild(result[i]);
                    }
                }

            children = current.getChildren();
            int size = children.size();

            for(i = 0; i < size; i++)
                {
                String name = children.get(i).getName();
                if(current.getChild(name) instanceof ContentCollection)
                    {
                    if(traceLevel > 0)
                        {
                        System.out.println("Directory ? = " + name);
                        }
                    }
                else
                    {
                    tempBuf.addElement(new String(name));
                    if(traceLevel > 0)
                        {
                        System.out.println("Possible node = " + name);
                        }
                    }
                }
            }
        catch (ContentRepositoryException ex)
            {
            if(traceLevel > 0)
                {
                System.err.println("Exception in getFileList - " + ex);
                }
            traceLevel = 1;
            return null;
            }
        traceLevel = 1;
        return tempBuf;
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
        if(traceLevel > 3)
            {
            System.out.println("Enter sendChat with message = " + msg + " from = " + from + " to = " + to);
            }
        if(myChatListener == null)
            getChat();
        chatConnection.sendTextMessage(msg, from, to);
        }
    /**
     * getChat - establish the connection out to the text chat interface and the chat listener
     * 
     */
    public void getChat()
        {
        if(traceLevel > 3)
            {
            System.out.println("Enter getChat");
            }
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
        chatConnection = (TextChatConnection) session.getConnection(TextChatConnectionType.CLIENT_TYPE);

        if(myChatListener == null)
            {
            myChatListener = new myTextChatListener();
            chatConnection.addTextChatListener(myChatListener);
            }
        }
    
    
        class myTextChatListener implements TextChatListener 
            {
            public void textMessage(String arg0, String arg1, String arg2)
                {
                if(traceLevel > 3)
                    {
                    System.out.println("Text message = " + arg0 + " - from " + arg1 + " - to " + arg2);
                    }
                chatMessage = arg0;
                chatFrom = arg1;
                chatTo = arg2;
                executeScript(CHAT_EVENT, null);
                }
            }

    /**
     * yat - a query to determine what avatars are present and where - method to be called from a script
     *
     */
    public  void    yat()
        {
        if(traceLevel > 3)
            {
            System.out.println("Enter yat");
            }
        if(pm == null)
            {
            System.out.println("Enter yat with pm null - should call getYat() first");
            return;
            }
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
            if(traceLevel > 4)
                {
                System.out.println("Inside yat loop - pi = " + pi.toString());
                }
            Cell myCell = ClientContext.getCellCache(clientSession).getCell(pi.getCellID());
            CellTransform pos = myCell.getWorldTransform();

            Vector3f v3f = new Vector3f();
            pos.getTranslation(v3f);

            PresenceItem presenceItem = new PresenceItem();
            presenceItem.x = v3f.x;
            presenceItem.y = v3f.y;
            presenceItem.z = v3f.z;
            String[] result = pi.getUserID().toString().split("=");
            String[] piTokens = result[1].split(" ");

            presenceItem.name = piTokens[0];
            presenceItem.clientID = pi.getClientID();
            presenceList.add(presenceItem);
            if(traceLevel > 4)
                {
                System.out.println("In yat - set item - x = " + presenceItem.x + " z = " + presenceItem.z);
                }
            }
        presenceListSize = presenceList.size();
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
    
    public void getPresenceItem(int i)
        {
        if(i > presenceList.size() - 1)
            {
            presenceListItem = "Sorry - item " + i + " does not exist in list of size " + presenceList.size();
            }
        else
            {
            PresenceItem pi = (PresenceItem)presenceList.get(i);
            presenceListItem = String.format("%s,%d,%f,%f,%f", pi.name, pi.clientID, pi.x, pi.y, pi.z);
            }
        }

    class myPresenceListener implements PresenceManagerListener
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

            Cell myCell = ClientContext.getCellCache(clientSession).getCell(pi.getCellID());
            CellTransform pos = myCell.getWorldTransform();

            Vector3f v3f = new Vector3f();
            pos.getTranslation(v3f);

            PresenceItem presenceItem = new PresenceItem();
            presenceItem.x = v3f.x;
            presenceItem.y = v3f.y;
            presenceItem.z = v3f.z;
            String[] result = pi.getUserID().toString().split("=");
            String[] piTokens = result[1].split(" ");

            presenceItem.name = piTokens[0];
            presenceItem.clientID = pi.getClientID();
            presenceList.add(presenceItem);
            if(traceLevel > 4)
                {
                System.out.println("In yat - set item - x = " + presenceItem.x + " z = " + presenceItem.z);
                }

            executeScript(PRESENCE_EVENT, null);
            if(traceLevel > 4)
                {
                System.out.println("presenceInfoChanged - " + v3f + "Change type = " + arg1);
                }
            }

        public void aliasChanged(String arg0, PresenceInfo arg1)
            {
            if(traceLevel > 4)
                {
                System.out.println("presence aliasChanged");
                }
            }
        }

    /**
     * getYat - extablish a connection to the presence interface and a listener for presence events - script call method
     *
     */
    public  void    getYat()
        {
        clientSession = LoginManager.getPrimary().getPrimarySession();
        pm = PresenceManagerFactory.getPresenceManager(clientSession);
        if(presenceListener == null)
            {
            presenceListener = new myPresenceListener();
            pm.addPresenceManagerListener(presenceListener);
            }
        }

    public void releaseYat()
        {
        if(pm != null)
            {
            if(presenceListener != null)
                pm.removePresenceManagerListener(presenceListener);
            pm = null;
            }
        }

    /**
     * getInfo - get the 'info' for this cell and return
     *
     * @return String the info
     */
    public String getInfo()
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In getInfo - info = " + this.info);
            }
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
        eventResource = ((ScriptingComponentClientState)clientState).getEventResource();
        cellOwner = ((ScriptingComponentClientState)clientState).getCellOwner();
        useGlobalScripts = ((ScriptingComponentClientState)clientState).getUseGlobalScripts();
        if(traceLevel > 4)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setClientState - info = " + info);
            }
        }

    /**
     * setStatus
     *
     * @param status CellStatus
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);
        switch(status)
            {
            case DISK:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("ScriptingComponent - DISK - increasing = " + increasing);
                    }
                break;
                }
            case INACTIVE:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("ScriptingComponent - INACTIVE - increasing = " + increasing);
                    }
                break;
                }
            case VISIBLE:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("ScriptingComponent - VISIBLE - increasing = " + increasing);
                    }
                break;
                }
            case RENDERING:
                {
/* Get local node */
                if(increasing)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = RENDERING - increasing ");
                        }
                    if(!firstEntry)
                        {
                        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
                        userName = session.getUserID().getUsername();
                        proximityUserName = userName;
                        
                        if(cellOwner.length() == 0)
                            {
                            cellOwner = userName;
                            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), cellOwner, useGlobalScripts, CHANGE_USER_MESSAGE);
                            channelComp.send(msg);
                            }

                        if(cellType.equals("NPC"))
                            {
                            if(traceLevel > 4)
                                {
                                System.out.println("In ScriptingComponent - setStatus RENDERING - NPC found");
                                }
                            }
                        else
                            {
                            ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);

                            Entity mye = ret.getEntity();
                            RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
                            localNode = rc.getSceneRoot();
                            if(myListener == null)
                                {
                                myListener = new MouseEventListener();
                                myListener.addToEntity(mye);
                                }
                            if(myKeyListener == null)
                                {
                                myKeyListener = new KeyEventListener();
                                myKeyListener.addToEntity(mye);
                                }
                            }

                        wm = ClientContextJME.getWorldManager();
                        wm.getRenderManager().setFrameRateListener(new FrameRateListener()
                            {
                            public void currentFramerate(float frames)
                                {
                                setFrameRate(frames);
                                }
                            }, 50);

                        if(intercellListener == null)
                            {
                            intercellListener = new IntercellListener();
                            ClientContext.getInputManager().addGlobalEventListener(intercellListener);
                            }
                        if (menuFactory == null)
                            {
                            final MenuItemListener l = new MenuItemListener();
                            menuFactory = new ContextMenuFactorySPI()
                                {
                                public ContextMenuItem[] getContextMenuItems(ContextEvent event)
                                    {
                                    return new ContextMenuItem[]
                                        {
                                        new SimpleContextMenuItem("Scripting Editor", l)
                                        };
                                    }
                                };
                            contextComp.addContextMenuFactory(menuFactory);
                            }
                        if(traceLevel > 4)
                            {
                            System.out.println("In component setStatus - renderer = " + ret);
                            }
/* Execute the startup script */
                        executeScript(STARTUP_EVENT, null);
                        firstEntry = true;
                        }
                    }
                else
                    {
                    ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);

                    Entity mye = ret.getEntity();
                    RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
                    if(traceLevel > 4)
                        {
                        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = RENDERING - decreasing ");
                        }
                    if(myKeyListener != null)
                        {
                        myKeyListener.removeFromEntity(mye);
                        myKeyListener = null;
                        }
                    if(myListener != null)
                        {
                        myListener.removeFromEntity(mye);
                        myListener = null;
                        }
                    if (menuFactory != null)
                        {
                        contextComp.removeContextMenuFactory(menuFactory);
                        menuFactory = null;
                        }
// Stop a repeater if running
                    stopRepeater();
                    ClientContext.getInputManager().removeGlobalEventListener(intercellListener);

                    intercellListener = null;
                    }
                break;
                }
            case ACTIVE:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = ACTIVE - increasing = " + increasing);
                    }
                if(increasing)
                    {
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
                                        ScriptingComponentChangeMessage scm = (ScriptingComponentChangeMessage)message;
                                        changeMessageType = scm.getChangeType();
                                        switch(changeMessageType)
                                            {
                                            case CHANGE_SCRIPTS_MESSAGE:
                                                {
//                                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is my message - Use it");
//                                            ScriptingComponentChangeMessage scm = (ScriptingComponentChangeMessage)message;
                                                eventNames = scm.getEventNames();
                                                eventScriptType = scm.getScriptType();
                                                eventResource = scm.getEventResource();
                                                break;
                                                }
                                            case CHANGE_USER_MESSAGE:
                                                {
                                                cellOwner = scm.getCellOwner();
                                                useGlobalScripts = scm.getUseGlobalScripts();
                                                break;
                                                }
                                            }
                                        }
                                    else
                                        {
                                        if(traceLevel > 4)
                                            {
                                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is new message - Ignore it");
                                            }
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
                                            if(traceLevel > 4)
                                                {
                                                System.out.println("ScriptingComponent : Cell " + cell + " : In Intercell listener in commitEvent - Code not in list - payload = " + ice.getPayload() + " Code = " + ice.getIceCode());
                                                }
                                            }
                                        }
                                    else
                                        {
                                        if(traceLevel > 4)
                                            {
                                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is new message - Ignore it");
                                            }
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
                                        if(traceLevel > 4)
                                            {
                                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is new message - Ignore it");
                                            }
                                        }
                                    }
                                else if(message instanceof ScriptingComponentNpcMoveMessage)
                                    {
                                    if(traceLevel > 4)
                                        {
                                        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In messageReceived - This is npc move message");
                                        }
                                    }
                                }

                            };
                        channelComp.addMessageReceiver(ScriptingComponentChangeMessage.class, msgReceiver);
                        channelComp.addMessageReceiver(ScriptingComponentICEMessage.class, msgReceiver);
                        channelComp.addMessageReceiver(ScriptingComponentTransformMessage.class, msgReceiver);
                        channelComp.addMessageReceiver(ScriptingComponentNpcMoveMessage.class, msgReceiver);
                        }
/* Execute the startup script */
//                executeScript(STARTUP_EVENT, null);
                    break;
                    }
                }    // if increasing
            default:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In default for setStatus - status other than ACTIVE");
                    }
                }
            }
        }

    /**
     * makeMeICEReflector - Tell the message receiver for messages from server to broadcast incoming ICE messages - script method
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket int version - Message code " + code + " Error code = " + errorCode);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket float version - Message code " + code + "Error code = " + errorCode);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket int version - Message code " + code + " Error code = " + errorCode);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishSocket float version - Message code " + code + "Error code = " + errorCode);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : sendMessage - Message code ");
            }
        sif.sendBuffer(buffer);
        }

    /**
     * sendIncomingMessage - send a message on the incoming socket connection - script method
     * 
     * @param buffer String the message to send
     */
    public void sendIncomingMessage(String buffer)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : sendMessage - Message code ");
            }
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
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : watchMessage - Message code " + code + " already in watch list");
                }
            }
        else
            {
            watchMessages.add(new Float(code));
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : watchMessage - Message code " + code + " added to watch list");
                }
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
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : dontWatchMessage - Message code " + code + " removed from watch list");
                }
            }
        else
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : dontWatchMessage - Message code " + code + " not in watch list");
                }
            }
        }

    public void clearWatchMessages()
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : clearWatchMessages");
            }
        watchMessages.clear();
        }

    class myProximityListener implements ProximityListener
        {
        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex)
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : proximity listener - entered = "+ entered + " - index = " + proximityIndex);
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : proximity listener - proximityVolume = "+ proximityVolume);
                }
            AvatarCell ac = (AvatarCell)cell.getCellCache().getCell(viewCellID);
            String uName = ac.getIdentity().getUsername();
            if(traceLevel > 4)
                {
                System.out.println("Proximity - CellID = " + viewCellID + " - user = " + uName);
                }
            proximityBounds = proximityIndex;
            proximityDir = entered;
            proximityUserName = uName;
            executeScript(PROXIMITY_EVENT, null);
            }
        }  // , new BoundingVolume[] { new BoundingSphere((float)outer, new Vector3f()), new BoundingSphere((float)middle, new Vector3f()), new BoundingSphere((float)inner, new Vector3f())});

    /**
     * establishProximity - connect to the proximity interface and establish a proximity listener with three radii - script method
     *
     * @param outer float the outer most radius
     * @param middle float the middle radius
     * @param inner float the inner most radius
     */
    public void establishProximity(float outer, float middle, float inner)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : establishProximity - outer, middle, inner = " + outer + ", " + middle + ", " + inner);
            }
        if(comp != null)
            comp = null;

        comp = new ProximityComponent(cell);
        proxListener = new myProximityListener();
        comp.addProximityListener(proxListener, new BoundingVolume[] { new BoundingSphere((float)outer, new Vector3f()), new BoundingSphere((float)middle, new Vector3f()), new BoundingSphere((float)inner, new Vector3f())});
        cell.addComponent(comp);
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In establishProximity : Prox class = " + cell.getComponent(ProximityComponent.class));
            }
        }

    public void removeProximity()
        {
        if(comp != null)
            {
            comp.removeProximityListener(proxListener);
            proxListener = null;
            cell.removeComponent(ProximityComponent.class);
            comp = null;
            }
        }

    /**
     * postMessageEvent - send an ICE message (int parameter) - script method
     *
     * @param payload String contents of the message
     * @param Code int the message code
     */
    public void postMessageEvent(String payload, int Code)
        {
        if(traceLevel > 0)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEvent with payload = " + payload + " code = " + Code);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEvent with payload = " + payload + " code = " + Code);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEventToServer with payload = " + payload + " code = " + Code);
            }
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
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In postMessageEventToServer with payload = " + payload + " code = " + Code);
            }
        ScriptingComponentICEMessage msg = new ScriptingComponentICEMessage(cell.getCellID(), Code, payload);
        channelComp.send(msg);
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
        myFrameRate = frames;
        if(traceLevel > 6)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Enter setFrameRate - rate = " + myFrameRate);
            }
        }

    public float getFrameRate()
        {
        System.out.println("Framerate = " + myFrameRate);
        return myFrameRate;
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
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), eventNames, eventScriptType, eventResource, CHANGE_SCRIPTS_MESSAGE);
            channelComp.send(msg);
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptName " + which + " set to " + name);
                }
            }
//        else
//            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptName " + which + " already " + name);
//            }
        }
    
    public void setScriptType(String name, int which)
        {
        if(eventScriptType[which].compareTo(name) != 0)
            {
            eventScriptType[which] = name;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), eventNames, eventScriptType, eventResource, CHANGE_SCRIPTS_MESSAGE);
            channelComp.send(msg);
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptType " + which + " set to " + name);
                }
            }
//        else
//            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptType " + which + " already " + name);
//            }
        }
    
    public void setEventResource(boolean resource, int which)
        {
        if(eventResource[which].compareTo(resource) != 0)
            {
            eventResource[which] = resource;
            ScriptingComponentChangeMessage msg = new ScriptingComponentChangeMessage(cell.getCellID(), eventNames, eventScriptType, eventResource, CHANGE_SCRIPTS_MESSAGE);
            channelComp.send(msg);
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setEventResource " + which + " set to " + resource);
                }
            }
//        else
//            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setScriptResource " + which + " already " + resource);
//            }
        }

    public String getScriptName(int which)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptName " + which + " get " + eventNames[which]);
            }
        return eventNames[which];
        }
    
    public String getScriptType(int which)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getScriptType " + which + " get " + eventScriptType[which]);
            }
        return eventScriptType[which];
        }

    public Boolean getEventResource(int which)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : getEventResource " + which + " get " + eventResource[which]);
            }
        return eventResource[which];
        }

    public void clearScriptMap()
        {
        scriptMap.clear();
        }

    public void retrieveScriptProps(Properties props)
        {
        String temp = null;
        String propName = null;
        String propType = null;

        for(Integer i = 0; i < totalEvents; i++)
            {
            propName = "name" + i.toString();
            propType = "type" + i.toString();
            if(traceLevel > 4)
                {
                System.out.println("properties - name = " + propName + " = type = " + propType + " - i = " + i);
                }
            temp = props.getProperty(propName);
            if(temp != null)
                eventNames[i] = temp;
            temp = props.getProperty(propType);
            if(temp != null)
                eventScriptType[i] = temp;
            if(traceLevel > 4)
                {
                System.out.println("properties - name = " + eventNames[i] + " = type = " + eventScriptType[i]);
                }
            }
        }

    public void testExecuteScript(String scriptName)
        {
        int index = getScriptIndex(scriptName);
        clearScriptMap();
        executeScript(index, null);
        }

    public void executeScript(int eventType, Vector3f coorW)
        {
        String thePath = null;
        BufferedReader in = null;
        String propResource = null;
        Bindings bindings = null;
        ScriptEngine jsEngine = null;

//        System.out.println("&&&&&&&&&&&&&&&& frame rate " + this.myFrameRate);
        Properties props = new Properties();
        try
            {
            in = new BufferedReader(new InputStreamReader(theCell.getClass().getResourceAsStream("resources/" + "module.properties")));
            props.load(in);
            propResource = props.getProperty("scripts");
            if(propResource == null)
                {
                if(traceLevel > 4)
                    {
                    System.out.println("Received null for cell scripts resource " + propResource);
                    }
                propResource = "webdav";
                }
            else
                {
                if(propResource.equals("cell"))
                    retrieveScriptProps(props);
                }
            }
        catch(Exception ex)
            {
            if(traceLevel > 4)
                {
                System.out.println("Exception in load properties - " + ex);
                }
            propResource = "webdav";
            }
        if(propResource.equals("component"))
            {
            if(eventResource[eventType] == false)
                propResource = "webdav";
            }
        else if(propResource.equals("webdav"))
            {
            if(eventResource[eventType] == true)
                propResource = "component";
            }
        if(traceLevel > 4)
            {
            System.out.println("Scripts property = " + propResource);
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Start of executeScript - useGlobalScripts = " + useGlobalScripts + " - userName = " + userName + " - cellOwner = " + cellOwner);
            }
        if(true)
            {
            worldCoor = coorW;
            getInitialPosition();

            if(propResource.equals("component") || propResource.equals("cell"))
                {
                thePath = "resources/" + eventNames[eventType];
                }
            else
                {
                thePath = buildScriptPath(eventNames[eventType]);
                }

            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : scriptPath = " + thePath);
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script type = " + eventScriptType[eventType]);
                }
            try
                {
                WonderlandSession session = cell.getCellCache().getSession();
                ClassLoader cl = session.getSessionManager().getClassloader();
                ScriptEngineManager engineManager = new ScriptEngineManager(cl);
                jsEngine = engineManager.getEngineByName(eventScriptType[eventType]);
                bindings = jsEngine.createBindings();
                }
            catch(Exception ex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("There was a problem getting the classloader or there is no scripting engine for " + eventScriptType[eventType]);
                    }
                }
// This line passes 'this' instance over to the script
//           bindings.put("CommThread", mth);
            bindings.put("MyClass", this);
            bindings.put("stateString", stateString);
            bindings.put("stateInt", stateInt);
            bindings.put("stateBoolean", stateBoolean);
            bindings.put("stateFloat", stateFloat);
            bindings.put("Event", eventType);
            bindings.put("FrameRate", myFrameRate);
            bindings.put("eventNames", eventNames);
            bindings.put("eventScriptType", eventScriptType);
            bindings.put("initialX", initialX);
            bindings.put("initialY", initialY);
            bindings.put("initialZ", initialZ);
            bindings.put("initialRotationX", initialRotationX);
            bindings.put("initialRotationY", initialRotationY);
            bindings.put("initialRotationZ", initialRotationZ);
            bindings.put("initialAngle", initialAngle);
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
            bindings.put("proximityUserName", proximityUserName);
            bindings.put("aniFrame", aniFrame);
            bindings.put("contentRead", contentRead);
            bindings.put("cellName", cellName);
            bindings.put("userName", userName);
            bindings.put("cellID", thisCellID);
            bindings.put("avatarEventType", avatarEventType);
            bindings.put("avatarEventSource", avatarEventSource);
            bindings.put("avatarEventAnimation", avatarEventAnimation);
            bindings.put("presenceListSize", presenceListSize);
            bindings.put("presenceListItem", presenceListItem);
            
            try
                {
                if(jsEngine instanceof Compilable)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : This script takes Compiled path");
                        }
                    CompiledScript  theScript = scriptMap.get(eventNames[eventType]);
                    if(theScript == null)
                        {
                        Compilable compilingEngine = (Compilable)jsEngine;
                        if(propResource.equals("component"))
                            {
                            if(traceLevel > 4)
                                {
                                System.out.println("Script takes component path");
                                }
                            in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(thePath)));
                            }
                        else if(propResource.equals("cell"))
                            {
                            if(traceLevel > 4)
                                {
                                System.out.println("Script takes cell path");
                                }
                            in = new BufferedReader(new InputStreamReader(theCell.getClass().getResourceAsStream(thePath)));
                            }
                        else
                            {
                            if(traceLevel > 4)
                                {
                                System.out.println("Script takes the webdav path");
                                }
                            URL myURL = new URL(thePath);
                            try
                                {
                                in = new BufferedReader(new InputStreamReader(myURL.openStream()));
                                }
                            catch(FileNotFoundException FNF)
                                {
                                if(propResource.equals("webdav") || propResource.equals("cell"))
                                    {
                                    thePath = "resources/" + eventNames[eventType];
                                    in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(thePath)));
                                    }
                                }
                            }
                        theScript = compilingEngine.compile(in);
                        scriptMap.put(eventNames[eventType], theScript);
                        }
                    else
                        {
                        if(traceLevel > 4)
                            {
                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script " + eventNames[eventType] + " was already compiled and was already in the script map");
                            }
                        }
                    theScript.eval(bindings);
                    }
                else
                    {
                    if(propResource.equals("component"))
                        {
                        in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(thePath)));
                        }
                    else if(propResource.equals("cell"))
                        {
                        in = new BufferedReader(new InputStreamReader(theCell.getClass().getResourceAsStream(thePath)));
                        }
                    else
                        {
                        URL myURL = new URL(thePath);
                        in = new BufferedReader(new InputStreamReader(myURL.openStream()));
                        }
                    jsEngine.eval(in, bindings);
                    }
                }
            catch(ScriptException ex)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script exception from the whole mechanism of compiling and executing the script " + ex);
                    ex.printStackTrace();
                    }
                }
            catch(FileNotFoundException fnf)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Script file " + thePath + " not found");
                    fnf.printStackTrace();
                    }
                }
            catch(Exception e)
                {
                if(traceLevel > 0)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : General exception in the whole mechanism of compiling and executing the script  " + e);
                    e.printStackTrace();
                    }
                }
            }
       }
    
    private String buildScriptPath(String theScript)
        {
        String  thePath = null;
        if(useGlobalScripts)
            {
            thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/scripts/" + theCell.getName() + "/" + theScript;
            }
        else
            {
            thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/users/" + userName + "/scripts/" + theCell.getName() + "/" + theScript;
            }
        return thePath;
        }

    private String buildRobotPath(String theRobot)
        {
        String  thePath = null;
        if(useGlobalScripts)
            {
            thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/robots/" + theCell.getName() + "/" + theRobot;
            }
        else
            {
            thePath = cell.getCellCache().getSession().getSessionManager().getServerURL() + "/webdav/content/users/" + userName + "/robots/" + theCell.getName() + "/" + theRobot;
            }
        return thePath;
        }

    public void getInitialRotation()
        {
        Vector3f axis = new Vector3f();
        float angle;

        Quaternion orig = localNode.getLocalRotation();
        
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In rotateObject - Original quat = " + orig);
        angle = orig.toAngleAxis(axis);

        initialRotationX = axis.x;
        initialRotationY = axis.y;
        initialRotationZ = axis.z;
        initialAngle = angle;
        initialQuat = orig;
        }

    public void getInitialPosition()
        {
        if(cellType.equals("NPC"))
            {
            initialX = 0.0f;
            initialY = 0.0f;
            initialZ = 0.0f;
            }
        else
            {
            Vector3f v3f = localNode.getLocalTranslation();
            initialX = v3f.x;
            initialY = v3f.y;
            initialZ = v3f.z;
            }
        }

    public Quaternion getInitialQuat()
        {
        return initialQuat;
        }
    
    public float getInitialX()
        {
        return initialX;
        }

    public float getInitialY()
        {
        return initialY;
        }

    public float getInitialZ()
        {
        return initialZ;
        }

    public float getInitialRotationZ()
        {
        return initialRotationZ;
        }

    public float getInitialRotationX()
        {
        return initialRotationX;
        }

    public float getInitialRotationY()
        {
        return initialRotationY;
        }

    public float getInitialAngle()
        {
        return initialAngle;
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

    public void setTranslationVector(Vector3f vector, int notify)
        {
        final Vector3f vect;
        vect = vector;

        SceneWorker.addWorker(new WorkCommit()
            {
            public void commit()
                {
                localNode.setLocalTranslation(vect);
                ClientContextJME.getWorldManager().addToUpdateList(localNode);
                }
            });
        if(notify == YES_NOTIFY)
            doNotifyTranslate(vect);
        }

    public void testAxes()
        {
            if(traceLevel > 3)
                {
                System.out.println("Enter testAxes");
                }
            getInitialRotation();
            Quaternion toTurn = new Quaternion();
            Quaternion toRoll = new Quaternion();
            Quaternion step = new Quaternion();
            Quaternion to = new Quaternion();
            Vector3f axis = new Vector3f();
            float angle;

            toTurn.fromAngleAxis((float) (Math.PI / 12), new Vector3f(0, 1, 0));

            angle = initialQuat.toAngleAxis(axis);
//            System.out.println("Initial rot - angle = " + angle + " - axis " + axis);

            step = toTurn.mult(initialQuat);
            angle = step.toAngleAxis(axis);
//            System.out.println("Step rot - angle = " + angle + " - axis " + axis);

            toRoll.fromAngleAxis((float) -(Math.PI / 12), new Vector3f((float)Math.cos(angle), 0, -(float)Math.sin(angle)));
//            toRoll.fromAngleAxis((float) -(Math.PI / 12), new Vector3f(1, 0, -(float)Math.sin(angle)));

            to = toRoll.mult(step);
            angle = to.toAngleAxis(axis);
//            System.out.println("Step rot - angle = " + angle + " - axis " + axis);

            setRotation(axis.x, axis.y, axis.z, angle, 1);
            mySleep(5000);
            getInitialRotation();
            angle = initialQuat.toAngleAxis(axis);
//            System.out.println("After first rotation Initial rot - angle = " + angle + " - axis " + axis);
        }


    public void testAxesReverse()
        {
//            System.out.println("Enter testAxesReverse");
            getInitialRotation();
            Quaternion toTurn = new Quaternion();
            Quaternion toRoll = new Quaternion();
            Quaternion step = new Quaternion();
            Quaternion to = new Quaternion();
            Vector3f axis = new Vector3f();
            float angle;

            toTurn.fromAngleAxis(-(float) (Math.PI / 12), new Vector3f(0, 1, 0));

            angle = initialQuat.toAngleAxis(axis);
//            System.out.println("Initial rot - angle = " + angle + " - axis " + axis);

            step = toTurn.mult(initialQuat);
            angle = step.toAngleAxis(axis);
//            System.out.println("Step rot - angle = " + angle + " - axis " + axis);

            toRoll.fromAngleAxis((float) +(Math.PI / 12), new Vector3f((float)Math.cos(angle), 0, -(float)Math.sin(angle)));
//            toRoll.fromAngleAxis((float) +(Math.PI / 12), new Vector3f(1, 0, +(float)Math.sin(angle)));

            to = toRoll.mult(step);
            angle = to.toAngleAxis(axis);
//            System.out.println("Step rot - angle = " + angle + " - axis " + axis);

            setRotation(axis.x, axis.y, axis.z, angle, 1);
            mySleep(5000);
            getInitialRotation();
            angle = initialQuat.toAngleAxis(axis);
//            System.out.println("After first rotation Initial rot - angle = " + angle + " - axis " + axis);
        }

    public void testAxes2()
        {
//            System.out.println("Enter testAxes2");
            getInitialRotation();
            Quaternion toTurn = new Quaternion();
            Quaternion toRoll = new Quaternion();
            Quaternion step = new Quaternion();
            Quaternion to = new Quaternion();
            Vector3f axis = new Vector3f();
            float angle;

            toRoll.fromAngleAxis((float) -(Math.PI / 12), new Vector3f(1, 0, 0));

//            toTurn.fromAngleAxis((float) (Math.PI / 12), new Vector3f(0, 1, 0));

            angle = initialQuat.toAngleAxis(axis);
//            System.out.println("Initial rot - angle = " + angle + " - axis " + axis);

            step = toRoll.mult(initialQuat);
            angle = step.toAngleAxis(axis);
//            System.out.println("Step rot - angle = " + angle + " - axis " + axis);

            toTurn.fromAngleAxis((float) (Math.PI / 12), new Vector3f(0, 1, -(float)Math.sin(angle)));

//            toRoll.fromAngleAxis((float) -(Math.PI / 12), new Vector3f((float)Math.cos(angle), 0, -(float)Math.sin(angle)));
//            toRoll.fromAngleAxis((float) -(Math.PI / 12), new Vector3f(1, 0, -(float)Math.sin(angle)));

            to = toTurn.mult(step);
            angle = to.toAngleAxis(axis);
//            System.out.println("Step rot - angle = " + angle + " - axis " + axis);

            setRotation(axis.x, axis.y, axis.z, angle, 1);
            mySleep(5000);
            getInitialRotation();
            angle = initialQuat.toAngleAxis(axis);
//            System.out.println("After first rotation Initial rot - angle = " + angle + " - axis " + axis);
        }

    public void setRotation(float x, float y, float z, float w, int notify)
        {
        final Quaternion roll;
        
        
        Quaternion orig = localNode.getLocalRotation();
//        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In setRotation - Original rotation = " + orig);
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
 
    public void setRotationQuat(Quaternion quat, int notify)
        {
        final Quaternion roll;
        roll = quat;
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
            if(traceLevel > 0)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Sleep exception in mySleep(int) method");
                }
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
            if(traceLevel > 0)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Sleep exception in mySleep(float) method");
                }
            }
       }
    
    public ArrayList buildAnimation(String animationName) 
        {
        String line;
        aniList = new ArrayList();
        String thePath = buildScriptPath(animationName);
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In buildAnimation - The path = " + thePath);
            }
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
                if(ani.rest.equals("i") || ani.rest.equals("I"))
                    {
                    ani.code = new String(result[9]);
                    ani.payload = new String(result[10]);
                    }
                else
                    {
                    ani.code = new String("");
                    ani.payload = new String("");
                    }
                aniList.add(ani);
                if(traceLevel > 3)
                    {
                    System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In buildAnimation - Loading animation - Ani step -> " + ani.xLoc + "," + ani.yLoc + "," + ani.zLoc + "," +
                        ani.xAxis + "," + ani.yAxis + "," + ani.zAxis + "," + ani.rot + "," + ani.delay + "," + ani.rest);
                    }
                }
            }
        catch(Exception e)
            {
            if(traceLevel > 0)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Exception reading while in the process of reading animation - The path = " + thePath);
                e.printStackTrace();
                }
            }
        aniFrame = 0;
        return aniList;
        }

    public ArrayList buildRobot(String robotName)
        {
        String line;
        String result[];
        robotList = new ArrayList();
        int     robotVersion = 1;

        String thePath = buildRobotPath(robotName);
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In buildAnimation - The path = " + thePath);
            }
        try
            {
            URL myURL = new URL(thePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
            while((line = in.readLine()) != null)
                {
                result = line.split(":");
                if(result[0].equals("version"))
                    {
                    robotVersion = Integer.parseInt(result[1]);
                    }
                else
                    {
                    if(robotVersion == 1)
                        {
                        robotLast++;
                        Robot robot = new Robot();
                        if(traceLevel > 4)
                            {
                            System.out.println("robotVersion = " + robotVersion);
                            System.out.println("result 0 = " + result[0]);
                            System.out.println("result 1 = " + result[1]);
                            System.out.println("result 2 = " + result[2]);
                            System.out.println("result 3 = " + result[3]);
                            System.out.println("result 4 = " + result[4]);
                            System.out.println("result 5 = " + result[5]);
                            System.out.println("result 6 = " + result[6]);
                            System.out.println("result 7 = " + result[7]);
                            System.out.println("result 8 = " + result[8]);
                            System.out.println("result 9 = " + result[9]);
                            System.out.println("result 10 = " + result[10]);
                            System.out.println("result 11 = " + result[11]);
                            System.out.println("result 12 = " + result[12]);
                            System.out.println("result 13 = " + result[13]);
                            }
                        robot.move = new Boolean(result[0]).booleanValue();
                        robot.xLoc = new Float(result[1]).floatValue();
                        robot.yLoc = new Float(result[2]).floatValue();
                        robot.zLoc = new Float(result[3]).floatValue();
                        robot.playClip = new Boolean(result[4]).booleanValue();
                        robot.clip = new String(result[5]);
                        robot.playAnimation = new Boolean(result[6]).booleanValue();
                        robot.animationName = new String(result[7]);
                        if(result[8].equals("chatText"))
                            {
                            robot.chatText = true;
                            robot.chatFile = false;
                            robot.theChatText = new String(result[9]);
                            }
                        else if(result[8].equals("chatFile"))
                            {
                            robot.chatText = false;
                            robot.chatFile = true;
                            robot.theChatFile = new String(result[9]);
                            }
                        robot.chatOrigin = new String(result[10]);

                        if(result[11].equals("ice"))
                            {
                            robot.ice = true;
                            robot.iceCode = Integer.parseInt(result[12]);
                            robot.iceMessage = new String(result[13]);
                            }

                        robotList.add(robot);
                        if(traceLevel > 3)
                            {
                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In buildRobot - Loading animation - Ani step -> " + robot.xLoc + "," + robot.yLoc + "," + robot.zLoc + "," +
                                robot.playClip + "," + robot.clip);
                            }
                        robot.version = robotVersion;
                        }
                    }
                }
            }
        catch(Exception e)
            {
            if(traceLevel > 0)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : Exception reading while in the process of reading robot - The path = " + thePath);
                e.printStackTrace();
                }
            }
        robotFrame = 0;
        return robotList;
        }
    
    class expired extends TimerTask
        {
        public void run()
            {
            if(traceLevel > 3)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In method expired - This is the normal path for a timer expiration");
                }
            if(aniFrame < aniLast || animation == 0)
                executeScript(TIMER_EVENT, worldCoor);
            }
        }
    
    public void startTimer(int timeValue)
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In start timer - This is the method called to initiate a timer");
            }
        Timer timer = new Timer();
        timer.schedule(new expired(), timeValue);
        }

    class Path
        {
        public float xLoc;
        public float yLoc;
        public float zLoc;
        public int nodeType;
        public int version;
        }

    class Robot
        {
        public  boolean     move;
        public  float       xLoc;
        public  float       yLoc;
        public  float       zLoc;
        public  boolean     playClip;
        public  String      clip;
        public  boolean     playAnimation;
        public  String      animationName;
        public  boolean     chatText;
        public  String      theChatText;
        public  boolean     chatFile;
        public  String      theChatFile;
        public  String      chatOrigin;
        public  boolean     ice;
        public  int         iceCode;
        public  String      iceMessage;
        public  int         version;
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
        public  String  code;
        public  String  payload;
        }

    class PresenceItem
        {
        public  float   x;
        public  float   y;
        public  float   z;
        public  String  name;
        public  BigInteger  clientID;
        }

    public void playPath(String thePath)
        {
        getMyAvatar();

        GameContext context = myAvatar.getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        helm.clearTasks();
        helm.setEnable(true);
        contentReadFile("paths/" + thePath, CONTENT_ROOT);
        for(Enumeration e = contentRead.elements(); e.hasMoreElements();)
            {
            String ele = (String)e.nextElement();

            String[] result = ele.split(":");
            Vector3f v3f = new Vector3f(Float.valueOf(result[0]), Float.valueOf(result[1]), Float.valueOf(result[2]));
            helm.addTaskToBottom(new GoTo(v3f, context));
            }
        }

    public int playRobotFrame()
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playRobotFrame - Frame = " + robotFrame + " of " + robotLast);
            }
        if(((Robot) robotList.get(robotFrame)).playClip)
            {
            if(traceLevel > 4)
                {
                System.out.println("play clip");
                }
            playSound(((Robot) robotList.get(robotFrame)).clip, 1900, 0);
            }
        if(((Robot) robotList.get(robotFrame)).chatText)
            {
            if(traceLevel > 4)
                {
                System.out.println("chat text");
                }
            sendChat(((Robot) robotList.get(robotFrame)).theChatText, ((Robot) robotList.get(robotFrame)).chatOrigin, proximityUserName);
            }
        if(((Robot) robotList.get(robotFrame)).chatFile)
            {
            if(traceLevel > 4)
                {
                System.out.println("chat file");
                }
            String thePath = "chats/" + cellName + "/" + ((Robot) robotList.get(robotFrame)).theChatFile;
            contentReadFile(thePath, CONTENT_ROOT);
            for(Enumeration e = contentRead.elements(); e.hasMoreElements();)
                {
                sendChat((String) e.nextElement(), ((Robot) robotList.get(robotFrame)).chatOrigin, proximityUserName);
                }
            }
        if(((Robot) robotList.get(robotFrame)).ice)
            {
            if(traceLevel > 4)
                {
                System.out.println("ice message");
                }
            postMessageEvent(((Robot) robotList.get(robotFrame)).iceMessage, ((Robot) robotList.get(robotFrame)).iceCode);
            }
        if(((Robot) robotList.get(robotFrame)).move)
            {
            if(traceLevel > 4)
                {
                System.out.println("Move");
                }
            executeAction("move", ((Robot) robotList.get(robotFrame)).xLoc,
                              ((Robot) robotList.get(robotFrame)).yLoc,
                              ((Robot) robotList.get(robotFrame)).zLoc);
            }
        else if (((Robot) robotList.get(robotFrame)).playAnimation)
            {
            if(traceLevel > 4)
                {
                System.out.println("play animation - animation = " + ((Robot) robotList.get(robotFrame)).animationName);
                }
            executeAction("runAnimation", ((Robot) robotList.get(robotFrame)).animationName, 0);
            }

        robotFrame++;
        if(robotFrame >= robotLast)
            {
            robotList.clear();
            robotLast = 0;
            return 0;
            }
        else
            {
            return 1;
            }
        }

    public int playAnimationFrame()
        {
        if(traceLevel > 3)
            {
            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - Frame = " + aniFrame + " of " + aniLast + " rest = " + ((Animation)aniList.get(aniFrame)).rest);
            }
        if(((Animation)aniList.get(aniFrame)).rest.equals("r"))
            {
// set rotation - absolute
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setRotation");
                }
            setRotation(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("q"))
            {
// set rotation - absolute
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call rotateObject");
                }
            rotateObject(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("R"))
            {
// set rotation - absolute
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setRotation - Notify");
                }
            setRotation(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("Q"))
            {
// set rotation - absolute
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call rotateObject - Notify");
                }
            rotateObject(((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("m"))
            {
// move object - relative move
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call moveObject");
                }
            moveObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("t"))
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setTranslation");
                }
//translate object - absolute move            
            setTranslation(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("M"))
            {
// move object - relative move
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call moveObject - Notify");
                }
            moveObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("T"))
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setTranslation - Notify");
//translate object - absolute move            
                }
            setTranslation(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("c"))
            {
// scale object - relative scale
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call scaleObject");
                }
            scaleObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("s"))
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setScale");
//scale object - absolute scale            
                }
            setScale(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                NO_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("C"))
            {
// scale object - relative scale
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call scaleObject - Notify");
                }
            scaleObject(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("S"))
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call setScale - Notify");
//scale object - absolute scale            
                }
            setScale(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                YES_NOTIFY);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("i"))
            {
            postMessageEvent(((Animation)aniList.get(aniFrame)).payload,
                Integer.parseInt( ((Animation)aniList.get(aniFrame)).code) );
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("I"))
            {
            postMessageEventToServer(((Animation)aniList.get(aniFrame)).payload,
                Integer.parseInt( ((Animation)aniList.get(aniFrame)).code) );
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("p"))
            {
            playSound(((Animation)aniList.get(aniFrame)).payload,
                Integer.parseInt( ((Animation)aniList.get(aniFrame)).code) ,
                0);
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("P"))
            {
            playSound(((Animation)aniList.get(aniFrame)).payload,
                Integer.parseInt( ((Animation)aniList.get(aniFrame)).code) ,
                1);
            }

        if(((Animation)aniList.get(aniFrame)).delay > 0)
            {
            if(traceLevel > 4)
                {
                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In playAnimationFrame - call mySleep ");
                }
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
        ScriptingComponentTransformMessage msg = new ScriptingComponentTransformMessage(cell.getCellID(), ScriptingComponentTransformMessage.TRANSLATE_TRANSFORM, translate);
        channelComp.send(msg);
//        cell.getComponent(MovableComponent.class).localMoveRequest(cell.getLocalTransform());
        }
    
    public void doNotifyRotate(Quaternion transform)
        {
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

    public void playRobot(String robotName, String npcName)
        {
        postMessageEvent("Test message", 400);
        }

    public void createRobot(String robotName, String npcName)
        {
        String newline = "\n";

        String script = null;
        script = "print('Hello from startup script for " + npcName + "\\r\\n');" + newline;
//        script = script + "MyClass.executeAction('selectAvatar', 'My_Avatar__5_1.xml', 1);" + newline;
        script = script + "MyClass.executeAction('selectAvatar', 'NewAvatar1_1.xml', 0);" + newline;
        script = script + "MyClass.setNpcAvatarName('" + npcName + "');" + newline;
        script = script + "MyClass.watchMessage(400);" + newline;
        contentWriteFile("scripts/" + npcName + "/", "startup.js", script, 1);

        script = "print('Hello from ice.js script for " + npcName + "\\r\\n');" + newline;
        script = script + "MyClass.buildRobot('" + robotName + "');" + newline;
        script = script + "MyClass.setNpcAvatarName('" + npcName + "');" + newline;
        script = script + "stateInt[0] = 0;" + newline;
        script = script + "stateInt[1] = 0;" + newline;
        script = script + "stateInt[2] = 0;" + newline;
        script = script + "stateInt[3] = 0;" + newline;
        script = script + "stateInt[4] = 1;" + newline;
        script = script + "MyClass.enableAvatarEvents();" + newline;
        script = script + "MyClass.playRobotFrame();" + newline;
        contentWriteFile("scripts/" + npcName + "/", "ice.js", script, 1);

        script = "print('Enter avatar.js script\\r\\n');" + newline;
        script = script + "print('Event type - ' + avatarEventType + '\\r\\n');" + newline;
        script = script + "print('Event source - ' + avatarEventSource + '\\r\\n');" + newline;
        script = script + "print('Event animation - ' + avatarEventAnimation + '\\r\\n');" + newline;
        script = script + "if(stateInt[0] == 0)" + newline;
        script = script + "    {" + newline;
        script = script + "    if(avatarEventType == 'STARTED')" + newline;
        script = script + "        {" + newline;
        script = script + "        if(avatarEventAnimation == 'Walk')" + newline;
        script = script + "            {" + newline;
        script = script + "            stateInt[0] = 1;" + newline;
        script = script + "            stateInt[1] = 0;" + newline;
        script = script + "            stateInt[2] = 1;" + newline;
        script = script + "            stateInt[3] = 0;" + newline;
        script = script + "            }" + newline;
        script = script + "        if(avatarEventAnimation == 'Male_PublicSpeaking' || avatarEventAnimation == 'Female_PublicSpeaking')" + newline;
        script = script + "            {" + newline;
        script = script + "            stateInt[0] = 1;" + newline;
        script = script + "            stateInt[1] = 0;" + newline;
        script = script + "            stateInt[2] = 1;" + newline;
        script = script + "            stateInt[3] = 1;" + newline;
        script = script + "            }" + newline;
        script = script + "        }" + newline;
        script = script + "    }" + newline;
        script = script + "else" + newline;
        script = script + "    {" + newline;
        script = script + "    if(avatarEventType == 'STOPPED')" + newline;
        script = script + "        {" + newline;
        script = script + "        if(avatarEventAnimation == null)" + newline;
        script = script + "            {" + newline;
        script = script + "            stateInt[3]--;" + newline;
        script = script + "            }" + newline;
        script = script + "        else" + newline;
        script = script + "            {" + newline;
        script = script + "            stateInt[2]--;" + newline;
        script = script + "            }" + newline;
        script = script + "        }" + newline;
        script = script + "    if(stateInt[1] == 0 && stateInt[2] == 0 && stateInt[3] == 0)" + newline;
        script = script + "        {" + newline;
        script = script + "        stateInt[0] = 0;" + newline;
        script = script + "        print('Playing next frame\\r\\n');" + newline;
        script = script + "        if(stateInt[4] == 1)" + newline;
        script = script + "            stateInt[4] = MyClass.playRobotFrame();" + newline;
        script = script + "        }" + newline;
        script = script + "    }" + newline;
        contentWriteFile("scripts/" + npcName + "/", "avatar.js", script, 1);

        createCellInstance("org.jdesktop.wonderland.modules.npc.common.NpcCellServerState", 1.0f, 0.0f, 1.0f, "rupert");
        }

    public void createCellInstance(String className, float x, float y, float z, String cellName)
        {
        if(traceLevel > 0)
            {
            System.out.println("Enter createCellInstance");
            }
        ScriptingComponentCellCreateMessage msg =
                new ScriptingComponentCellCreateMessage(className, x, y, z, cellName);
        channelComp.send(msg);
        }

    public void createSitTargetList(String[] sitNameList)
        {
        sitTargetGroup.clear();

        for (Cell aCell : cell.getCellCache().getRootCells())
            {
            for(int i = 0; i < sitNameList.length; i++)
                {
                if(aCell.getName().equals(sitNameList[i]))
                    {
//                    if(aCell instanceof SittingChair)
//                        {
//                        sitTargetGroup.add((ChairObject)aCell);
//                        }


                    }
                }
            }
        }

    public void deleteCellInstance(String cellName)
        {
        for (Cell aCell : cell.getCellCache().getRootCells())
            {
            if(cellName.equals(aCell.getName()))
                {
                System.out.println("Delete cell - found a match");
                CellUtils.deleteCell(aCell);
                }
            }
        }

    public void teleportAvatar(float X, float Y, float Z)
        {
        Vector3f v3f = new Vector3f(X, Y, Z);
        Quaternion quat = new Quaternion();
        try
            {
            ClientContextJME.getClientMain().gotoLocation(null, v3f, quat);
            }
        catch (IOException ex)
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void teleportAvatar(float X, float Y, float Z, float rX, float rY, float rZ, float W)
        {
        Vector3f v3f = new Vector3f(X, Y, Z);
        Quaternion quat = new Quaternion();
        quat.fromAngleNormalAxis(W , new Vector3f(rX, rY, rZ));

        try
            {
            ClientContextJME.getClientMain().gotoLocation(null, v3f, quat);
            }
        catch (IOException ex)
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void teleportAvatar(float X, float Y, float Z, float angleX, float angleY, float angleZ)
        {
        Vector3f v3f = new Vector3f(X, Y, Z);
        Quaternion quat = new Quaternion();
        quat.fromAngles((float) ((Math.PI / 180) * angleX), (float) ((Math.PI / 180) * angleY), (float) ((Math.PI / 180) * angleZ));

        try
            {
            ClientContextJME.getClientMain().gotoLocation(null, v3f, quat);
            }
        catch (IOException ex)
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void teleportAvatar(String url, float X, float Y, float Z)
        {
        Vector3f v3f = new Vector3f(X, Y, Z);
        Quaternion quat = new Quaternion();
        try
            {
            ClientContextJME.getClientMain().gotoLocation(url, v3f, quat);
            }
        catch (IOException ex)
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void teleportAvatar(String url, float X, float Y, float Z, float rX, float rY, float rZ, float W)
        {
        Vector3f v3f = new Vector3f(X, Y, Z);
        Quaternion quat = new Quaternion();
        quat.fromAngleNormalAxis(W , new Vector3f(rX, rY, rZ));

        try
            {
            ClientContextJME.getClientMain().gotoLocation(url, v3f, quat);
            }
        catch (IOException ex)
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void teleportAvatar(String url, float X, float Y, float Z, float angleX, float angleY, float angleZ)
        {
        Vector3f v3f = new Vector3f(X, Y, Z);
        Quaternion quat = new Quaternion();
        quat.fromAngles((float) ((Math.PI / 180) * angleX), (float) ((Math.PI / 180) * angleY), (float) ((Math.PI / 180) * angleZ));

        try
            {
            ClientContextJME.getClientMain().gotoLocation(url, v3f, quat);
            }
        catch (IOException ex)
            {
            Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void fillDefaults(String type, String cellName, boolean global, int fillValue)
        {
        String[] baseEventNames = new String[totalEvents];

        baseEventNames[MOUSE1_EVENT] = "mouse1";
        baseEventNames[MOUSE2_EVENT] = "mouse2";
        baseEventNames[MOUSE3_EVENT] = "mouse3";
        baseEventNames[MOUSE1S_EVENT] = "mouse1s";
        baseEventNames[MOUSE2S_EVENT] = "mouse2s";
        baseEventNames[MOUSE3S_EVENT] = "mouse3s";
        baseEventNames[MOUSE1C_EVENT] = "mouse1c";
        baseEventNames[MOUSE2C_EVENT] = "mouse2c";
        baseEventNames[MOUSE3C_EVENT] = "mouse3c";
        baseEventNames[MOUSE1A_EVENT] = "mouse1a";
        baseEventNames[MOUSE2A_EVENT] = "mouse2a";
        baseEventNames[MOUSE3A_EVENT] = "mouse3a";
        baseEventNames[TIMER_EVENT] = "timer";
        baseEventNames[STARTUP_EVENT] = "startup";
        baseEventNames[PROXIMITY_EVENT] = "prox";
        baseEventNames[MESSAGE1_EVENT] = "message1";
        baseEventNames[MESSAGE2_EVENT] = "message2";
        baseEventNames[MESSAGE3_EVENT] = "message3";
        baseEventNames[MESSAGE4_EVENT] = "message4";
        baseEventNames[INTERCELL_EVENT] = "ice";
        baseEventNames[CHAT_EVENT] = "chat";
        baseEventNames[PRESENCE_EVENT] = "presence";
        baseEventNames[CONTROLLER_EVENT] = "controller";
        baseEventNames[PROPERTIES_EVENT] = "properties";
        baseEventNames[AVATAR_EVENT] = "avatar";
        System.out.println("fillValue = " + fillValue);

        BufferedReader br = null;
        String myString = null;

        if(type.equals("javascript"))
            {
            for(int i = 0; i < totalEvents; i++)
                {
                switch (fillValue)
                    {
                    case 0:
                        {
                        br = new BufferedReader(new InputStreamReader(theCell.getClass().getResourceAsStream("resources/" + "scriptJs" + i)));
                        break;
                        }
                    case 1:
                        {
                        br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("resources/" + "scriptJs" + i)));
                        break;
                        }
                    }

                String temp = "";
                try
                    {
                    while ((myString = br.readLine()) != null)
                        {
                        temp = temp + myString + "\r\n";
                        }
                    }
                catch (IOException ex)
                    {
                    Logger.getLogger(ScriptingComponent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                System.out.println("default script " + i + " read -> ");
                System.out.println(temp);
                contentWriteFile("scripts/" + cellName + "/", baseEventNames[i] + ".js", temp, 1);
                }
            }
        else if(type.equals("php"))
            {

            }
        else if(type.equals("jython"))
            {

            }
        else if(type.equals("jruby"))
            {
            }
        else if(type.equals("sleep"))
            {

            }
        }

    class KeyEventListener extends EventClassListener
        {
        @Override
        public Class[] eventClassesToConsume()
            {
            return new Class[]{KeyEvent3D.class};
            }
        @Override
        public void computeEvent(Event event)
            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In computeEvent for key event");
            }
        @Override
        public void commitEvent(Event event)
            {

            KeyEvent key = (KeyEvent) ((KeyEvent3D)event).getAwtEvent();
            if(key.getID() == KeyEvent.KEY_PRESSED)
                {
//                System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In commitEvent for key event - key = " + key.getKeyChar());

                switch(key.getKeyChar())
                    {
                    case 'p':
                        {
                        executeScript(PROPERTIES_EVENT, null);
                        break;
                        }
                    }
                }
            }
        }
    class MouseEventListener extends EventClassListener
        {
        @Override
        public Class[] eventClassesToConsume()
            {
            return new Class[]{MouseButtonEvent3D.class};
            }

        @Override
        public void commitEvent(Event event)
            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In commitEvent for mouse event");
            }

        @Override
        public void computeEvent(Event event)
            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In commitEvent for mouse event");
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
            return new Class[] { IntercellEvent.class, AvatarAnimationEvent.class };
            }

@Override
        public void computeEvent(Event event)
            {
            if(event instanceof IntercellEvent)
                {
//                if(!iceEventInFlight)
//                    {
                    iceEventInFlight = true;
                    IntercellEvent ice = (IntercellEvent)event;
                    if(watchMessages.contains(new Float(ice.getCode())))
                        {
                        if(traceLevel > 4)
                            {
                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In Intercell listener in commitEvent - payload = " + ice.getPayload() + " Code = " + ice.getCode());
                            }
                        ICEEventCode = ice.getCode();
                        ICEEventMessage = ice.getPayload();
                        executeScript(INTERCELL_EVENT, null);
                        }
                    else
                        {
                        if(traceLevel > 4)
                            {
                            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In Intercell listener in commitEvent - Code not in list - payload = " + ice.getPayload() + " Code = " + ice.getCode());
                            }
                        }
                    iceEventInFlight = false;
//                    }
//                else
//                    {
//                    System.out.println("ICE event in flight");
//                    }
                }
            else if (event instanceof AvatarAnimationEvent)
                {
                if(traceLevel > 5)
                    {
                    System.out.println("************* In computeEvent for AvatarAnimationListener " + event);
                    }
                if(avatarEventEnable == 1)
                    {
                    AvatarAnimationEvent aee = (AvatarAnimationEvent)event;
                    if(aee.getSource().getName().equals(npcAvatarName))
                        {
                        if(traceLevel > 5)
                            {
                            System.out.println("Avatar name = " + aee.getSource().getName());
                            System.out.println("Event - type = " + aee.getType() + " - avatar = " + aee.getSource().getName() + " - animation = " + aee.getAnimationName());
                            }
                        avatarEventType = aee.getType().name();
                        avatarEventSource = aee.getSource().getName();
                        avatarEventAnimation = aee.getAnimationName();
                        executeScript(AVATAR_EVENT, null);
                        }
                    }
                }
            }
        }
        class MenuItemListener implements ContextMenuActionListener
        {
        public void actionPerformed(ContextMenuItemEvent event)
            {
            executeScript(PROPERTIES_EVENT, null);
            }
        }
    }
