
package org.jdesktop.wonderland.modules.ezscript.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
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
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;
import org.jdesktop.wonderland.modules.ezscript.common.CellTriggerEventMessage;
import org.jdesktop.wonderland.modules.ezscript.common.SharedBounds;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import sun.org.mozilla.javascript.internal.EcmaError;
import sun.org.mozilla.javascript.internal.WrappedException;

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

public class EZScriptComponent extends CellComponent {
    
    //<editor-fold defaultstate="collapsed" desc="Variables">
    private ScriptEngineManager engineManager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
    private ScriptEngine scriptEngine = null;
    private Bindings scriptBindings = null;
    private JDialog dialog;
    private ScriptEditorPanel panel = null;
    private String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
                                 "j", "k", "l", "m", "n", "o", "p", "q", "r",
                                 "s", "t", "u", "v", "w", "x", "y", "z", " "};

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

    //local callback containers
    // - these Runnables only get executed on the local client, they do not get
    // propogated across the network.
    private List<Runnable> localOnClick;
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

    //List of behaviors
    private List<Behavior> behaviors;
    
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
    //state variables
    private boolean mouseEventsEnabled = false;
    private boolean proximityEventsEnabled = false;
    private boolean keyEventsEnabled = false;
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Wonderland Boilerplate">
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

        //initialize local callbacks
        localOnClick = new ArrayList<Runnable>();
        localOnLoad = new ArrayList<Runnable>();
        localOnUnload = new ArrayList<Runnable>();
        localOnMouseEnter = new ArrayList<Runnable>();
        localOnMouseExit = new ArrayList<Runnable>();
        localOnApproach = new ArrayList<Runnable>();
        localOnLeave = new ArrayList<Runnable>();
        localOnKeyPress = new HashMap();
        for(String letter: alphabet) {
            List<Runnable> l = new ArrayList<Runnable>();
            localOnKeyPress.put(letter, l);
        }

        triggerCellEvents = new HashMap<String, List<Runnable>>();
        localTriggerEvents = new HashMap<String, List<Runnable>>();
        behaviors = new ArrayList();
        //intialize listeners
        mouseEventListener = new MouseEventListener();
        keyEventListener = new KeyboardEventListener();
        mapListener = new SharedMapListener();
        proximityListener = new ProximityListenerImpl();

        scriptEngine = engineManager.getEngineByName("JavaScript");
        cell.getClass().getName();
        scriptBindings = scriptEngine.createBindings();
        ScriptManager.getInstance().addCell(cell);
        dialog = new JDialog();
        panel = new ScriptEditorPanel(this, dialog);
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        Iterator<ScriptMethodSPI> iter = loader.getInstances(ScriptMethod.class,
                                                        ScriptMethodSPI.class);
        //grab all global void methods
        while(iter.hasNext()) {
            final ScriptMethodSPI method = iter.next();
            this.addFunctionBinding(method);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(method);
                }
            });
        }

        //grab all returnablesa
        Iterator<ReturnableScriptMethodSPI> returnables 
                            = loader.getInstances(ReturnableScriptMethod.class,
                                               ReturnableScriptMethodSPI.class);
        while(returnables.hasNext()) {
            ReturnableScriptMethodSPI method = returnables.next();
            this.addFunctionBinding(method);
            panel.addLibraryEntry(method);
        }

        //add $() function to script bindings
        this.addGetFunction();
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


                    //intialize shared state component and map                                       
                    new Thread(new Runnable() {
                        public void run() {
                            //grab the "callbacks" map in order to hopefully
                            //use an additional map for "state" if needed
                            synchronized(sharedStateComponent) {
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
                    }).start();
                }
               
                break;
            case ACTIVE:
                if(increasing) {
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
                if(!increasing) {
                    if(menuFactory != null) {
                        contextMenuComponent.removeContextMenuFactory(menuFactory);
                        menuFactory = null;
                    }
                    channelComponent.removeMessageReceiver(CellTriggerEventMessage.class);
                    callbacksMap.removeSharedMapListener(mapListener);
                    scriptsMap.removeSharedMapListener(mapListener);
                    stateMap.removeSharedMapListener(mapListener);
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
                    ScriptManager.getInstance().removeCell(this.cell);
                  //  callbacksMap.clear();
                   // scriptsMap.clear();
                    renderer = null;
                }
        }
    }
