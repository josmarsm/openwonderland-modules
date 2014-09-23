/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
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
import org.jdesktop.wonderland.modules.ezscript.client.SPI.FriendlyErrorInfoSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.IBindingsLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;
import org.jdesktop.wonderland.modules.ezscript.client.cell.EZCellWrapper;
import org.jdesktop.wonderland.modules.ezscript.client.errorinfo.DefaultFriendlyErrorInfo;
import org.jdesktop.wonderland.modules.ezscript.client.errorinfo.DefaultFriendlyJavaErrorInfo;
import org.jdesktop.wonderland.modules.ezscript.client.errorinfo.DefaultFriendlyJavascriptErrorInfo;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratedCellMethod;
import org.jdesktop.wonderland.modules.ezscript.client.loaders.SerialLoader;
import org.jdesktop.wonderland.modules.ezscript.common.CellTriggerEventMessage;
import org.jdesktop.wonderland.modules.ezscript.common.SharedBounds;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
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
 *
 * @author JagWire
 * @author Abhishek Upadhyay
 */
public class EZScriptComponent extends CellComponent implements CapabilityBridge {

    //<editor-fold defaultstate="collapsed" desc="Variables">
//    private ScriptEngineManager engineManager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
    private ScriptEngineManager engineManager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
    private ScriptEngine scriptEngine = null;
    private Bindings scriptBindings = null;
    private JDialog dialog;
    private ScriptEditorPanel panel = null;
    private String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r",
        "s", "t", "u", "v", "w", "x", "y", "z", " "};
    private String[] modifiers = {"none", "shift", "alt", "ctrl"};
    private static Logger logger = Logger.getLogger(EZScriptComponent.class.getName());
    private BasicRenderer renderer = null;
    private ExecutorService executor;
    //callback containers
    // - these containers hold runnable objects that will get executed
    //   on it's respective event.
    // - it's important to note that one can not pick and choose which runnables
    //   per container get executed, all of a container's runnables will be
    //   executed per event.
    private Map<String, List<Runnable>> callbacksOnClick;        //mouse click
    private List<Runnable> callbacksOnLoad;         //cell load
    private List<Runnable> callbacksOnUnload;       //cell unload
    private List<Runnable> callbacksOnMouseEnter;   //mouse enter
    private List<Runnable> callbacksOnMouseExit;    //mouse exit
    private List<Runnable> callbacksOnApproach;     //avatar approach
    private List<Runnable> callbacksOnLeave;        //avatar leave
    private Map<String, List<Runnable>> callbacksOnKeyPress; // keypress
    //local callback containers
    // - these Runnables only get executed on the local client, they do not get
    // propogated across the network.
    private Map<String, List<Runnable>> localOnClick;
    private List<Runnable> localOnLoad;
    private List<Runnable> localOnUnload;
    private List<Runnable> localOnMouseEnter;
    private List<Runnable> localOnMouseExit;
    private List<Runnable> localOnApproach;
    private List<Runnable> localOnLeave;
    private Map<String, List<Runnable>> localOnKeyPress;
    //Functions to be run from remote cells to alter this particular cell
    //only one runnable per name, no overloading supported as of yet...
    private Map<String, List<Runnable>> triggerCellEvents;
    private Map<String, List<Runnable>> localTriggerEvents;
    //event listeners
    private MouseEventListener mouseEventListener;
    private KeyboardEventListener keyEventListener;
    //sharedstate variables
    @UsesCellComponent
    private SharedStateComponent sharedStateComponent;
    private SharedMapCli callbacksMap; // used in syncing callbacks across clients
    private SharedMapCli scriptsMap;// used for executing scripts and client sync
    private SharedMapCli stateMap; //used for persisting state variables, including current script
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
    @UsesCellComponent
    private AnotherMovableComponent anotherMovable;
    //dislike this.
    @UsesCellComponent
    private MovableComponent movable;
    //state variables
    private boolean mouseEventsEnabled = false;
    private boolean proximityEventsEnabled = false;
    private boolean keyEventsEnabled = false;
    //intiation/response variables
    private boolean initiatesMouseEvents = false;
    private boolean respondsToMouseEvents = false;
    private boolean initiatesProximityEvents = false;
    private boolean respondsToProximityEvents = false;
    private boolean initiatesKeyEvents = false;
    private boolean respondsToKeyEvents = false;

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Wonderland Boilerplate">
    public EZScriptComponent(Cell cell) {
        super(cell);

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        //initialize callback containers
//        callbacksOnClick = new ArrayList<Runnable>();
        callbacksOnLoad = new ArrayList<Runnable>();
        callbacksOnUnload = new ArrayList<Runnable>();
        callbacksOnMouseEnter = new ArrayList<Runnable>();
        callbacksOnMouseExit = new ArrayList<Runnable>();
        callbacksOnApproach = new ArrayList<Runnable>();
        callbacksOnLeave = new ArrayList<Runnable>();

        //initialize keypress map
        callbacksOnKeyPress = new HashMap<String, List<Runnable>>();
        for (String letter : alphabet) {
            //for each letter in the alphabet, add an entry in the hashmap
            List<Runnable> l = new ArrayList<Runnable>();
            callbacksOnKeyPress.put(letter, l);
        }

        callbacksOnClick = new HashMap<String, List<Runnable>>();
        for (String modifier : modifiers) {
            List<Runnable> l = new ArrayList<Runnable>();
            callbacksOnClick.put(modifier, l);
        }


        //initialize local callbacks
//        localOnClick = new ArrayList<Runnable>();
        localOnLoad = new ArrayList<Runnable>();
        localOnUnload = new ArrayList<Runnable>();
        localOnMouseEnter = new ArrayList<Runnable>();
        localOnMouseExit = new ArrayList<Runnable>();
        localOnApproach = new ArrayList<Runnable>();
        localOnLeave = new ArrayList<Runnable>();
        localOnKeyPress = new HashMap();
        for (String letter : alphabet) {
            List<Runnable> l = new ArrayList<Runnable>();
            localOnKeyPress.put(letter, l);
        }

        localOnClick = new HashMap<String, List<Runnable>>();
        for (String modifier : modifiers) {
            List<Runnable> l = new ArrayList<Runnable>();
            localOnClick.put(modifier, l);
        }

        triggerCellEvents = new HashMap<String, List<Runnable>>();
        localTriggerEvents = new HashMap<String, List<Runnable>>();

        //intialize listeners
        mouseEventListener = new MouseEventListener();
        keyEventListener = new KeyboardEventListener();
        mapListener = new SharedMapListener();
        proximityListener = new ProximityListenerImpl();

        IBindingsLoader bindingLoader = new SerialLoader();
//        IBindingsLoader bindingLoader = new OptimizedLoader();

        bindingLoader.loadBindings();

        scriptEngine = bindingLoader.getEngine();
        scriptBindings = bindingLoader.getBindings();


//        scriptEngine = engineManager.getEngineByName("JavaScript");
//        cell.getClass().getName();
//        scriptBindings = scriptEngine.createBindings();
        ScriptManager.getInstance().addCell(cell);
        dialog = new JDialog();
        
        final EZScriptComponent ezRef = this;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel = new ScriptEditorPanel(ezRef, dialog);

            }
        });

