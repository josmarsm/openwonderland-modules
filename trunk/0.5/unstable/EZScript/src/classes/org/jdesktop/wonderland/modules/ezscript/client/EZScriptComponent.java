
package org.jdesktop.wonderland.modules.ezscript.client;


import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.jme.bounding.BoundingVolume;
//import com.jme.entity.Entity;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.GeometricUpdateListener;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import imi.scene.PTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.jdesktop.mtgame.AwtEventCondition;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;

import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.FarCellEventSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.FarCellEvent;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.common.FarCellEventMessage;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;


/**
 * Client-side scripting cell component
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author JagWire
 */

/**
 * This component allows scripts to write callbacks for various cell related
 * events. When the event occurs, they are processed through the callback
 * listener classes defined at the bottom of the file. The listeners send
 * messages over the shared state component to sync across all instances of this
 * particular cell. Upon receipt of the shared state component's message, the
 * Runnable callbacks that were defined in the script will get executed.
 *
 * Callbacks must be enabled before trying to use them.
 * @author JagWire
 */
public class EZScriptComponent extends CellComponent implements GeometricUpdateListener {

    private ScriptEngineManager engineManager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
    private ScriptEngine scriptEngine = null;
    private Bindings scriptBindings = null;
    private ScriptEditorPanel panel = null;
    private String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
                                 "j", "k", "l", "m", "n", "o", "p", "q", "r",
                                 "s", "t", "u", "v", "w", "x", "y", "z", " "};
    //for testing only... not currently used...
    private final String script = "function clickRun() { print(\"hello!\"); }"
                                + "function enterRun() { print(\"hello mouse enter!\"); }"
                                + "function exitRun() { print(\"hello mouse exit!\"); }"
                                + "function approachRun() { print(\"hello approach!\"); }"
                                + "function leaveRun() { print(\"hello leave!\");}"
                                + "ScriptContext.enableMouseEvents();"
                                + "ScriptContext.enableProximityEvents();"
                                + "ScriptContext.onClick(clickRun);"
                                + "ScriptContext.onMouseEnter(enterRun);"
                                + "ScriptContext.onMouseExit(exitRun);"
                                + "ScriptContext.onApproach(approachRun);"
                                + "ScriptContext.onLeave(leaveRun);"
                                + "var name = cell.getClass().getName();"
                                + "print(name);";


    private static Logger logger = Logger.getLogger(EZScriptComponent.class.getName());
    private BasicRenderer renderer = null;
    
    //callback containers
    // - these containers hold runnable objects that will get executed
    //   on it's respective event.
    // - it's important to note that one can not pick and choose which runnables
    //   per container get executed, all of a container's runnables will be
    //   executed per event.
    private List<Runnable> callbacksOnClick;        //mouse click
    private List<Runnable> callbacksOnLoad;         //cell load
    private List<Runnable> callbacksOnUnload;       //cell unload
    private List<Runnable> callbacksOnMouseEnter;   //mouse enter
    private List<Runnable> callbacksOnMouseExit;    //mouse exit
    private List<Runnable> callbacksOnApproach;     //avatar approach
    private List<Runnable> callbacksOnLeave;        //avatar leave
    private Map<String, List<Runnable>> callbacksOnKeyPress; // keypress

    //Functions to be run from remote cells to alter this particular cell
    //only one runnable per name, no overloading supported as of yet...
    private Map<String, FarCellEventSPI> farCellEvents;
    
    //event listeners
    private MouseEventListener mouseEventListener;
    private KeyboardEventListener keyEventListener;

    //sharedstate variables
    @UsesCellComponent
    private SharedStateComponent sharedStateComponent;
    private SharedMapCli callbacksMap; // used in syncing callbacks across clients
    private SharedMapCli scriptsMap;// used for persisting scripts and client sync
   
    private SharedMapListener mapListener;

    //proximity variables
    @UsesCellComponent
    private ProximityComponent proximityComponent;
    private ProximityListenerImpl proximityListener;

    //ContextMenu variables
    @UsesCellComponent
    private ContextMenuComponent contextMenuComponent;
    private ContextMenuFactorySPI menuFactory;
    private MenuItemListener menuListener;

    //ChannelComponent variables
    @UsesCellComponent
    private ChannelComponent channelComponent;

    //state variables
    private boolean mouseEventsEnabled = false;
    private boolean proximityEventsEnabled = false;
    private boolean keyEventsEnabled = false;

    public EZScriptComponent(Cell cell) {
        super(cell);
        
        //initialize callback containers
        callbacksOnClick = new ArrayList<Runnable>();
        callbacksOnLoad = new ArrayList<Runnable>();
        callbacksOnUnload = new ArrayList<Runnable>();
        callbacksOnMouseEnter = new ArrayList<Runnable>();
        callbacksOnMouseExit = new ArrayList<Runnable>();
        callbacksOnApproach = new ArrayList<Runnable>();
        callbacksOnLeave = new ArrayList<Runnable>();
        
        //initialize keypress map
        callbacksOnKeyPress = new HashMap<String, List<Runnable>>();
        for(String letter: alphabet) {
            //for each letter in the alphabet, add an entry in the hashmap
            List<Runnable> l = new ArrayList<Runnable>();
            callbacksOnKeyPress.put(letter, l);
        }

        farCellEvents = new HashMap<String, FarCellEventSPI>();

        //intialize listeners
        mouseEventListener = new MouseEventListener();
        keyEventListener = new KeyboardEventListener();
        mapListener = new SharedMapListener();
        proximityListener = new ProximityListenerImpl();

        scriptEngine = engineManager.getEngineByName("JavaScript");
        cell.getClass().getName();
        scriptBindings = scriptEngine.createBindings();
        //scriptBindings.put("cell", cell);
        
        //scriptBindings.put("ScriptContext", this);
        //scriptEngine.setBindings(scriptBindings, ScriptContext.ENGINE_SCOPE);


        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        Iterator<ScriptMethodSPI> iter = loader.getInstances(ScriptMethod.class,
                                                        ScriptMethodSPI.class);
        //grab all global void methods
        while(iter.hasNext()) {
            this.addFunctionBinding(iter.next());
        }

        //grab all returnables
        Iterator<ReturnableScriptMethodSPI> returnables = loader.getInstances(ReturnableScriptMethod.class, ReturnableScriptMethodSPI.class);
        while(iter.hasNext()) {
            this.addFunctionBinding(returnables.next());
        }

        //grab all events
        Iterator<FarCellEventSPI> eventIter = loader.getInstances(FarCellEvent.class,
                                                             FarCellEventSPI.class);
        while(eventIter.hasNext()) {
            FarCellEventSPI spi = eventIter.next();
            if(spi.getCellClassName().equals(cell.getClass().getName())) {
                if(!farCellEvents.containsKey(spi.getEventName())) {
                    farCellEvents.put(spi.getEventName(), spi);
                }
                else {
                    logger.info("Cell already has event defined: "
                                                           +spi.getEventName());
                }
            }
            else {
                logger.finest("This event is not for our cell: "
                                                           +spi.getEventName());
            }
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch(status) {
            case RENDERING:
                    if (increasing) {
                        renderer = (BasicRenderer) cell.getCellRenderer(RendererType.RENDERER_JME);
                        if (mouseEventsEnabled) {
                            mouseEventListener = new MouseEventListener();
                            mouseEventListener.addToEntity(renderer.getEntity());
                        }
                        if(keyEventsEnabled) {
                            keyEventListener = new KeyboardEventListener();
                            keyEventListener.addToEntity(renderer.getEntity());
                        }
                        if (menuFactory == null) {
                            menuListener = new MenuItemListener();
                            menuFactory = new ContextMenuFactorySPI() {
                                public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                                    return new ContextMenuItem[] {
                                        new SimpleContextMenuItem("Script", menuListener)
                                    };
                                }
                            };
                            contextMenuComponent.addContextMenuFactory(menuFactory);
                        }
                    }
               
                break;
            case ACTIVE:
                if(increasing) {
                    channelComponent.addMessageReceiver(FarCellEventMessage.class,
                                                        new FarCellEventReceiver());
                    //intialize shared state component and map
                    scriptBindings.put("ScriptContext", this);
                    scriptBindings.put("cell", this.cell);
                    new Thread(new Runnable() {
                        public void run() {
                            //grab the "callbacks" map in order to hopefully
                            //use an additional map for "state" if needed
                           callbacksMap = sharedStateComponent.get("callbacks");
                           scriptsMap = sharedStateComponent.get("scripts");
                           callbacksMap.addSharedMapListener(mapListener);
                           callbacksMap.addSharedMapListener(mapListener);
                           //don't grab any persisted scripts just yet.

                           //get other maps here.
                        }
                    }).start();


                }
                break;
            case INACTIVE:
                if(!increasing) {
                    if(menuFactory != null) {
                        contextMenuComponent.removeContextMenuFactory(menuFactory);
                        menuFactory = null;
                    }
                }
                break;
            case DISK:
                if(!increasing) {
                    if(mouseEventListener != null) {
                        mouseEventListener.removeFromEntity(renderer.getEntity());
                        mouseEventListener = null;
                    }

                    if(keyEventListener != null) {
                        keyEventListener.removeFromEntity(renderer.getEntity());
                        keyEventListener = null;
                    }                        

                    renderer = null;
                }
        }
    }

    public void enableMouseEvents() {
        if(mouseEventListener == null) {
            mouseEventListener = new MouseEventListener();
        }

        if(!mouseEventListener.isListeningForEntity(renderer.getEntity())) {
            mouseEventListener.addToEntity(renderer.getEntity());
            mouseEventsEnabled = true;
        }
    }
    public MouseEventListener getMouseEventListener() {
        return this.mouseEventListener;
    }
    public boolean getMouseEventsEnabled() {
        return mouseEventsEnabled;
    }

    public void disableMouseEvents() {
        if(mouseEventListener != null) {
            mouseEventListener.removeFromEntity(renderer.getEntity());
        }
        mouseEventListener = null;
        mouseEventsEnabled = false;
    }

    public void enableKeyEvents() {
        if(keyEventListener == null) {
            keyEventListener = new KeyboardEventListener();
        }
        if(!keyEventListener.isListeningForEntity(renderer.getEntity())) {
            keyEventListener.addToEntity(renderer.getEntity());
            keyEventsEnabled = true;
        }
    }

    public void disableKeyEvents() {
        if(keyEventListener != null) {
            keyEventListener.removeFromEntity(renderer.getEntity());
        }
        keyEventListener = null;
        keyEventsEnabled = false;
    }
    public void enableProximityEvents() {
        if(proximityListener == null) {
            proximityListener = new ProximityListenerImpl();
        }
        proximityComponent.addProximityListener(proximityListener,
                                                new BoundingVolume[] {
                                                    cell.getLocalBounds()
                                                });
        proximityEventsEnabled = true;
    }

    public void disableProximityEvents() {
        proximityComponent.removeProximityListener(proximityListener);
        proximityEventsEnabled = false;
    }

    public void onClick(Runnable r) {
        callbacksOnClick.add(r);
    }

    public void onMouseEnter(Runnable r) {
        callbacksOnMouseEnter.add(r);
    }

    public void onMouseExit(Runnable r) {
        callbacksOnMouseExit.add(r);
    }

    public void onLoad(Runnable r) {
        callbacksOnLoad.add(r);
    }

    public void onUnload(Runnable r) {
        callbacksOnUnload.add(r);
    }

    public void onApproach(Runnable r) {
        callbacksOnApproach.add(r);
    }

    public void onLeave(Runnable r) {
        callbacksOnLeave.add(r);
    }

    public void onKeyPress(String key, Runnable r) {
        List<Runnable> list = callbacksOnKeyPress.get(key);
        if(list == null) {
            list = new ArrayList();
            //list.add(r);
            
        }
        list.add(r);
        callbacksOnKeyPress.put(key, list);
    }

    public void executeOnClick() {
        for(Runnable r: callbacksOnClick) {
            r.run();
        }
    }

    public void executeOnMouseEnter() {
        for(Runnable r: callbacksOnMouseEnter) {
            r.run();
        }
    }

    public void executeOnMouseExit() {
        for(Runnable r: callbacksOnMouseExit) {
            r.run();
        }
    }

    public void executeOnLoad() {
        for(Runnable r: callbacksOnLoad) {
            r.run();
        }
    }

    public void executeOnUnload() {
        for(Runnable r: callbacksOnUnload) {
            r.run();
        }
    }

    public void executeOnApproach() {
        for(Runnable r: callbacksOnApproach) {
            r.run();
        }
    }

    public void executeOnLeave() {
        for(Runnable r: callbacksOnLeave) {
            r.run();
        }
    }

    public void executeOnKeyPress(String key) {
        List<Runnable> rs = callbacksOnKeyPress.get(key);
        if(rs == null) {
            return;
        }
        for(Runnable r: rs) {
            r.run();
        }
    }

    public void clearCallbacks() {
        callbacksOnClick.clear();
        callbacksOnLoad.clear();
        callbacksOnUnload.clear();
        callbacksOnMouseEnter.clear();
        callbacksOnMouseExit.clear();
        callbacksOnApproach.clear();
        callbacksOnLeave.clear();

        //to be thorough...
        for(List l : callbacksOnKeyPress.values()) {
            l.clear();
        }
        callbacksOnKeyPress.clear();
    }

    public void evaluateScript(String script) {

        try {
            scriptEngine.eval(script, scriptBindings);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public SharedMapCli getScriptMap() {
        return this.scriptsMap;
    }
    private EZScriptComponent getLocalInstance() {
        return this;
    }

   public class MouseEventListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class,
                                MouseEnterExitEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            if(event instanceof MouseButtonEvent3D) {
                //MouseButtonEvent3D m = (MouseButtonEvent3D)event;
                MouseButtonEvent3D m = (MouseButtonEvent3D)event;

                if(m.isClicked())
                    callbacksMap.put("onClick", new SharedString());
            }
            else if(event instanceof MouseEnterExitEvent3D) {
                MouseEnterExitEvent3D m = (MouseEnterExitEvent3D)event;
                if(m.isEnter()) {
                    callbacksMap.put("onMouseEnter", new SharedString());
                } else {
                    callbacksMap.put("onMouseExit", new SharedString());
                }
            }

        }
    }

    class KeyboardEventListener extends EventClassListener {
       @Override
       public Class[] eventClassesToConsume() {
           return new Class[] { KeyEvent3D.class };
       }

       @Override
       public void commitEvent(Event event) {
           if(event instanceof KeyEvent3D) {
               KeyEvent3D e = (KeyEvent3D)event;
               if(e.isPressed()) {
                   callbacksMap.put("onKeyPress", SharedString.valueOf(
                                                    Character.toString(
                                                        e.getKeyChar())));
               }

           }
       }
   }

    class SharedMapListener implements SharedMapListenerCli {
        public void propertyChanged(SharedMapEventCli event) {
            String property = event.getPropertyName();
            if(property.equals("onClick")) {
                executeOnClick();
            } else if(property.equals("onMouseEnter")) {
                executeOnMouseEnter();
            } else if(property.equals("onMouseExit")) {
                executeOnMouseExit();
            } else if(property.equals("onApproach")) {
                executeOnApproach();
            } else if(property.equals("onLeave")) {
                executeOnLeave();
            } else if(property.equals("onKeyPress")) {                
                executeOnKeyPress(event.getNewValue().toString());
            } else if(property.equals("editor")) {
                SharedString script = (SharedString)event.getNewValue();
                try {
                    //execute script typed in Scripting Editor
                    scriptEngine.eval(script.getValue(), scriptBindings);
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class ProximityListenerImpl implements ProximityListener {

        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
            if(entered) {
                callbacksMap.put("onApproach", new SharedString());
            }
            else {
                callbacksMap.put("onLeave", new SharedString());
            }
        }
   }

    class MenuItemListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            if(event.getContextMenuItem().getLabel().equals("Script")) {
                System.out.println("[iSocial] menu item fired!");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //1. Create the frame.
                        JDialog dialog = new JDialog();
                        //dialog.setName("Script Editor");
                        dialog.setTitle("Script Editor - " + cell.getName());
                        //2. Optional: What happens when the frame closes?
                        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

                        //3. Create component and put them in the frame.

                        dialog.setContentPane(new ScriptEditorPanel(getLocalInstance(), dialog));

                        //4. Size the frame.
                        dialog.pack();

                        //5. Show it.
                        dialog.setVisible(true);       
                    }
                });

            }
        }
    }

    class FarCellEventReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            if(message instanceof FarCellEventMessage) {
                FarCellEventMessage fcem = (FarCellEventMessage)message;
                String name = fcem.getEventName();

                if(farCellEvents.containsKey(name)) {
                    farCellEvents.get(name).setArguments(fcem.getArguments());
                    farCellEvents.get(name).run();
                } else {
                    logger.warning("Received an event request with no associated event: "+fcem.getEventName());
                }
            }
        }

    }

    public void addFunctionBinding(ScriptMethodSPI method) {
        scriptBindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            +"\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\targs[i] = arguments[i];\n"
            + "\t}\n"

           // + "\targs = "+method.getFunctionName()+".arguments;\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"
            +"}";

        try {
            System.out.println("evaluating script: \n"+scriptx);
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void addFunctionBinding(ReturnableScriptMethodSPI method) {
        scriptBindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            +"\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\targs[i] = arguments[i];\n"
            + "\t}\n"

           // + "\targs = "+method.getFunctionName()+".arguments;\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"
            + "\treturn this"+method.getFunctionName()+".return();\n"
            +"}";

        try {
            System.out.println("evaluating script: \n"+scriptx);
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void fireFarCellEvent(CellID cellID, String label, Object[] args) {
        channelComponent.send(new FarCellEventMessage(cellID, label, args));
    }

    /**
     *  EXPERIMENTAL - THIS WILL PROBABLY BE REMOVED FROM THIS CLASS IN THE NEAR
     *  FUTURE!
     */
    private CollisionConfiguration collisionConfiguration;
    private CollisionDispatcher collisionDispatcher;
    private AxisSweep3 overlappingPairCache;
    private SequentialImpulseConstraintSolver solver;
    private DiscreteDynamicsWorld world;
    private ObjectArrayList collisionShapes;
    private Map<CollisionObject, Node> bodiesToNodes;
    private ZBufferState zbuf;
    private PhysicsProcessor proc;
    private Queue<RigidBody> potentialBodies;
    private Node root;
    private RigidBody avatarBody;
    private Node avatarRoot;
    private WlAvatarCharacter avatarCharacter;
    
    public void enablePhysics() {
        potentialBodies = new LinkedList<RigidBody>();
        collisionConfiguration = new DefaultCollisionConfiguration();
        collisionDispatcher = new CollisionDispatcher(collisionConfiguration);
        collisionShapes = new ObjectArrayList();
        Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
        Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
        int maxProxies = 1024;

        overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
        solver = new SequentialImpulseConstraintSolver();

        world = new DiscreteDynamicsWorld(collisionDispatcher,
                                          overlappingPairCache,
                                          solver,
                                          collisionConfiguration);

        world.setGravity(new Vector3f(0, -2, 0));
        bodiesToNodes = new HashMap<CollisionObject, Node>();
        //TESTING
        float[] args = { 50, 0, 50};
        addRigidBody("Ground",  args);
        args[0] = 0.5f;
        //addRigidBody("Sphere",args);

        //avatar stuff - also experimental
        AvatarImiJME avatarRenderer = (AvatarImiJME)ClientContextJME.getViewManager().getPrimaryViewCell().getCellRenderer(RendererType.RENDERER_JME);
         avatarCharacter = avatarRenderer.getAvatarCharacter();
        avatarRoot = avatarCharacter.getJScene().getExternalKidsRoot();

        CollisionShape avatarShape = new BoxShape(new Vector3f(1, 1, 0.25f));
        collisionShapes.add(avatarShape);
        Transform avatarTransform = new Transform();
        avatarTransform.setIdentity();
        com.jme.math.Vector3f avatarPosition = avatarRoot.getLocalTranslation();
        avatarTransform.origin.set(avatarPosition.x,
                                   avatarPosition.y,
                                   avatarPosition.z);
        float mass = 10.0f;
        Vector3f localInertia = new Vector3f(0, 0 ,0);
        avatarShape.calculateLocalInertia(mass, localInertia);
        DefaultMotionState avatarMotionState = new DefaultMotionState(avatarTransform);
        RigidBodyConstructionInfo rigidAvatarInfo
                = new RigidBodyConstructionInfo(mass,
                                                avatarMotionState,
                                                avatarShape,
                                                localInertia);

        avatarBody =  new RigidBody(rigidAvatarInfo);
        avatarBody.setRestitution(0);
        avatarBody.setFriction(0);

        world.addRigidBody(avatarBody);
        bodiesToNodes.put(avatarBody, avatarRoot);
        avatarRoot.addGeometricUpdateListener(this);



    }

    public void geometricDataChanged(Spatial spatial) {
        com.jme.math.Vector3f trans = spatial.getWorldTranslation();
        com.jme.math.Quaternion rotation = spatial.getWorldRotation();
        synchronized(world) {
            Transform t = new Transform();
            t.setIdentity();
            t.origin.x = trans.x;
            t.origin.y = trans.y;
            t.origin.z = trans.z;
            t.setRotation(new Quat4f(rotation.x,
                                     rotation.y,
                                     rotation.z,
                                     rotation.w));//f2, f3));
            avatarBody.setWorldTransform(t);

            //avatarBody.setWorldTransform(t);

        }
    }
    public void jump() {
        synchronized(world) {
            System.out.println("JUMP!");
            Vector3f up = new Vector3f(0, 1, 0);
            up.normalize();
            up.scale(20);

            avatarBody.applyCentralImpulse(up);
        }
    }
    public void fireSphere(final float radius) {


        Entity e = proc.getEntity();
        synchronized(world) {
            float[] array = { radius };
            root.attachChild(addRigidBody("Sphere", array));
        }
    }
    
    public Node addRigidBody(String shape, float[] floats) {
        if(shape.equals("Sphere")) {
            //create the physics shape
            CollisionShape ballShape = new SphereShape(floats[0]);
            //create the material
            MaterialState matState = (MaterialState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
            matState.setDiffuse(ColorRGBA.blue);
            //create the JME node
            Node ballNode = new Node();
            ballNode.attachChild(new Sphere("sphere", 10, 10, floats[0]));
            ballNode.setRenderState(matState);
            collisionShapes.add(ballShape);

            //create Transform
            Transform ballTransform = new Transform();
            ballTransform.setIdentity();

            com.jme.math.Vector3f lookDirection = new com.jme.math.Vector3f();
            ClientContextJME.getViewManager().getCameraLookDirection(lookDirection);
            CellTransform transform = ClientContextJME.getViewManager().getPrimaryViewCell().getLocalTransform();
            com.jme.math.Vector3f avatarVector = transform.getTranslation(null);
            ballTransform.origin.set(new Vector3f(avatarVector.x,
                                                  avatarVector.y,
                                                  avatarVector.z));
            ballNode.setLocalTranslation(avatarVector.x,
                                         avatarVector.y+2f,
                                         avatarVector.z);
            float mass = 10.0f;
            //boolean isDynamic = true;
            Vector3f localInertia = new Vector3f(0, 0, 0);
            ballShape.calculateLocalInertia(mass, localInertia);
            DefaultMotionState ballMotionState = new DefaultMotionState(ballTransform);
            RigidBodyConstructionInfo rigidBallInfo = new RigidBodyConstructionInfo(mass,
                                                                                    ballMotionState,
                                                                                    ballShape,
                                                                                    localInertia);
            Vector3f impulse = new Vector3f(lookDirection.x, lookDirection.y, lookDirection.z);
            Vector3f velocity = impulse;
            impulse.normalize();
            impulse.scale(3);
            velocity.scale(8);
            RigidBody ballBody = new RigidBody(rigidBallInfo);
            ballBody.setRestitution(1);


            ballBody.applyCentralImpulse(impulse);
            ballBody.setLinearVelocity(velocity);
            
                //world.addRigidBody(ballBody);
            potentialBodies.add(ballBody);
            bodiesToNodes.put(ballBody, ballNode);
            return ballNode;

        } else if(shape.equals("Ground")) {
            //create the physics shape
            CollisionShape groundShape = new BoxShape(new Vector3f(floats[0], floats[1], floats[2]));

            //create the material so we can see it in wonderland
            MaterialState matState = (MaterialState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
            matState.setDiffuse(new ColorRGBA(0, 1, 0, 1));
            //create the JME scenegraph node
            Node groundNode = new Node();
            groundNode.attachChild(new Quad("Floor", floats[0], floats[2]));
            groundNode.setRenderState(matState);

            //add the physics shape to the physics collection
            collisionShapes.add(groundShape);

            //create the transform for the rigid body
            Transform groundTransform = new Transform();
            groundTransform.setIdentity();
            groundTransform.origin.set(new Vector3f(0.f, -3.0f, 0.f));

            //apply the same transforms to the JME scene as to the physics scene
            groundNode.setLocalTranslation(0, -1.0f, 0);
            Quaternion pitch90 = new Quaternion();
            pitch90.fromAngleAxis(-1*FastMath.PI/2, new com.jme.math.Vector3f(1, 0, 0));
            groundNode.setLocalRotation(pitch90);

            //floor has a mass of 0 so it doesn't move during collisions.
            float mass = 0f;
            boolean isDynamic = (mass != 0f);

            Vector3f localInertia = new Vector3f(0, 0, 0);
            if (isDynamic) {
                    groundShape.calculateLocalInertia(mass, localInertia);
            }

            // using motionstate is recommended, it provides interpolation
            // capabilities, and only synchronizes 'active' objects
            // create rigid body for physics scene.
            DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);

            RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
                                                            mass,
                                                            myMotionState,
                                                            groundShape,
                                                            localInertia);
            RigidBody groundBody = new RigidBody(rbInfo);
            groundBody.setRestitution(0.5f);

            // add the body to the dynamics world
            groundBody.setFriction(0.1f);
            world.addRigidBody(groundBody);
            bodiesToNodes.put(groundBody, groundNode);

            return groundNode;
        } else if(shape.equals("Box")) {
            return new Node();
            
        } else {
            return new Node();
        }
    }

    public void startSimulation(float seconds) {
         root = new Node();
        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        root.setRenderState(zbuf);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(root);
        Entity e = new Entity("jbullet");
        e.addComponent(RenderComponent.class, rc);
        rc.setSceneRoot(root);

        
        for(Node n : bodiesToNodes.values()) {
            root.attachChild(n);

        }

        proc = new PhysicsProcessor("Physics", seconds);
        e.addComponent(PhysicsProcessor.class, proc);
        ClientContextJME.getWorldManager().addEntity(e);
        //world.debugDrawWorld();

    }
    class PhysicsProcessor extends ProcessorComponent {

        private String name;
        private float seconds;
        private int frameIndex = 0;
        public PhysicsProcessor(String name, float seconds) {
            this.name = name;
            this.seconds = seconds;
            setArmingCondition(new NewFrameCondition(this));


            
        }
        public void idle() {
            setArmingCondition(new AwtEventCondition(this));

        }

        public void rearm() {
            setArmingCondition(new NewFrameCondition(this));
        }
        @Override
        public void compute(ProcessorArmingCollection pac) {
            if(frameIndex > (30*seconds)) {

                ClientContextJME.getWorldManager().removeEntity(this.getEntity());
                this.getEntity().removeComponent(PhysicsProcessor.class);
                avatarRoot.removeGeometricUpdateListener(getLocalInstance());
            }

            while(!potentialBodies.isEmpty()) {
                world.addRigidBody(potentialBodies.remove());
                //potentialBodies.remove();
            }
            synchronized(world) {
                world.stepSimulation(1/30f, 10);
            }
            frameIndex +=1;
        }

        @Override
        public void commit(ProcessorArmingCollection pac) {
            
                for(CollisionObject obj : world.getCollisionObjectArray()) {
                    RigidBody body = RigidBody.upcast(obj);
                    if (body != null && body.getMotionState() != null) {
                            Transform trans = new Transform();

                            body.getMotionState().getWorldTransform(trans);

                            Node n = bodiesToNodes.get(obj);
                            if(n.equals(avatarRoot)) {
                                //System.out.println("Commit processing avatarRoot!");
                                CellTransform previous = ClientContextJME.getViewManager().getPrimaryViewCell().getWorldTransform();
                                CellTransform transform = new CellTransform();
                                com.jme.math.Vector3f translation;
                                Quaternion rotation;
                                translation = new com.jme.math.Vector3f(trans.origin.x,
                                                                        trans.origin.y,
                                                                        trans.origin.z);
                                rotation = new Quaternion(trans.getRotation(new Quat4f()).x,
                                                          trans.getRotation(new Quat4f()).y,
                                                          trans.getRotation(new Quat4f()).z,
                                                          trans.getRotation(new Quat4f()).w);

                                transform.setTranslation(translation);
                                transform.setRotation(rotation);
                                avatarCharacter.getModelInst().setTransform(new PTransform(previous.getRotation(null), transform.getTranslation(null), 1.0f));
                                
                            }

                            n.setLocalTranslation(trans.origin.x, trans.origin.y, trans.origin.z);

                            ClientContextJME.getWorldManager().addToUpdateList(n);
                           /* System.out.printf("world pos = %f,%f,%f\n", trans.origin.x,
                                            trans.origin.y, trans.origin.z);*/
                    }
                }
            
        }

        @Override
        public void initialize() {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