//</editor-fold>

    public void handleStates(SharedMapCli states) {
        if(states.containsKey("proximity")) {
            SharedBoolean enabled = (SharedBoolean)states.get("proximity");
            if(enabled.getValue()) {
                enableProximityEvents();
            }
        }

        if(states.containsKey("mouse")) {
            SharedBoolean enabled = (SharedBoolean)states.get("mouse");
            if(enabled.getValue()) {
                enableMouseEvents();
            }
        }

        if(states.containsKey("keyboard")) {
            SharedBoolean enabled = (SharedBoolean)states.get("keyboard");
            if(enabled.getValue()) {
                enableKeyEvents();
            }
        }

        if(states.containsKey("bounds")) {
            SharedBounds bounds = (SharedBounds)states.get("bounds");
            BoundingVolume volume = null;
            if(bounds.getValue().equals("BOX")) {
                volume = new BoundingBox(new Vector3f(),
                                         bounds.getExtents()[0],
                                         bounds.getExtents()[1],
                                         bounds.getExtents()[2]);

            } else {
                volume = new BoundingSphere(bounds.getExtents()[0],
                        new Vector3f());
            }
        }
    }

    public void handleScript(SharedMapCli states) {
        if(states.containsKey("script")) {
            final SharedString script = (SharedString)states.get("script");
            new Thread(new Runnable() {
                public void run() {
                    System.out.println("[EZScript] handling persisted script");
                    evaluateScript(script.getValue());
                }
            }).start();
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
        if(mouseEventListener == null) {
            mouseEventListener = new MouseEventListener();
        }

        if(!mouseEventListener.isListeningForEntity(renderer.getEntity())) {
            mouseEventListener.addToEntity(renderer.getEntity());
            mouseEventsEnabled = true;
        }
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
        if(spatial.equals("BOX")) {
            volume = new BoundingBox(new Vector3f(), info[0], info[1], info[2]);
        } else {
            volume = new BoundingSphere(info[0], new Vector3f());
        }

        cell.setLocalBounds(volume);
        if(proximityEventsEnabled == true && proximityListener != null) {
            proximityComponent.removeProximityListener(proximityListener);
            proximityListener = null;
            enableProximityEvents();
        }
    }

    public void disableProximityEvents() {
        proximityComponent.removeProximityListener(proximityListener);
        proximityEventsEnabled = false;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Callback Assignments">
    public void setTrigger(String s, Runnable r, boolean local) {

        if(local) {
            if(localTriggerEvents.containsKey(s)) {
                localTriggerEvents.get(s).add(r);
            } else {
                List<Runnable> l = new ArrayList();
                l.add(r);
                localTriggerEvents.put(s, l);
            }
        } else {
            if(triggerCellEvents.containsKey(s)) {
                triggerCellEvents.get(s).add(r);
            } else {

                List<Runnable> l = new ArrayList();
                l.add(r);
                triggerCellEvents.put(s, l);
            }
        }
    }

    public void onClick(Runnable r, boolean local) {
        if(local) {
            localOnClick.add(r);
        } else {
            callbacksOnClick.add(r);
        }
    }

    public void onMouseEnter(Runnable r, boolean local) {
        if(local) {
            localOnMouseEnter.add(r);
        } else {
            callbacksOnMouseEnter.add(r);
        }
    }

    public void onMouseExit(Runnable r, boolean local) {
        if(local)  {
            localOnMouseExit.add(r);
        } else {
            callbacksOnMouseExit.add(r);
        }
    }

    public void onLoad(Runnable r, boolean local) {
        if(local) {
            localOnLoad.add(r);
        } else {
            callbacksOnLoad.add(r);
        }
    }

    public void onUnload(Runnable r, boolean local) {
        if(local) {
            localOnLoad.add(r);
        } else {
            callbacksOnUnload.add(r);
        }
    }

    public void onApproach(Runnable r, boolean local) {
        if(local) {
            localOnApproach.add(r);
        } else {
            callbacksOnApproach.add(r);
        }
        
    }

    public void onLeave(Runnable r, boolean local) {
        if(local) {
            localOnLeave.add(r);
        } else {
            callbacksOnLeave.add(r);
        }
    }

    public void onKeyPress(String key, Runnable r, boolean local) {
        List<Runnable> list;
        if(local) {
            list = localOnKeyPress.get(key);
            if(list == null) {
                list = new ArrayList();
            }
            list.add(r);
            localOnKeyPress.put(key, list);
                            
        } else {
            list = callbacksOnKeyPress.get(key);
            if(list == null) {
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
    public void executeOnClick(boolean local) {
        if(local) {
            threadedExecute(localOnClick);

        } else {
            threadedExecute(callbacksOnClick);
        }
    }

    public void executeOnMouseEnter(boolean local) {
        if(local) {
            threadedExecute(localOnMouseEnter);
  
        } else {
            threadedExecute(callbacksOnMouseEnter);
        }
    }

    public void executeOnMouseExit(boolean local) {
        if(local) {
            threadedExecute(localOnMouseExit);

        } else {
            threadedExecute(callbacksOnMouseExit);
        }
    }

    public void executeOnLoad(boolean local) {
        if(local) {
            threadedExecute(localOnLoad);
        } else {
            threadedExecute(callbacksOnLoad);
        }
    }

    public void executeOnUnload(boolean local) {
        if(local) {
            threadedExecute(localOnUnload);
        } else {
            threadedExecute(callbacksOnUnload);
        }
    }

    public void executeOnApproach(boolean local) {
        if(local) {
            threadedExecute(localOnApproach);

        } else {
            threadedExecute(callbacksOnApproach);
        }
    }

    public void executeOnLeave(boolean local) {
        if(local) {
            threadedExecute(localOnLeave);

        } else {
            threadedExecute(callbacksOnLeave);
        }
    }

    private void trigger(String s, boolean local) {
        final List<Runnable> rs;
        if(local) {            
            rs = localTriggerEvents.get(s);
            //threadedExecute(triggerCellEvents.get(s));
        } else {
            rs = triggerCellEvents.get(s);
        }

        if(rs == null)
            return;

        threadedExecute(rs);

    }

    private void threadedExecute(List<Runnable> rs) {

        for(Runnable r: rs)
            new Thread(r).start();

    }

    public void executeOnKeyPress(String key, boolean local) {
        final List<Runnable> rs;
        if(local) {
            rs = localOnKeyPress.get(key);
        } else {
            rs = callbacksOnKeyPress.get(key);
        }
         
        if(rs == null) {
            return;
        }

        threadedExecute(rs);
    }
// </editor-fold>

    /**
     * Utility method to clear the given callback map.
     * @param m the map of a string associated with a list of Runnable
     */
    private void clearMap(Map<String, List<Runnable>> m) {
        for(List l: m.values())
            l.clear();

        m.clear();
    }

    public void evaluateScript(final String script) {

        try {
            scriptEngine.eval(script, scriptBindings);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.setScriptTextArea(script);
                }
            });
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
            if(event instanceof MouseButtonEvent3D) {
                //MouseButtonEvent3D m = (MouseButtonEvent3D)event;
                MouseButtonEvent3D m = (MouseButtonEvent3D)event;

                if(m.isClicked() && m.getButton() == ButtonId.BUTTON1) {
                    executeOnClick(true);
                    callbacksMap.put("onClick", new SharedString().valueOf(""+System.currentTimeMillis()));
                }
            }
            else if(event instanceof MouseEnterExitEvent3D) {
                MouseEnterExitEvent3D m = (MouseEnterExitEvent3D)event;
                if(m.isEnter()) {
                    executeOnMouseEnter(true);                    
                    callbacksMap.put("onMouseEnter", new SharedString().valueOf(""+System.currentTimeMillis()));
                } else {
                    executeOnMouseExit(true);
                    callbacksMap.put("onMouseExit",new SharedString().valueOf(""+System.currentTimeMillis()));
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
            if(name.equals("callbacks")) {
                if(property.equals("onClick")) {
                    executeOnClick(false);
                } else if(property.equals("onMouseEnter")) {
                    executeOnMouseEnter(false);
                } else if(property.equals("onMouseExit")) {
                    executeOnMouseExit(false);
                } else if(property.equals("onApproach")) {
                    executeOnApproach(false);
                } else if(property.equals("onLeave")) {
                    executeOnLeave(false);
                } else if(property.equals("onKeyPress")) {                
                    executeOnKeyPress(event.getNewValue().toString(), false);
                } else if(property.equals("clear")) {
                    clearCallbacks();
                }
                return;
            }

            //scripts are most likely to be called second most frequently
            if(name.equals("scripts")) {
                if(property.equals("editor")) {
                    SharedString script = (SharedString)event.getNewValue();
                    try {
                        //execute script typed in Scripting Editor
                        System.out.println("executing script...");
                        //scriptEngine.eval(script.getValue(), scriptBindings);
                        //Need to add this script to the script editor panel.
                        evaluateScript(script.getValue());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            //state messages should be send least frequently
            if(name.equals("state")) {

                if(property.equals("script")) {
                    return;
                }

                if(property.equals("bounds")) {
                    SharedBounds b = (SharedBounds)event.getNewValue();
                    System.out.println(event.getNewValue());
                    //updating cell bounds takes care of updating the proximity
                    //listener as well.
                    updateCellBounds(b.getValue(), b.getExtents());
                    return;
                }
                //SharedBoolean p = (SharedBoolean)event.getNewValue();
                if(property.equals("proximity")) {
                    if(((SharedBoolean)event.getNewValue()).getValue())
                        enableProximityEvents();
                    else
                        disableProximityEvents();
                } else if(property.equals("mouse")) {
                    if(((SharedBoolean)event.getNewValue()).getValue())
                        enableMouseEvents();
                    else
                        disableMouseEvents();
                } else if(property.equals("keyboard")) {
                    if(((SharedBoolean)event.getNewValue()).getValue())
                        enableKeyEvents();
                    else
                        disableKeyEvents();
                } 
                return;
            }
        }
    }

    class ProximityListenerImpl implements ProximityListener {

        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
            if(entered) {
                executeOnApproach(true);
                callbacksMap.put("onApproach", new SharedString().valueOf(""+System.currentTimeMillis()));
            }
            else {
                executeOnLeave(true);
                callbacksMap.put("onLeave", new SharedString().valueOf(""+System.currentTimeMillis()));
            }
        }
   }

    class MenuItemListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            if(event.getContextMenuItem().getLabel().equals("Script")) {
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
            if(message instanceof CellTriggerEventMessage) {
                CellTriggerEventMessage eventMessage = (CellTriggerEventMessage)message;
                String name = eventMessage.getEventName();
                
                if(triggerCellEvents.containsKey(name)) {
                    threadedExecute(triggerCellEvents.get(name));
                    //triggerCellEvents.get(name).setArguments(eventMessage.getArguments());
                    //triggerCellEvents.get(name).run();
                } else {
                    logger.warning("Received an event request with no associated event: "+eventMessage.getEventName());
                }
            }
        }
    }
    //</editor-fold>

    public void addFunctionBinding(ScriptMethodSPI method) {
        scriptBindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\targs[i] = arguments[i];\n"
            + "\t}\n"

           // + "\targs = "+method.getFunctionName()+".arguments;\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"
            +"}";

        try {
            //System.out.println("evaluating script: \n"+scriptx);
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void addFunctionBinding(ReturnableScriptMethodSPI method) {
        scriptBindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\t\targs[i] = arguments[i];\n"
            + "\t}\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"

            + "\tvar tmp = this"+method.getFunctionName()+".returns();\n"
            + "\treturn tmp\n;"
            +"}";

        try {
           // System.out.println("evaluating script: \n"+scriptx);
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void triggerCell(CellID cellID, String label, Object[] args) {

        channelComponent.send(new CellTriggerEventMessage(cellID, label, args));
    }

    public void removeCallback(List l, Runnable r) {
        if(r == null)
            return;

    }

    public void addCallback(List l, Runnable r) {
        if (r == null)
            return;
        if(!l.contains(r)) {
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
        String scriptx = "function $(cellname) { " +
                "\treturn Context.getCellIDByName(cellname);" +
                "}";

        try {
            scriptEngine.eval(scriptx, scriptBindings);
        } catch(ScriptException e) {
           if(e.getCause() instanceof WrappedException) {
               WrappedException we = (WrappedException)e.getCause();
               //java issue
           } else if(e.getCause() instanceof EcmaError)
            e.printStackTrace();
        }
    }
}