//        scriptBindings.putAll(dao().getCellBindings());
        generateDocumentation();


    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch (status) {
            case RENDERING:
                if (increasing) {
                    renderer = (BasicRenderer) cell.getCellRenderer(RendererType.RENDERER_JME);
                    if (mouseEventsEnabled) {
                        mouseEventListener = new MouseEventListener();
                        mouseEventListener.addToEntity(renderer.getEntity());
                    }
                    if (keyEventsEnabled) {
                        keyEventListener = new KeyboardEventListener();
                        keyEventListener.addToEntity(renderer.getEntity());
                    }
                    if (menuFactory == null) {
                        menuListener = new MenuItemListener();
                        menuFactory = new ContextMenuFactorySPI() {
                            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                                return new ContextMenuItem[]{
                                            new SimpleContextMenuItem("Script", menuListener)
                                        };
                            }
                        };
                        contextMenuComponent.addContextMenuFactory(menuFactory);
                    }

                    executor.submit(new Runnable() {
                        public void run() {

                            //grab the "callbacks" map in order to hopefully
                            //use an additional map for "state" if needed
                            synchronized (sharedStateComponent) {
                                callbacksMap = sharedStateComponent.get("callbacks");
                                scriptsMap = sharedStateComponent.get("scripts");
                                stateMap = sharedStateComponent.get("state");
                            }
                            callbacksMap.addSharedMapListener(mapListener);
                            scriptsMap.addSharedMapListener(mapListener);
                            stateMap.addSharedMapListener(mapListener);

                            //process the states map
                            handleStates(stateMap);
                            handleScript(stateMap);

                            //get other maps here.
                        }
                    });
                    //intialize shared state component and map                                       
//                    new Thread(new Runnable() {
//
//                        public void run() {
//                            //grab the "callbacks" map in order to hopefully
//                            //use an additional map for "state" if needed
//                            synchronized (sharedStateComponent) {
//                                callbacksMap = sharedStateComponent.get("callbacks");
//                                scriptsMap = sharedStateComponent.get("scripts");
//                                stateMap = sharedStateComponent.get("state");
//                            }
//                            callbacksMap.addSharedMapListener(mapListener);
//                            scriptsMap.addSharedMapListener(mapListener);
//                            stateMap.addSharedMapListener(mapListener);
//
//                            //process the states map
//                            handleStates(stateMap);
//                            handleScript(stateMap);
//
//                            //get other maps here.
//                        }
//                    }).start();
                }

                break;
            case ACTIVE:
                if (increasing) {
                    channelComponent.addMessageReceiver(CellTriggerEventMessage.class,
                            new TriggerCellEventReceiver());
//                    //intialize shared state component and map
//                    
                    scriptBindings.put("cell", this.cell);
                    scriptBindings.put("Context", this);

//                    new Thread(new Runnable() {
//                        public void run() {
//                            //grab the "callbacks" map in order to hopefully
//                            //use an additional map for "state" if needed
//                           callbacksMap = sharedStateComponent.get("callbacks");
//                           scriptsMap = sharedStateComponent.get("scripts");
//                           stateMap = sharedStateComponent.get("state");
//                           callbacksMap.addSharedMapListener(mapListener);
//                           scriptsMap.addSharedMapListener(mapListener);
//                           stateMap.addSharedMapListener(mapListener);
//
//                           //process the states map
//                           handleStates(stateMap);
//                           handleScript(stateMap);
//
//                           //get other maps here.
//                        }
//                    }).start();                    
                }
                break;
            case INACTIVE:
                if (!increasing) {
                    if (menuFactory != null) {
                        contextMenuComponent.removeContextMenuFactory(menuFactory);
                        menuFactory = null;
                    }
                    channelComponent.removeMessageReceiver(CellTriggerEventMessage.class);
                    callbacksMap.removeSharedMapListener(mapListener);
                    scriptsMap.removeSharedMapListener(mapListener);
                    stateMap.removeSharedMapListener(mapListener);

                    executor.shutdownNow();
                }
                break;
            case DISK:
                if (!increasing) {
                    if (mouseEventListener != null) {
                        mouseEventListener.removeFromEntity(renderer.getEntity());
                        mouseEventListener = null;
                    }

                    if (keyEventListener != null) {
                        keyEventListener.removeFromEntity(renderer.getEntity());
                        keyEventListener = null;
                    }

                    if (proximityListener != null) {
                        proximityComponent.removeProximityListener(proximityListener);
                        proximityListener = null;
                    }
                    ScriptManager.getInstance().removeCell(this.cell);
                    clearCallbacks();
                    //  callbacksMap.clear();
                    // scriptsMap.clear();
                    renderer = null;
                }
        }
    }
//</editor-fold>

    private ScriptedObjectDataSource dao() {

//        ScriptedObjectDataSource.INSTANCE.initialize();

        return ScriptedObjectDataSource.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="javascript generation methods">
    private void generateDocumentation() {

        generateVoidDocumentation();

        generateNonVoidDocumentation();

        generateCellFactoryDocumentation();


        //add $() function to script bindings
        addGetFunction();
    }

    private void generateNonVoidDocumentation() {

        for (final ReturnableScriptMethodSPI returnable : dao().getReturnables()) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(returnable);
                }
            });
        }
    }

    private void generateCellFactoryDocumentation() {

        for (CellFactorySPI factory : dao().getCellFactories()) {
            final ReturnableScriptMethodSPI returnable = new GeneratedCellMethod(factory);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(returnable);
                }
            });
        }
    }

    private void generateVoidDocumentation() {
        for (final ScriptMethodSPI method : dao().getVoids()) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(method);
                }
            });
        }
    }
    //</editor-fold>

    public void handleStates(SharedMapCli states) {

        //Handle "bounds" first, since it is a dependency of "proximity".
        //What this means is that if there exists information for bounds from
        //the ezscript component, make sure it trumps the cell's bounds.

        if (states.containsKey("bounds")) {
            SharedBounds bounds = (SharedBounds) states.get("bounds");
            BoundingVolume volume = null;
            if (bounds.getValue().equals("BOX")) {
                volume = new BoundingBox(new Vector3f(),
                        bounds.getExtents()[0],
                        bounds.getExtents()[1],
                        bounds.getExtents()[2]);

            } else {
                volume = new BoundingSphere(bounds.getExtents()[0],
                        new Vector3f());
            }
            cell.setLocalBounds(volume);
        }

        //see above comment for handling "bounds".
        if (states.containsKey("proximity")) {
            SharedBoolean enabled = (SharedBoolean) states.get("proximity");
            if (enabled.getValue()) {
                enableProximityEvents();
            }
        }

        if (states.containsKey("mouse")) {
            SharedBoolean enabled = (SharedBoolean) states.get("mouse");
            if (enabled.getValue()) {
                enableMouseEvents();
            }
        }

        if (states.containsKey("keyboard")) {
            SharedBoolean enabled = (SharedBoolean) states.get("keyboard");
            if (enabled.getValue()) {
                enableKeyEvents();
            }
        }

    }

    public void handleScript(SharedMapCli states) {
        if (states.containsKey("script")) {
            final SharedString script = (SharedString) states.get("script");



//            executor.submit(new Runnable() {
//                public void run() {
            logger.warning("[EZSCRIPT] EXECUTING LOADED SCRIPT FOR CELL: " + cell.getName());
//                    System.out.println(script.getValue());

            evaluateScript(script.getValue());
//                }
//            });
//            
//            new Thread(new Runnable() {
//
//                public void run() {
//                    //System.out.println("[EZScript] handling persisted script");
//                    evaluateScript(script.getValue());
//                }
//            }).start();
        }
    }

    public MouseEventListener getMouseEventListener() {
        return this.mouseEventListener;
    }

    public boolean getMouseEventsEnabled() {
        return mouseEventsEnabled;
    }

    // <editor-fold defaultstate="collapsed" desc="Event Dis/Enablers">
    public void enableMouseEvents() {
        if (mouseEventListener == null) {
            mouseEventListener = new MouseEventListener();
            initiatesMouseEvents = true;
            respondsToMouseEvents = true;
        }

        if (!mouseEventListener.isListeningForEntity(renderer.getEntity())) {
            mouseEventListener.addToEntity(renderer.getEntity());
            mouseEventsEnabled = true;

        }
        initiatesMouseEvents = true;
        respondsToMouseEvents = true;
    }

    public void disableMouseEvents() {
        if (mouseEventListener != null) {
            mouseEventListener.removeFromEntity(renderer.getEntity());
        }
        mouseEventListener = null;
        mouseEventsEnabled = false;
        initiatesMouseEvents = false;
        respondsToMouseEvents = false;
    }

    public void enableKeyEvents() {
        if (keyEventListener == null) {
            keyEventListener = new KeyboardEventListener();
        }
        if (!keyEventListener.isListeningForEntity(renderer.getEntity())) {
            keyEventListener.addToEntity(renderer.getEntity());
            keyEventsEnabled = true;
        }
    }

    public void disableKeyEvents() {
        if (keyEventListener != null) {
            keyEventListener.removeFromEntity(renderer.getEntity());
        }
        keyEventListener = null;
        keyEventsEnabled = false;
    }

    public void enableProximityEvents() {
        if (proximityListener == null) {
            proximityListener = new ProximityListenerImpl();
        }
        proximityComponent.addProximityListener(proximityListener,
                new BoundingVolume[]{
                    cell.getLocalBounds()
                });
        proximityEventsEnabled = true;
        initiatesProximityEvents = true;
        respondsToProximityEvents = true;
    }

    /**
     * Updates the bounding volume for a cell. If proximity events are enabled,
     * they will be reprocessed after calling this method.
     *
     * @param spatial is the name of the bounding type. Should ONLY ever be
     * "BOX" or "SPHERE"
     * @param info will be different based on the value of spatial. "BOX" will
     * contain three floats: the x, y, and z extents of the box. "SPHERE: will
     * only contain one float: the radius of the sphere.
     */
    public void updateCellBounds(String spatial, float[] info) {
        BoundingVolume volume;
        CellTransform t = cell.getLocalTransform();
        //Vector3f translation = new Vector3f();//t.getTranslation(null);
        if (spatial.equals("BOX")) {
            volume = new BoundingBox(new Vector3f(), info[0], info[1], info[2]);
        } else {
            volume = new BoundingSphere(info[0], new Vector3f());
        }
        cell.setLocalBounds(volume);
        if (proximityEventsEnabled == true && proximityListener != null) {
            proximityComponent.removeProximityListener(proximityListener);
            proximityListener = null;
            enableProximityEvents();
        }
    }

    public void disableProximityEvents() {
        proximityComponent.removeProximityListener(proximityListener);
        proximityEventsEnabled = false;
        initiatesProximityEvents = false;
        respondsToProximityEvents = false;
    }

    public boolean areMouseEventsEnabled() {
        return this.mouseEventsEnabled;
    }

    public boolean areProximityEventsEnabled() {
        return this.proximityEventsEnabled;
    }

    public boolean areKeyEventsEnabled() {
        return this.keyEventsEnabled;
    }

    public boolean areTriggersEnabled() {
        return true;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Callback Assignments">
    public void setTrigger(String s, Runnable r, boolean local) {

        if (local) {
            if (localTriggerEvents.containsKey(s)) {
                localTriggerEvents.get(s).add(r);
            } else {
                List<Runnable> l = new ArrayList();
                l.add(r);
                localTriggerEvents.put(s, l);
            }
        } else {
            if (triggerCellEvents.containsKey(s)) {
                triggerCellEvents.get(s).add(r);
            } else {

                List<Runnable> l = new ArrayList();
                l.add(r);
                triggerCellEvents.put(s, l);
            }
        }
    }

    public void onClick(Runnable r, boolean local) {
        onClick("none", r, local);
    }

    public void onClick(String modifier, Runnable r, boolean local) {
        List<Runnable> rs;
        modifier = modifier.toLowerCase();
        if (local) {
            rs = localOnClick.get(modifier);
            if (rs == null) {
                rs = new ArrayList<Runnable>();
            }
            rs.add(r);
            localOnClick.put(modifier, rs);
        } else {
            rs = callbacksOnClick.get(modifier);
            if (rs == null) {
                rs = new ArrayList<Runnable>();
            }
            rs.add(r);
            callbacksOnClick.put(modifier, rs);
        }
    }

    public void onMouseEnter(Runnable r, boolean local) {
        if (local) {
            localOnMouseEnter.add(r);
        } else {
            callbacksOnMouseEnter.add(r);
        }
    }

    public void onMouseExit(Runnable r, boolean local) {
        if (local) {
            localOnMouseExit.add(r);
        } else {
            callbacksOnMouseExit.add(r);
        }
    }

    public void onLoad(Runnable r, boolean local) {
        if (local) {
            localOnLoad.add(r);
        } else {
            callbacksOnLoad.add(r);
        }
    }

    public void onUnload(Runnable r, boolean local) {
        if (local) {
            localOnLoad.add(r);
        } else {
            callbacksOnUnload.add(r);
        }
    }

    public void onApproach(Runnable r, boolean local) {
        if (local) {
            localOnApproach.add(r);
        } else {
            callbacksOnApproach.add(r);
        }

    }

    public void onLeave(Runnable r, boolean local) {
        if (local) {
            localOnLeave.add(r);
        } else {
            callbacksOnLeave.add(r);
        }
    }

    public void onKeyPress(String key, Runnable r, boolean local) {
        List<Runnable> list;
        if (local) {
            list = localOnKeyPress.get(key);
            if (list == null) {
                list = new ArrayList();
            }
            list.add(r);
            localOnKeyPress.put(key, list);

        } else {
            list = callbacksOnKeyPress.get(key);
            if (list == null) {
                list = new ArrayList();
            }
            list.add(r);
            callbacksOnKeyPress.put(key, list);
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

        localOnClick.clear();
        localOnLoad.clear();
        localOnUnload.clear();
        localOnMouseEnter.clear();
        localOnMouseExit.clear();
        localOnApproach.clear();
        localOnLeave.clear();

        //to be thorough...
        clearMap(callbacksOnKeyPress);
        clearMap(localOnKeyPress);
        clearMap(triggerCellEvents);
        clearMap(localTriggerEvents);

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Event Executors">
    public void executeOnClick(String modifier, boolean local) {
        final List<Runnable> rs;
        if (local) {
            rs = localOnClick.get(modifier);

        } else {
            rs = callbacksOnClick.get(modifier);
        }
        if (rs == null) {
            return;
        }

        threadedExecute(rs);

    }

    public void executeOnMouseEnter(boolean local) {
        if (local) {
            threadedExecute(localOnMouseEnter);

        } else {
            threadedExecute(callbacksOnMouseEnter);
        }
    }

    public void executeOnMouseExit(boolean local) {
        if (local) {
            threadedExecute(localOnMouseExit);

        } else {
            threadedExecute(callbacksOnMouseExit);
        }
    }

    public void executeOnLoad(boolean local) {
        if (local) {
            threadedExecute(localOnLoad);
        } else {
            threadedExecute(callbacksOnLoad);
        }
    }

    public void executeOnUnload(boolean local) {
        if (local) {
            threadedExecute(localOnUnload);
        } else {
            threadedExecute(callbacksOnUnload);
        }
    }

    public void executeOnApproach(boolean local) {
        if (local) {
            threadedExecute(localOnApproach);

        } else {
            threadedExecute(callbacksOnApproach);
        }
    }

    public void executeOnLeave(boolean local) {
        if (local) {
            threadedExecute(localOnLeave);

        } else {
            threadedExecute(callbacksOnLeave);
        }
    }

    private void trigger(String s, boolean local) {
        final List<Runnable> rs;
        if (local) {
            rs = localTriggerEvents.get(s);
            //threadedExecute(triggerCellEvents.get(s));
        } else {
            rs = triggerCellEvents.get(s);
        }

        if (rs == null) {
            return;
        }

        threadedExecute(rs);

    }

    private void threadedExecute(List<Runnable> rs) {

        for (Runnable r : rs) {
            executor.submit(r);
//            new Thread(r).start();
        }

    }

    public void executeOnKeyPress(String key, boolean local) {
        final List<Runnable> rs;
        if (local) {
            rs = localOnKeyPress.get(key);
        } else {
            rs = callbacksOnKeyPress.get(key);
        }

        if (rs == null) {
            return;
        }

        threadedExecute(rs);
    }
// </editor-fold>

    /**
     * Utility method to clear the given callback map.
     *
     * @param m the map of a string associated with a list of Runnable
     */
    private void clearMap(Map<String, List<Runnable>> m) {
        for (List l : m.values()) {
            l.clear();
        }

        m.clear();
    }

    private void bindScript(final String script) {
        try {
            scriptEngine.eval(script, scriptBindings);
        } catch (ScriptException ex) {
            Logger.getLogger(EZScriptComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void evaluateScript(final String script) {
        synchronized (scriptEngine) {
            try {
//                scriptBindings.clear();
//                scriptBindings.putAll(dao().getCellBindings());
                scriptEngine.eval(script, scriptBindings);
            } catch (Exception e) {
                processException(e);
            } finally {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.setScriptTextArea(script);
                    }
                });
            }
        }
    }

    public SharedMapCli getScriptMap() {
        return this.scriptsMap;
    }

    //<editor-fold defaultstate="collapsed" desc="Event Listeners">
    public class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class,
                        MouseEnterExitEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            if (isInitiatesMouseEvents()) {
                if (event instanceof MouseButtonEvent3D) {
                    //MouseButtonEvent3D m = (MouseButtonEvent3D)event;
                    MouseButtonEvent3D m = (MouseButtonEvent3D) event;

//                    if (m.isClicked() && m.getButton() == ButtonId.BUTTON1) {
                    //FIX FOR EZMOVE
                    InputEvent awtEvent = m.getAwtEvent();
                    if (awtEvent.getID() == MouseEvent.MOUSE_CLICKED
                            //                            && awtEvent.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
                            && awtEvent.getModifiersEx() == 0
                            && SwingUtilities.isLeftMouseButton((MouseEvent) awtEvent)) {
                        if (respondsToMouseEvents) {
                            executeOnClick("none", true);
                        }
                        callbacksMap.put("onClick", new SharedString().valueOf("none"));
                    } else if (awtEvent.getID() == MouseEvent.MOUSE_CLICKED
                            && awtEvent.getModifiersEx() == MouseEvent.ALT_DOWN_MASK
                            && SwingUtilities.isLeftMouseButton((MouseEvent) awtEvent)) {
                        //alt events
                        if (respondsToMouseEvents) {
                            executeOnClick("alt", true);
                        }
                        callbacksMap.put("onClick", new SharedString().valueOf("alt"));



                    } else if (awtEvent.getID() == MouseEvent.MOUSE_CLICKED
                            && awtEvent.getModifiersEx() == MouseEvent.CTRL_DOWN_MASK
                            && SwingUtilities.isLeftMouseButton((MouseEvent) awtEvent)) {
                        //ctrl events
                        if (respondsToMouseEvents) {
                            executeOnClick("ctrl", true);
                        }
                        callbacksMap.put("onClick", new SharedString().valueOf("ctrl"));




                    } else if (awtEvent.getID() == MouseEvent.MOUSE_CLICKED
                            && awtEvent.getModifiersEx() == MouseEvent.SHIFT_DOWN_MASK
                            && SwingUtilities.isLeftMouseButton((MouseEvent) awtEvent)) {
                        //shift events
                        if (respondsToMouseEvents) {
                            executeOnClick("shift", true);
                        }
                        callbacksMap.put("onClick", new SharedString().valueOf("shift"));
                    }
                } else if (event instanceof MouseEnterExitEvent3D) {
                    MouseEnterExitEvent3D m = (MouseEnterExitEvent3D) event;
                    if (m.isEnter()) {
                        if (respondsToMouseEvents) {
                            executeOnMouseEnter(true);
                        }
                        callbacksMap.put("onMouseEnter", new SharedString().valueOf("" + System.currentTimeMillis()));
                    } else {
                        if (respondsToMouseEvents) {
                            executeOnMouseExit(true);
                        }
                        callbacksMap.put("onMouseExit", new SharedString().valueOf("" + System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    class KeyboardEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{KeyEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            if (event instanceof KeyEvent3D) {
                KeyEvent3D e = (KeyEvent3D) event;
                if (e.isPressed()) {
                    executeOnKeyPress(Character.toString(e.getKeyChar()), true);
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
            String name = event.getMap().getName();

            //callbacks are most likely to be called the most frequently.
            if (name.equals("callbacks")) { //if it's the callbacks map
                if (property.equals("onClick")) { //if someone clicked the mouse
                    if (respondsToMouseEvents) { //and I'm allowed to respond
                        String modifier = ((SharedString) event.getNewValue()).getValue();
                        executeOnClick(modifier, false);
                    }
                } else if (property.equals("onMouseEnter")) {//if someone moved the mouse
                    if (respondsToMouseEvents) { //and I'm allowed to respond
                        executeOnMouseEnter(false);
                    }
                } else if (property.equals("onMouseExit")) { //if someone moved the mouse away
                    if (respondsToMouseEvents) { //and I'm allowed to respond
                        executeOnMouseExit(false);
                    }
                } else if (property.equals("onApproach")) { //if someone entered bounds
                    if (respondsToProximityEvents) { //and I'm allowed to respond
                        executeOnApproach(false);
                    }
                } else if (property.equals("onLeave")) { //if someone entered bounds
                    if (respondsToProximityEvents) { //and I'm allowed to respond
                        executeOnLeave(false);
                    }
                } else if (property.equals("onKeyPress")) {
                    executeOnKeyPress(event.getNewValue().toString(), false);
                } else if (property.equals("clear")) {
                    clearCallbacks();
                }
                return;
            }

            //scripts are most likely to be called second most frequently
            if (name.equals("scripts")) {
                if (property.equals("editor")) {
                    final SharedString script = (SharedString) event.getNewValue();
                    try {
                        //execute script typed in Scripting Editor
                        System.out.println("executing script...");
                        //scriptEngine.eval(script.getValue(), scriptBindings);
                        //Need to add this script to the script editor panel.
                        clearCallbacks();

                        executor.submit(new Runnable() {
                            public void run() {
                                evaluateScript(script.getValue());
                            }
                        });

//                        
//                        new Thread(new Runnable() {
//
//                            public void run() {
//                                evaluateScript(script.getValue());
//                            }
//                        }).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            //state messages should be send least frequently
            if (name.equals("state")) {

                if (property.equals("script")) {
                    return;
                }

                if (property.equals("bounds")) {
                    SharedBounds b = (SharedBounds) event.getNewValue();
                    System.out.println(event.getNewValue());
                    //updating cell bounds takes care of updating the proximity
                    //listener as well.
                    updateCellBounds(b.getValue(), b.getExtents());
                    return;
                }
                //SharedBoolean p = (SharedBoolean)event.getNewValue();
                if (property.equals("proximity")) {
                    if (((SharedBoolean) event.getNewValue()).getValue()) {
                        enableProximityEvents();
                    } else {
                        disableProximityEvents();
                    }
                } else if (property.equals("mouse")) {
                    if (((SharedBoolean) event.getNewValue()).getValue()) {
                        enableMouseEvents();
                    } else {
                        disableMouseEvents();
                    }
                } else if (property.equals("keyboard")) {
                    if (((SharedBoolean) event.getNewValue()).getValue()) {
                        enableKeyEvents();
                    } else {
                        disableKeyEvents();
                    }
                }
                return;
            }
        }
    }
    
    //for animation module
    public void executeScript(final String script) {
        try {
            //execute script typed in Scripting Editor
            //Need to add this script to the script editor panel.
            clearCallbacks();

            executor.submit(new Runnable() {
                public void run() {
                    synchronized (scriptEngine) {
                        try {
                            scriptEngine.eval(script, scriptBindings);
                        } catch (Exception e) {
                            processException(e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ProximityListenerImpl implements ProximityListener {

        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
            if (initiatesProximityEvents) {
                if (entered) {
                    if (respondsToProximityEvents) {
                        executeOnApproach(true);
                    }
                    callbacksMap.put("onApproach", new SharedString().valueOf("" + System.currentTimeMillis()));
                } else {
                    if (respondsToProximityEvents) {
                        executeOnLeave(true);
                    }
                    callbacksMap.put("onLeave", new SharedString().valueOf("" + System.currentTimeMillis()));
                }
            }
        }
    }

    class MenuItemListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            if (event.getContextMenuItem().getLabel().equals("Script")) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dialog.setResizable(false);

                        dialog.setTitle("Script Editor - " + cell.getName());
                        //2. Optional: What happens when the frame closes?
                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                        //3. Create component and put them in the frame.

                        dialog.setContentPane(panel);

                        //4. Size the frame.
                        dialog.pack();

                        //5. Show it.
                        dialog.setVisible(true);
                    }
                });

            }
        }
    }

    class TriggerCellEventReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            if (message instanceof CellTriggerEventMessage) {
                CellTriggerEventMessage eventMessage = (CellTriggerEventMessage) message;
                String name = eventMessage.getEventName();

                if (triggerCellEvents.containsKey(name)) {

                    logger.warning(cell.getName() + " RECEIVED TRIGGER: " + eventMessage.getEventName());

                    threadedExecute(triggerCellEvents.get(name));
                    //triggerCellEvents.get(name).setArguments(eventMessage.getArguments());
                    //triggerCellEvents.get(name).run();
                } else {
                    logger.warning("Received an event request with no associated event: " + eventMessage.getEventName());
                }
            }
        }
    }
    //</editor-fold>

    public void triggerLocalCell(CellID cellID, String label, Object[] args) {

        logger.warning(this.cell.getName() + " IS LOCALLY TRIGGERING: " + label);

        //obtain primary session so we can get the cell cache.
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();

        //acquire the cell cache for the primary session.
        CellCache cache = ClientContextJME.getCellCache(session);

        //get the cell we're looking for from the cell cache.
        Cell tmpCell = cache.getCell(cellID);

        //grab the ezscript component from the cell we're looking for.
        EZScriptComponent ez = tmpCell.getComponent(EZScriptComponent.class);
        if (ez == null) {
            //oh noes!
            //fail gracefully
            logger.warning("OH NOES!!!");
            return;
        }
        //acquire the map of triggers for the cell we're looking for.
        Map<String, List<Runnable>> triggers = ez.triggerCellEvents;

        //check to see if the trigger we're executing is relevant. That is to 
        //say, a trigger has been registered with that name.
        if (triggers.containsKey(label)) {
            logger.warning("EXECUTING TRIGGER: " + label + " FOR CELL: " + tmpCell.getName());
            ez.threadedExecute(ez.triggerCellEvents.get(label));
        } else {
            //fail gracefully.
            logger.warning(tmpCell.getName() + " received a trigger request with no associated trigger: " + label);
            return;
        }
    }

    public void triggerCell(CellID cellID, String label, Object[] args) {

        String name = cell.getCellCache().getCell(cellID).getName();
        logger.warning("SENDING TRIGGER: " + label + " TO CELL: " + name);
        channelComponent.send(new CellTriggerEventMessage(cellID, label, args));
    }

    public void removeCallback(List l, Runnable r) {
        if (r == null) {
            return;
        }

    }

    public void addCallback(List l, Runnable r) {
        if (r == null) {
            return;
        }
        if (!l.contains(r)) {
            l.add(r);
        }
    }

    public SharedMapCli getStateMap() {
        return stateMap;
    }

    public SharedMapCli getCallbacksMap() {
        return callbacksMap;
    }

    public JDialog getDialog() {
        return dialog;
    }

    public void showEditorWindow() {
        dialog.setResizable(false);

        dialog.setTitle("Script Editor - " + cell.getName());
        //2. Optional: What happens when the frame closes?
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //3. Create component and put them in the frame.

        dialog.setContentPane(panel);

        //4. Size the frame.
        dialog.pack();

        //5. Show it.
        dialog.setVisible(true);
    }

    public CellID getCellIDByName(String name) {
        return ScriptManager.getInstance().getCellID(name);
    }

    public void addGetFunction() {
        String scriptx =
                "function $(cellname) { \n"
                + "   return Context.getCellByName(cellname);\n"
                + "}\n";

        try {
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            processException(e);
        }
    }

    public EZCellWrapper getCellByName(String name) {
        CellID cellID = getCellIDByName(name);
        Cell cell = ClientContextJME.getViewManager() //get View Manager
                .getPrimaryViewCell() //get View Manager's primary view
                .getCellCache() // get primary view's cell cache 
                .getCell(cellID); //get cell cache's cell
        return new EZCellWrapper(cell);
    }

    private void processException(Exception e) {
        final ErrorWindow window;
        FriendlyErrorInfoSPI info = null;
        if (e.getMessage().contains("WrappedException")) {
            //add default friendly java
            info = new DefaultFriendlyJavaErrorInfo(cell.getName());

        } else if (e.getMessage().contains("EcmaError")) {
            //add default friendly javascript
            info = new DefaultFriendlyJavascriptErrorInfo(cell.getName());
        } else {
            //add default friendly
            info = new DefaultFriendlyErrorInfo(cell.getName());
        }

        window = new ErrorWindow(info.getSummary(), info.getSolutions());
        TextAreaOutputStream output = new TextAreaOutputStream(window.getDetailsArea());
        e.printStackTrace(new PrintStream(output));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                HUDComponent c = mainHUD.createComponent(window);
                window.setHUDComponent(c);
                c.setDecoratable(true);
                c.setPreferredLocation(Layout.CENTER);
                mainHUD.addComponent(c);
                c.setVisible(true);
            }
        });
    }

    public SharedStateComponent getSharedStateComponent() {
        return this.sharedStateComponent;
    }

    public Collection<String> getRemoteTriggerList() {
        return this.triggerCellEvents.keySet();
    }

    public Collection<String> getLocalTriggerList() {
        return this.localTriggerEvents.keySet();
    }

    public void executeRemoteTrigger(String name) {
        if (triggerCellEvents.containsKey(name)) {
            threadedExecute(triggerCellEvents.get(name));
        } else {
            logger.warning("NO TRIGGER: " + name + " FOUND FOR CELL: " + cell.getName());
        }
    }

    public void executeLocalTrigger(String name) {

        if (localTriggerEvents.containsKey(name)) {
            threadedExecute(localTriggerEvents.get(name));
        } else {
            logger.warning("NO TRIGGER: " + name + " FOUND FOR CELL: " + cell.getName());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="getters/setters">
    public boolean isInitiatesKeyEvents() {
        return initiatesKeyEvents;
    }

    public void setInitiatesKeyEvents(boolean initiatesKeyEvents) {
        this.initiatesKeyEvents = initiatesKeyEvents;
    }

    public boolean isInitiatesMouseEvents() {
        return initiatesMouseEvents;
    }

    public void setInitiatesMouseEvents(boolean initiatesMouseEvents) {
        this.initiatesMouseEvents = initiatesMouseEvents;
    }

    public boolean isInitiatesProximityEvents() {
        return initiatesProximityEvents;
    }

    public void setInitiatesProximityEvents(boolean initiatesProximityEvents) {
        this.initiatesProximityEvents = initiatesProximityEvents;
    }

    public boolean isRespondsToKeyEvents() {
        return respondsToKeyEvents;
    }

    public void setRespondsToKeyEvents(boolean respondsToKeyEvents) {
        this.respondsToKeyEvents = respondsToKeyEvents;
    }

    public boolean isRespondsToMouseEvents() {
        return respondsToMouseEvents;
    }

    public void setRespondsToMouseEvents(boolean respondsToMouseEvents) {
        this.respondsToMouseEvents = respondsToMouseEvents;
    }

    public boolean isRespondsToProximityEvents() {
        return respondsToProximityEvents;
    }

    public void setRespondsToProximityEvents(boolean respondsToProximityEvents) {
        this.respondsToProximityEvents = respondsToProximityEvents;
    }
    //</editor-fold>
}
